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

/**
 * Allgemeiner Response-Body für Schreiboperationen.
 */
sealed interface GenericBody {
    /**
     * Body mit einem textuellen Wert.
     * @property text Der Text
     */
    data class Text(val text: String) : GenericBody

    /**
     * Body, wenn es mehrere Werte, wie z.B. Constraint-Verletzungen, gibt.
     * @property values Die verletzten Constraints
     */
    data class Values(val values: Map<String, String>) : GenericBody
}
