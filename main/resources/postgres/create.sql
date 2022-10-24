-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec postgres bash
-- psql --dbname=arzt --username=arzt --file=/scripts/create.sql

-- https://www.postgresql.org/docs/devel/app-psql.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html
-- https://www.postgresql.org/docs/current/ddl-schemas.html#DDL-SCHEMAS-CREATE
-- "user-private schema" (Default-Schema: public)
CREATE SCHEMA IF NOT EXISTS AUTHORIZATION arzt;

ALTER ROLE arzt SET search_path = 'arzt';

CREATE TABLE IF NOT EXISTS umsatz (
    id        uuid PRIMARY KEY USING INDEX TABLESPACE arztspace,
              -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-NUMERIC-DECIMAL
              -- https://www.postgresql.org/docs/current/datatype-money.html
              -- 10 Stellen, davon 2 Nachkommastellen
    betrag    decimal(10,2) NOT NULL,
    waehrung  char(3) NOT NULL CHECK (waehrung ~ '[A-Z]{3}')
);

CREATE TABLE IF NOT EXISTS adresse (
    id    uuid PRIMARY KEY USING INDEX TABLESPACE arztspace,
    plz   char(5) NOT NULL CHECK (plz ~ '\d{5}'),
    ort   varchar(40) NOT NULL
) TABLESPACE arztspace;

-- default: btree
CREATE INDEX IF NOT EXISTS adresse_plz_idx ON adresse(plz) TABLESPACE arztspace;

CREATE TABLE IF NOT EXISTS arzt (
    id            uuid PRIMARY KEY USING INDEX TABLESPACE arztspace,
                  -- https://www.postgresql.org/docs/current/datatype-numeric.html#DATATYPE-INT
    version       integer NOT NULL DEFAULT 0,
    nachname      varchar(40) NOT NULL,
                  -- impliziter Index als B-Baum durch UNIQUE
                  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-UNIQUE-CONSTRAINTS
    email         varchar(40) NOT NULL UNIQUE USING INDEX TABLESPACE arztspace,
                  -- https://www.postgresql.org/docs/current/ddl-constraints.html#DDL-CONSTRAINTS-CHECK-CONSTRAINTS
    kategorie     integer NOT NULL CHECK (kategorie >= 0 AND kategorie <= 9),
                  -- https://www.postgresql.org/docs/current/datatype-boolean.html
    newsletter    boolean NOT NULL DEFAULT FALSE,
                  -- https://www.postgresql.org/docs/current/datatype-datetime.html
    geburtsdatum  date CHECK (geburtsdatum < current_date),
    homepage      varchar(40),
    geschlecht    char(1) CHECK (geschlecht ~ 'M|W|D'),
    familienstand varchar(2) CHECK (familienstand ~ 'L|VH|G|VW'),
    umsatz_id     uuid REFERENCES umsatz,
    adresse_id    uuid NOT NULL REFERENCES adresse,
                  -- https://www.postgresql.org/docs/current/datatype-datetime.html
    erzeugt       timestamp NOT NULL,
    aktualisiert  timestamp NOT NULL
) TABLESPACE arztspace;

CREATE INDEX IF NOT EXISTS arzt_nachname_idx ON arzt(nachname) TABLESPACE arztspace;

CREATE TABLE IF NOT EXISTS arzt_interessen (
    arzt_id  uuid NOT NULL REFERENCES arzt,
    interesse char(1) NOT NULL CHECK (interesse ~ 'S|L|R'),

    PRIMARY KEY (arzt_id, interesse) USING INDEX TABLESPACE arztspace
) TABLESPACE arztspace;

CREATE INDEX IF NOT EXISTS arzt_interessen_arzt_idx ON arzt_interessen(arzt_id) TABLESPACE arztspace;
