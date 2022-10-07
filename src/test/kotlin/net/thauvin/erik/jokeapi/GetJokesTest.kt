/*
 * GetJokeTest.kt
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

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.each
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import assertk.assertions.size
import net.thauvin.erik.jokeapi.JokeApi.Companion.getJokes
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.models.Joke
import net.thauvin.erik.jokeapi.models.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class GetJokesTest {
    @Test
    fun `Get Multiple Jokes`() {
        val amount = 2
        val jokes = getJokes(amount = amount, safe = true, language = Language.FR)
        assertThat(jokes, "jokes").all {
            size().isEqualTo(amount)
            each {
                it.prop(Joke::id).isGreaterThanOrEqualTo(0)
                it.prop(Joke::safe).isTrue()
                it.prop(Joke::flags).isEmpty()
                it.prop(Joke::language).isEqualTo(Language.FR)
            }
        }
    }

    @Test
    fun `Get Jokes with Invalid Amount`() {
        val e = assertThrows<IllegalArgumentException> { getJokes(amount = -1) }
        assertThat(e::message).isNotNull().contains("-1")
    }

    @Test
    fun `Get One Joke as Multiple`() {
        val jokes = getJokes(amount = 1, safe = true)
        jokes.forEach {
            println(it.joke.joinToString("\n"))
            println("-".repeat(46))
        }
        assertThat(jokes, "jokes").all {
            size().isEqualTo(1)
            index(0).all {
                prop(Joke::id).isGreaterThanOrEqualTo(0)
                prop(Joke::flags).isEmpty()
                prop(Joke::safe).isTrue()
            }
        }
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            with(logger) {
                addHandler(ConsoleHandler().apply { level = Level.FINE })
                level = Level.FINE
            }
        }
    }
}
