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

import net.thauvin.erik.jokeapi.JokeApi.Companion.fetchUrl
import net.thauvin.erik.jokeapi.JokeApi.Companion.getJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.Category
import net.thauvin.erik.jokeapi.models.Flag
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
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class GetJokeTest {
    @Test
    fun `Get Joke`() {
        val joke = getJoke()
        logger.fine(joke.toString())
        assertAll("No Param Joke",
            { assertFalse(joke.error, "error should be false") },
            { assertTrue(joke.joke.isNotEmpty(), "joke should not be empty") },
            { assertTrue(joke.type == Type.TWOPART || joke.type == Type.SINGLE, "type should validate") },
            { assertTrue(joke.id >= 0, "id should be >= 0") },
            { assertEquals(Language.EN, joke.language, "language should be english") })
    }

    @Test
    fun `Get Joke without Blacklist Flags`() {
        val joke = getJoke(flags = setOf(Flag.ALL))
        assertTrue(joke.flags.isEmpty(), "flags should be empty.")
    }

    @Test
    fun `Get Joke without any Blacklist Flags`() {
        val allFlags = Flag.values().filter { it != Flag.ALL }.toSet()
        val joke = getJoke(flags = allFlags)
        assertTrue(joke.flags.isEmpty(), "flags should be empty.")
    }

    @Test
    fun `Get Joke with ID`() {
        val id = 172
        val joke = getJoke(idRange = IdRange(id))
        logger.fine(joke.toString())
        assertAll("Joke by ID",
            { assertTrue(joke.flags.contains(Flag.NSFW) && joke.flags.contains(Flag.EXPLICIT), "nsfw & explicit") },
            { assertEquals(172, joke.id) { "id is $id" } },
            { assertEquals(Category.PUN, joke.category, "category should be pun") })
    }

    @Test
    fun `Get Joke with ID Range`() {
        val idRange = IdRange(1, 100)
        val joke = getJoke(idRange = idRange)
        logger.fine(joke.toString())
        assertTrue(joke.id >= idRange.start && joke.id <= idRange.end, "id should be in range")
    }

    @Test
    fun `Get Joke with invalid ID Range`() {
        val joke = getJoke(idRange = IdRange(100, 1))
        logger.fine(joke.toString())
        assertFalse(joke.error, "should not be an error")
    }

    @Test
    fun `Get Joke with max ID Range`() {
        val e = assertThrows<JokeException> { getJoke(idRange = IdRange(1, 30000)) }
        assertAll("Joke w/ max ID Range",
            { assertTrue(e.error, "should be an error") },
            { assertTrue(e.additionalInfo.contains("ID range"), "should contain ID range") })
    }

    @Test
    fun `Get Joke with two Categories`() {
        val joke = getJoke(categories = setOf(Category.PROGRAMMING, Category.MISC))
        logger.fine(joke.toString())
        assertTrue(joke.category == Category.PROGRAMMING || joke.category == Category.MISC) {
            "category should be ${Category.PROGRAMMING.value} or ${Category.MISC.value}"
        }
    }

    @Test
    fun `Get Joke with each Categories`() {
        Category.values().filter { it != Category.ANY }.forEach {
            val joke = getJoke(categories = setOf(it))
            logger.fine(joke.toString())
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
        val joke = getJoke(
            categories = setOf(Category.DARK), type = Type.SINGLE, idRange = IdRange(178), splitNewLine = false
        )
        assertTrue(joke.joke.toString().contains("\n"), "should contain newline")
    }

    @Test
    fun `Get Safe Joke`() {
        val joke = getJoke(safe = true)
        logger.fine(joke.toString())
        assertAll("Safe Joke",
            { assertTrue(joke.safe, "should be safe") },
            { assertTrue(joke.flags.isEmpty(), "flags should be empty") })
    }

    @Test
    fun `Get Single Joke`() {
        val joke = getJoke(type = Type.SINGLE)
        logger.fine(joke.toString())
        assertEquals(Type.SINGLE, joke.type, "type should be single")
    }

    @Test
    fun `Get Two-Parts Joke`() {
        val joke = getJoke(type = Type.TWOPART)
        logger.fine(joke.toString())
        assertAll("Two-Parts Joke",
            { assertEquals(Type.TWOPART, joke.type, "type should be two-part") },
            { assertTrue(joke.joke.size > 1, "should have multiple lines") })
    }

    @Test
    fun `Get Joke using Search`() {
        val id = 265
        val joke =
            getJoke(search = "his wife", categories = setOf(Category.PROGRAMMING), idRange = IdRange(id), safe = true)
        logger.fine(joke.toString())
        assertEquals(id, joke.id) { "id should be $id" }
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
