@file:Suppress("unused")

/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.arzt.rest

import com.acme.arzt.entity.ArztId
import com.acme.arzt.rest.ArztGetController.Companion.API_PATH
import com.acme.arzt.service.ArztReadService
import com.acme.arzt.service.FindByIdResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.ResponseEntity.ok
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 *
 * ![Klassendiagramm](../../../images/ArztGetController.svg)
 *
 * @constructor Einen ArztController mit einem injizierten [ArztReadService] erzeugen.
 *
 * @property service Injiziertes Objekt von [ArztReadService]
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Aerzte API")
@Suppress("RegExpUnexpectedAnchor")
class ArztGetController(private val service: ArztReadService) {
    // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-ann-methods
    /**
     * Suche anhand der Arzt-ID als Pfad-Parameter
     * @param id ID des zu suchenden Arztes
     * @param request Das Request-Objekt, um Links für HATEOAS zu erstellen.
     * @return Ein Response mit dem Statuscode 200 und dem gefundenen Arzt mit Atom-Links oder Statuscode 404.
     */
    @GetMapping(path = ["/{id:$ID_PATTERN}"], produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit der Arzt-ID", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Arzt gefunden"),
        ApiResponse(responseCode = "404", description = "Arzt nicht gefunden"),
    ) // https://localhost:8080/swagger-ui.html
    suspend fun findById(@PathVariable id: ArztId, request: ServerHttpRequest): ResponseEntity<ArztModel> {
        logger.debug("findById: id={}", id)

        // Anwendungskern
        val arzt = when (val result = service.findById(id)) {
            is FindByIdResult.Found -> result.arzt
            is FindByIdResult.NotFound -> return notFound().build()
        }
        logger.trace("findById: {}", arzt)

        val model = ArztModel(arzt)
        val baseUri = getBaseUri(request.headers, request.uri, id)
        val idUri = "$baseUri/${arzt.id}"

        val selfLink = Link.of(idUri)
        val listLink = Link.of(baseUri, LinkRelation.of("list"))
        val addLink = Link.of(baseUri, LinkRelation.of("add"))
        val updateLink = Link.of(idUri, LinkRelation.of("update"))
        model.add(selfLink, listLink, addLink, updateLink)

        return ok(model)
    }

    /**
     * Eine Suche mit diversen Suchkriterien als Query-Parameter
     *
     * Returns: Ein Response mit den gefundenen Aerzten als Collection und dem Statuscode 200 oder Statuscode 404"
     */
    @GetMapping(produces = [HAL_JSON_VALUE])
    @Operation(summary = "Suche mit Suchkriterien", tags = ["Suchen"])
    @ApiResponses(
        ApiResponse(responseCode = "200", description = " CollectionModel mit den Aerzten"),
        ApiResponse(responseCode = "404", description = " Keinen Arzt gefunden"),
    ) // https://localhost:8080/swagger-ui.html
    suspend fun find(
        @RequestParam queryParams: MultiValueMap<String, String>,
        request: ServerHttpRequest,
    ): ResponseEntity<CollectionModel<ArztModel>> {
        logger.debug("find: id={}")

        val baseUri = getBaseUri(request.headers, request.uri)
        val modelList = service.find(queryParams)
            .map { arzt ->
                val model = ArztModel(arzt)
                val selfLink = Link.of("$baseUri/${arzt.id}")
                model.add(selfLink)
            }
        logger.debug("find: {}", modelList)

        if (modelList.isEmpty()) {
            return notFound().build()
        }

        return ok(CollectionModel.of(modelList))
    }

    /**
     * Konstante für den REST-Controller
     */

    companion object {
        /**
         * Basis-Pfad der REST-Schnittstelle. const: "compile time constant"
         */
        const val API_PATH = "/api"

        private const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster für eine UUID.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"

        private val logger: Logger = LoggerFactory.getLogger(ArztGetController::class.java)
    }
}
