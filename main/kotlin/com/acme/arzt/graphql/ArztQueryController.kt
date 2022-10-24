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

import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.ArztId
import com.acme.arzt.service.ArztReadService
import com.acme.arzt.service.FindByIdResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.util.LinkedMultiValueMap

/**
 * Eine _Controller_-Klasse für das Lesen mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Einen ArztQueryController mit einem injizierten [ArztReadService] erzeugen.
 *
 * @property service Injiziertes Objekt von [ArztReadService]
 */
@Controller
@Suppress("unused")
class ArztQueryController(val service: ArztReadService) {
    /**
     * Suche anhand der Arzt-ID als Pfad-Parameter
     * @param id ID des zu suchenden Aerzte
     * @return Der gefundene Arzt
     * @throws NotFoundException falls kein Arzt gefunden wurde
     */
    @QueryMapping
    suspend fun arzt(@Argument id: ArztId): Arzt {
        logger.debug("findById: id={}", id)

        return when (val result = service.findById(id)) {
            is FindByIdResult.Found -> result.arzt
            is FindByIdResult.NotFound -> throw NotFoundException(id)
        }
    }

    /**
     * Suche mit diversen Suchkriterien
     * @param suchkriterien Suchkriterien und ihre Werte, z.B. `nachname` und `Alpha`
     * @return Der gefundene Arzt
     * @throws NotFoundException falls kein Arzt gefunden wurde
     */
    @QueryMapping
    suspend fun aerzte(@Argument("input") suchkriterien: Suchkriterien?): Collection<Arzt> {
        logger.debug("find: suchkriterien={}", suchkriterien)

        val aerzte = service.find(suchkriterien?.toMultiValueMap() ?: LinkedMultiValueMap())
        logger.debug("find: {}", aerzte)

        if (aerzte.isEmpty()) {
            throw NotFoundException(suchkriterien = suchkriterien)
        }
        return aerzte
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ArztQueryController::class.java)
    }
}
