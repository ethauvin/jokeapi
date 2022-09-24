/*
 * GetRawJokeTest.kt
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

import net.thauvin.erik.jokeapi.JokeApi.Companion.getRawJoke
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.models.Format
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class GetRawJokeTest {
    @Test
    fun `Get Raw Joke with TXT`() {
        val response = getRawJoke(format = Format.TEXT)
        assertAll("Plain Text",
            { assertTrue(response.isNotEmpty(), "should be not empty") },
            { assertFalse(response.startsWith("Error "), "should not be an error") })
    }

    @Test
    fun `Get Raw Joke with invalid Amount`() {
        val response = getRawJoke(amount = 100)
        assertFalse(response.contains("\"amount\":"), "should not have amount")
    }

    @Test
    fun `Get Raw Joke with XML`() {
        val response = getRawJoke(format = Format.XML)
        assertTrue(
            response.startsWith("<?xml version='1.0'?>\n<data>\n    <error>false</error>"), "should be xml"
        )
    }

    @Test
    fun `Get Raw Joke with YAML`() {
        val response = getRawJoke(format = Format.YAML)
        assertTrue(response.startsWith("error: false"), "should be yaml")
    }

    @Test
    fun `Get Raw Jokes`() {
        val response = getRawJoke(amount = 2)
        assertTrue(response.contains("\"amount\": 2"), "amount should be 2")
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
