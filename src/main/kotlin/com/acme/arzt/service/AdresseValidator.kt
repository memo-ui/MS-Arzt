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
import com.acme.arzt.entity.Adresse
import org.springframework.stereotype.Service

/**
 * Validierung von Objekten der Klasse [Adresse].
 */
@Service
@Suppress("UseDataClass")
class AdresseValidator {
    /**
     * Ein Validierungsobjekt für die Validierung von Adresse-Objekten
     */
    val validator = validator<Adresse> {
        Adresse::plz {
            notEmpty().message(
                ViolationMessage.of("adresse.plz.notEmpty", "ZIP code is required."),
            )
                .pattern(PLZ_PATTERN).message(
                    ViolationMessage.of("adresse.plz.pattern", "ZIP code does not consist of 5 digits."),
                )
        }

        Adresse::ort {
            notEmpty().message(
                ViolationMessage.of("adresse.ort.notEmpty", "Location is required."),
            )
        }
    }

    /**
     * Konstante für die Validierung einer Adresse.
     */
    companion object {
        /**
         * Konstante für den regulären Ausdruck einer Postleitzahl als 5-stellige Zahl mit führender Null.
         */
        const val PLZ_PATTERN = "\\d{5}"
    }
}
