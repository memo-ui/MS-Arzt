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
package com.acme.arzt.service

import com.acme.arzt.entity.Adresse
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.ArztId
import com.acme.arzt.entity.ArztId.randomUUID
import com.acme.arzt.entity.FamilienstandType.LEDIG
import com.acme.arzt.entity.GeschlechtType.WEIBLICH
import com.acme.arzt.entity.InteresseType.LESEN
import com.acme.arzt.entity.InteresseType.REISEN
import com.acme.arzt.entity.Umsatz
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldBeEqualIgnoringCase
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.smallrye.mutiny.Uni
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hibernate.reactive.mutiny.Mutiny
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.condition.EnabledForJreRange
import org.junit.jupiter.api.condition.JRE.JAVA_17
import org.junit.jupiter.api.condition.JRE.JAVA_18
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.util.LinkedMultiValueMap
import java.math.BigDecimal.ONE
import java.net.URL
import java.time.LocalDate
import java.util.Currency
import java.util.Locale.GERMANY
import javax.persistence.NoResultException
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.get

// https://junit.org/junit5/docs/current/user-guide
// https://assertj.github.io/doc

@Tag("service")
@Tag("service_read")
@DisplayName("Anwendungskern fuer Lesen testen")
@ExtendWith(MockKExtension::class)
@EnabledForJreRange(min = JAVA_17, max = JAVA_18)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExperimentalCoroutinesApi
@Suppress(
    "ReactorUnusedPublisher",
    "ReactiveStreamsUnusedPublisher",
    "UndocumentedPublicClass",
    "UndocumentedPublicFunction",
)
class ArztReadServiceTest {
    private var sessionFactory = mockk<SessionFactory>()

    private val criteriaBuilder = mockk<CriteriaBuilder>()
    private val criteriaQuery = mockk<CriteriaQuery<Arzt>>()
    private val arztRoot = mockk<Root<Arzt>>()
    private val predicate = mockk<Predicate>()

    private val queryBuilder = QueryBuilder(sessionFactory)
    private val service = ArztReadService(sessionFactory, queryBuilder)

    @BeforeEach
    fun beforeEach() {
        clearMocks(
            sessionFactory,
            criteriaBuilder,
            criteriaQuery,
            arztRoot,
        )
    }

    @Suppress("ClassName")
    @Nested
    inner class `Suche anhand der ID` {
        @ParameterizedTest
        @CsvSource("$ID_VORHANDEN, $NACHNAME")
        @Order(1000)
        fun `Suche mit vorhandener ID`(idStr: String, nachname: String) = runTest {
            // given
            val id = ArztId.fromString(idStr)
            val arztMock = createArztMock(id, nachname)
            val arztUniMock = Uni.createFrom().item(arztMock)
            every {
                sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<Arzt>>>())
            } returns arztUniMock

            // when
            val result = service.findById(id)

            // then
            assertSoftly(result) {
                shouldBeTypeOf<FindByIdResult.Found>()
                arzt.id shouldBe id
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [ID_NICHT_VORHANDEN])
        @Order(1100)
        fun `Suche mit nicht vorhandener ID`(idStr: String) = runTest {
            val id = ArztId.fromString(idStr)
            every {
                sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<Arzt>>>())
            } returns Uni.createFrom().nullItem()

            // when
            val result = service.findById(id)

            // then
            result.shouldBeTypeOf<FindByIdResult.NotFound>()
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [NACHNAME])
    @Order(500)
    fun `Suche alle Arztn`(nachname: String) = runTest {
        // given
        every { sessionFactory.criteriaBuilder } returns criteriaBuilder
        every { criteriaBuilder.createQuery(Arzt::class.java) } returns criteriaQuery
        every { criteriaQuery.from(Arzt::class.java) } returns arztRoot

        val arztMock = createArztMock(nachname)
        val arztnUniMock = Uni.createFrom().item(listOf(arztMock))
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<List<Arzt>>>>())
        } returns arztnUniMock
        val emptyQueryParams = LinkedMultiValueMap<String, String>()

        // when
        val arztn = service.find(emptyQueryParams)

        // then
        arztn shouldNot beEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = [NACHNAME])
    @Order(600)
    fun `Suche mit vorhandenem Nachnamen`(nachname: String) = runTest {
        // given
        val arztMock = createArztMock(nachname)
        val arztnUniMock = Uni.createFrom().item(listOf(arztMock))
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<List<Arzt>>>>())
        } returns arztnUniMock
        val queryParams = LinkedMultiValueMap(mapOf("nachname" to listOfNotNull(nachname)))

        // when
        val arztn = service.find(queryParams)

        // then
        arztn shouldNot beEmpty()
        assertSoftly {
            arztn.forEach { arzt ->
                arzt.nachname shouldBe nachname
            }
        }
    }

    @ParameterizedTest
    @CsvSource("$NACHNAME, $EMAIL")
    @Order(700)
    fun `Suche mit vorhandener Emailadresse`(
        nachname: String,
        email: String,
    ) = runTest {
        // given
        val arztMock = createArztMock(nachname)
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<Arzt?>>>())
        } returns Uni.createFrom().item(arztMock)
        val queryParams = LinkedMultiValueMap(mapOf("email" to listOfNotNull(email)))

        // when
        val arztn = service.find(queryParams)

        // then
        arztn shouldNot beEmpty()
        assertSoftly {
            arztn.forEach { arzt ->
                arzt.email shouldBeEqualIgnoringCase email
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [EMAIL])
    @Order(800)
    fun `Suche mit nicht-vorhandener Emailadresse`(email: String) = runTest {
        // given
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<Arzt?>>>())
        } throws NoResultException()
        val queryParams = LinkedMultiValueMap(mapOf("email" to listOfNotNull(email)))

        // when
        val arztn = service.find(queryParams)

        // then
        arztn.shouldBeEmpty()
    }

    @ParameterizedTest
    @CsvSource("$ID_VORHANDEN, $NACHNAME, $EMAIL, $PLZ")
    @Order(900)
    fun `Suche mit vorhandener PLZ`(
        idStr: String,
        nachname: String,
        email: String,
        plz: String,
    ) = runTest {
        // given
        val queryParams = LinkedMultiValueMap(mapOf("plz" to listOfNotNull(plz)))

        every { sessionFactory.criteriaBuilder } returns criteriaBuilder
        every { criteriaBuilder.createQuery(Arzt::class.java) } returns criteriaQuery
        every { criteriaQuery.from(Arzt::class.java) } returns arztRoot
        every {
            criteriaBuilder.like(arztRoot.get(Arzt::adresse).get(Adresse::plz), "$plz%")
        } returns predicate
        every { criteriaBuilder.and(*listOf(predicate).toTypedArray()) } returns predicate
        every { criteriaQuery.where(predicate) } returns criteriaQuery

        val id = ArztId.fromString(idStr)
        val arztMock = createArztMock(id, nachname, email, plz)
        val arztnUniMock = Uni.createFrom().item(listOf(arztMock))
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<List<Arzt>>>>())
        } returns arztnUniMock

        // when
        val arztn = service.find(queryParams)

        // then
        arztn shouldNot beEmpty()
        assertSoftly {
            arztn.map { arzt -> arzt.adresse.plz }
                .forEach { p -> p shouldBe plz }
        }
    }

    @ParameterizedTest
    @CsvSource("$ID_VORHANDEN, $NACHNAME, $EMAIL, $PLZ")
    @Order(1000)
    fun `Suche mit vorhandenem Nachnamen und PLZ`(
        idStr: String,
        nachname: String,
        email: String,
        plz: String,
    ) = runTest {
        // given
        val queryParams =
            LinkedMultiValueMap(mapOf("nachname" to listOfNotNull(nachname), "plz" to listOfNotNull(plz)))

        every { sessionFactory.criteriaBuilder } returns criteriaBuilder
        every { criteriaBuilder.createQuery(Arzt::class.java) } returns criteriaQuery
        every { criteriaQuery.from(Arzt::class.java) } returns arztRoot
        every { criteriaBuilder.like(arztRoot.get(Arzt::nachname), "%$nachname%") } returns predicate
        every {
            criteriaBuilder.like(arztRoot.get(Arzt::adresse).get(Adresse::plz), "$plz%")
        } returns predicate
        every { criteriaBuilder.and(*listOf(predicate, predicate).toTypedArray()) } returns predicate
        every { criteriaQuery.where(predicate) } returns criteriaQuery

        val id = ArztId.fromString(idStr)
        val arztMock = createArztMock(id, nachname, email, plz)
        val arztnUniMock = Uni.createFrom().item(listOf(arztMock))
        every {
            sessionFactory.withSession(any<JavaFunction<Mutiny.Session, Uni<List<Arzt>>>>())
        } returns arztnUniMock

        // when
        val arztn = service.find(queryParams)

        // then
        arztn shouldNot beEmpty()
        assertSoftly {
            arztn.forEach { arzt ->
                assertSoftly(arzt) {
                    nachname shouldBeEqualIgnoringCase nachname
                    adresse.plz shouldBe plz
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mocking
    // -------------------------------------------------------------------------
    private fun createArztMock(nachname: String): Arzt = createArztMock(randomUUID(), nachname)

    private fun createArztMock(id: ArztId, nachname: String): Arzt = createArztMock(id, nachname, EMAIL, PLZ)

    private fun createArztMock(
        id: ArztId? = null,
        nachname: String,
        email: String,
        plz: String,
    ): Arzt {
        val adresse = Adresse(plz = plz, ort = ORT)
        return Arzt(
            id = id,
            version = 0,
            nachname = nachname,
            email = email,
            newsletter = true,
            umsatz = Umsatz(betrag = ONE, waehrung = WAEHRUNG),
            homepage = URL(HOMEPAGE),
            geburtsdatum = GEBURTSDATUM,
            geschlecht = WEIBLICH,
            familienstand = LEDIG,
            interessen = setOfNotNull(LESEN, REISEN),
            adresse = adresse,
        )
    }

    private companion object {
        const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val PLZ = "12345"
        const val ORT = "Testort"
        const val NACHNAME = "Nachname-Test"
        const val EMAIL = "theo@test.de"
        val GEBURTSDATUM: LocalDate = LocalDate.of(2018, 1, 1)
        val WAEHRUNG: Currency = Currency.getInstance(GERMANY)
        const val HOMEPAGE = "https://test.de"
    }
}
