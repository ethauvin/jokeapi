/*
 * ExceptionsTests.kt
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
import net.thauvin.erik.jokeapi.JokeApi.logger
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(BeforeAllTests::class)
internal class ExceptionsTests {
    @Test
    fun `Validate Joke Exception`() {
        val e = assertThrows<JokeException> {
            joke(categories = setOf(Category.CHRISTMAS), contains = "foo")
        }
        logger.fine(e.debug())
        assertThat(e, "joke(${Category.CHRISTMAS},foo)").all {
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
    @CsvSource(
        "https://httpbin.org/status/401, 401",
        "https://httpbin.org/status/404, 404"
    )
    fun `Validate HTTP Exceptions`(url: String, expectedCode: Int) {
        val e = assertThrows<HttpErrorException> {
            fetchUrl(url)
        }
        assertThat(e, "fetchUrl($expectedCode)").all {
            prop(HttpErrorException::statusCode).isEqualTo(expectedCode)
            prop(HttpErrorException::message).isNotNull().isNotEmpty()
        }
    }
}
