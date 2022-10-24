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
package com.acme.arzt.rest

import com.acme.arzt.entity.Adresse
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.FamilienstandType
import com.acme.arzt.entity.GeschlechtType
import java.net.URL
import java.time.LocalDate

/**
 * ValueObject für das Ändern eines neuen Arztn. Beim Lesen wird die Klasse [ArztModel] für die Ausgabe verwendet
 * und für das Neuanlegen die Klasse [ArztUserDTO].
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property nachname Gültiger Nachname eines Arztn, d.h. mit einem geeigneten Muster.
 * @property email Email eines Arztn.
 * @property kategorie Kategorie eines Arztn mit eingeschränkten Werten.
 * @property newsletter Flag, ob es ein Newsletter-Abo gibt.
 * @property geburtsdatum Das Geburtsdatum eines Arztn.
 * @property homepage Die Homepage eines Arztn.
 * @property geschlecht Das Geschlecht eines Arztn.
 * @property familienstand Der Familienstand eines Arztn.
 */
data class ArztUpdateDTO(
    val nachname: String,

    val email: String,

    val kategorie: Int = 0,

    val newsletter: Boolean = false,

    val geburtsdatum: LocalDate?,

    val homepage: URL?,

    val geschlecht: GeschlechtType?,

    val familienstand: FamilienstandType?,
) {
    /**
     * Konvertierung in ein Objekt des Anwendungskerns
     * @return Arztobjekt für den Anwendungskern
     */
    fun toArzt() = Arzt(
        nachname = nachname,
        email = email,
        kategorie = kategorie,
        newsletter = newsletter,
        geburtsdatum = geburtsdatum,
        homepage = homepage,
        geschlecht = geschlecht,
        familienstand = familienstand,
        interessen = null,
        umsatz = null,
        // Dummy-Adresse
        adresse = Adresse(plz = "00000", ort = "Ungueltig"),
    )

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Arzt-) Objekt die gleiche email hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArztUpdateDTO
        return email == other.email
    }

    /**
     * Hashwert aufgrund der email.
     * @return Der Hashwert.
     */
    override fun hashCode() = email.hashCode()
}
