/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.arzt.rest

import com.acme.arzt.config.ProfilesBanner.DEV
import com.acme.arzt.rest.ArztGetController.Companion.API_PATH
import com.acme.arzt.service.ArztValidator
import com.jayway.jsonpath.JsonPath
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.json.shouldContainJsonKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_18
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.getBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContext
import org.springframework.hateoas.MediaTypes.HAL_JSON
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange

// https://junit.org/junit5/docs/current/user-guide
// https://assertj.github.io/doc

@Tag("rest")
@Tag("rest_get")
@DisplayName("REST-Schnittstelle fuer GET-Requests testen")
// Alternative zu @ContextConfiguration von Spring
// Default: webEnvironment = MOCK, d.h.
//          Mocking mit ReactiveWebApplicationContext anstatt z.B. Netty oder Tomcat
@SpringBootTest(webEnvironment = RANDOM_PORT)
// @SpringBootTest(webEnvironment = DEFINED_PORT, ...)
// ggf.: @DirtiesContext, falls z.B. ein Spring Bean modifiziert wurde
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_17, max = JAVA_18)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExperimentalCoroutinesApi
@Suppress(
    "ClassName",
    "UndocumentedPublicClass",
    "UndocumentedPublicProperty",
    "UndocumentedPublicFunction",
    "StringLiteralDuplication",
)
class ArztGetRestTest(@LocalServerPort private val port: Int, ctx: ApplicationContext) {
    private val baseUrl = "$SCHEMA://$HOST:$port$API_PATH"

    // WebClient auf der Basis von "Reactor Netty"
    // Alternative: Http Client von Java http://openjdk.java.net/groups/net/httpclient/intro.html

    val clientArzt = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    init {
        ctx.getBean<ArztGetController>() shouldNotBe null
    }

    @Test
    @Order(100)
    fun `Immer erfolgreich`() {
        true shouldBe true
    }

    @Nested
    inner class `Suche anhand der ID` {
        @ParameterizedTest
        @ValueSource(strings = [ID_VORHANDEN, ID_VORHANDEN_ANDERER_ARZT])
        @Order(1000)
        fun `Suche mit vorhandener ID und JsonPath`(id: String) = runTest {
            // when
            val response = clientArzt.get()
                .uri(ID_PATH, id)
                .accept(HAL_JSON)
                .awaitExchange { response -> response.awaitEntity<String>() }

            // then
            // Pact https://docs.pact.io ist eine Alternative zu JsonPath
            assertSoftly(response) {
                statusCode shouldBe OK

                body shouldNotBe null

                val nachnamePath = "$.nachname"
                body shouldContainJsonKey nachnamePath
                val nachname: String = JsonPath.read(body, nachnamePath)
                nachname shouldMatch Regex(ArztValidator.NACHNAME_PATTERN)

                val emailPath = "$.email"
                body shouldContainJsonKey emailPath
                val email: String = JsonPath.read(body, emailPath)
                email shouldContain "@"

                val selfLink = HalLinkDiscoverer().findLinkWithRel("self", body ?: "").get().href
                selfLink shouldBe "$baseUrl/$id"
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [ID_NICHT_VORHANDEN])
        @Order(1600)
        fun `Suche mit nicht-vorhandener ID`(id: String) = runTest {
            // when
            val statusCode = clientArzt.get()
                .uri(ID_PATH, id)
                .awaitExchange { response -> response.statusCode() }

            // then
            statusCode shouldBe NOT_FOUND
        }

        @ParameterizedTest
        @ValueSource(strings = [ID_INVALID])
        @Order(1800)
        fun `Suche mit syntaktisch ungueltiger ID`(id: String) = runTest {
            // when
            val statusCode = clientArzt.get()
                .uri(ID_PATH, id)
                .awaitExchange { response -> response.statusCode() }

            // then
            statusCode shouldBe NOT_FOUND
        }
    }
    private companion object {
        const val SCHEMA = "http"
        const val HOST = "localhost"
        const val ID_PATH = "/{id}"

        const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
        const val ID_VORHANDEN_ANDERER_ARZT = "00000000-0000-0000-0000-000000000002"
        const val ID_INVALID = "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
    }
}
