/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package com.acme.arzt.config

import org.hibernate.dialect.MySQL8Dialect
import org.hibernate.dialect.Oracle12cDialect
import org.hibernate.dialect.PostgreSQL10Dialect

/**
 * Enum-Typ mit den unterstützten DB-Systemen
 * @property sqlDialect Der SQL-Dialekt für Hibernate
 */
enum class DbSystemType(val sqlDialect: String) {
    /**
     * DB-System PostgreSQL in application.yml
     */
    POSTGRES(PostgreSQL10Dialect::class.java.name),

    /**
     * DB-System MySQL in application.yml
     */
    MYSQL(MySQL8Dialect::class.java.name),

    /**
     * DB-System Oracle in application.yml
     */
    ORACLE(Oracle12cDialect::class.java.name);
}
