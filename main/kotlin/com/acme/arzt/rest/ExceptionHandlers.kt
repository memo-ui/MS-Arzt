/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.arzt.rest

import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Allgemeiner ExceptionHandler für z.B. Timeouts
 */
@RestControllerAdvice
class ExceptionHandlers {
    /**
     * Handler für Timeouts
     * @param ex TimeoutCancellationException
     * @return Meldung für Timeout
     */
    @ExceptionHandler(TimeoutCancellationException::class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun handleCoroutineTimeout(ex: TimeoutCancellationException): String {
        logger.warn("handleCoroutineTimeout: ${ex.message}")
        return "Ein Timeout ist aufgetreten"
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(ExceptionHandlers::class.java)
    }
}
