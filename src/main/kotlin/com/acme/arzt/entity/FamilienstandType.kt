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
package com.acme.arzt.entity

import com.fasterxml.jackson.annotation.JsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.persistence.AttributeConverter

/**
 * Enum für Familienstand. Dazu kann auf der Clientseite z.B. ein Dropdown-Menü realisiert werden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property value Der interne Wert
 */
enum class FamilienstandType(val value: String) {
    /**
     * _Ledig_ mit dem internen Wert `L` für z.B. das Mapping in einem JSON-Datensatz oder das Abspeichern in einer DB.
     */
    LEDIG("L"),

    /**
     * _Verheiratet_ mit dem internen Wert `VH` für z.B. das Mapping in einem JSON-Datensatz oder
     * das Abspeichern in einer DB.
     */
    VERHEIRATET("VH"),

    /**
     * _Geschieden_ mit dem internen Wert `G` für z.B. das Mapping in einem JSON-Datensatz oder
     * das Abspeichern in einer DB.
     */
    GESCHIEDEN("G"),

    /**
     * _Verwitwet_ mit dem internen Wert `VW` für z.B. das Mapping in einem JSON-Datensatz oder
     * das Abspeichern in einer DB.
     */
    VERWITWET("VW");

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     * Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
     * [https://github.com/FasterXML/jackson-databind/wiki]
     * @return Der interne Wert.
     */
    @JsonValue
    override fun toString() = value

    /**
     * Companion Object, um aus einem String einen Enum-Wert von FamilienstandType zu bauen
     */
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FamilienstandType::class.java)

        /**
         * Konvertierung eines Strings in einen Enum-Wert
         * @param value Der String, zu dem ein passender Enum-Wert ermittelt werden soll.
         * @return Passender Enum-Wert.
         */
        fun fromValue(value: String?) = try {
            enumValues<FamilienstandType>().single { familienstand -> familienstand.value == value }
        } catch (e: NoSuchElementException) {
            logger.warn("Ungueltiger Wert '{}' fuer FamilienstandType: {}", value, e.message)
            null
        }
    }
}

/**
 * Konvertierungsklasse, um die Enum-Werte abgekürzt abzuspeichern.
 */
class FamilienstandTypeConverter : AttributeConverter<FamilienstandType, String> {
    /**
     * Konvertierungsfunktion, um einen Enum-Wert in einen abgekürzten String für die DB zu transformieren.
     * @param familienstand Der Enum-Wert
     * @return Der abgekürzte String
     */
    override fun convertToDatabaseColumn(familienstand: FamilienstandType?): String? = familienstand?.value

    /**
     * Konvertierungsfunktion, um einen abgekürzten String aus einer DB-Spalte in einen Enum-Wert zu transformieren.
     * @param value Der abgekürzte String
     * @return Der Enum-Wert
     */
    override fun convertToEntityAttribute(value: String?) = FamilienstandType.fromValue(value)
}
