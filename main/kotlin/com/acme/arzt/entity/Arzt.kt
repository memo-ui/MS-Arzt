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

@file:JvmName("ArztKt")

package com.acme.arzt.entity

import com.acme.arzt.service.ArztValidator.Companion.TIMEZONE_BERLIN
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.util.UUID
import javax.persistence.CascadeType.PERSIST
import javax.persistence.CascadeType.REMOVE
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.NamedQuery
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Version

// https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html
// https://medium.com/swlh/defining-jpa-hibernate-entities-in-kotlin-1ff8ee470805

/**
 * Unveränderliche Daten eines Arztes. In DDD ist Arzt ist ein _Aggregate Root_.
 *
 * ![Klassendiagramm](../../../images/Arzt.svg)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 *
 * @property id ID eines Arzt als UUID.
 * @property version Versionsnummer in der DB
 * @property nachname Gültiger Nachname eines Arztes, d.h. mit einem geeigneten Muster.
 * @property email Email eines Arztes.
 * @property kategorie Kategorie eines Arztes
 * @property newsletter Flag, ob es ein Newsletter-Abo gibt.
 * @property geburtsdatum Das Geburtsdatum eines Arztes.
 * @property homepage Die Homepage eines Arztes.
 * @property geschlecht Das Geschlecht eines Arztes.
 * @property familienstand Der Familienstand eines Arztes.
 * @property interessen Die Interessen eines Arztes.
 * @property umsatz Der Umsatz eines Arztes.
 * @property adresse Die Adresse eines Arztes.
 */
@Entity
@Table(name = "arzt")
@NamedQuery(
    name = Arzt.ALL,
    query = "SELECT a FROM Arzt a",
)
@NamedQuery(
    name = Arzt.BY_EMAIL,
    query = """
        SELECT a
        FROM  Arzt a
        WHERE a.email = :${Arzt.PARAM_EMAIL}
    """,
)
@NamedQuery(
    name = Arzt.EMAIL_EXISTS,
    query = """
        SELECT COUNT(*)
        FROM  Arzt a
        WHERE a.email = :${Arzt.PARAM_EMAIL}
    """,
)
@NamedQuery(
    name = Arzt.BY_NACHNAME,
    query = """
        SELECT a
        FROM  Arzt a
        WHERE a.nachname LIKE :${Arzt.PARAM_NACHNAME}
    """,
)
@NamedQuery(
    name = Arzt.NACHNAMEN_PREFIX,
    query = """
        SELECT DISTINCT a.nachname
        FROM  Arzt a
        WHERE a.nachname LIKE :${Arzt.PARAM_NACHNAME}
    """,
)
// "var" fuer Properties, weil JPA fuer "mutable" entworfen wurde :-(
// https://www.jpa-buddy.com/blog/best-practices-and-common-pitfalls/#rules-for-jpa-entities
// https://vladmihalcea.com/immutable-entity-jpa-hibernate
@Suppress("DataClassShouldBeImmutable")
data class Arzt(
    @Id
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-uuid
    // https://in.relation.to/2022/05/12/orm-uuid-mapping
    @GeneratedValue
    // Oracle: https://stackoverflow.com/questions/50003906/storing-uuid-as-string-in-mysql-using-jpa
    // @GeneratedValue(generator = "UUID")
    // @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    // @Column(columnDefinition = "char(36)")
    // @Type(type = "org.hibernate.type.UUIDCharType")
    @Suppress("experimental:annotation-spacing")
    val id: ArztId? = null,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#locking-optimistic-mapping
    @Version
    val version: Int = 0,

    var nachname: String,

    var email: String,

    var kategorie: Int = 0,

    var newsletter: Boolean = false,

    var geburtsdatum: LocalDate?,

    var homepage: URL? = null,

    @Convert(converter = GeschlechtTypeConverter::class)
    var geschlecht: GeschlechtType?,

    @Convert(converter = FamilienstandTypeConverter::class)
    var familienstand: FamilienstandType? = null,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#access-embeddable-types
    @ElementCollection(fetch = EAGER)
    @CollectionTable
    @Column(name = "interesse")
    @Convert(converter = InteresseTypeConverter::class)
    val interessen: Set<InteresseType>?,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#associations-one-to-one-unidirectional
    @OneToOne(cascade = [PERSIST, REMOVE])
    @JoinColumn(updatable = false)
    val umsatz: Umsatz? = null,

    @OneToOne(optional = false, cascade = [PERSIST, REMOVE])
    @JoinColumn(updatable = false)
    val adresse: Adresse,

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#mapping-generated-CreationTimestamp
    @CreationTimestamp
    @Suppress("UnusedPrivateMember")
    private val erzeugt: LocalDateTime = now(ZoneId.of(TIMEZONE_BERLIN)),

    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#mapping-generated-UpdateTimestamp
    @UpdateTimestamp
    @Suppress("UnusedPrivateMember")
    private val aktualisiert: LocalDateTime = now(ZoneId.of(TIMEZONE_BERLIN)),
) {
    /**
     * Vergleich mit einem anderen Objekt oder null.
     * https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     * https://vladmihalcea.com/the-best-way-to-implement-equals-hashcode-and-tostring-with-jpa-and-hibernate
     * https://thorben-janssen.com/ultimate-guide-to-implementing-equals-and-hashcode-with-hibernate
     *
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Arzt-) Objekt die gleiche ID hat.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arzt
        return id != null && id == other.id
    }

    /**
     * Hashwert aufgrund der ID.
     * @return Der Hashwert.
     */
    override fun hashCode() = id?.hashCode() ?: this::class.hashCode()

    /**
     * Properties überschreiben, z.B. bei PUT-Requests von der REST-Schnittstelle
     * @param neu Ein transientes Arzt-Objekt mit den neuen Werten für die Properties
     */
    @Suppress("DataClassContainsFunctions")
    fun set(neu: Arzt) {
        nachname = neu.nachname
        email = neu.email
        kategorie = neu.kategorie
        newsletter = neu.newsletter
        geburtsdatum = neu.geburtsdatum
        homepage = neu.homepage
        geschlecht = neu.geschlecht
        familienstand = neu.familienstand
    }

    /**
     * Konstante für Named Queries
     */
    companion object {
        private const val PREFIX = "Arzt."

        /**
         * Name für die Named Query, mit der alle Aerzte gesucht werden
         */
        const val ALL = "${PREFIX}all"

        /**
         * Name für die Named Query, mit der Aerzte anhand der Emailadresse gesucht werden
         */
        const val BY_EMAIL = "${PREFIX}byEmail"

        /**
         * Name für die Named Query, mit der ermittelt wird, ob es zu einer Emailadresse bereits einen Arzt gibt
         */
        const val EMAIL_EXISTS = "${PREFIX}emailExists"

        /**
         * Name für die Named Query, mit der Aerzte anhand eines Teilstrings für den Nachnamen gesucht werden
         */
        const val BY_NACHNAME = "${PREFIX}byNachname"

        /**
         * Name für die Named Query, mit der die Nachnamen zu einem Teilstring gesucht werden
         */
        const val NACHNAMEN_PREFIX = "${PREFIX}nachnamenPrefix"

        /**
         * Parametername für die Emailadresse
         */
        const val PARAM_EMAIL = "email"

        /**
         * Parametername für den Nachnamen
         */
        const val PARAM_NACHNAME = "nachname"
    }
}

/**
 * Datentyp für die IDs von Arzt-Objekten
 */
typealias ArztId = UUID
// typealias ArztId = Long

/**
 * Datentyp für sonstige IDs in DB-Tabellen
 */
typealias DbId = UUID
// typealias DbId = Long
