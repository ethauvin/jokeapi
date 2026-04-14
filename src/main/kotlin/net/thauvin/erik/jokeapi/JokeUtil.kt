/*
 * JokeUtil.kt
 *
 * Copyright 2022-2026 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

@file:JvmName("JokeUtil")

package net.thauvin.erik.jokeapi

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.*
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger

private val httpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(30))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

val logger: Logger by lazy { Logger.getLogger("net.thauvin.erik.jokeapi.JokeUtil") }

private fun createRequest(url: String, auth: String = ""): HttpRequest {
    val builder = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .GET()

    if (auth.isNotBlank()) {
        builder.header("Authorization", auth)
    }

    return builder.build()
}


/**
 * Fetches a URL and returns the raw JokeAPI response.
 *
 * The request is executed synchronously. Network errors and invalid responses
 * are converted into domain-specific exceptions.
 */
@SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
internal fun fetchUrl(url: String, auth: String? = null): JokeResponse {
    if (logger.isLoggable(Level.FINE)) {
        logger.fine(url)
    }

    val request = createRequest(url, auth ?: "")

    val response = try {
        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    } catch (e: IOException) {
        throw JokeException(
            internalError = true,
            code = -1,
            message = "Network error while contacting JokeAPI.",
            causedBy = listOf(e.message ?: "Unknown network error"),
            additionalInfo = "",
            timestamp = System.currentTimeMillis(),
            cause = e
        )
    }

    return processResponse(response)
}

/**
 * Creates an [HttpErrorException] describing the HTTP error returned by JokeAPI.
 */
@SuppressFBWarnings("CE_CLASS_ENVY")
internal fun httpError(responseCode: Int): HttpErrorException {
    return when (responseCode) {
        400 -> HttpErrorException(
            responseCode,
            "Bad Request",
            IOException("The request is formatted incorrectly and cannot be processed.")
        )

        403 -> HttpErrorException(
            responseCode,
            "Forbidden",
            IOException("You have been blacklisted due to malicious behavior.")
        )

        404 -> HttpErrorException(
            responseCode,
            "Not Found",
            IOException("The requested URL could not be found.")
        )

        413 -> HttpErrorException(
            responseCode,
            "URI Too Long",
            IOException("The URL exceeds the maximum allowed length.")
        )

        414 -> HttpErrorException(
            responseCode,
            "Payload Too Large",
            IOException("The payload exceeds the maximum allowed size.")
        )

        429 -> HttpErrorException(
            responseCode,
            "Too Many Requests",
            IOException("You have exceeded the rate limit of 120 requests per minute.")
        )

        500 -> HttpErrorException(
            responseCode,
            "Internal Server Error",
            IOException("A general internal error occurred within JokeAPI.")
        )

        523 -> HttpErrorException(
            responseCode,
            "Origin Unreachable",
            IOException("The server is temporarily offline or unreachable.")
        )

        else -> HttpErrorException(responseCode, "Unknown HTTP Error")
    }
}

private fun isInvalidErrorResponse(body: String, response: HttpResponse<String>): Boolean {
    val contentType = response.headers()
        .firstValue("content-type")
        .orElse(null) // convert Optional<String> → String?
        ?.lowercase() // safe call
        ?: "" // fallback

    if (body.isBlank()) return true
    if ("text/html" in contentType) return true
    if (!contentType.contains("application/json")) return true

    return false
}

/**
 * Parses a JokeAPI error response into a `JokeException`.
 */
internal fun parseError(json: JSONObject): JokeException {
    val causedByArray = json.optJSONArray("causedBy")
    val causes = if (causedByArray != null) {
        List(causedByArray.length()) { i -> causedByArray.optString(i) }
    } else {
        emptyList()
    }

    return JokeException(
        internalError = json.optBoolean("internalError", false),
        code = json.optInt("code", -1),
        message = json.optString("message", "Unknown error"),
        causedBy = causes,
        additionalInfo = json.optString("additionalInfo", ""),
        timestamp = json.optLong("timestamp", System.currentTimeMillis())
    )
}

/**
 * Parses a JokeAPI joke response into a `Joke` instance.
 *
 * JokeAPI may return either a single-line joke or a two-part joke. Newlines
 * may optionally be split into separate parts.
 */
@SuppressFBWarnings("BC_BAD_CAST_TO_ABSTRACT_COLLECTION")
internal fun parseJoke(json: JSONObject, splitNewLine: Boolean): Joke {
    val jokes = mutableListOf<String>()

    if (json.has("setup")) {
        jokes.add(json.optString("setup"))
        jokes.add(json.optString("delivery"))
    } else {
        val raw = json.optString("joke", "")
        if (splitNewLine) {
            jokes.addAll(raw.split("\n").filter { it.isNotBlank() })
        } else {
            jokes.add(raw)
        }
    }

    val jsonFlags = json.optJSONObject("flags") ?: JSONObject()
    val enabledFlags = Flag.entries
        .filter { it != Flag.ALL && jsonFlags.optBoolean(it.value, false) }
        .toSet()

    val categoryRaw = json.optString("category")
    val typeRaw = json.optString(Parameter.TYPE)
    val langRaw = json.optString(Parameter.LANG)

    val category = Category.entries.firstOrNull {
        it.name.equals(categoryRaw, ignoreCase = true)
    } ?: Category.ANY

    val type = Type.entries.firstOrNull {
        it.name.equals(typeRaw, ignoreCase = true)
    } ?: Type.SINGLE

    val lang = Language.entries.firstOrNull {
        it.name.equals(langRaw, ignoreCase = true)
    } ?: Language.EN

    return Joke(
        category = category,
        type = type,
        joke = jokes,
        flags = enabledFlags,
        safe = json.optBoolean("safe", false),
        id = json.optInt("id", -1),
        lang = lang
    )
}

private fun processResponse(response: HttpResponse<String>): JokeResponse {
    val responseCode = response.statusCode()
    val body = response.body()

    val isSuccess = responseCode in 200..399

    if (!isSuccess && isInvalidErrorResponse(body, response)) {
        throw httpError(responseCode)
    }

    if (logger.isLoggable(Level.FINE)) {
        logger.fine("Response body ->\n$body")
    }

    return JokeResponse(responseCode, body)
}
