/*
 * JokeConfigTest.kt
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

import net.thauvin.erik.jokeapi.JokeApi.Companion.getJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.getRawJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.models.Category
import net.thauvin.erik.jokeapi.models.Flag
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.IdRange
import net.thauvin.erik.jokeapi.models.Language
import net.thauvin.erik.jokeapi.models.Type
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.logging.ConsoleHandler
import java.util.logging.Level

class JokeConfigTest {
    @Test
    fun `Get Joke with Builder`() {
        val id = 266
        val config = JokeConfig.Builder().apply {
            categories(setOf(Category.PROGRAMMING))
            language(Language.EN)
            flags(setOf(Flag.ALL))
            type(Type.TWOPART)
            idRange(IdRange(id - 2, id + 2))
            safe(true)
            splitNewLine(false)
        }.build()
        val joke = getJoke(config)
        logger.fine(joke.toString())
        assertAll("Two-Parts Joke",
            { assertEquals(Type.TWOPART, joke.type, "type should be two-part") },
            { assertEquals(joke.category, Category.PROGRAMMING) { "category should be ${Category.PROGRAMMING}" } },
            { assertEquals(joke.joke.size, 2, "should have two lines") },
            { assertEquals(joke.language, Language.EN, "language should be english") },
            { assertTrue(joke.flags.isEmpty(), "flags should empty") },
            { assertTrue(joke.id in id - 2..id + 2) { "id should be $id +- 2" } })
    }

    @Test
    fun `Get Raw Joke with Builder`() {
        val config = JokeConfig.Builder().apply {
            categories(setOf(Category.PROGRAMMING))
            format(Format.TEXT)
            search("bar")
            amount(2)
            safe(true)
        }.build()
        val joke = getRawJoke(config)
        assertTrue(
            joke.contains("----------------------------------------------"), "should contain -- delimiter"
        )
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
