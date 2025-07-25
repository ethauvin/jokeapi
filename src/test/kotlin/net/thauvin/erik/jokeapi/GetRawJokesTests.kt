/*
 * GetRawJokesTests.kt
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
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.IdRange
import net.thauvin.erik.jokeapi.models.JokeResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BeforeAllTests::class)
internal class GetRawJokesTests {
    @Test
    fun `Get Raw Joke with TXT`() {
        val response = rawJokes(format = Format.TXT)
        assertThat(response).all {
            prop("statusCode", JokeResponse::statusCode).isEqualTo(200)
            prop("data", JokeResponse::data).all {
                isNotEmpty()
                doesNotContain("Error")
            }
        }
    }

    @Test
    fun `Get Raw Joke with XML`() {
        val response = rawJokes(format = Format.XML)
        assertThat(response).all {
            prop("statusCode", JokeResponse::statusCode).isEqualTo(200)
            prop("data", JokeResponse::data)
                .startsWith("<?xml version='1.0'?>\n<data>\n    <error>false</error>")
        }
    }

    @Test
    fun `Get Raw Joke with YAML`() {
        val response = rawJokes(format = Format.YAML)
        assertThat(response).all {
            prop("statusCode", JokeResponse::statusCode).isEqualTo(200)
            prop("data", JokeResponse::data).startsWith("error: false")
        }
    }

    @Test
    fun `Get Raw Jokes`() {
        val response = rawJokes(amount = 2)
        assertThat(response).all {
            prop("statusCode", JokeResponse::statusCode).isEqualTo(200)
            prop("data", JokeResponse::data).isNotEmpty()
        }
    }

    @Test
    fun `Get Raw Invalid Jokes`() {
        val response = rawJokes(contains = "foo", safe = true, amount = 2, idRange = IdRange(160, 161))
        assertThat(response).all {
            prop("statusCode", JokeResponse::statusCode).isEqualTo(400)
            prop("data", JokeResponse::data).contains("\"error\": true")
        }
    }
}
