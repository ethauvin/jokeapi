/*
 * GetJokeTest.kt
 *
 * Copyright 2022-2024 Erik C. Thauvin (erik@thauvin.net)
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
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BeforeAllTests::class)
internal class GetJokeTest {
    @Test
    fun `Get Joke`() {
        val joke = joke()
        logger.fine(joke.toString())
        assertThat(joke, "joke()").all {
            prop(Joke::joke).isNotEmpty()
            prop(Joke::type).isIn(Type.SINGLE, Type.TWOPART)
            prop(Joke::id).isGreaterThanOrEqualTo(0)
            prop(Joke::lang).isEqualTo(Language.EN)
        }
    }

    @Test
    fun `Get Joke without Blacklist Flags`() {
        val joke = joke(blacklistFlags = setOf(Flag.ALL))
        assertThat(joke::flags).isEmpty()
    }

    @Test
    fun `Get Joke without any Blacklist Flags`() {
        val allFlags = Flag.entries.filter { it != Flag.ALL }.toSet()
        val joke = joke(blacklistFlags = allFlags)
        assertThat(joke::flags).isEmpty()
    }

    @Test
    fun `Get Joke with ID`() {
        val id = 172
        val joke = joke(idRange = IdRange(id))
        logger.fine(joke.toString())
        assertThat(joke, "joke($id)").all {
            prop(Joke::flags).all {
                contains(Flag.EXPLICIT)
                contains(Flag.NSFW)
            }
            prop(Joke::id).isEqualTo(172)
            prop(Joke::category).isEqualTo(Category.PUN)
        }
    }

    @Test
    fun `Get Joke with ID Range`() {
        val idRange = IdRange(1, 100)
        val joke = joke(idRange = idRange)
        logger.fine(joke.toString())
        assertThat(joke::id).isBetween(idRange.start, idRange.end)
    }

    @Test
    fun `Get Joke with invalid ID Range`() {
        val idRange = IdRange(100, 1)
        val e = assertThrows<IllegalArgumentException> { joke(idRange = idRange, lang = Language.DE) }
        assertThat(e::message).isNotNull().contains("100, 1")
    }

    @Test
    fun `Get Joke with max ID Range`() {
        val idRange = IdRange(1, 30000)
        val e = assertThrows<JokeException> { joke(idRange = idRange) }
        assertThat(e, "joke{${idRange})").all {
            prop(JokeException::additionalInfo).contains("ID range")
        }
    }

    @Test
    fun `Get Joke with two Categories`() {
        val joke = joke(categories = setOf(Category.PROGRAMMING, Category.MISC))
        logger.fine(joke.toString())
        assertThat(joke.category, "joke(${Category.PROGRAMMING},${Category.MISC})").isIn(
            Category.PROGRAMMING,
            Category.MISC
        )
    }

    @Test
    fun `Get Joke with each Categories`() {
        Category.entries.filter { it != Category.ANY }.forEach {
            val joke = joke(categories = setOf(it))
            logger.fine(joke.toString())
            assertThat(joke::category, "joke($it)").prop(Category::value).isEqualTo(it.value)
        }
    }

    @Test
    fun `Get Joke with each Languages`() {
        Language.entries.forEach {
            val joke = joke(lang = it)
            logger.fine(joke.toString())
            assertThat(joke::lang, "joke($it)").prop(Language::value).isEqualTo(it.value)
        }
    }

    @Test
    fun `Get Joke with Split Newline`() {
        val joke = joke(
            categories = setOf(Category.DARK), type = Type.SINGLE, idRange = IdRange(178), splitNewLine = true
        )
        logger.fine(joke.toString())
        assertThat(joke::joke, "joke(splitNewLine=true)").all {
            size().isEqualTo(2)
            each {
                containsNone("\n")
            }
        }
    }

    @Test
    fun `Get Safe Joke`() {
        val joke = joke(safe = true)
        logger.fine(joke.toString())
        assertThat(joke, "joke(safe)").all {
            prop(Joke::safe).isTrue()
        }
    }

    @Test
    fun `Get Single Joke`() {
        val joke = joke(type = Type.SINGLE)
        logger.fine(joke.toString())
        assertThat(joke::type).assertThat(Type.SINGLE)
    }

    @Test
    fun `Get Two-Parts Joke`() {
        val joke = joke(type = Type.TWOPART)
        logger.fine(joke.toString())
        assertThat(joke, "joke(${Type.TWOPART})").all {
            prop(Joke::type).isEqualTo(Type.TWOPART)
            prop(Joke::joke).size().isGreaterThan(1)
        }
    }

    @Test
    fun `Get Joke using Search`() {
        val id = 265
        val search = "his wife"
        val joke =
            joke(contains = search, categories = setOf(Category.PROGRAMMING), idRange = IdRange(id), safe = true)
        logger.fine(joke.toString())
        assertThat(joke, "joke($search)").all {
            prop(Joke::id).isEqualTo(id)
            prop(Joke::joke).any {
                it.contains(search)
            }
        }
    }
}
