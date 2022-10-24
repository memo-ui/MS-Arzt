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

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

/**
 * Eine _Value_-Klasse für Eingabedaten passend zu `Suchkriterien` aus dem GraphQL-Schema.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Ein Suchkriterien-Objekt mit den empfangenen Properties erzeugen.
 *
 * @property nachname Nachname
 * @property email Emailadresse
 */
data class Suchkriterien(val nachname: String?, val email: String?) {
    /**
     * Konvertierung in eine Map
     * @return Das konvertierte Map-Objekt
     */
    fun toMultiValueMap(): MultiValueMap<String, String> {
        val map = LinkedMultiValueMap<String, String>()
        if (nachname != null) {
            map["nachname"] = nachname
        }
        if (email != null) {
            map["email"] = email
        }
        return map
    }
}
