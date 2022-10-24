/*
 * Copyright (C) 2013 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import net.minidev.json.annotate.JsonIgnore
import java.math.BigDecimal
import java.util.Currency
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property id Generierte UUID
 * @property betrag Der Betrag als unveränderliches Pflichtfeld.
 * @property waehrung Die Währungseinheit als unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Betrag und Währungseinheit.
 */
@Entity
@Table(name = "umsatz")
data class Umsatz(
    @Id
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-uuid
    @GeneratedValue
    // Oracle: https://stackoverflow.com/questions/50003906/storing-uuid-as-string-in-mysql-using-jpa
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    // @Column(columnDefinition = "char(36)")
    // @Type(type = "org.hibernate.type.UUIDCharType")
    @JsonIgnore
    val id: DbId? = null,

    val betrag: BigDecimal,

    val waehrung: Currency,
)
