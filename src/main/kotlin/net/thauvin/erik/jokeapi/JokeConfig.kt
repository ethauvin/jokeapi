/*
 * JokeConfig.kt
 *
 * Copyright 2022-2023 Erik C. Thauvin (erik@thauvin.net)
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

import net.thauvin.erik.jokeapi.JokeConfig.Builder
import net.thauvin.erik.jokeapi.models.*

/**
 * Joke Configuration.
 *
 * Use the [Builder] to create a new configuration.
 */
class JokeConfig private constructor(builder: Builder) {
    val categories = builder.categories
    val lang = builder.lang
    val blacklistFlags = builder.blacklistFlags
    val type = builder.type
    val format = builder.format
    val contains = builder.contains
    val idRange = builder.idRange
    val amount = builder.amount
    val safe = builder.safe
    val splitNewLine = builder.splitNewLine
    val auth = builder.auth

    /**
     * [Builds][build] a new configuration.
     *
     * Sse the [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint) for more details.
     *
     * @param splitNewLine Split newline within [Type.SINGLE] joke.
     */
    data class Builder(
        var categories: Set<Category> = setOf(Category.ANY),
        var lang: Language = Language.EN,
        var blacklistFlags: Set<Flag> = emptySet(),
        var type: Type = Type.ALL,
        var format: Format = Format.JSON,
        var contains: String = "",
        var idRange: IdRange = IdRange(),
        var amount: Int = 1,
        var safe: Boolean = false,
        var splitNewLine: Boolean = false,
        var auth: String = ""
    ) {
        /**
         * JokeAPI has a first, coarse filter that just categorizes the jokes depending on what the joke is
         * about or who the joke is directed at. A joke about programming will be in the [Category.PROGRAMMING]
         * category, dark humor will be in the [Category.DARK] category and so on. If you want jokes from all
         * categories, you can instead use [Category.ANY], which will make JokeAPI randomly choose a category.
         */
        fun categories(categories: Set<Category>): Builder = apply { this.categories = categories }

        /**
         * There are two types of languages; system languages and joke languages. Both are separate from each other.
         * All system messages like errors can have a certain system language, while jokes can only have a joke
         * language. It is possible, that system languages don't yet exist for your language while jokes already do.
         * If no suitable system language is found, JokeAPI will default to English.
         */
        fun lang(language: Language): Builder = apply { lang = language }

        /**
         * Blacklist Flags (or just "Flags") are a more fine layer of filtering. Multiple flags can be
         * set on each joke, and they tell you something about the offensiveness of each joke.
         */
        fun blacklistFlags(flags: Set<Flag>): Builder = apply { blacklistFlags = flags }

        /**
         * Each joke comes with one of two types: [Type.SINGLE] or [Type.TWOPART]. If a joke is of type
         * [Type.TWOPART], it has a setup string and a delivery string, which are both part of the joke. They are
         * separated because you might want to present the users the delivery after a timeout or in a different section
         * of the UI. A joke of type [Type.SINGLE] only has a single string, which is the entire joke.
         */
        fun type(type: Type): Builder = apply { this.type = type }

        /**
         * Response Formats (or just "Formats") are a way to get your data in a different file format.
         * Maybe your environment or language doesn't support JSON natively. In that case, JokeAPI is able to convert
         * the JSON-formatted joke to a different format for you.
         */
        fun format(format: Format): Builder = apply { this.format = format }

        /**
         * If the search string filter is used, only jokes that contain the specified string will be returned.
         */
        fun contains(search: String): Builder = apply { contains = search }

        /**
         * If this filter is used, you will only get jokes that are within the provided range of IDs.
         * You don't necessarily need to provide an ID range though, a single ID will work just fine as well.
         * For example, an ID range of 0-9 will mean you will only get one of the first 10 jokes, while an ID range
         * of 5 will mean you will only get the 6th joke.
         */
        fun idRange(idRange: IdRange): Builder = apply { this.idRange = idRange }

        /**
         * This filter allows you to set a certain amount of jokes to receive in a single call. Setting the
         * filter to an invalid number will result in the API defaulting to serving a single joke. Setting it to a
         * number larger than 10 will make JokeAPI default to the maximum (10).
         */
        fun amount(amount: Int): Builder = apply { this.amount = amount }

        /**
         * Safe Mode. If enabled, JokeAPI will try its best to serve only jokes that are considered safe for
         * everyone. Unsafe jokes are those who can be considered explicit in any way, either through the used language,
         * its references or its [flags][blacklistFlags]. Jokes from the category [Category.DARK] are also generally
         * marked as unsafe.
         */
        fun safe(safe: Boolean): Builder = apply { this.safe = safe }

        /**
         * Split newline within [Type.SINGLE] joke.
         */
        fun splitNewLine(splitNewLine: Boolean): Builder = apply { this.splitNewLine = splitNewLine }

        /**
         * JokeAPI has a way of whitelisting certain clients. This is achieved through an API token.
         * At the moment, you will only receive one of these tokens temporarily if something breaks or if you are a
         * business and need more than 120 requests per minute.
         */
        fun auth(auth: String): Builder = apply { this.auth = auth }

        /**
         * Builds a new configuration.
         */
        fun build() = JokeConfig(this)
    }
}
