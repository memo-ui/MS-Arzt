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
import com.acme.arzt.entity.Arzt.Companion.EMAIL_EXISTS
import com.acme.arzt.entity.Arzt.Companion.PARAM_EMAIL
import com.acme.arzt.entity.ArztId
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.withTimeout
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

/**
 * Anwendungslogik für Arzt.
 *
 * ![Klassendiagramm](../../../images/ArztWriteService.svg)
 *
 * @constructor Einen ArztService mit einem injizierten `ValidatorFactory` erzeugen.
 */
@Service
class ArztWriteService(
    private val factory: SessionFactory,
    @Lazy private val validator: ArztValidator,
    @Lazy private val readService: ArztReadService,
) {
    @Suppress("ReturnCount")
    /**
     * Einen neuen Arzt anlegen.
     * @param arzt Das Objekt des neu anzulegenden Arztes.
     * @return Ein Resultatobjekt mit entweder dem neu angelegten Arzt oder mit dem Fehlermeldungsobjekt
     */
    suspend fun create(arzt: Arzt): CreateResult {
        logger.debug("create: {}", arzt)
        val violations = validator.validate(arzt)
        if (violations.isNotEmpty()) {
            logger.debug("create: violations={}", violations)
            return CreateResult.ConstraintViolations(violations)
        }
        val email = arzt.email
        if (emailExists(email)) {
            return CreateResult.EmailExists(email)
        }
        logger.trace("create: Email noch nicht vorhanden")

        withTimeout(timeoutLong) {
            // https://hibernate.org/reactive/documentation/1.1/reference/html_single/#_transactions
            // kein Transaktionsobjekt erforderlich, da es keine Bedingung gibt, die zu Rollback fuehren kann
            // zuerst den Arzt abspeichern, dann die Benutzerdaten
            factory.withTransaction { session ->
                session.persist(arzt)
            }.awaitSuspending()
        }

        logger.debug("Neuer Arzt erstellt: {}", arzt)
        return CreateResult.Created(arzt)
    }

    private suspend fun emailExists(email: String) = withTimeout(timeout) {
        factory.withSession { session ->
            @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
            session.createNamedQuery<java.lang.Long>(EMAIL_EXISTS)
                .setParameter(PARAM_EMAIL, email)
                .singleResult
                .map { it > 0 }
        }
            .awaitSuspending()
    }

    /**
     * Einen vorhandenen Arzt aktualisieren.
     * @param arzt Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Arztes
     * @return Ein Resultatobjekt mit entweder dem aktualisierten Arzt oder mit dem Fehlermeldungsobjekt
     */
    @Suppress("ReturnCount")
    suspend fun update(arzt: Arzt, id: ArztId): UpdateResult {
        logger.debug("update: {}", arzt)
        logger.debug("update: id={}", id)
        val violations = validator.validate(arzt)
        if (violations.isNotEmpty()) {
            logger.debug("update: violations={}", violations)
            return UpdateResult.ConstraintViolations(violations)
        }

        val arztDb = readService.findByArztId(id) ?: return UpdateResult.NotFound
        logger.trace("arztDb={}", arztDb)
        val email = arzt.email
        if (emailExists(arztDb, email)) {
            return UpdateResult.EmailExists(email)
        }

        val arztMitId = arzt.copy(id = id)
        logger.debug("update: {}", arztMitId)
        return UpdateResult.Updated(arztMitId)
    }
    private suspend fun emailExists(arztDb: Arzt, neueEmail: String): Boolean {
        // Hat sich die Emailadresse ueberhaupt geaendert?
        if (arztDb.email == neueEmail) {
            logger.trace("emailExists: Email nicht geaendert: {}", neueEmail)
            return false
        }

        logger.trace("emailExists: Email geaendert: {} -> {}", arztDb.email, neueEmail)
        // Gibt es die neue Emailadresse bei einem existierenden Arzt?
        return emailExists(neueEmail)
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ArztWriteService::class.java)
        const val timeout = 1500L
        const val timeoutLong = 2000L
    }
}
