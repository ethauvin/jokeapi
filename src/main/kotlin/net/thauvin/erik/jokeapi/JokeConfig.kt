/*
 * Configuration.kt
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

import net.thauvin.erik.jokeapi.JokeConfig.Builder
import net.thauvin.erik.jokeapi.models.Category
import net.thauvin.erik.jokeapi.models.Flag
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.IdRange
import net.thauvin.erik.jokeapi.models.Language
import net.thauvin.erik.jokeapi.models.Type

/**
 * Joke Configuration.
 *
 * Use the [Builder] to create a new configuration.
 */
class JokeConfig private constructor(
    val categories: Set<Category>,
    val language: Language,
    val flags: Set<Flag>,
    val type: Type,
    val format: Format,
    val contains: String,
    val idRange: IdRange,
    val amount: Int,
    val safe: Boolean,
    val splitNewLine: Boolean,
    val auth: String
) {
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
        var auth: String = "",
        var splitNewLine: Boolean = false
    ) {
        fun categories(categories: Set<Category>) = apply { this.categories = categories }
        fun lang(language: Language) = apply { lang = language }
        fun blacklistFlags(flags: Set<Flag>) = apply { blacklistFlags = flags }
        fun type(type: Type) = apply { this.type = type }
        fun format(format: Format) = apply { this.format = format }
        fun contains(search: String) = apply { contains = search }
        fun idRange(idRange: IdRange) = apply { this.idRange = idRange }
        fun amount(amount: Int) = apply { this.amount = amount }
        fun safe(safe: Boolean) = apply { this.safe = safe }
        fun auth(auth: String) = apply { this.auth = auth }
        fun splitNewLine(splitNewLine: Boolean) = apply { this.splitNewLine = splitNewLine }

        fun build() = JokeConfig(
            categories, lang, blacklistFlags, type, format, contains, idRange, amount, safe, splitNewLine, auth
        )
    }
}
