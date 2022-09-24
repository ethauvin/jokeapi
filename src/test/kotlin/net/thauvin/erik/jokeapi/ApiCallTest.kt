/*
 * ApiCallTest.kt
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

import net.thauvin.erik.jokeapi.JokeApi.Companion.apiCall
import net.thauvin.erik.jokeapi.JokeApi.Companion.logger
import net.thauvin.erik.jokeapi.models.Format
import net.thauvin.erik.jokeapi.models.Language
import net.thauvin.erik.jokeapi.models.Parameter
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.logging.ConsoleHandler
import java.util.logging.Level

internal class ApiCallTest {
    @Test
    fun `Get Flags`() {
        // See https://v2.jokeapi.dev/#flags-endpoint
        val response = apiCall(endPoint = "flags")
        val json = JSONObject(response)
        assertAll("Validate JSON",
            { assertFalse(json.getBoolean("error"), "should not be an error") },
            { assertTrue(json.getJSONArray("flags").length() > 0, "should have flags") },
            { assertTrue(json.getLong("timestamp") > 0, "should have a timestamp") })
    }

    @Test
    fun `Get Language Code`() {
        // See https://v2.jokeapi.dev/#langcode-endpoint
        val lang = apiCall(
            endPoint = "langcode", path = "french",
            params = mapOf(Parameter.FORMAT to Format.YAML.value)
        )
        assertTrue(lang.contains("code: \"fr\"")) { "should contain ${Language.FR.value}" }
    }

    @Test
    fun `Get Ping Response`() {
        // See https://v2.jokeapi.dev/#ping-endpoint
        val ping = apiCall(endPoint = "ping", params = mapOf(Parameter.FORMAT to Format.TXT.value))
        assertTrue(ping.startsWith("Pong!"), "should return pong")
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
