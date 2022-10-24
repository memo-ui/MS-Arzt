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

import graphql.GraphQLError // NOSONAR
import org.springframework.graphql.execution.ErrorType.BAD_REQUEST

/**
 * Exception, falls die Emailadresse für den neu anzulegenden Arzt bereits existiert.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property email Die bereits existierende Email
 */
class EmailExistsException(val email: String) : Exception()

/**
 * Fehlerklasse für GraphQL, falls eine [EmailExistsException] geworfen wurde. Die Abbildung erfolgt in
 * [ExceptionResolverAdapter].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property email Die bereits existierende Email
 */
class EmailExistsError(private val email: String) : GraphQLError {
    /**
     * Message innerhalb von _Errors_ beim Response für einen GraphQL-Request.
     */
    override fun getMessage() = "Die Emailadresse $email existiert bereits."

    /**
     * Keine Angabe von Zeilen- und Spaltennummer der GraphQL-Mutation, falls die Emailadresse bereits existiert.
     */
    override fun getLocations() = null

    /**
     * `ErrorType` auf `BAD_REQUEST` setzen.
     */
    override fun getErrorType() = BAD_REQUEST
}
