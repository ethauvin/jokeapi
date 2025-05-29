/*
 * JokeApi.kt
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

package net.thauvin.erik.jokeapi

import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.*
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.json.JSONObject
import java.util.logging.Logger
import java.util.stream.Collectors

/**
 * Implements the [Sv443's JokeAPI](https://jokeapi.dev/).
 */
object JokeApi {
    private const val API_URL = "https://v2.jokeapi.dev/"

    /**
     * The logger instance.
     */
    @JvmStatic
    val logger: Logger by lazy { Logger.getLogger(JokeApi::class.java.simpleName) }

    /**
     * Makes a direct API call.
     *
     * See the [JokeAPI Documentation](https://jokeapi.dev/#endpoints) for more details.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(HttpErrorException::class)
    fun apiCall(
        endPoint: String,
        path: String = "",
        params: Map<String, String> = emptyMap(),
        auth: String = ""
    ): JokeResponse {
        val urlBuilder = StringBuilder("$API_URL$endPoint")

        if (path.isNotEmpty()) {
            if (!urlBuilder.endsWith(('/'))) {
                urlBuilder.append('/')
            }
            urlBuilder.append(path)
        }

        if (params.isNotEmpty()) {
            urlBuilder.append('?')
            params.entries.joinTo(urlBuilder, "&") { (key, value) ->
                if (value.isEmpty()) {
                    key
                } else {
                    "$key=${UrlEncoderUtil.encode(value)}"
                }
            }

        }
        return fetchUrl(urlBuilder.toString(), auth)
    }

    /**
     * Returns one or more jokes using a [configuration][JokeConfig].
     *
     * See the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
     */
    @JvmStatic
    @Throws(HttpErrorException::class)
    fun getRawJokes(config: JokeConfig): JokeResponse {
        return rawJokes(
            categories = config.categories,
            lang = config.lang,
            blacklistFlags = config.blacklistFlags,
            type = config.type,
            format = config.format,
            contains = config.contains,
            idRange = config.idRange,
            amount = config.amount,
            safe = config.safe,
            auth = config.auth
        )
    }

    /**
     * Retrieve a [Joke] instance using a [configuration][JokeConfig].
     *
     * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
     */
    @JvmStatic
    @JvmOverloads
    @Throws(HttpErrorException::class, JokeException::class)
    fun joke(config: JokeConfig = JokeConfig.Builder().build()): Joke {
        return joke(
            categories = config.categories,
            lang = config.lang,
            blacklistFlags = config.blacklistFlags,
            type = config.type,
            contains = config.contains,
            idRange = config.idRange,
            safe = config.safe,
            auth = config.auth,
            splitNewLine = config.splitNewLine
        )
    }

    /**
     * Returns an array of [Joke] instances using a [configuration][JokeConfig].
     *
     * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
     */
    @JvmStatic
    @Throws(HttpErrorException::class, JokeException::class)
    fun jokes(config: JokeConfig): Array<Joke> {
        return jokes(
            categories = config.categories,
            lang = config.lang,
            blacklistFlags = config.blacklistFlags,
            type = config.type,
            contains = config.contains,
            idRange = config.idRange,
            amount = config.amount,
            safe = config.safe,
            auth = config.auth,
            splitNewLine = config.splitNewLine
        )
    }
}


/**
 * Returns a [Joke] instance.
 *
 * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
 *
 * @param categories JokeAPI has a first, coarse filter that just categorizes the jokes depending on what the joke is
 * about or who the joke is directed at. A joke about programming will be in the [Category.PROGRAMMING] category, dark
 * humor will be in the [Category.DARK] category and so on. If you want jokes from all categories, you can instead use
 * [Category.ANY], which will make JokeAPI randomly choose a category.
 * @param lang There are two types of languages; system languages and joke languages. Both are separate from each other.
 * All system messages like errors can have a certain system language, while jokes can only have a joke language.
 * It is possible, that system languages don't yet exist for your language while jokes already do.
 * If no suitable system language is found, JokeAPI will default to English.
 * @param blacklistFlags Blacklist Flags (or just "Flags") are a more fine layer of filtering. Multiple flags can be
 * set on each joke, and they tell you something about the offensiveness of each joke.
 * @param type Each joke comes with one of two types: [Type.SINGLE] or [Type.TWOPART]. If a joke is of type
 * [Type.TWOPART], it has a setup string and a delivery string, which are both part of the joke. They are separated
 * because you might want to present the users the delivery after a timeout or in a different section of the UI.
 * A joke of type [Type.SINGLE] only has a single string, which is the entire joke.
 * @param contains If the search string filter is used, only jokes that contain the specified string will be returned.
 * @param idRange If this filter is used, you will only get jokes that are within the provided range of IDs.
 * You don't necessarily need to provide an ID range though, a single ID will work just fine as well.
 * For example, an ID range of 0-9 will mean you will only get one of the first 10 jokes, while an ID range of 5 will
 * mean you will only get the 6th joke.
 * @param safe Safe Mode. If enabled, JokeAPI will try its best to serve only jokes that are considered safe for
 * everyone. Unsafe jokes are those who can be considered explicit in any way, either through the used language, its
 * references or its [flags][blacklistFlags]. Jokes from the category [Category.DARK] are also generally marked as
 * unsafe.
 * @param auth JokeAPI has a way of whitelisting certain clients. This is achieved through an API token.
 * At the moment, you will only receive one of these tokens temporarily if something breaks or if you are a business
 * and need more than 120 requests per minute.
 * @param splitNewLine Split newline within [Type.SINGLE] joke.
 */
fun joke(
    categories: Set<Category> = setOf(Category.ANY),
    lang: Language = Language.EN,
    blacklistFlags: Set<Flag> = emptySet(),
    type: Type = Type.ALL,
    contains: String = "",
    idRange: IdRange = IdRange(),
    safe: Boolean = false,
    auth: String = "",
    splitNewLine: Boolean = false
): Joke {
    val json = JSONObject(
        rawJokes(
            categories = categories,
            lang = lang,
            blacklistFlags = blacklistFlags,
            type = type,
            contains = contains,
            idRange = idRange,
            safe = safe,
            auth = auth
        ).data
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
 * @param amount This filter allows you to set a certain amount of jokes to receive in a single call. Setting the
 * filter to an invalid number will result in the API defaulting to serving a single joke. Setting it to a number
 * larger than 10 will make JokeAPI default to the maximum (10).
 * @param categories JokeAPI has a first, coarse filter that just categorizes the jokes depending on what the joke is
 * about or who the joke is directed at. A joke about programming will be in the [Category.PROGRAMMING] category, dark
 * humor will be in the [Category.DARK] category and so on. If you want jokes from all categories, you can instead use
 * [Category.ANY], which will make JokeAPI randomly choose a category.
 * @param lang There are two types of languages; system languages and joke languages. Both are separate from each other.
 * All system messages like errors can have a certain system language, while jokes can only have a joke language.
 * It is possible, that system languages don't yet exist for your language while jokes already do.
 * If no suitable system language is found, JokeAPI will default to English.
 * @param blacklistFlags Blacklist Flags (or just "Flags") are a more fine layer of filtering. Multiple flags can be
 * set on each joke, and they tell you something about the offensiveness of each joke.
 * @param type Each joke comes with one of two types: [Type.SINGLE] or [Type.TWOPART]. If a joke is of type
 * [Type.TWOPART], it has a setup string and a delivery string, which are both part of the joke. They are separated
 * because you might want to present the users the delivery after a timeout or in a different section of the UI.
 * A joke of type [Type.SINGLE] only has a single string, which is the entire joke.
 * @param contains If the search string filter is used, only jokes that contain the specified string will be returned.
 * @param idRange If this filter is used, you will only get jokes that are within the provided range of IDs.
 * You don't necessarily need to provide an ID range though, a single ID will work just fine as well.
 * For example, an ID range of 0-9 will mean you will only get one of the first 10 jokes, while an ID range of 5 will
 * mean you will only get the 6th joke.
 * @param safe Safe Mode. If enabled, JokeAPI will try its best to serve only jokes that are considered safe for
 * everyone. Unsafe jokes are those who can be considered explicit in any way, either through the used language, its
 * references or its [flags][blacklistFlags]. Jokes from the category [Category.DARK] are also generally marked as
 * unsafe.
 * @param auth JokeAPI has a way of whitelisting certain clients. This is achieved through an API token.
 * At the moment, you will only receive one of these tokens temporarily if something breaks or if you are a business
 * and need more than 120 requests per minute.
 * @param splitNewLine Split newline within [Type.SINGLE] joke.
 */
fun jokes(
    amount: Int,
    categories: Set<Category> = setOf(Category.ANY),
    lang: Language = Language.EN,
    blacklistFlags: Set<Flag> = emptySet(),
    type: Type = Type.ALL,
    contains: String = "",
    idRange: IdRange = IdRange(),
    safe: Boolean = false,
    auth: String = "",
    splitNewLine: Boolean = false
): Array<Joke> {
    val json = JSONObject(
        rawJokes(
            categories = categories,
            lang = lang,
            blacklistFlags = blacklistFlags,
            type = type,
            contains = contains,
            idRange = idRange,
            amount = amount,
            safe = safe,
            auth = auth
        ).data
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
 * Returns one or more jokes.
 *
 * See the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
 *
 * @param categories JokeAPI has a first, coarse filter that just categorizes the jokes depending on what the joke is
 * about or who the joke is directed at. A joke about programming will be in the [Category.PROGRAMMING] category, dark
 * humor will be in the [Category.DARK] category and so on. If you want jokes from all categories, you can instead use
 * [Category.ANY], which will make JokeAPI randomly choose a category.
 * @param lang There are two types of languages; system languages and joke languages. Both are separate from each other.
 * All system messages like errors can have a certain system language, while jokes can only have a joke language.
 * It is possible, that system languages don't yet exist for your language while jokes already do.
 * If no suitable system language is found, JokeAPI will default to English.
 * @param blacklistFlags Blacklist Flags (or just "Flags") are a more fine layer of filtering. Multiple flags can be
 * set on each joke, and they tell you something about the offensiveness of each joke.
 * @param type Each joke comes with one of two types: [Type.SINGLE] or [Type.TWOPART]. If a joke is of type
 * [Type.TWOPART], it has a setup string and a delivery string, which are both part of the joke. They are separated
 * because you might want to present the users the delivery after a timeout or in a different section of the UI.
 * A joke of type [Type.SINGLE] only has a single string, which is the entire joke.
 * @param contains If the search string filter is used, only jokes that contain the specified string will be returned.
 * @param format  Response Formats (or just "Formats") are a way to get your data in a different file format.
 * Maybe your environment or language doesn't support JSON natively. In that case, JokeAPI is able to convert the
 * JSON-formatted joke to a different format for you.
 * @param idRange If this filter is used, you will only get jokes that are within the provided range of IDs.
 * You don't necessarily need to provide an ID range though, a single ID will work just fine as well.
 * For example, an ID range of 0-9 will mean you will only get one of the first 10 jokes, while an ID range of 5 will
 * mean you will only get the 6th joke.
 * @param amount This filter allows you to set a certain amount of jokes to receive in a single call. Setting the
 * filter to an invalid number will result in the API defaulting to serving a single joke. Setting it to a number
 * larger than 10 will make JokeAPI default to the maximum (10).
 * @param safe Safe Mode. If enabled, JokeAPI will try its best to serve only jokes that are considered safe for
 * everyone. Unsafe jokes are those who can be considered explicit in any way, either through the used language, its
 * references or its [flags][blacklistFlags]. Jokes from the category [Category.DARK] are also generally marked as
 * unsafe.
 * @param auth JokeAPI has a way of whitelisting certain clients. This is achieved through an API token.
 * At the moment, you will only receive one of these tokens temporarily if something breaks or if you are a business
 * and need more than 120 requests per minute.
 */
@Throws(HttpErrorException::class)
fun rawJokes(
    categories: Set<Category> = setOf(Category.ANY),
    lang: Language = Language.EN,
    blacklistFlags: Set<Flag> = emptySet(),
    type: Type = Type.ALL,
    format: Format = Format.JSON,
    contains: String = "",
    idRange: IdRange = IdRange(),
    amount: Int = 1,
    safe: Boolean = false,
    auth: String = ""
): JokeResponse {
    val params = mutableMapOf<String, String>()

    // Categories
    val path = if (categories.isEmpty() || categories.contains(Category.ANY)) {
        Category.ANY.value
    } else {
        categories.stream().map(Category::value).collect(Collectors.joining(","))
    }

    // Language
    if (lang != Language.EN) {
        params[Parameter.LANG] = lang.value
    }

    // Flags
    if (blacklistFlags.isNotEmpty()) {
        if (blacklistFlags.contains(Flag.ALL)) {
            params[Parameter.FLAGS] = Flag.ALL.value
        } else {
            params[Parameter.FLAGS] = blacklistFlags.stream().map(Flag::value).collect(Collectors.joining(","))
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
    if (contains.isNotBlank()) {
        params[Parameter.CONTAINS] = contains
    }

    // Range
    if (idRange.start >= 0) {
        if (idRange.end == -1 || idRange.start == idRange.end) {
            params[Parameter.RANGE] = idRange.start.toString()
        } else {
            require(idRange.end > idRange.start) { "Invalid ID Range: ${idRange.start}, ${idRange.end}" }
            params[Parameter.RANGE] = "${idRange.start}-${idRange.end}"
        }
    }

    // Amount
    require(amount > 0) { "Invalid Amount: $amount" }
    if (amount > 1) {
        params[Parameter.AMOUNT] = amount.toString()
    }

    // Safe
    if (safe) {
        params[Parameter.SAFE] = ""
    }

    return JokeApi.apiCall("joke", path, params, auth)
}
