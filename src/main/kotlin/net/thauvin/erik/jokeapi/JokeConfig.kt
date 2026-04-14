/*
 * JokeConfig.kt
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

package net.thauvin.erik.jokeapi

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import net.thauvin.erik.jokeapi.models.*

/**
 * Immutable configuration for JokeAPI requests.
 *
 * A configuration defines the categories, language, filters, and output format
 * used when retrieving jokes. Use [Builder] to construct an instance.
 */
@SuppressFBWarnings("EI_EXPOSE_REP")
class JokeConfig private constructor(builder: Builder) {
    /**
     * Categories of jokes to retrieve.
     */
    val categories: Set<Category> = builder.categories.toSet()

    /**
     * Language used for jokes.
     */
    val lang: Language = builder.lang

    /**
     * Blacklist flags used to exclude jokes.
     */
    val blacklistFlags: Set<Flag> = builder.blacklistFlags.toSet()

    /**
     * Joke type: single-line or two-part.
     */
    val type: Type = builder.type

    /**
     * Response format returned by JokeAPI.
     */
    val format: Format = builder.format

    /**
     * Optional substring filter applied to jokes.
     */
    val contains: String? = builder.contains

    /**
     * ID range filter for selecting specific jokes.
     */
    val idRange: IdRange = builder.idRange.copy()

    /**
     * Number of jokes to retrieve, clamped to JokeAPI limits.
     */
    val amount: Int = builder.amount

    /**
     * Whether to restrict results to safe jokes.
     */
    val safe: Boolean = builder.safe

    /**
     * Whether to split newlines in single-line jokes.
     */
    val splitNewLine: Boolean = builder.splitNewLine

    /**
     *  Optional API authentication token.
     */
    val auth: String? = builder.auth

    /**
     * Builder for constructing [JokeConfig] instances.
     *
     * All fields default to [JokeAPI Documentation](https://jokeapi.dev/#joke-endpoint)'s standard behavior.
     * Each setter returns the builder itself, allowing fluent configuration.
    &
     * @param splitNewLine Split newline within [Type.SINGLE] joke.
     */
    @SuppressFBWarnings("USBR_UNNECESSARY_STORE_BEFORE_RETURN")
    data class Builder(
        var categories: Set<Category> = setOf(Category.ANY),
        var lang: Language = Language.EN,
        var blacklistFlags: Set<Flag> = emptySet(),
        var type: Type = Type.ALL,
        var format: Format = Format.JSON,
        var contains: String? = null,
        var idRange: IdRange = IdRange(),
        var amount: Int = 1,
        var safe: Boolean = false,
        var splitNewLine: Boolean = false,
        var auth: String? = null
    ) {
        /**
         * Sets the categories of jokes to retrieve.
         *
         * Categories define the general topic of a joke. Using [Category.ANY]
         * allows JokeAPI to choose a category at random.
         */
        fun categories(categories: Set<Category>): Builder = apply {
            this.categories = categories.toSet()
        }

        /**
         * Convenience overload for specifying categories as varargs.
         */
        fun categories(vararg categories: Category): Builder = apply {
            this.categories = categories.toSet()
        }

        /**
         * Sets the joke language.
         *
         * JokeAPI distinguishes between system languages and joke languages.
         * If a system language is unavailable, English is used as a fallback.
         */
        fun lang(language: Language): Builder = apply {
            lang = language
        }

        /**
         * Sets blacklist flags used to exclude jokes.
         *
         * Flags provide fine-grained filtering for potentially offensive content.
         */
        fun blacklistFlags(flags: Set<Flag>): Builder = apply {
            blacklistFlags = flags.toSet()
        }

        /** Convenience overload for specifying blacklist flags as varargs. */
        fun blacklistFlags(vararg flags: Flag): Builder = apply {
            blacklistFlags = flags.toSet()
        }

        /**
         * Sets the joke type.
         *
         * Single-line jokes contain one string. Two-part jokes contain a setup
         * and a delivery, which may be presented separately.
         */
        fun type(type: Type): Builder = apply {
            this.type = type
        }

        /**
         * Sets the response format.
         *
         * JokeAPI can convert JSON jokes into alternative formats if needed.
         */
        fun format(format: Format): Builder = apply {
            this.format = format
        }

        /**
         * Sets an optional substring filter.
         *
         * Only jokes containing the specified text will be returned.
         */
        fun contains(search: String?): Builder = apply {
            contains = search
        }

        /**
         * Sets the ID range filter.
         *
         * A range restricts results to specific joke IDs. A single ID may also
         * be provided by using a range with identical start and end values.
         */
        fun idRange(idRange: IdRange): Builder = apply {
            this.idRange = idRange.copy()
        }

        /**
         * Sets the number of jokes to retrieve.
         *
         * Values outside JokeAPI's supported range are clamped to 1–10.
         */
        fun amount(amount: Int): Builder = apply {
            this.amount = amount.coerceIn(1, 10)
        }

        /**
         * Enables or disables safe mode.
         *
         * Safe mode attempts to exclude explicit or otherwise unsafe jokes.
         */
        fun safe(safe: Boolean): Builder = apply {
            this.safe = safe
        }

        /**
         * Enables splitting newlines in single-line jokes.
         *
         * Useful when presenting jokes in environments where embedded newlines
         * should be rendered as separate lines.
         */
        fun splitNewLine(splitNewLine: Boolean): Builder = apply {
            this.splitNewLine = splitNewLine
        }

        /**
         * Sets an optional authentication token.
         *
         * Tokens are used for whitelisting or elevated rate limits.
         */
        fun auth(auth: String?): Builder = apply {
            this.auth = auth
        }

        /**
         * Builds an immutable [JokeConfig] instance.
         */
        fun build(): JokeConfig = JokeConfig(this)
    }
}
