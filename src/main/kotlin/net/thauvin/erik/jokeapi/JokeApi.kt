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

import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import net.thauvin.erik.jokeapi.models.Flag
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.IdRange
import net.thauvin.erik.jokeapi.models.Joke
import net.thauvin.erik.jokeapi.models.Language
import net.thauvin.erik.jokeapi.models.Parameter
import net.thauvin.erik.jokeapi.models.Type
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors

/**
 * Implements the [Sv443's JokeAPI](https://jokeapi.dev/).
 */
class JokeApi {
    companion object {
        private const val API_URL = "https://v2.jokeapi.dev/"
        private const val JOKE_ENDPOINT = "joke"

        @JvmStatic
        val logger: Logger by lazy { Logger.getLogger(JokeApi::class.java.simpleName) }

        /**
         * Makes a direct API call.
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#endpoints) for more details.
         */
        @JvmStatic
        @JvmOverloads
        @Throws(HttpErrorException::class, IOException::class)
        fun apiCall(endPoint: String, path: String = "", params: Map<String, String> = emptyMap()): String {
            val urlBuilder = StringBuilder("$API_URL$endPoint")

            if (path.isNotEmpty()) {
                if (!urlBuilder.endsWith(('/'))) {
                    urlBuilder.append('/')
                }
                urlBuilder.append(path)
            }

            if (params.isNotEmpty()) {
                urlBuilder.append('?')
                val it = params.iterator()
                while (it.hasNext()) {
                    val param = it.next()
                    urlBuilder.append(param.key)
                    if (param.value.isNotEmpty()) {
                        urlBuilder.append("=").append(
                            URLEncoder.encode(param.value, StandardCharsets.UTF_8).replace("+", "%20")
                                .replace("*", "%2A").replace("%7E", "~")
                        )
                    }
                    if (it.hasNext()) {
                        urlBuilder.append("&")
                    }
                }
            }
            return fetchUrl(urlBuilder.toString())
        }

        /**
         * Returns one or more jokes.
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         * @see [getJoke]
         */
        @JvmStatic
        @Throws(HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getRawJokes(
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
            val params = mutableMapOf<String, String>()

            // Categories
            val path = if (!categories.contains(Category.ANY)) {
                categories.stream().map(Category::value).collect(Collectors.joining(","))
            } else {
                Category.ANY.value
            }

            // Language
            if (language != Language.ENGLISH) {
                params[Parameter.LANG] = language.value
            }

            // Flags
            if (flags.isNotEmpty()) {
                if (flags.contains(Flag.ALL)) {
                    params[Parameter.FLAGS] = Flag.ALL.value
                } else {
                    params[Parameter.FLAGS] = flags.stream().map(Flag::value).collect(Collectors.joining(","))
                }
            }

            // Type
            if (type != Type.ALL) {
                params[Parameter.TYPE] = type.value
            }

            // Format
            if (format != Format.JSON) {
                params[Parameter.FORMAT] = format.value
            }

            // Contains
            if (search.isNotBlank()) {
                params[Parameter.CONTAINS] = search
            }

            // Range
            if (idRange.start >= 0) {
                if (idRange.end == -1 || idRange.start == idRange.end) {
                    params[Parameter.RANGE] = idRange.start.toString()
                } else if (idRange.end > idRange.start) {
                    params[Parameter.RANGE] = "${idRange.start}-${idRange.end}"
                } else {
                    throw IllegalArgumentException("Invalid ID Range: ${idRange.start}, ${idRange.end}")
                }
            }

            // Amount
            if (amount > 1) {
                params[Parameter.AMOUNT] = amount.toString()
            } else if (amount <= 0) {
                throw IllegalArgumentException("Invalid Amount: $amount")
            }

            // Safe
            if (safe) {
                params[Parameter.SAFE] = ""
            }

            return apiCall(JOKE_ENDPOINT, path, params)
        }

        /**
         * Returns one or more jokes using a [configuration][JokeConfig].
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         */
        @JvmStatic
        @Throws(HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getRawJokes(config: JokeConfig): String {
            return getRawJokes(
                categories = config.categories,
                language = config.language,
                flags = config.flags,
                type = config.type,
                format = config.format,
                search = config.search,
                idRange = config.idRange,
                amount = config.amount,
                safe = config.safe
            )
        }

        @Throws(HttpErrorException::class, IOException::class)
        internal fun fetchUrl(url: String): String {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(url)
            }

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty(
                "User-Agent", "Mozilla/5.0 (Linux x86_64; rv:105.0) Gecko/20100101 Firefox/105.0"
            )

            if (connection.responseCode in 200..399) {
                val body = connection.inputStream.bufferedReader().readText()
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(body)
                }
                return body
            } else {
                throw httpError(connection.responseCode)
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
         * Returns a [Joke] instance.
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         *
         * @param splitNewLine Split newline within [Type.SINGLE] joke.
         * @see [getRawJokes]
         */
        @JvmStatic
        @Throws(JokeException::class, HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getJoke(
            categories: Set<Category> = setOf(Category.ANY),
            language: Language = Language.ENGLISH,
            flags: Set<Flag> = emptySet(),
            type: Type = Type.ALL,
            search: String = "",
            idRange: IdRange = IdRange(),
            safe: Boolean = false,
            splitNewLine: Boolean = false
        ): Joke {
            val json = JSONObject(
                getRawJokes(
                    categories = categories,
                    language = language,
                    flags = flags,
                    type = type,
                    search = search,
                    idRange = idRange,
                    safe = safe
                )
            )
            if (json.getBoolean("error")) {
                throw parseError(json)
            } else {
                return parseJoke(json, splitNewLine)
            }
        }

        /**
         * Returns an array of [Joke] instances.
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         *
         * @param amount The required amount of jokes to return.
         * @param splitNewLine Split newline within [Type.SINGLE] joke.
         * @see [getRawJokes]
         */
        @JvmStatic
        @Throws(JokeException::class, HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getJokes(
            amount: Int,
            categories: Set<Category> = setOf(Category.ANY),
            language: Language = Language.ENGLISH,
            flags: Set<Flag> = emptySet(),
            type: Type = Type.ALL,
            search: String = "",
            idRange: IdRange = IdRange(),
            safe: Boolean = false,
            splitNewLine: Boolean = false
        ): Array<Joke> {
            val json = JSONObject(
                getRawJokes(
                    categories = categories,
                    language = language,
                    flags = flags,
                    type = type,
                    search = search,
                    idRange = idRange,
                    amount = amount,
                    safe = safe
                )
            )
            if (json.getBoolean("error")) {
                throw parseError(json)
            } else {
                return if (json.has("amount")) {
                    val jokes = json.getJSONArray("jokes")
                    Array(jokes.length()) { i -> parseJoke(jokes.getJSONObject(i), splitNewLine) }
                } else {
                    arrayOf(parseJoke(json, splitNewLine))
                }
            }
        }

        /**
         * Retrieve a [Joke] instance using a [configuration][JokeConfig].
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         */
        @JvmStatic
        @Throws(JokeException::class, HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getJoke(config: JokeConfig): Joke {
            return getJoke(
                categories = config.categories,
                language = config.language,
                flags = config.flags,
                type = config.type,
                search = config.search,
                idRange = config.idRange,
                safe = config.safe,
                splitNewLine = config.splitNewLine
            )
        }

        /**
         * Returns an array of [Joke] instances using a [configuration][JokeConfig].
         *
         * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
         */
        @JvmStatic
        @Throws(JokeException::class, HttpErrorException::class, IOException::class, IllegalArgumentException::class)
        fun getJokes(config: JokeConfig): Array<Joke> {
            return getJokes(
                categories = config.categories,
                language = config.language,
                flags = config.flags,
                type = config.type,
                search = config.search,
                idRange = config.idRange,
                amount = config.amount,
                safe = config.safe,
                splitNewLine = config.splitNewLine
            )
        }

        private fun parseError(json: JSONObject): JokeException {
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

        private fun parseJoke(json: JSONObject, splitNewLine: Boolean): Joke {
            val jokes = mutableListOf<String>()
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
                category = Category.valueOf(json.getString("category").uppercase()),
                type = Type.valueOf(json.getString(Parameter.TYPE).uppercase()),
                joke = jokes,
                flags = enabledFlags,
                safe = json.getBoolean("safe"),
                id = json.getInt("id"),
                language = Language.valueOf(json.getString(Parameter.LANG).uppercase())
            )
        }
    }
}
