/*
 * JokeConfigTests.kt
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

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import net.thauvin.erik.jokeapi.JokeApi.getRawJokes
import net.thauvin.erik.jokeapi.JokeApi.joke
import net.thauvin.erik.jokeapi.JokeApi.jokes
import net.thauvin.erik.jokeapi.JokeApi.logger
import net.thauvin.erik.jokeapi.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertContains

@ExtendWith(BeforeAllTests::class)
internal class JokeConfigTests {
    @Test
    fun `Get Joke with Default Builder`() {
        val joke = joke()
        assertThat(joke, "joke").all {
            prop(Joke::id).isGreaterThanOrEqualTo(0)
            prop(Joke::lang).isEqualTo(Language.EN)
        }
    }

    @Test
    fun `Get Joke with Builder`() {
        val id = 266
        val config = JokeConfig.Builder().apply {
            categories(setOf(Category.PROGRAMMING))
            lang(Language.EN)
            blacklistFlags(setOf(Flag.ALL))
            type(Type.TWOPART)
            idRange(IdRange(id - 2, id + 2))
            safe(true)
        }.build()
        val joke = joke(config)
        logger.fine(joke.toString())
        assertThat(joke, "config").all {
            prop(Joke::type).isEqualTo(Type.TWOPART)
            prop(Joke::category).isEqualTo(Category.PROGRAMMING)
            prop(Joke::joke).size().isEqualTo(2)
            prop(Joke::lang).isEqualTo(Language.EN)
            prop(Joke::flags).isEmpty()
            prop(Joke::id).isBetween(id - 2, id + 2)
        }
    }

    @Test
    fun `Get joke with Builder and Split Newline`() {
        val id = 5
        val config = JokeConfig.Builder().apply {
            categories(setOf(Category.PROGRAMMING))
            idRange(IdRange(id))
            splitNewLine(true)
        }.build()
        val joke = joke(config)
        logger.fine(joke.toString())
        assertThat(joke, "config").all {
            prop(Joke::id).isEqualTo(id)
            prop(Joke::joke).size().isEqualTo(2)
        }
    }

    @Test
    fun `Get Raw Joke with Builder`() {
        val config = JokeConfig.Builder().apply {
            categories(setOf(Category.PROGRAMMING))
            format(Format.TXT)
            contains("bar")
            amount(2)
            safe(true)
        }.build()
        val jokes = getRawJokes(config)
        assertThat(jokes.statusCode).isEqualTo(200)
        assertContains(
            jokes.data, "----------------------------------------------",
            false, "config.amount(2)"
        )
    }

    @Test
    fun `Get Multiple Jokes with Builder`() {
        val amount = 2
        val config = JokeConfig.Builder().apply {
            amount(amount)
            safe(true)
            lang(Language.FR)
        }.build()
        val jokes = jokes(config)
        assertThat(jokes, "jokes").all {
            size().isEqualTo(amount)
            each {
                it.prop(Joke::id).isGreaterThanOrEqualTo(0)
                it.prop(Joke::safe).isTrue()
                it.prop(Joke::flags).isEmpty()
                it.prop(Joke::lang).isEqualTo(Language.FR)
            }
        }
    }

    @Test
    fun `Validate Config`() {
        val categories = setOf(Category.ANY)
        val language = Language.CS
        val flags = setOf(Flag.POLITICAL, Flag.RELIGIOUS)
        val type = Type.TWOPART
        val format = Format.XML
        val search = "foo"
        val idRange = IdRange(1, 20)
        val amount = 10
        val safe = true
        val splitNewLine = true
        val auth = "token"
        val config = JokeConfig.Builder().apply {
            categories(categories)
            lang(language)
            blacklistFlags(flags)
            type(type)
            format(format)
            contains(search)
            idRange(idRange)
            amount(amount)
            safe(safe)
            splitNewLine(splitNewLine)
            auth(auth)
        }.build()
        assertThat(config, "config").all {
            prop(JokeConfig::categories).isEqualTo(categories)
            prop(JokeConfig::lang).isEqualTo(language)
            prop(JokeConfig::blacklistFlags).isEqualTo(flags)
            prop(JokeConfig::type).isEqualTo(type)
            prop(JokeConfig::format).isEqualTo(format)
            prop(JokeConfig::contains).isEqualTo(search)
            prop(JokeConfig::idRange).isEqualTo(idRange)
            prop(JokeConfig::amount).isEqualTo(amount)
            prop(JokeConfig::safe).isEqualTo(safe)
            prop(JokeConfig::splitNewLine).isEqualTo(splitNewLine)
            prop(JokeConfig::auth).isEqualTo(auth)
        }
    }
}
