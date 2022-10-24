@file:Suppress("unused")

package com.acme.arzt.rest

import am.ik.yavi.core.ConstraintViolation
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.ArztId
import com.acme.arzt.rest.ArztGetController.Companion.API_PATH
import com.acme.arzt.rest.ArztGetController.Companion.ID_PATTERN
import com.acme.arzt.service.ArztReadService
import com.acme.arzt.service.ArztWriteService
import com.acme.arzt.service.CreateResult
import com.acme.arzt.service.UpdateResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.created
import org.springframework.http.ResponseEntity.noContent
import org.springframework.http.ResponseEntity.notFound
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * Eine `@RestController`-Klasse bildet die REST-Schnittstelle, wobei die HTTP-Methoden, Pfade und MIME-Typen auf die
 * Funktionen der Klasse abgebildet werden
 *
 * ![Klassendiagramm](../../../images/ArztWriteController.svg)
 *
 * @constructor Einen ArztController mit einem injizierten [ArztWriteService] erzeugen.
 *
 * @property service Injiziertes Objekt von [ArztWriteService]
 */
@RestController
@RequestMapping(API_PATH)
@Tag(name = "Aerzte API")
@Suppress("RegExpUnexpectedAnchor")
class ArztWriteController(private val service: ArztWriteService, private val readService: ArztReadService) {
    // https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-ann-methods

    /**
     * Einen neuen Arzt-Datensatz anlegen.
     * @param arztDTO Das Arztobjekt aus dem eingegangenen Request-Body.
     * @param request Das Request-Objekt, um `Location` im Response-Header zu erstellen.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder Statuscode 400 falls Constraints verletzt
     *      sind oder der JSON-Datensatz syntaktisch nicht korrekt ist oder falls die Emailadresse bereits existiert.
     */
    @PostMapping(consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Einen neuen Arzt anlegen")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "Neuer Arzt wurde angelegt"),
        ApiResponse(responseCode = "400", description = "Ungültige Werte oder Email vorhanden"),
    )
    suspend fun create(
        @RequestBody arztDTO: ArztDTO,
        request: ServerHttpRequest,
    ): ResponseEntity<GenericBody> {
        logger.debug("create: {}", arztDTO)

        return when (val result = service.create(arztDTO.toArzt())) {
            is CreateResult.Created -> handleCreated(result.arzt, request)
            is CreateResult.EmailExists -> badRequest().body(GenericBody.Text("${result.email} existiert bereits"))
            is CreateResult.ConstraintViolations -> handleConstraintViolations(result.violations)
        }
    }

    private fun handleCreated(neuerArzt: Arzt, request: ServerHttpRequest): ResponseEntity<GenericBody> {
        logger.trace("handleCreated: {}", neuerArzt)
        val baseUri = getBaseUri(request.headers, request.uri)
        val location = URI("$baseUri/${neuerArzt.id}")
        return created(location).build()
    }

    private fun handleConstraintViolations(violations: Collection<ConstraintViolation>): ResponseEntity<GenericBody> {
        if (violations.isEmpty()) {
            return badRequest().build()
        }

        val arztViolations = violations.associate { violation ->
            violation.messageKey() to violation.message()
        }
        logger.trace("mapConstraintViolations: {}", arztViolations)

        return badRequest().body(GenericBody.Values(arztViolations))
    }

    /**
     * Einen vorhandenen Arzt-Datensatz überschreiben.
     * @param id ID des zu aktualisierenden Arztes.
     * @param arztDTO Das Arztobjekt aus dem eingegangenen Request-Body.
     * @return Response mit Statuscode 204 oder Statuscode 400, falls Constraints verletzt sind oder
     *      der JSON-Datensatz syntaktisch nicht korrekt ist oder falls die Emailadresse bereits existiert.
     */
    @PutMapping(path = ["/{id:$ID_PATTERN}"], consumes = [APPLICATION_JSON_VALUE])
    @Operation(summary = "Einen Arzt mit neuen Werten aktualisieren", tags = ["Aktualisieren"])
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Aktualisiert"),
        ApiResponse(responseCode = "400", description = "Ungültige Werte oder Email vorhanden"),
        ApiResponse(responseCode = "404", description = "Arzt nicht vorhanden"),
    )
    suspend fun update(
        @PathVariable id: ArztId,
        @RequestBody arztDTO: ArztDTO,
    ): ResponseEntity<GenericBody> {
        logger.debug("update: id={}", id, arztDTO)

        val result = service.update(arztDTO.toArzt(), id)
        return handleUpdateResult(result)
    }

    private fun handleUpdateResult(result: UpdateResult): ResponseEntity<GenericBody> =
        when (result) {
            is UpdateResult.Updated -> noContent().build() // 204
            is UpdateResult.NotFound -> notFound().build() // 404
            is UpdateResult.EmailExists -> badRequest().body(GenericBody.Text("${result.email} existiert bereits"))
            is UpdateResult.ConstraintViolations -> handleConstraintViolations(result.violations)
        }

    /**
     * Konstante für den REST-Controller
     */
    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ArztWriteController::class.java)
    }
}
