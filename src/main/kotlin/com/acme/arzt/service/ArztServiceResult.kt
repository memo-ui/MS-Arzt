// ktlint-disable filename
@file:Suppress("TooManyFunctions")

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

import am.ik.yavi.core.ConstraintViolation
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.ArztId

/**
 * Resultat-Typ für [ArztReadService.findByArztId]. Ein weiteres Resultat könnte z.B.
 * fehlende Zugriffsrechte sein.
 */
sealed interface FindByIdResult {
    /**
     * Resultat-Typ, wenn ein Arzt gefunden wurde.
     * @property arzt Der gefundene Arzt
     */
    data class Found(val arzt: Arzt) : FindByIdResult

    /**
     * Resultat-Typ, wenn kein Arzt gefunden wurde.
     * @property id ID des nicht-vorhandenen Arztes
     */
    data class NotFound(val id: ArztId) : FindByIdResult
}

/**
 * Resultat-Typ für [ArztWriteService.create]
 */
sealed interface CreateResult {
    /**
     * Resultat-Typ, wenn ein neuer Arzt erfolgreich angelegt wurde.
     * @property arzt Der neu angelegte Arzt
     */
    data class Created(val arzt: Arzt) : CreateResult

    /**
     * Resultat-Typ, wenn die Emailadresse bereits existiert.
     * @property email Die bereits existierende Emailadresse
     */
    data class EmailExists(val email: String) : CreateResult

    /**
     * Resultat-Typ, wenn ein Arzt wegen Constraint-Verletzungen nicht angelegt wurde.
     * @property violations Die verletzten Constraints
     */
    data class ConstraintViolations(val violations: Collection<ConstraintViolation>) : CreateResult
}

/**
 * Resultat-Typ für [ArztWriteService.update]
 */
sealed interface UpdateResult {
    /**
     * Resultat-Typ, wenn ein Arzt erfolgreich aktualisiert wurde.
     * @property arzt Der aktualisierte Arzt
     */
    data class Updated(val arzt: Arzt) : UpdateResult

    /**
     * Resultat-Typ, wenn die Emailadresse bereits existiert.
     * @property email Die bereits existierende Emailadresse
     */
    data class EmailExists(val email: String) : UpdateResult

    /**
     * Resultat-Typ, wenn ein Arzt wegen Constraint-Verletzungen nicht aktualisiert wurde.
     * @property violations Die verletzten Constraints
     */
    data class ConstraintViolations(val violations: Collection<ConstraintViolation>) : UpdateResult

    /**
     * Resultat-Typ, wenn ein nicht-vorhandener Arzt aktualisiert werden sollte.
     */
    object NotFound : UpdateResult
}
