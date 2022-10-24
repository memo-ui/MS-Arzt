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

import am.ik.yavi.core.ConstraintViolation
import graphql.GraphQLError // NOSONAR
import org.springframework.graphql.execution.ErrorType.BAD_REQUEST

/**
 * Exception, falls die Werte für den neu anzulegenden Arzt nicht valide sind.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property violations Die verletzten Constraints
 */
class ConstraintViolationsException(val violations: Collection<ConstraintViolation>) : Exception()

/**
 * Fehlerklasse für GraphQL, falls eine [ConstraintViolationsException] geworfen wurde. Die Abbildung erfolgt in
 * [ExceptionResolverAdapter].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property violation Das verletzte Constraint
 */
class ConstraintViolationsError(private val violation: ConstraintViolation) : GraphQLError {
    /**
     * Message innerhalb von _Errors_ beim Response für einen GraphQL-Request.
     * @return _Message Key_ zum verletzten Constraint
     */
    override fun getMessage() = "${violation.messageKey()}: ${violation.message()}"

    /**
     * Keine Angabe von Zeilen- und Spaltennummer der GraphQL-Mutation, falls Constraints verletzt sind.
     * @return null
     */
    override fun getLocations() = null

    /**
     * `ErrorType` auf `BAD_REQUEST` setzen.
     * @return `BAD_REQUEST`
     */
    override fun getErrorType() = BAD_REQUEST

    /**
     * Pfadangabe von der Wurzel bis zum fehlerhaften Datenfeld
     * @return Liste der Datenfelder von der Wurzel bis zum Fehler
     */
    override fun getPath() = listOf("input") + violation.name().split('.')
}
