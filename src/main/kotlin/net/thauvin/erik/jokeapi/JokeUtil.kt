/*
 * JokeUtil.kt
 *
 * Copyright 2022-2025 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.*
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.util.logging.Level

/**
 * Fetch a URL.
 */
internal fun fetchUrl(url: String, auth: String = ""): JokeResponse {
    if (JokeApi.logger.isLoggable(Level.FINE)) {
        JokeApi.logger.fine(url)
    }

    val connection = URI(url).toURL().openConnection() as HttpURLConnection
    try {
        connection.setRequestProperty(
            "User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0"
        )
        if (auth.isNotEmpty()) {
            connection.setRequestProperty("Authentication", auth)
        }

        val isSuccess = connection.responseCode in 200..399
        val stream = if (isSuccess) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (!isSuccess && (body.isBlank() || connection.contentType.contains("text/html"))) {
            throw httpError(connection.responseCode)
        } else if (JokeApi.logger.isLoggable(Level.FINE)) {
            JokeApi.logger.fine(body)
        }
        return JokeResponse(connection.responseCode, body)
    } finally {
        connection.disconnect()
    }
}

private fun httpError(responseCode: Int): HttpErrorException {
    val httpException: HttpErrorException
    when (responseCode) {
        400 -> httpException = HttpErrorException(
            responseCode, "Bad Request", IOException(
                "The request you have sent to JokeAPI is formatted incorrectly and cannot be processed."
            )
        )

        403 -> httpException = HttpErrorException(
            responseCode, "Forbidden", IOException(
                "You have been added to the blacklist due to malicious behavior and are not allowed"
                        + " to send requests to JokeAPI anymore."
            )
        )

        404 -> httpException = HttpErrorException(
            responseCode, "Not Found", IOException("The URL you have requested couldn't be found.")
        )

        413 -> httpException = HttpErrorException(
            responseCode, "URI Too Long", IOException("The URL exceeds the maximum length of 250 characters.")
        )

        414 -> httpException = HttpErrorException(
            responseCode,
            "Payload Too Large",
            IOException("The payload data sent to the server exceeds the maximum size of 5120 bytes.")
        )

        429 -> httpException = HttpErrorException(
            responseCode, "Too Many Requests", IOException(
                "You have exceeded the limit of 120 requests per minute and have to wait a bit"
                        + " until you are allowed to send requests again."
            )
        )

        500 -> httpException = HttpErrorException(
            responseCode, "Internal Server Error", IOException(
                "There was a general internal error within JokeAPI. You can get more info from"
                        + " the properties in the response text."
            )
        )

        523 -> httpException = HttpErrorException(
            responseCode, "Origin Unreachable", IOException(
                "The server is temporarily offline due to maintenance or a dynamic IP update."
                        + " Please be patient in this case."
            )
        )

        else -> httpException = HttpErrorException(responseCode, "Unknown HTTP Error")
    }

    return httpException
}

/**
 * Parse Error.
 */
internal fun parseError(json: JSONObject): JokeException {
    val causedBy = json.getJSONArray("causedBy")
    val causes = List<String>(causedBy.length()) { i -> causedBy.getString(i) }
    return JokeException(
        internalError = json.getBoolean("internalError"),
        code = json.getInt("code"),
        message = json.getString("message"),
        causedBy = causes,
        additionalInfo = json.getString("additionalInfo"),
        timestamp = json.getLong("timestamp")
    )
}

/**
 * Parse Joke.
 */
internal fun parseJoke(json: JSONObject, splitNewLine: Boolean): Joke {
    val jokes = mutableListOf<String>()
    if (json.has("setup")) {
        jokes.add(json.getString("setup"))
        jokes.add(json.getString(("delivery")))
    } else {
        if (splitNewLine) {
            jokes.addAll(json.getString("joke").split("\n").filter { it.isNotBlank() })
        } else {
            jokes.add(json.getString("joke"))
        }
    }
    val enabledFlags = mutableSetOf<Flag>()
    val jsonFlags = json.getJSONObject("flags")
    Flag.entries.filter { it != Flag.ALL }.forEach {
        if (jsonFlags.has(it.value) && jsonFlags.getBoolean(it.value)) {
            enabledFlags.add(it)
        }
    }
    return Joke(
        category = Category.valueOf(json.getString("category").uppercase()),
        type = Type.valueOf(json.getString(Parameter.TYPE).uppercase()),
        joke = jokes,
        flags = enabledFlags,
        safe = json.getBoolean("safe"),
        id = json.getInt("id"),
        lang = Language.valueOf(json.getString(Parameter.LANG).uppercase())
    )
}

