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

import net.thauvin.erik.jokeapi.JokeApi.Companion.fetchUrl
import net.thauvin.erik.jokeapi.JokeApi.Companion.getJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
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
        assertAll("JokeException Validation",
            { assertEquals(106, e.code, "code should be valid") },
            { assertTrue(e.error, "should be an error") },
            { assertFalse(e.internalError, "should not be internal error") },
            { assertEquals("No matching joke found", e.message, "message should match") },
            { assertEquals(1, e.causedBy.size, "causedBy size should be 1") },
            { assertTrue(e.causedBy[0].startsWith("No jokes"), "causedBy should start with no jokes") },
            { assertTrue(e.additionalInfo.isNotEmpty(), "additional info should not be empty") },
            { assertTrue(e.timestamp > 0, "timestamp should be > 0") })
    }

    @ParameterizedTest(name = "HTTP Status Code: {0}")
    @ValueSource(ints = [400, 404, 403, 413, 414, 429, 500, 523])
    fun `Validate HTTP Error Exceptions`(input: Int) {
        val e = assertThrows<HttpErrorException> {
            fetchUrl("https://httpstat.us/$input")
        }
        assertAll("HttpErrorException Validation",
            { assertEquals(input, e.statusCode) { "status code should be $input" } },
            { assertTrue(e.message!!.isNotEmpty()) { "message for $input should not be empty" } },
            { assertTrue(e.cause!!.message!!.isNotEmpty()) { "cause of $input should not be empty" } })
    }

    @Test
    fun `Fetch Invalid URL`() {
        val statusCode = 999
        val e = assertThrows<HttpErrorException> {
            fetchUrl("https://httpstat.us/$statusCode")
        }
        Assertions.assertAll("JokeException Validation",
            { assertEquals(statusCode, e.statusCode) { "status code should be $statusCode" } },
            { assertTrue(e.message!!.isNotEmpty(), "message should not be empty") },
            { assertTrue(e.cause == null, "cause should be null") })
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
