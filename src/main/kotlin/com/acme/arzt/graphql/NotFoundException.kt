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

import com.acme.arzt.entity.ArztId
import graphql.GraphQLError // NOSONAR
import org.springframework.graphql.execution.ErrorType.NOT_FOUND

/**
 * Exception, falls mit dem Anwendungskern kein Arzt gefunden wird.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property id ID des nicht-vorhandenen Arzt
 * @property suchkriterien Suchkriterien, zu denen es keinen Arzt gibt
 */
class NotFoundException(val id: ArztId? = null, val suchkriterien: Suchkriterien? = null) : Exception()

/**
 * Fehlerklasse für GraphQL, falls eine [NotFoundException] geworfen wurde. Die Abbildung erfolgt in
 * [ExceptionResolverAdapter].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
class NotFoundError(private val id: ArztId? = null, private val suchkriterien: Suchkriterien? = null) : GraphQLError {
    /**
     * Message innerhalb von _Errors_ beim Response für einen GraphQL-Request.
     */
    override fun getMessage(): String {
        if (id != null) {
            return "Kein Arzt mit der ID $id gefunden"
        }
        return "Kein Arzt gefunden: $suchkriterien"
    }

    /**
     * Keine Angabe von Zeilen- und Spaltennummer der GraphQL-Query, falls kein Arzt gefunden wurde.
     */
    override fun getLocations() = null

    /**
     * `ErrorType` auf `NOT_FOUND` setzen.
     */
    override fun getErrorType() = NOT_FOUND
}
