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

--  docker compose exec postgres bash
--  psql --dbname=arzt --username=arzt --file=/scripts/insert.sql

INSERT INTO umsatz (id, betrag, waehrung) VALUES ('10000000-0000-0000-0000-000000000000',0,'EUR');
INSERT INTO umsatz (id, betrag, waehrung) VALUES ('10000000-0000-0000-0000-000000000001',10,'EUR');
INSERT INTO umsatz (id, betrag, waehrung) VALUES ('10000000-0000-0000-0000-000000000002',20,'USD');
INSERT INTO umsatz (id, betrag, waehrung) VALUES ('10000000-0000-0000-0000-000000000030',30,'CHF');
INSERT INTO umsatz (id, betrag, waehrung) VALUES ('10000000-0000-0000-0000-000000000040',40,'GBP');

INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000000','00000','Aachen');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000001','11111','Augsburg');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000002','22222','Aalen');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000030','33333','Ahlen');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000040','44444','Dortmund');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000050','55555','Essen');
INSERT INTO adresse (id, plz, ort) VALUES ('20000000-0000-0000-0000-000000000060','66666','Freiburg');

-- admin
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000000',0,'Admin','admin@acme.com',0,true,'2021-01-31','https://www.acme.com','W','VH','10000000-0000-0000-0000-000000000000','20000000-0000-0000-0000-000000000000','2021-01-31 00:00:00','2021-01-31 00:00:00');
-- HTTP GET
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000001',0,'Alpha','alpha@acme.de',1,true,'2021-01-01','https://www.acme.de','M','L','10000000-0000-0000-0000-000000000001','20000000-0000-0000-0000-000000000001','2021-01-01 00:00:00','2021-01-01 00:00:00');
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000002',0,'Alpha','alpha@acme.edu',2,true,'2021-01-02','https://www.acme.edu','W','G','10000000-0000-0000-0000-000000000002','20000000-0000-0000-0000-000000000002','2021-01-02 00:00:00','2021-01-02 00:00:00');
-- HTTP PUT
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000030',0,'Alpha','alpha@acme.ch',3,true,'2021-01-03','https://www.acme.ch','M','VW','10000000-0000-0000-0000-000000000030','20000000-0000-0000-0000-000000000030','2021-01-03 00:00:00','2021-01-03 00:00:00');
-- HTTP PATCH
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000040',0,'Delta','delta@acme.uk',4,true,'2021-01-04','https://www.acme.uk','W','VH','10000000-0000-0000-0000-000000000040','20000000-0000-0000-0000-000000000040','2021-01-04 00:00:00','2021-01-04 00:00:00');
-- HTTP DELETE
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000050',0,'Epsilon','epsilon@acme.jp',5,true,'2021-01-05','https://www.acme.jp','M','L',null,'20000000-0000-0000-0000-000000000050','2021-01-05 00:00:00','2021-01-05 00:00:00');
-- zur freien Verfuegung
INSERT INTO arzt (id, version, nachname, email, kategorie, newsletter, geburtsdatum, homepage, geschlecht, familienstand, umsatz_id, adresse_id, erzeugt, aktualisiert) VALUES ('00000000-0000-0000-0000-000000000060',0,'Phi','phi@acme.cn',6,true,'2021-01-06','https://www.acme.cn','D','L',null,'20000000-0000-0000-0000-000000000060','2021-01-06 00:00:00','2021-01-06 00:00:00');

INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000000','L');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000001','S');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000001','L');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000030','S');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000030','R');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000040','L');
INSERT INTO arzt_interessen (arzt_id, interesse) VALUES ('00000000-0000-0000-0000-000000000040','R');
