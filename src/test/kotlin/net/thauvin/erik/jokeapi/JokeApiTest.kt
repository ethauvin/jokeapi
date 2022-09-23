/*
 * JokeApiTest.kt
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
import net.thauvin.erik.jokeapi.JokeApi.Companion.getRawJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import net.thauvin.erik.jokeapi.models.Flag
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.IdRange
import net.thauvin.erik.jokeapi.models.Language
import net.thauvin.erik.jokeapi.models.Type
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class JokeApiTest {
    @Test
    fun `Get Joke`() {
        val joke = getJoke()
        logger.log(Level.FINE, joke.toString())
        assertAll("no param",
            { assertFalse(joke.error) { "error should be false" } },
            { assertTrue(joke.joke.isNotEmpty()) { "joke should not be empty" } },
            { assertTrue(joke.type == Type.TWOPART || joke.type == Type.SINGLE) { "type should validate" } },
            { assertTrue(joke.id >= 0) { "id should be >= 0" } },
            { assertEquals(Language.EN, joke.language) { "language should be english" } })
    }

    @Test
    fun `Get Joke with ID`() {
        val id = 172
        val joke = getJoke(idRange = IdRange(id))
        logger.log(Level.FINE, joke.toString())
        assertAll("joke by id",
            { assertTrue(joke.flags.contains(Flag.NSFW) && joke.flags.contains(Flag.EXPLICIT)) { "nsfw & explicit" } },
            { assertEquals(172, joke.id) { "id is $id" } },
            { assertEquals(Category.PUN, joke.category) { "category should be pun" } })
    }

    @Test
    fun `Get Joke with ID Range`() {
        val idRange = IdRange(1, 100)
        val joke = getJoke(idRange = idRange)
        logger.log(Level.FINE, joke.toString())
        assertTrue(joke.id >= idRange.start && joke.id <= idRange.end) { "id should be in range" }
    }

    @Test
    fun `Get Joke with each Categories`() {
        Category.values().filter { it != Category.ANY }.forEach {
            val joke = getJoke(categories = setOf(it))
            assertEquals(it.value, joke.category.value) { "category should be ${it.value}" }

        }
    }

    @Test
    fun `Get Joke with each Languages`() {
        Language.values().forEach {
            val joke = getJoke(language = it)
            assertEquals(it.value, joke.language.value) { "language should be ${it.value}" }
        }
    }

    @Test
    fun `Get Joke with Newline`() {
        val joke =
            getJoke(categories = setOf(Category.DARK), type = Type.SINGLE, idRange = IdRange(178), splitNewLine = false)
        logger.log(Level.FINE, joke.toString())
        assertEquals(1, joke.joke.size) { "should be a oneliner" }
    }

    @Test
    fun `Get Safe Joke`() {
        val joke = getJoke(safe = true)
        logger.log(Level.FINE, joke.toString())
        assertAll("safe joke",
            { assertTrue(joke.safe) { "should be safe" } },
            { assertTrue(joke.flags.isEmpty()) { "flags should be empty" } })
    }

    @Test
    fun `Get Single Joke`() {
        val joke = getJoke(type = Type.SINGLE)
        logger.log(Level.FINE, joke.toString())
        assertEquals(Type.SINGLE, joke.type) { "type should be single" }
    }

    @Test
    fun `Get Two-Parts Joke`() {
        val joke = getJoke(type = Type.TWOPART)
        logger.log(Level.FINE, joke.toString())
        assertAll("two-part joke",
            { assertEquals(Type.TWOPART, joke.type) { "type should be two-part" } },
            { assertTrue(joke.joke.size > 1) { "should have multiple lines" } })
    }

    @Test
    fun `Get Joke using Search`() {
        val id = 1
        val joke = getJoke(search = "man", categories = setOf(Category.PROGRAMMING), idRange = IdRange(id), safe = true)
        logger.log(Level.FINE, joke.toString())
        assertEquals(id, joke.id) { "id should be 1" }
    }

    @Test
    fun `Get Raw Joke with TXT`() {
        val response = getRawJoke(format = Format.TEXT)
        logger.log(Level.FINE, response)
        assertAll("plain text",
            { assertTrue(response.isNotEmpty()) { "should be not empty" } },
            { assertFalse(response.startsWith("Error ")) { "should not be an error" } })
    }

    @Test
    fun `Get Raw Joke with invalid ID Range`() {
        val response = getRawJoke(format = Format.TXT, idRange = IdRange(0, 30000))
        logger.log(Level.FINE, response)
        assertTrue(response.startsWith("Error ")) { "should be an error" }
    }

    @Test
    fun `Get Raw Joke with XML`() {
        val response = getRawJoke(format = Format.XML)
        logger.log(Level.FINE, response)
        assertTrue(response.startsWith("<?xml version='1.0'?>\n<data>\n    <error>false</error>")) { "should be xml" }
    }

    @Test
    fun `Get Raw Joke with YAML`() {
        val response = getRawJoke(format = Format.YAML)
        logger.log(Level.FINE, response)
        assertTrue(response.startsWith("error: false")) { "should be yaml" }
    }

    @Test
    fun `Fetch Invalid URL`() {
        val statusCode = 999
        val e = assertThrows<HttpErrorException> {
            fetchUrl("https://httpstat.us/$statusCode")
        }
        assertAll("JokeException validation",
            { assertEquals(statusCode, e.statusCode) { "status code should be $statusCode" } },
            { assertTrue(e.message!!.isNotEmpty()) { "message should not be empty" } },
            { assertTrue(e.cause == null) { "cause should be null" } })
    }

    @Test
    fun `Validate Joke Exception`() {
        val e = assertThrows<JokeException> {
            getJoke(categories = setOf(Category.CHRISTMAS), search = "foo")
        }
        logger.log(Level.FINE, e.debug())
        assertAll("JokeException validation",
            { assertEquals(106, e.code) { "code should be valid" } },
            { assertTrue(e.error) { "should be an error" } },
            { assertFalse(e.internalError) { "should not be internal error" } },
            { assertEquals("No matching joke found", e.message) { "message should match" } },
            { assertEquals(1, e.causedBy.size) { "causedBy size should be 1" } },
            { assertTrue(e.causedBy[0].startsWith("No jokes")) { "causedBy should start with no jokes" } },
            { assertTrue(e.additionalInfo.isNotEmpty()) { "additional info should not be empty" } },
            { assertTrue(e.timestamp > 0) { "timestamp should be > 0" } })
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 404, 403, 413, 414, 429, 500, 523])
    fun `Validate HTTP Error Exceptions`(input: Int) {
        val e = assertThrows<HttpErrorException> {
            fetchUrl("https://httpstat.us/$input")
        }
        assertAll("JokeException validation",
            { assertEquals(input, e.statusCode) { "status code should be $input" } },
            { assertTrue(e.message!!.isNotEmpty()) { "message for $input should not be empty" } },
            { assertTrue(e.cause!!.message!!.isNotEmpty()) { "cause of $input should not be empty" } })
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
