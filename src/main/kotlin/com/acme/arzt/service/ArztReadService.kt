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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.arzt.service

import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.ArztId
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.withTimeout
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.hibernate.reactive.mutiny.find
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import javax.persistence.NoResultException

/**
 * Anwendungslogik für Aerzte.
 *
 * ![Klassendiagramm](../../../images/ArztReadService.svg)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen ArztService mit einem injizierten `ValidatorFactory` erzeugen.
 */
@Service
class ArztReadService(
    private val factory: SessionFactory,
    @Lazy private val queryBuilder: QueryBuilder,
) {
    /**
     * Einen Arzt anhand seiner ID suchen.
     * @param id Die Id des gesuchten Arztes
     * @return Der gefundene Arzt oder null.
     */
    suspend fun findById(id: ArztId): FindByIdResult {
        logger.debug("findById: id={}", id)
        val arzt = findByArztId(id)
        return if (arzt == null) {
            logger.debug("findById:kein Arzt gefunden")
            FindByIdResult.NotFound(id)
        } else {
            FindByIdResult.Found(arzt)
        }
    }

    /**
     * Einen Arzt anhand seiner ID suchen.
     * @param id Die Id des gesuchten Arztes
     * @return Der gefundene Arzt oder null.
     */
    suspend fun findByArztId(id: ArztId): Arzt? {
        // ggf. TimeoutCancellationException
        val arzt = withTimeout(timeoutShort) {
            // https://github.com/spring-projects/spring-data-examples/tree/master/mongodb/fluent-api
            factory.withSession { session ->
                // session.find(Arzt::class.java, id)
                session.find<Arzt>(id) // Lesen: keine Transaktino erforderlich
            }.awaitSuspending()
        }
        logger.debug("findById: {}", arzt)
        return arzt
    }

    /**
     * Aerzte anhand von Suchkriterien suchen
     *
     * @param suchkriterien Die Suchkriterien
     *
     * @return Die gefundenen Aerzte
     */

    @Suppress("ReturnCount")
    suspend fun find(suchkriterien: MultiValueMap<String, String>): Collection<Arzt> {
        logger.debug("find: suchkriterien={}", suchkriterien)

        if (suchkriterien.isEmpty()) {
            return findAll()
        }

        if (suchkriterien.size == 1) {
            val nachnamen = suchkriterien["nachname"]
            if (nachnamen?.size == 1) {
                return findByNachname(nachnamen[0])
            }

            val emails = suchkriterien["email"]
            if (emails?.size == 1) {
                val arzt = findByEmail(emails[0]) ?: return emptyList()
                return listOf(arzt)
            }
        }

        val criteriaQuery = when (val builderResult = queryBuilder.build(suchkriterien)) {
            is QueryBuilderResult.Failure -> return emptyList()
            is QueryBuilderResult.Success -> builderResult.criteriaQuery
        }

        val aerzte = withTimeout(timeoutLong) {
            factory.withSession { session ->
                session.createQuery(criteriaQuery).resultList // Lesen: keine Transaktion erforderlich
            }.awaitSuspending()
        }
        logger.debug("find: {}", aerzte)

        return aerzte
    }

    /**
     * Alle Aerzte ermitteln
     *
     * Returns: Alle Aerzte
     */
    suspend fun findAll(): Collection<Arzt> = withTimeout(timeoutShort) {
        factory.withSession { session ->
            session.createNamedQuery<Arzt>(Arzt.ALL)
                .resultList
        }.awaitSuspending()
    }

    private suspend fun findByNachname(nachname: String): Collection<Arzt> = withTimeout(timeoutShort) {
        factory.withSession { session ->
            session.createNamedQuery<Arzt>(Arzt.BY_NACHNAME)
                .setParameter(Arzt.PARAM_NACHNAME, "%$nachname%")
                .resultList
        }.awaitSuspending()
    }

    @Suppress("SwallowedException")
    private suspend fun findByEmail(email: String) = try {
        withTimeout(timeoutShort) {
            factory.withSession { session ->
                // session.createNamedQuery(Arzt.BY_EMAIL, Arzt::class.java)
                session.createNamedQuery<Arzt>(Arzt.BY_EMAIL)
                    .setParameter(Arzt.PARAM_EMAIL, email)
                    .singleResult
            }.awaitSuspending()
        }
    } catch (e: NoResultException) {
        logger.debug("Keine Arzt mit der Emailadresse '{}'", email)
        null
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ArztReadService::class.java)

        const val timeoutShort = 1500L
        const val timeoutLong = 2000L
    }
}
