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

import graphql.schema.DataFetchingEnvironment
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

/**
 * Abbildung von Exceptions auf `GraphQLError`.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Component
@Suppress("unused")
class ExceptionResolverAdapter : DataFetcherExceptionResolverAdapter() {
    /**
     * Abbildung der Exceptions aus ArztGraphQlController auf `GraphQLError`
     * @param ex Exception aus ArztGraphQlController
     * @param env Environment-Objekt
     */
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment) =
        when (ex) {
            is NotFoundException -> NotFoundError(ex.id, ex.suchkriterien)
            is EmailExistsException -> EmailExistsError(ex.email)
            else -> super.resolveToSingleError(ex, env)
        }

    /**
     * Abbildung der Exceptions aus ArztGraphQlController auf `GraphQLError`
     * @param ex Exception aus ArztGraphQlController
     * @param env Environment-Objekt
     */
    override fun resolveToMultipleErrors(ex: Throwable, env: DataFetchingEnvironment) =
        if (ex is ConstraintViolationsException) {
            ex.violations.map { violation -> ConstraintViolationsError(violation) }
                .onEach { error -> logger.debug("error.message={}", error.message) }
        } else {
            super.resolveToMultipleErrors(ex, env)
        }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ExceptionResolverAdapter::class.java)
    }
}
