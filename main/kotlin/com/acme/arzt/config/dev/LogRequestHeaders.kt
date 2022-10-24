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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.arzt.config.dev

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain

/**
 * WebFilter zur Protokollierung des Request-Headers.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
interface LogRequestHeaders {
    /**
     * WebFilter zur Protokollierung des Request-Headers und zur Pufferung des Headers _Accept-Language_.
     */
    @Bean
    fun logRequestHeaders() = WebFilter { exchange: ServerWebExchange, chain: WebFilterChain ->
        chain.filter(exchange).contextWrite { context ->
            logger.debug("headers: {}", exchange.request.headers)
            context
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(LogRequestHeaders::class.java)
    }
}
