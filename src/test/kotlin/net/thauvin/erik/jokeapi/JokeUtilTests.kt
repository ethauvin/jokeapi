/*
 * JokeUtilTests.kt
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
import net.thauvin.erik.jokeapi.exceptions.HttpErrorException
import net.thauvin.erik.jokeapi.exceptions.JokeException
import net.thauvin.erik.jokeapi.models.*
import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(BeforeAll::class)
internal class JokeUtilTests {
    @Test
    fun `Invalid JSON Error`() {
        assertThrows<JSONException> { parseError(JSONObject("{}")) }
    }

    @Test
    fun `Invalid JSON Joke`() {
        assertThrows<JSONException> { parseJoke(JSONObject("{}"), false) }
    }

    @Test
    fun `Validate Authentication Header`() {
        val token = "AUTH-TOKEN"
        val response = fetchUrl("https://postman-echo.com/get", token)
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.data, "body").contains("\"authentication\": \"$token\"")
    }

    @Test
    fun `HTTP Error Tests`() {
        listOf(
            400 to "Bad Request",
            403 to "Forbidden",
            404 to "Not Found",
            413 to "URI Too Long",
            414 to "Payload Too Large",
            429 to "Too Many Requests",
            500 to "Internal Server Error",
            523 to "Origin Unreachable",
            999 to "Unknown HTTP Error"
        ).forEach { (code, message) ->
            val exception = assertThrows<HttpErrorException> { throw httpError(code) }
            assertThat(exception.statusCode).isEqualTo(code)
            assertThat(exception.message).isEqualTo(message)
        }
    }

    @Nested
    @DisplayName("Parse Tests")
    inner class ParseTests {
        @Test
        fun `Parse Error`() {
            val json = JSONObject(
                """{
                "error": true,
                "internalError": false,
                "code": 106,
                "message": "No matching joke found",
                "causedBy": [
                    "No jokes were found that match your provided filter(s)"
                ],
                "additionalInfo": "The specified category is invalid",
                "timestamp": 1579170794412
                }""".trimIndent()
            )
            val parsedError = parseError(json)
            assertThat(parsedError).all {
                prop(JokeException::internalError).isFalse()
                prop(JokeException::code).isEqualTo(106)
                prop(JokeException::message).isEqualTo("No matching joke found")
                prop(JokeException::causedBy).index(0)
                    .isEqualTo("No jokes were found that match your provided filter(s)")
                prop(JokeException::additionalInfo).isEqualTo("The specified category is invalid")
                prop(JokeException::timestamp).isEqualTo(1579170794412)
            }
        }

        @Test
        fun `Parse Joke`() {
            val json = JSONObject(
                """{
                    "error": false,
                    "category": "Programming",
                    "type": "single",
                    "joke": "\"Knock, knock.\"\n\"Who's there?\"\n\n[very long pause]\n\n\"Java.\"",
                    "flags": {
                        "nsfw": false,
                        "religious": false,
                        "political": false,
                        "racist": false,
                        "sexist": false,
                        "explicit": false
                    },
                    "id": 184,
                    "safe": false,
                    "lang": "en"
                }""".trimIndent()
            )

            val joke = parseJoke(json, true)
            assertThat(joke).all {
                prop(Joke::category).isEqualTo(Category.PROGRAMMING)
                prop(Joke::type).isEqualTo(Type.SINGLE)
                prop(Joke::joke).all {
                    index(0).isEqualTo("\"Knock, knock.\"")
                    index(1).isEqualTo("\"Who's there?\"")
                    index(2).isEqualTo("[very long pause]")
                    index(3).isEqualTo("\"Java.\"")
                }
                prop(Joke::flags).isEmpty()
                prop(Joke::safe).isFalse()
                prop(Joke::id).isEqualTo(184)
                prop(Joke::lang).isEqualTo(Language.EN)
            }
        }

        @Test
        fun `Parse Two Parts Joke`() {
            val json = JSONObject(
                """{
                "error": false,
                "category": "Spooky",
                "type": "twopart",
                "setup": "What does a turkey dress up as for Halloween?",
                "delivery": "A gobblin'!",
                "flags": {
                    "nsfw": true,
                    "religious": false,
                    "political": false,
                    "racist": false,
                    "sexist": true,
                    "explicit": false
                },
                "safe": true,
                "id": 297,
                "lang": "en"
                }""".trimIndent()
            )


            val joke = parseJoke(json, false)
            assertThat(joke).all {
                prop(Joke::category).isEqualTo(Category.SPOOKY)
                prop(Joke::type).isEqualTo(Type.TWOPART)
                prop(Joke::joke).all {
                    index(0).isEqualTo("What does a turkey dress up as for Halloween?")
                    index(1).isEqualTo("A gobblin'!")
                }
                prop(Joke::flags).all {
                    hasSize(2)
                    contains(Flag.NSFW)
                    contains(Flag.SEXIST)
                }
                prop(Joke::safe).isTrue()
                prop(Joke::id).isEqualTo(297)
                prop(Joke::lang).isEqualTo(Language.EN)
            }
        }
    }
}
