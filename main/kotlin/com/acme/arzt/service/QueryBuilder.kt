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
package com.acme.arzt.service

import com.acme.arzt.entity.Adresse
import com.acme.arzt.entity.Arzt
import com.acme.arzt.entity.FamilienstandType
import com.acme.arzt.entity.GeschlechtType
import com.acme.arzt.entity.InteresseType
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root
import javax.persistence.criteria.createQuery
import javax.persistence.criteria.from
import javax.persistence.criteria.get

// https://howtodoinjava.com/hibernate/hibernate-criteria-queries-tutorial
// https://www.baeldung.com/hibernate-criteria-queries
// https://lifeinide.com/post/2021-04-29-making-jpa-criteria-api-less-awkward-with-kotlin/#typesafe-entity-fields-access

// BEACHTE: Metamodel-Klassen von Hibernate benötigen einen Annotation-Processor, wie z.B. apt (Java) oder kapt (Kotlin)
// ABER: kapt für Kotlin ist deprecated und unterstuetzt *nicht* das neue Compiler-IR-Backend von Kotlin

/**
 * Singleton-Klasse, um _Criteria Queries_ für _Hibernate_ zu bauen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
 */
@Suppress("TooManyFunctions")
@Service
class QueryBuilder(private val factory: SessionFactory) {
    private val logger: Logger = LoggerFactory.getLogger(QueryBuilder::class.java)

    /**
     * Aus einer `MultiValueMap` von _Spring_ wird eine Criteria Query für Hibernate gebaut, um flexibel nach Aerzte
     * suchen zu können.
     * @param queryParams Die Query-Parameter in einer `MultiValueMap`.
     * @return [QueryBuilderResult] abhängig von den Query-Parametern.
     */
    fun build(queryParams: MultiValueMap<String, String>): QueryBuilderResult {
        logger.debug("build: queryParams={}", queryParams)

        val criteriaBuilder = factory.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery<Arzt>() // session.createQuery(Arzt::class.java)
        val arztRoot = criteriaQuery.from(Arzt::class) // criteriaQuery.from(Arzt::class.java)

        if (queryParams.isEmpty()) {
            // keine Suchkriterien
            return QueryBuilderResult.Success(criteriaQuery)
        }

        val predicates = queryParams.map { (paramName, paramValues) ->
            getPredicate(paramName, paramValues, criteriaBuilder, arztRoot)
        }.filterNotNull()

        if (predicates.isEmpty()) {
            return QueryBuilderResult.Failure
        }
        logger.debug("build: #predicates={}", predicates.size)

        @Suppress("SpreadOperator")
        val predicate = criteriaBuilder.and(*predicates.toTypedArray()) // variable Argumentleiste
        criteriaQuery.where(predicate)

        return QueryBuilderResult.Success(criteriaQuery)
    }

    private fun getPredicate(
        paramName: String,
        paramValues: Collection<String>?,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ): Predicate? {
        // zu "interesse" kann es mehrere Werte geben
        // https://tools.ietf.org/html/rfc3986#section-3.4
        if (paramName == interesse) {
            return getPredicateInteressen(paramValues, criteriaBuilder, arztRoot)
        }

        if (paramValues?.size != 1) {
            return null
        }

        logger.debug("getPredicate: propertyValues={}", paramValues)

        val value = paramValues.first()
        return when (paramName) {
            nachname -> getPredicateNachname(value, criteriaBuilder, arztRoot)
            email -> getPredicateEmail(value, criteriaBuilder, arztRoot)
            kategorie -> getPredicateKategorie(value, criteriaBuilder, arztRoot)
            plz -> getPredicatePlz(value, criteriaBuilder, arztRoot)
            ort -> getPredicateOrt(value, criteriaBuilder, arztRoot)
            umsatzMin -> getPredicateUmsatz(value, criteriaBuilder, arztRoot)
            geschlecht -> getPredicateGeschlecht(value, criteriaBuilder, arztRoot)
            familienstand -> getPredicateFamilienstand(value, criteriaBuilder, arztRoot)
            else -> null
        }
    }

    // Nachname: Suche nach Teilstrings
    private fun getPredicateNachname(nachname: String, criteriaBuilder: CriteriaBuilder, arztRoot: Root<Arzt>) =
        criteriaBuilder.like(arztRoot.get(Arzt::nachname), "%$nachname%")

    // Email: Suche mit Teilstring ohne Gross-/Kleinschreibung
    private fun getPredicateEmail(email: String, criteriaBuilder: CriteriaBuilder, arztRoot: Root<Arzt>) =
        criteriaBuilder.like(arztRoot.get(Arzt::email), "%$email%")

    private fun getPredicateKategorie(
        kategorieStr: String,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ): Predicate? {
        val kategorieVal = kategorieStr.toIntOrNull() ?: return null
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        return criteriaBuilder.equal(arztRoot.get(Arzt::kategorie), kategorieVal)
    }

    // PLZ: Suche mit Praefix
    private fun getPredicatePlz(plz: String, criteriaBuilder: CriteriaBuilder, arztRoot: Root<Arzt>) =
        criteriaBuilder.like(arztRoot.get(Arzt::adresse).get(Adresse::plz), "$plz%")

    // Ort: Suche mit Praefix
    private fun getPredicateOrt(ort: String, criteriaBuilder: CriteriaBuilder, arztRoot: Root<Arzt>) =
        criteriaBuilder.like(arztRoot.get(Arzt::adresse).get(Adresse::ort), "$ort%")

    private fun getPredicateUmsatz(
        umsatz: String,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ): Predicate? {
        val umsatzVal = umsatz.toBigDecimalOrNull() ?: return null
        return criteriaBuilder.equal(arztRoot.get(Arzt::kategorie), umsatzVal)
    }

    private fun getPredicateGeschlecht(
        geschlechtValue: String,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ) =
        criteriaBuilder.equal(
            arztRoot.get(Arzt::geschlecht),
            GeschlechtType.fromValue(geschlechtValue),
        )

    private fun getPredicateFamilienstand(
        familienstandValue: String,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ) = criteriaBuilder.equal(
        arztRoot.get(Arzt::familienstand),
        FamilienstandType.fromValue(familienstandValue),
    )

    private fun getPredicateInteressen(
        interessen: Collection<String>?,
        criteriaBuilder: CriteriaBuilder,
        arztRoot: Root<Arzt>,
    ): Predicate? {
        logger.debug("getPredicateInteressen: interessen={}", interessen)

        if (interessen == null || interessen.isEmpty()) {
            return null
        }

        val predicates = interessen.map { interesse ->
            InteresseType.fromValue(interesse) ?: return null
        }.map { interesseOrdinal ->
            criteriaBuilder.isMember(
                interesseOrdinal,
                arztRoot.get(Arzt::interessen),
            )
        }
        @Suppress("SpreadOperator")
        return criteriaBuilder.and(*predicates.toTypedArray())
    }

    private companion object {
        private const val nachname = "nachname"
        private const val email = "email"
        private const val kategorie = "kategorie"
        private const val plz = "plz"
        private const val ort = "ort"
        private const val umsatzMin = "umsatzmin"
        private const val geschlecht = "geschlecht"
        private const val familienstand = "familienstand"
        private const val interesse = "interesse"
    }
}

/**
 * Resultat-Typ für [QueryBuilder.build]
 */
sealed interface QueryBuilderResult {
    /**
     * Resultat-Typ, wenn die Query-Parameter korrekt sind.
     * @property criteriaQuery Die CriteriaQuery
     */
    data class Success(val criteriaQuery: CriteriaQuery<Arzt>) : QueryBuilderResult

    /**
     * Resultat-Typ, wenn mindestens 1 Query-Parameter falsch ist.
     */
    object Failure : QueryBuilderResult
}
