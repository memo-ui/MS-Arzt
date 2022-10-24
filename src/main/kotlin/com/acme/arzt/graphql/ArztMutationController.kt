/*
 * Copyright (C) 2021 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.arzt.graphql

import com.acme.arzt.service.ArztWriteService
import com.acme.arzt.service.CreateResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

/**
 * Eine _Controller_-Klasse für das Schreiben mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen ArztMutationController mit einem injizierten [ArztWriteService] erzeugen.
 *
 * @property service Injiziertes Objekt von [ArztWriteService]
 */
@Controller
@Suppress("unused")
class ArztMutationController(val service: ArztWriteService) {
    /**
     * Einen neuen Arzt anlegen
     * @param arztInput Die Eingabedaten für einen neuen Arzt
     * @return Die generierte ID für den neuen Arzt
     * @throws ConstraintViolationsException falls Constraints verletzt sind
     * @throws EmailExistsException falls es bereits einen Arzt mit der Emailadresse gibt
     */
    @MutationMapping
    suspend fun create(@Argument("input") arztInput: ArztInput): CreatePayLoad {
        logger.debug("create: arztInput={}", arztInput)

        return when (val result = service.create(arztInput.toArzt())) {
            is CreateResult.Created -> CreatePayLoad(result.arzt.id)
            is CreateResult.ConstraintViolations -> throw ConstraintViolationsException(result.violations)
            is CreateResult.EmailExists -> throw EmailExistsException(result.email)
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ArztMutationController::class.java)
    }
}
