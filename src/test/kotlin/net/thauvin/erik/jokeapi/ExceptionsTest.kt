/*
 * ExceptionsTest.kt
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
import assertk.assertions.index
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import assertk.assertions.size
import assertk.assertions.startsWith
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class ExceptionsTest {
    @Test
    fun `Validate Joke Exception`() {
        val e = assertThrows<JokeException> {
            getJoke(categories = setOf(Category.CHRISTMAS), search = "foo")
        }
        logger.fine(e.debug())
        assertThat(e, "getJoke(${Category.CHRISTMAS},foo)").all {
            prop(JokeException::code).isEqualTo(106)
            prop(JokeException::internalError).isFalse()
            prop(JokeException::message).isEqualTo("No matching joke found")
            prop(JokeException::causedBy).size().isEqualTo(1)
            prop(JokeException::causedBy).index(0).startsWith("No jokes")
            prop(JokeException::additionalInfo).isNotEmpty()
            prop(JokeException::timestamp).isGreaterThan(0)
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 404, 403, 413, 414, 429, 500, 523, 666])
    fun `Validate HTTP Exceptions`(code: Int) {
        val e = assertThrows<HttpErrorException> {
            fetchUrl("https://httpstat.us/$code")
        }
        assertThat(e, "fetchUrl($code)").all {
            prop(HttpErrorException::statusCode).isEqualTo(code)
            prop(HttpErrorException::message).isNotNull().isNotEmpty()
            if (code < 600)
                prop(HttpErrorException::cause).isNotNull().assertThat(Throwable::message).isNotNull()
            else
                prop(HttpErrorException::cause).isNull()
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
