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

import com.acme.arzt.config.DbSystemType.MYSQL
import com.acme.arzt.config.DbSystemType.ORACLE
import com.acme.arzt.config.DbSystemType.POSTGRES
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.bind.DefaultValue

/**
 * Eingelesene Properties mit dem Praefix `db`
 *
 * @property system Das DB-System: `POSTGRES`, `MYSQL`, `ORACLE`
 * @property user Der Username für den DB-User
 * @property password Das Passwort für den DB-User
 * @property dbHost Rechnername des DB-Servers
 * @property dbname Der DB-Name
 * @property url URL für den DB-Zugriff
 * @property sqlDialect SQL-Dialekt für Hibernate
 */
@ConfigurationProperties(prefix = "app.db")
@ConstructorBinding
data class DbProps(
    @DefaultValue("POSTGRES")
    val system: DbSystemType,

    @DefaultValue("arzt")
    val user: String,

    @DefaultValue("p")
    val password: String,

    private val dbHost: String = if (osName.startsWith("Windows")) {
        "localhost"
    } else {
        system.name.lowercase()
    },

    @DefaultValue("arzt")
    private val dbname: String,

    // https://jdbc.postgresql.org/documentation/head/connect.html
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-jdbc-url-format.html
    // https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html
    // Default-Port
    //      PostgreSQL: 5432
    //      MySQL: 3306
    //      Oracle: 1521
    val url: String =
        when (system) {
            POSTGRES -> "postgresql://$dbHost/$dbname"
            MYSQL -> "mysql://$dbHost/$dbname"
            // io.vertx:vertx-oracle-client nutzt ojdbc11
            ORACLE -> "oracle:thin:@$dbHost/XEPDB1"
        },

    val sqlDialect: String = system.sqlDialect,
) {
    private companion object {
        val osName = System.getProperty("os.name") ?: error("Die Property 'os.name' existiert nicht")
    }
}
