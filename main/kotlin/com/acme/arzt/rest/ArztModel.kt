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
package com.acme.arzt.rest

import com.acme.arzt.entity.Adresse
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.FamilienstandType
import com.acme.arzt.entity.GeschlechtType
import com.acme.arzt.entity.InteresseType
import com.acme.arzt.entity.Umsatz
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.hateoas.RepresentationModel
import org.springframework.hateoas.server.core.Relation
import java.net.URL
import java.time.LocalDate

/**
 * Unveränderliche Daten eines Arztes an der REST-Schnittstelle.
 *
 * @property nachname Gültiger Nachname eines Arztes, d.h. mit einem geeigneten Muster.
 * @property email Email eines Arztes.
 * @property kategorie Kategorie eines Arztes mit eingeschränkten Werten.
 * @property newsletter Flag, ob es ein Newsletter-Abo gibt.
 * @property geburtsdatum Das Geburtsdatum eines Arztes.
 * @property homepage Die Homepage eines Arztes.
 * @property geschlecht Das Geschlecht eines Arztes.
 * @property familienstand Der Familienstand eines Arztes.
 * @property interessen Die Interessen eines Arztes.
 * @property umsatz Der Umsatz eines Arztes.
 * @property adresse Die Adresse eines Arztes.
 */
@JsonPropertyOrder(
    "nachname", "email", "kategorie", "newsletter", "geburtsdatum", "homepage", "geschlecht", "familienstand",
    "interessen", "umsatz", "adresse",
)
@Relation(collectionRelation = "aerzte", itemRelation = "arzt")
data class ArztModel(
    val nachname: String,

    val email: String,

    val kategorie: Int = 0,

    val newsletter: Boolean = false,

    val geburtsdatum: LocalDate?,

    val homepage: URL?,

    val geschlecht: GeschlechtType?,

    val familienstand: FamilienstandType?,

    val interessen: List<InteresseType>?,

    val umsatz: Umsatz?,

    val adresse: Adresse,
) : RepresentationModel<ArztModel>() {
    constructor(arzt: Arzt) : this(
        arzt.nachname,
        arzt.email,
        arzt.kategorie,
        arzt.newsletter,
        arzt.geburtsdatum,
        arzt.homepage,
        arzt.geschlecht,
        arzt.familienstand,
        arzt.interessen?.toList(),
        arzt.umsatz,
        arzt.adresse,
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

        other as ArztModel
        return email == other.email
    }

    /**
     * Hashwert aufgrund der email.
     * @return Der Hashwert.
     */
    override fun hashCode() = email.hashCode()
}
