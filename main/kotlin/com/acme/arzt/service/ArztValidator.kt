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
package com.acme.arzt.service

import am.ik.yavi.builder.validator
import am.ik.yavi.core.ViolationMessage
import com.acme.arzt.entity.Arzt
import org.springframework.stereotype.Service

/**
 * Validierung von Objekten der Klasse [Arzt].
 */

@Service
class ArztValidator(adresseValidator: AdresseValidator) {
    private val validator = validator<Arzt> {
        Arzt::nachname {
            notEmpty().message(
                ViolationMessage.of(
                    "arzt.nachname.notEmpty",
                    "Lastname is required.",
                ),
            )
                .pattern(NACHNAME_PATTERN).message(
                    ViolationMessage.of(
                        "arzt.nachname.pattern",
                        "After a capital letter at least one lowercase letter is required. Compound names are allowed.",
                    ),
                )
        }
        Arzt::email {
            notEmpty().message(
                ViolationMessage.of(
                    "arzt.email.notEmpty",
                    "Email is required.",
                ),
            )
                .email().message(
                    ViolationMessage.of(
                        "arzt.email.pattern",
                        "The email address is invalid.",
                    ),
                )
        }
        Arzt::kategorie {
            greaterThanOrEqual(MIN_KATEGORIE).message(
                ViolationMessage.of(
                    "arzt.kategorie.min",
                    "The category value must be at least {1}.",
                ),
            )
                .lessThanOrEqual(MAX_KATEGORIE).message(
                    ViolationMessage.of(
                        "arzt.kategorie.max",
                        "The category value must not exceed {1}.",
                    ),
                )
        }
        Arzt::interessen {
            unique().message(
                ViolationMessage.of(
                    "arzt.interessen.unique",
                    "The interests mus be unique.",
                ),
            )
        }
        Arzt::adresse.nest(adresseValidator.validator)
    }

    /**
     * Validierung eines Entity-Objekts der Klasse [Arzt]
     *
     * @param arzt Das zu validierende Arzt-Objekt
     * @return Eine Liste mit den Verletzungen der Constraints oder eine leere Liste
     */
    fun validate(arzt: Arzt) = validator.validate(arzt)

    /**
     * Konstante für Validierung
     */
    companion object {
        private const val NACHNAME_PREFIX = "o'|von|von der|von und zu|van"
        private const val NAME_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+"

        /**
         * Muster für einen gültigen Nachnamen.
         */
        const val NACHNAME_PATTERN = "($NACHNAME_PREFIX)?$NAME_PATTERN(-$NAME_PATTERN)?"

        /**
         * Kleinster Wert für eine Kategorie.
         */
        const val MIN_KATEGORIE = 0

        /**
         * Maximaler Wert für eine Kategorie.
         */
        const val MAX_KATEGORIE = 9

        /**
         * Mitteleuropäische Zeitzone
         */
        const val TIMEZONE_BERLIN = "Europe/Berlin"
    }
}
