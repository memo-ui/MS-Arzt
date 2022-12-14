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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.arzt.rest

import com.acme.arzt.entity.ArztId
import com.acme.arzt.rest.ArztGetController.Companion.API_PATH
import org.springframework.http.HttpHeaders
import java.net.URI

/**
 * Basis-URI ermitteln, d.h. ohne angehängten Pfad-Parameter für die ID und ohne Query-Parameter
 * @param headers Header-Daten des Request-Objekts
 * @param uri URI zum eingegangenen Request
 * @param id Eine Arzt-ID oder null als Defaultwert
 * @return Die Basis-URI als String
 */
@Suppress("UastIncorrectHttpHeaderInspection")
fun getBaseUri(headers: HttpHeaders, uri: URI, id: ArztId? = null): String {
    val envoyOriginalPath = headers.getFirst("x-envoy-original-path")
    if (envoyOriginalPath != null) {
        // Forwarding durch Envoy von K8s
        // host: "localhost"
        // x-forwarded-proto: "http"
        // x-envoy-decorator-operation: "arzt.acme.svc.cluster.local:8080/aerzte/*",
        // x-envoy-original-path: "/aerzte/api/00000000-0000-0000-0000-000000000001"
        val host = headers.getFirst("Host")
        val forwardedProto = headers.getFirst("x-forwarded-proto")
        val basePath = envoyOriginalPath.substringBefore('?').removeSuffix("/")
        return "$forwardedProto://$host$basePath"
    }

    val forwardedHost = headers.getFirst("x-forwarded-host")
    if (forwardedHost != null) {
        // Forwarding durch Spring Cloud Gateway
        // x-forwarded-proto: "https"
        // x-forwarded-host: "localhost:8443"
        // x-forwarded-prefix: "/aerzte"
        val forwardedProto = headers.getFirst("x-forwarded-proto")
        val forwardedPrefix = headers.getFirst("x-forwarded-prefix")
        return "$forwardedProto://$forwardedHost$forwardedPrefix$API_PATH"
    }

    // KEIN Forwarding von einem API-Gateway
    val baseUri = uri.toString().substringBefore('?').removeSuffix("/")
    if (id == null) {
        return baseUri
    }
    return baseUri.removeSuffix("/$id")
}
