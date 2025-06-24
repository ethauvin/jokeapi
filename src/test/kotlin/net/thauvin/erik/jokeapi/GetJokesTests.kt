/*
 * GetJokesTests.kt
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
import net.thauvin.erik.jokeapi.models.Joke
import net.thauvin.erik.jokeapi.models.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BeforeAllTests::class)
internal class GetJokesTests {
    @Test
    fun `Get Multiple Jokes`() {
        val amount = 2
        val jokes = jokes(amount = amount, safe = true, lang = Language.FR)
        assertThat(jokes, "jokes").all {
            size().isEqualTo(amount)
            each {
                it.prop(Joke::id).isGreaterThanOrEqualTo(0)
                it.prop(Joke::safe).isTrue()
                it.prop(Joke::lang).isEqualTo(Language.FR)
            }
        }
    }

    @Test
    fun `Get Jokes with Invalid Amount`() {
        val e = assertThrows<IllegalArgumentException> { jokes(amount = -1) }
        assertThat(e::message).isNotNull().contains("-1")
    }

    @Test
    fun `Get One Joke as Multiple`() {
        val jokes = jokes(amount = 1, safe = true)
        assertThat(jokes, "jokes").all {
            size().isEqualTo(1)
            index(0).all {
                prop(Joke::id).isGreaterThanOrEqualTo(0)
                prop(Joke::safe).isTrue()
            }
        }
    }
}
