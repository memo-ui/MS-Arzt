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

import com.acme.arzt.entity.Adresse
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.FamilienstandType
import com.acme.arzt.entity.GeschlechtType
import com.acme.arzt.entity.InteresseType
import com.acme.arzt.entity.Umsatz
import java.math.BigDecimal
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Currency

/**
 * Eine _Value_-Klasse für Eingabedaten passend zu `ArztInput` aus dem GraphQL-Schema.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @constructor Ein ArztInput-Objekt mit den empfangenen Properties erzeugen.
 *
 * @property nachname Nachname
 * @property email Emailadresse
 * @property kategorie Kategorie
 * @property newsletter Newsletter-Abonnement
 * @property geburtsdatum Geburtsdatum
 * @property homepage URL der Homepage
 * @property geschlecht Geschlecht
 * @property familienstand Familienstand
 * @property interessen Interessen als Liste
 * @property umsatz Umsatz
 * @property adresse Adresse
 */
data class ArztInput(
    val nachname: String,

    val email: String,

    val kategorie: Int = 0,

    val newsletter: Boolean = false,

    // TODO LocalDate
    val geburtsdatum: String?,

    val homepage: URL?,

    val geschlecht: GeschlechtType = GeschlechtType.WEIBLICH,

    val familienstand: FamilienstandType = FamilienstandType.LEDIG,

    val interessen: List<InteresseType> = emptyList(),

    val umsatz: UmsatzInput?,

    val adresse: AdresseInput,
) {
    /**
     * Konvertierung in ein Objekt der Entity-Klasse [Arzt]
     * @return Das konvertierte Arzt-Objekt
     */
    fun toArzt(): Arzt {
        val geburtsdatum = try {
            LocalDate.parse(geburtsdatum)
        } catch (e: DateTimeParseException) {
            null
        }
        val umsatz = if (umsatz == null) {
            null
        } else {
            Umsatz(betrag = umsatz.betrag, waehrung = umsatz.waehrung)
        }
        val adresse = Adresse(plz = adresse.plz, ort = adresse.ort)

        return Arzt(
            id = null,
            nachname = nachname,
            email = email,
            kategorie = kategorie,
            newsletter = newsletter,
            geburtsdatum = geburtsdatum,
            homepage = homepage,
            geschlecht = geschlecht,
            familienstand = familienstand,
            interessen = interessen?.toSet(),
            umsatz = umsatz,
            adresse = adresse,
        )
    }
}

/**
 * Adressdaten.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property plz Die 5-stellige Postleitzahl als unveränderliches Pflichtfeld.
 * @property ort Der Ort als unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Postleitzahl und Ort.
 */
data class AdresseInput(val plz: String, val ort: String)

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property betrag Der Betrag als unveränderliches Pflichtfeld.
 * @property waehrung Die Währungseinheit als unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Betrag und Währungseinheit.
 */
data class UmsatzInput(val betrag: BigDecimal, val waehrung: Currency)
