/*
 * JokeApi.kt
 *
 * Copyright (c) 2022, Erik C. Thauvin (erik@thauvin.net)
 * All rights reserved.
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

package net.thauvin.erik.jokeapi

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors

class JokeApi {
    companion object {
        private const val API_URL = "https://v2.jokeapi.dev/joke/"
        val logger: Logger by lazy { Logger.getLogger(JokeApi::class.java.simpleName) }

        @JvmStatic
        @JvmOverloads
        @Throws(IOException::class)
        fun apiCall(
            categories: Set<Category> = setOf(Category.ANY),
            language: Language = Language.ENGLISH,
            flags: Set<Flag> = emptySet(),
            type: Type = Type.ALL,
            format: Format = Format.JSON,
            search: String = "",
            idRange: IdRange = IdRange(),
            amount: Int = 1,
            safe: Boolean = false,
        ): String {
            val urlBuilder = StringBuilder(API_URL)
            val urlParams = mutableListOf<String>()

            // Category
            if (!categories.contains(Category.ANY)) {
                urlBuilder.append(categories.stream().map(Category::value).collect(Collectors.joining(",")))
            } else {
                urlBuilder.append(Category.ANY.value)
            }

            // Language
            if (language != Language.ENGLISH) {
                urlParams.add("lang=${language.value}")
            }

            // Flags
            if (flags.isNotEmpty()) {
                if (flags.contains(Flag.ALL)) {
                    urlParams.add("blacklistFlags=${Flag.ALL.value}")
                } else {
                    urlParams.add(
                        "blacklistFlags=" + flags.stream().map(Flag::value).collect(Collectors.joining(","))
                    )
                }
            }

            // Type
            if (type != Type.ALL) {
                urlParams.add("type=${type.value}")
            }

            // Format
            if (format != Format.JSON) {
                urlParams.add("format=${format.value}")
            }

            // Contains
            if (search.isNotBlank()) {
                urlParams.add("contains=${URLEncoder.encode(search, StandardCharsets.UTF_8)}")
            }

            // Range
            if (idRange.start >= 0) {
                if (idRange.end == -1 || idRange.start == idRange.end) {
                    urlParams.add("idRange=${idRange.start}")
                } else if (idRange.end > idRange.start) {
                    urlParams.add("idRange=${idRange.start}-${idRange.end}")
                }
            }

            // Amount
            if (amount in 2..10) {
                urlParams.add("amount=${amount}")
            }

            // Safe
            if (safe) {
                urlParams.add("safe-mode")
            }

            if (urlParams.isNotEmpty()) {
                urlBuilder.append('?')
                val it = urlParams.iterator()
                while (it.hasNext()) {
                    urlBuilder.append(it.next())
                    if (it.hasNext()) {
                        urlBuilder.append("&")
                    }
                }
            }

            return fetchUrl(urlBuilder.toString())
        }

        @Throws(JokeException::class, IOException::class)
        private fun fetchUrl(url: String): String {
            logger.log(Level.FINE, url)

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty(
                "User-Agent", "Mozilla/5.0 (Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0"
            )

            if (connection.responseCode in 200..399) {
                val body = connection.inputStream.bufferedReader().readText()
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, body)
                }
                return body
            } else {
                when (connection.responseCode) {
                    400 -> throw IOException(
                        "400: Bad Request", IOException(
                            "The request you have sent to JokeAPI is formatted incorrectly and cannot be processed"
                        )
                    )

                    403 -> throw IOException(
                        "4o3: Forbidden", IOException(
                            "You have been added to the blacklist due to malicious behavior and are not allowed" + " to send requests to JokeAPI anymore"
                        )
                    )

                    404 -> throw IOException(
                        "404: Not Found", IOException("The URL you have requested couldn't be found")
                    )

                    413 -> throw IOException(
                        "413: Payload Too Large",
                        IOException("The payload data sent to the server exceeds the maximum size of 5120 bytes")
                    )

                    428 -> throw IOException(
                        "429: Too Many Requests", IOException(
                            "You have exceeded the limit of 120 requests per minute and have to wait a bit" + " until you are allowed to send requests again"
                        )
                    )

                    500 -> throw IOException(
                        "500: Internal Server Error", IOException(
                            "There was a general internal error within JokeAPI. You can get more info from" + " the properties in the response text"
                        )
                    )

                    523 -> throw IOException(
                        "523: Origin Unreachable", IOException(
                            "The server is temporarily offline due to maintenance or a dynamic IP update." + " Please be patient in this case."
                        )
                    )

                    else -> throw IOException("${connection.responseCode}: Unknown Error")
                }
            }
        }

        @JvmStatic
        @JvmOverloads
        @Throws(JokeException::class, IOException::class)
        fun getJoke(
            categories: Set<Category> = setOf(Category.ANY),
            language: Language = Language.ENGLISH,
            flags: Set<Flag> = emptySet(),
            type: Type = Type.ALL,
            search: String = "",
            idRange: IdRange = IdRange(),
            safe: Boolean = false,
            splitNewLine: Boolean = true
        ): Joke {
            val json =
                JSONObject(apiCall(categories, language, flags, type, search = search, idRange = idRange, safe = safe))
            if (json.getBoolean("error")) {
                val causedBy = json.getJSONArray("causedBy")
                val causes = MutableList<String>(causedBy.length()) { i -> causedBy.getString(i) }

                throw JokeException(
                    error = true,
                    internalError = json.getBoolean("internalError"),
                    code = json.getInt("code"),
                    message = json.getString("message"),
                    causedBy = causes,
                    additionalInfo = json.getString("additionalInfo"),
                    timestamp = json.getLong("timestamp")
                )
            } else {
                val jokes = mutableSetOf<String>()
                if (json.has("setup")) {
                    jokes.add(json.getString("setup"))
                    jokes.add(json.getString(("delivery")))
                } else {
                    if (splitNewLine) {
                        jokes.addAll(json.getString("joke").split("\n"))
                    } else {
                        jokes.add(json.getString("joke"))
                    }
                }
                val enabledFlags = mutableSetOf<Flag>()
                val jsonFlags = json.getJSONObject("flags")
                Flag.values().filter { it != Flag.ALL }.forEach {
                    if (jsonFlags.has(it.value) && jsonFlags.getBoolean(it.value)) {
                        enabledFlags.add(it)
                    }
                }

                return Joke(
                    error = false,
                    category = Category.valueOf(json.getString("category").uppercase()),
                    type = Type.valueOf(json.getString("type").uppercase()),
                    joke = jokes,
                    flags = enabledFlags,
                    safe = json.getBoolean("safe"),
                    id = json.getInt("id"),
                    language = Language.valueOf(json.getString("lang").uppercase())
                )
            }
        }
    }
}
