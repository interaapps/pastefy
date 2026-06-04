# Backend Rewrite Roadmap

Ziel: `backend-kt` ersetzt `backend` ohne Verhaltensänderungen für bestehende Clients. Neben Feature-Parity ist 1:1 API-Parity erforderlich: HTTP-Methode, Pfad, Query-Parameter, Header, Statuscode, Content-Type, JSON-Feldname, Null-Verhalten, Defaultwert und Response-Struktur müssen kompatibel bleiben. Request- und Response-JSON verwendet verbindlich `snake_case`; `/v3/api-docs` muss denselben Vertrag dokumentieren.

## P0: API-Vertrag stabilisieren

- [ ] Für alle API-Responses dedizierte DTOs verwenden. Keine JPA-Entities direkt serialisieren, insbesondere nicht bei Admin-Usern, Notifications und Tag-Listings.
- [ ] `ExceptionResponse` exakt kompatibel machen: Das alte Backend liefert `success`, `exception`, `error` und `exists`, aber kein zusätzliches `message`-Feld.
- [ ] Validierungsfehler auf das Legacy-Fehlerformat abbilden. Prüfen, welcher Exception-Name und welcher HTTP-Status bei ungültigen Requests bisher zurückgegeben wurde.
- [ ] `CreatePasteResponse.success` standardmäßig wieder `false` setzen und nur nach erfolgreichem Speichern auf `true`.
- [ ] `PasteResponse.exists` standardmäßig wieder `false` setzen und nur für vorhandene Pastes auf `true`.
- [ ] Zeitwerte exakt kompatibel serialisieren. Das alte Backend verwendet bei vielen DTOs `Timestamp.toString()` statt ISO-8601.
- [ ] Nullable-Felder und ausgelassene Felder gegen das alte Gson-Verhalten prüfen. Jackson darf keine zusätzlichen `null`-Felder oder leeren Collections ausliefern, wenn sie vorher fehlten.
- [x] OAuth-Routen auf den Legacy-Pfad zurückführen: `/api/v2/auth/oauth2/{provider}` und `/api/v2/auth/oauth2/{provider}/callback`. Der aktuelle Kotlin-Pfad `/auth/oauth2/...` ist nicht API-kompatibel.
- [ ] `Server: InteraApps-Pastefy` wieder für Responses setzen.
- [x] Pagination-Response-Header `PAGINATION_LIMIT` und `PAGINATION_PAGE` wieder ausliefern, wo das alte Backend sie gesetzt hat.
- [ ] Rate-Limiter-Defaults korrigieren: Der allgemeine Legacy-Rate-Limiter war nur aktiv, wenn `ratelimiter.millis` konfiguriert war. Der Thumbnail-Limiter war dagegen immer aktiv.

## P0: Listenabfragen und Response-Mapping

- [x] Einen gemeinsamen Mapper `Paste -> PasteResponse` implementieren, inklusive `raw_url`, Tags, User, Starred-Status, optionaler AI-Analyse und Inhaltskürzung.
- [ ] `shorten_content=true` exakt nachbauen: Inhalte über 303 Zeichen auf 300 Zeichen plus `...` kürzen.
- [ ] `with_ai_analysis=true` exakt nachbauen und `PasteAiInfoResponse` nur dann einbetten.
- [ ] Einen gemeinsamen Listen-Query-Service für SQL und Elasticsearch implementieren.
- [ ] Legacy-Query-Parameter unterstützen: `page`, `page_limit`, `search`, `sort`, `filter`, `filters`, `filter_tags`, `shorten_content`.
- [ ] Das konfigurierte Maximum `PASTEFY_PAGINATION_PAGE_LIMIT` nachbauen.
- [ ] Legacy-Sichtbarkeitsregeln nachbauen: Nicht-Admins sehen öffentliche Pastes sowie eigene und selbst markierte Pastes; Admins dürfen ungefiltert lesen.
- [ ] SQL- und Elasticsearch-Ergebnisse auf identische Reihenfolge, Filterung und DTOs bringen.
- [ ] Elasticsearch-Volltextsuche mit Legacy-Gewichtung nachbauen: `title^3`, `content`, `user.name`, `user.uniqueName`.
- [ ] Elasticsearch-Filter für Tags, Sichtbarkeit, Verschlüsselung, User, Stars, Folder und Zeitwerte nachbauen.
- [ ] SQL-Fallback für `starredBy`, Tagfilter und Sortierung nach `engagementScore` nachbauen.
- [ ] Prüfen, ob `PASTEFY_LIST_PASTES` weiterhin benötigt wird, und das Legacy-Verhalten für anonyme Listenabfragen übernehmen.

## Paste-API

### `POST /api/v2/paste`

- [x] Handler implementieren.
- [x] Folder nur übernehmen, wenn er dem eingeloggten User gehört.
- [x] `title`, `content`, `encrypted`, `type`, `visibility`, `folder`, `expireAt`, `forkedFrom` und `tags` Legacy-kompatibel speichern.
- [x] `expireAt` wie bisher nur ab mindestens 16 Zeichen parsen.
- [x] Bei Forks eines öffentlichen Pastes asynchron `+10` Engagement auf dem Ursprungspaste buchen.
- [x] Tags speichern und Tag-Listing-Zähler aktualisieren.
- [ ] `ai=true` nachbauen: Für nicht verschlüsselte Pastes asynchron AI-Tags, Dateiname und Dateiendung ergänzen.
- [x] Response exakt als `CreatePasteResponse` mit eingebettetem `PasteResponse` ausliefern.

### `GET /api/v2/paste`

- [x] Handler über den gemeinsamen Listen-Query-Service implementieren.
- [ ] Permission `pastes:read`, Sichtbarkeitsregeln, Pagination, Filter, Suche und Elasticsearch-Fallback übernehmen.

### `GET /api/v2/paste/{id}`

- [x] Handler implementieren.
- [x] Private Pastes nur dem Owner zugänglich machen.
- [x] Öffentliche Fremdaufrufe asynchron bewerten: `+5` für anonyme Frontend-Aufrufe, `+4` für eingeloggte Frontend-Aufrufe, sonst `+2`.
- [x] Bei `from_frontend=true` PAGE-Analytics tracken.
- [x] Query-Parameter `shorten_content` und `with_ai_analysis` berücksichtigen.
- [x] Starred-Status und Public-User wie im alten DTO einbetten.

### `PUT /api/v2/paste/{id}`

- [x] Handler implementieren.
- [x] Nur Owner oder Admin zulassen.
- [x] Teilupdates für Titel, Inhalt, Folder, Typ, Verschlüsselung, Sichtbarkeit und Ablaufzeit übernehmen.
- [x] Tag-Diff speichern, entfernte Tags löschen und Tag-Listing-Zähler aktualisieren.
- [x] Cache invalidieren, S3-Speicherung und Elasticsearch-Reindexing auslösen.

### `DELETE /api/v2/paste/{id}`

- [x] Handler implementieren.
- [x] Nur Owner oder Admin zulassen.
- [x] Kommentare, Tags, Stars, Engagement, AI-Info und Hintergrundjobs löschen.
- [x] S3-Objekt, Redis-Cache und Elasticsearch-Dokument entfernen.

### Stars, Freunde und AI

- [x] `POST /api/v2/paste/{id}/star` implementieren.
- [x] `DELETE /api/v2/paste/{id}/star` implementieren.
- [x] Elasticsearch-Star-Felder nach Änderungen aktualisieren.
- [x] `POST /api/v2/paste/{id}/friend` bewusst Legacy-kompatibel behandeln. Das alte Backend wirft hier `RuntimeException("NOT IMPLEMENTED")`; entscheiden, ob exakt beibehalten oder versioniert entfernt wird.
- [x] `POST /api/v2/paste/{id}/ai-analysis` implementieren: Admin, Permission, Feature-Flag, Accessible-Paste-Prüfung und erzwungenes Enqueue.

## Raw-, Multipart- und Thumbnail-Endpunkte

### `GET /{id}/raw`

- [x] Handler implementieren.
- [x] Content-Type exakt `text/plain; charset=utf-8` setzen.
- [x] Fehlende Pastes legacy-kompatibel als HTTP 404 mit Text `404 - Paste not found` beantworten.
- [x] Private-Paste-Prüfung übernehmen.
- [x] Öffentliche Raw-Aufrufe asynchron mit `+1` Engagement bewerten.
- [x] RAW-Analytics tracken.
- [x] `part=<name>` für `MULTI_PASTE` unterstützen und den passenden Dateiinhalt zurückgeben.
- [x] DATABASE-, S3- und Redis-Inhalte korrekt ausgeben.

### `POST /` und `POST /api/asciicasts`

- [x] Multipart-Handler implementieren.
- [x] Alle Parts lesen; ein File wird `PASTE`, mehrere Files werden als JSON-Liste `MULTI_PASTE`.
- [x] Owner übernehmen, wenn eingeloggt.
- [x] Für `/api/asciicasts` Titel `<partname>.cast`, Tags `asciicast` und `asciinema` setzen.
- [x] Legacy-Textantworten exakt übernehmen, einschließlich abschließendem Newline.
- [x] Für `/api/asciicasts` die historische JSON-Struktur mit `url` und `message` sowie JSON-Content-Type liefern.
- [x] Fehlende Dateien mit `No file given. curl -F 'f=@filename' pastefy.app\n` beantworten.

### `GET /{id}/thumbnail.png`

- [x] Thumbnail-Handler implementieren.
- [x] SVG-Template und Fonts in `backend-kt/src/main/resources` übernehmen.
- [x] Nur nicht-private, nicht-verschlüsselte Pastes rendern.
- [x] Titel und maximal fünf Inhaltszeilen XML-sicher einsetzen.
- [x] PNG mit Batik erzeugen und streamen.
- [x] Image-Rate-Limit exakt `3` Requests pro `5000 ms` beibehalten.

## Kommentare

- [x] `GET /api/v2/paste/{pasteId}/comments` implementieren.
- [x] `page`, `page_limit` und optional `line` übernehmen; Defaults `1`, `10`, Maximum `30`.
- [x] Nur Top-Level-Kommentare laden und direkte Replies einbetten.
- [x] `GET /api/v2/paste/{pasteId}/comments/markers` implementieren.
- [x] Marker nach Zeile gruppieren, maximal zwei eindeutige Profile einbetten und `additionalProfiles` korrekt berechnen.
- [x] `POST /api/v2/paste/{pasteId}/comments` implementieren.
- [x] Content trimmen und auf maximal `2000` Zeichen begrenzen.
- [x] Parent nur akzeptieren, wenn er zum selben Paste gehört.
- [x] `lineFrom` und `lineTo` validieren; keine negativen Werte, kein inverser Bereich, maximale Spannweite `1000`.
- [x] `DELETE /api/v2/paste/{pasteId}/comments/{commentId}` implementieren.
- [x] Autor, Paste-Owner oder Admin löschen lassen und Replies wie bisher behandeln.

## Folder-API

- [x] `POST /api/v2/folder` implementieren.
- [x] Parent nur setzen, wenn er existiert und demselben User gehört.
- [x] `GET /api/v2/folder` implementieren: User-Sichtbarkeit, `page`, `page_limit` und `search`.
- [ ] Legacy-`filter` für `GET /api/v2/folder` übernehmen.
- [x] `GET /api/v2/folder/{id}` implementieren.
- [x] `hide_children` exakt übernehmen und rekursive Folder-/Paste-Strukturen kompatibel serialisieren.
- [x] Private Pastes im Folder nur für den Owner einbetten.
- [x] `DELETE /api/v2/folder/{id}` implementieren.
- [x] Nur Owner oder Admin löschen lassen.
- [x] Rekursives Löschen von Unterordnern und enthaltenen Pastes nachbauen.

## User-, Admin- und Notification-API

### User

- [x] `GET /api/v2/user` implementieren, inklusive anonymem `loggedIn=false`-Response.
- [x] `UserResponse` exakt mappen: `id`, `name`, `displayName`, feste Legacy-Farbe `#f52966`, `profilePicture`, `authType`, `authTypes`, `type`.
- [x] `GET /api/v2/user/overview` implementieren: Root-Pastes seitenweise, Root-Folder mit optionalen Kindern.
- [x] `GET /api/v2/user/folders` implementieren und `hide_children`, `hide_sub_children`, `hide_pastes` übernehmen.
- [x] `GET /api/v2/user/pastes` über den gemeinsamen Listen-Query-Service implementieren.
- [x] `GET /api/v2/user/sharedpastes` implementieren. Der Endpunkt ist deprecated, gehört aber zur API-Parity.
- [x] Verwaiste Shared-Paste-Einträge beim Lesen wie bisher löschen.
- [x] `GET /api/v2/user/starred-pastes` implementieren.

### API Keys

- [x] `POST /api/v2/user/keys` implementieren und nur `API`-Keys erzeugen.
- [x] `GET /api/v2/user/keys` implementieren und nur Key-Strings für den aktuellen User liefern.
- [x] `DELETE /api/v2/user/keys/{key}` implementieren und nur eigene Keys entfernen.

### Notifications

- [x] `POST /api/v2/user/notification` Legacy-kompatibel implementieren. Das alte Backend liefert aktuell nur `success=false`; keine neue Semantik unbemerkt einführen.
- [x] `GET /api/v2/user/notification` implementieren.
- [x] Query-Flags `not_received` und `not_read` übernehmen.
- [x] Gelesene Notifications nach dem Abruf als `received=true` markieren.
- [x] `GET /api/v2/user/notification/readall` implementieren und `received=true`, `already_read=true` setzen.
- [ ] Notification-Response über DTO stabilisieren und Entity-Felder gegen den Legacy-Payload prüfen.

### Admin

- [x] `GET /api/v2/admin/users` implementieren: Pagination, Suche und Sortierung nach `created_at`.
- [ ] Legacy-`filter` für `GET /api/v2/admin/users` übernehmen.
- [x] `GET /api/v2/admin/users/{id}` implementieren.
- [ ] Legacy-Null-/Fehlerverhalten für `GET /api/v2/admin/users/{id}` prüfen.
- [x] `DELETE /api/v2/admin/users/{id}` implementieren, inklusive abhängiger Daten.
- [x] `PUT /api/v2/admin/users/{id}` implementieren: partielle Updates für `name`, `uniqueName`, `type`.
- [ ] Admin-User-Response über ein kompatibles DTO absichern, damit keine zusätzlichen Entity-Felder erscheinen.

## App-, Stats-, Public- und SEO-Endpunkte

### App und Stats

- [x] `GET /api/v2/app/info` implementieren.
- [x] Konfiguration für `customLogo`, `customName`, `customFooter`, `encryptionIsDefault`, Login-Flags und Public-Pastes-Flag ergänzen.
- [x] `aiEnabled` und `analyticsEnabled` anhand tatsächlich verfügbarer Services liefern.
- [x] `GET /api/v2/app/stats` implementieren.
- [x] `PASTEFY_PUBLIC_STATS` nachbauen; andernfalls nur Admin oder Permission `stats:read`.
- [x] Stats zählen: Pastes, eingeloggte Pastes, User, Tags, Folder, S3-Pastes und Elasticsearch-Dokumente.
- [x] Snake-Case-Feldnamen ausliefern, insbesondere `s3_pastes`.

### Public

- [x] `GET /api/v2/public-pastes` implementieren: nur `PUBLIC`.
- [x] `GET /api/v2/public-pastes/trending` implementieren: nur öffentliche, unverschlüsselte Pastes, Sortierung nach Engagement.
- [x] Bei Query-Flag `trending` nur Pastes der letzten vier Tage berücksichtigen.
- [x] `GET /api/v2/public-pastes/latest` implementieren: nur öffentliche Pastes, Sortierung nach `createdAt`.
- [x] `GET /api/v2/public/user/{name}` implementieren und bei unbekanntem User 404 liefern.
- [x] `GET /api/v2/public/tags` implementieren: Sortierung nach `pasteCount`, Pagination und Suche.
- [ ] Legacy-`filter` für `GET /api/v2/public/tags` übernehmen.
- [x] `GET /api/v2/public/tags/{tag}` implementieren: Tag kürzen, bei Bedarf erstellen und zurückgeben.

### SEO und statisches Frontend

- [x] SSR-Renderer für `GET /{id}` portieren.
- [x] SSR-Renderer für `GET /tags/{tag}` portieren.
- [x] SPA-Assets, `static/index.html`, Thumbnail-Template und Fonts in den Spring-Boot-Resources bereitstellen.
- [x] SEO-Platzhalter `<!--META_TAGS-->`, `<!--META_TAGS_END-->`, `<title>` und `<div id="app">` kompatibel ersetzen.
- [ ] Meta-Tags, Canonical URL, OpenGraph, Twitter Cards, Paste-Vorschau, Tags, Autor und AI-Beschreibung übernehmen.
- [ ] `PASTEFY_META_TAGS`, `PASTEFY_META_TAGS_PREVIEW_LENGTH`, `SERVER_NAME`, `PASTEFY_CUSTOM_HEADER` und `PASTEFY_CUSTOM_BODY` unterstützen.
- [ ] SPA-Fallback für unbekannte Nicht-API-Pfade nachbauen.
- [ ] `/assets/**` mit `Cache-Control: public, max-age=604800, immutable` ausliefern.
- [ ] Unbekannte `/api/**`-Pfade weiterhin als 404 behandeln.

## Auth und OAuth2

- [ ] Header-Auswertung exakt prüfen: `x-auth-key`, Bearer und Basic Auth inklusive Legacy-Priorität.
- [ ] Anonyme Admin-Aufrufe wieder mit 401 statt 403 beantworten: Legacy-`admin` ruft zuerst `auth` auf.
- [ ] Prüfen, ob die historische Abhängigkeit von mindestens einem konfigurierten OAuth2-Provider beim Auth-Key-Login beibehalten oder bewusst korrigiert werden soll.
- [ ] `@Authenticated`, `@AdminRoute`, `@RejectBlocked`, `@RejectAwaitingAccess`, `@LoginRequiredForRead`, `@LoginRequiredForCreate` und `@PublicPastesEnabled` routeweise gegen die alten `@With`-Middlewares auditieren.
- [ ] Permission-Verhalten exakt prüfen: Nicht-`ACCESS_TOKEN`-Keys dürfen alle Scopes; `ACCESS_TOKEN`-Keys nur exakte oder Namespace-Scopes.
- [x] `POST /api/v2/auth/iaea` implementieren: InteraApps-App-Credentials prüfen, User suchen, `ACCESS_TOKEN` mit `appScopeList` erzeugen und Key-String liefern.
- [ ] OAuth-Provider InteraApps, Google, GitHub, Twitch, Discord und OIDC end-to-end gegen die Legacy-Profile mappen.
- [ ] Provider-Defaults und Scopes übernehmen, insbesondere InteraApps `user:read contacts.accepted:read`.
- [ ] Login-Callback kompatibel machen: User nach `authId` und Provider finden oder erstellen, eindeutigen `uniqueName` bilden, Profildaten bei jedem Login aktualisieren, `AWAITING_ACCESS` beachten und `USER`-AuthKey mit Access-/Refresh-Token speichern.
- [ ] OAuth-State-Schutz beibehalten, ohne die Legacy-Routen oder Redirects zu brechen.
- [ ] Redirect nach Login exakt `/auth?key=<key>` liefern.
- [ ] Legacy-Env-Namen als Aliase unterstützen oder dokumentiert migrieren: `OAUTH2_*`, `PASTEFY_LOGIN_REQUIRED`, `PASTEFY_GRANT_ACCESS_REQUIRED`.

## Analytics

- [ ] Die bereits implementierten ClickHouse-Queries gegen das alte Backend verifizieren.
- [ ] `GET /api/v2/analytics/admin` auf identische Auth-, Admin- und Permission-Semantik prüfen.
- [ ] `GET /api/v2/analytics/pastes/{id}` auf Owner-/Admin-Regeln und Filter `paste_key` prüfen.
- [ ] `GET /api/v2/analytics/user` auf Filter `paste_user_id` prüfen.
- [ ] Query-Parameter exakt unterstützen: `from`, `to`, `interval`, `group_by`, `include_summary`, `include_breakdown` sowie alle Legacy-Filterfelder.
- [ ] Analytics-Response gegen die Snake-Case-Feldnamen des API-Vertrags prüfen: `total_visits`, `unique_visitors`, `bot_visits`, `bot_tracking_enabled`, `series`, `breakdown`.
- [x] PAGE-Tracking an `GET /api/v2/paste/{id}?from_frontend=true` anbinden.
- [x] RAW-Tracking an `GET /{id}/raw` anbinden.
- [ ] Bounded Queue, Requeue bei ClickHouse-Ausfall, Drop-Zähler, Flush beim Shutdown und optionale GeoIP-Datenbank unter Last testen.
- [ ] Flyway-ClickHouse-Migration und Analytics-Service auf dieselben Tabellen-, TTL- und Identifier-Regeln festlegen.

## AI, Engagement und Hintergrundjobs

- [ ] Engagement-Service ergänzen: atomar erstellen/inkrementieren, Elasticsearch aktualisieren und AI-Enqueue prüfen.
- [x] Engagement-Hooks an GET-Paste, Raw-Paste und Fork-Erstellung anbinden.
- [x] AI-Info-Service an Engagement-Schwelle und manuellen Admin-Trigger anbinden.
- [ ] AI-Metadaten-Jobs gegen Race Conditions testen: Paste-Version vor und nach Modellaufruf prüfen, veraltete Jobs neu einreihen.
- [ ] Legacy-`generateTags` für `CreatePasteRequest.ai=true` mit Spring AI portieren.
- [ ] Legacy-`generateTagDescription` portieren und beim erstmaligen Erstellen eines Tags asynchron ausführen.
- [ ] AI-Provider- und Modellnamen in gespeicherten Metadaten stabil halten.
- [ ] Job-Leasing, Retry-Backoff, Max-Attempts und Sweeper mit mehreren App-Instanzen testen.
- [ ] Fehler- und Metrikpfad für dauerhaft fehlgeschlagene Jobs ergänzen.

## Speicherung, Cache und Suche

### S3 / MinIO

- [ ] S3-Referenzformat gegen bestehende Production-Daten prüfen und Legacy-JSON mit `server`, `bucket`, `objectName`, `region`, `etag` weiterhin lesen.
- [ ] Objektpfade exakt kompatibel halten: `pastes/<user-or-anonymous>/<key[0..2]>/<key[2..4]>/<key>/contents.txt`.
- [ ] Storage-Tags `userId` und `paste` übernehmen.
- [ ] Schwellenwert `MINIO_PASTESIZE_THRESHOLD` kompatibel anwenden.
- [ ] Fehlerverhalten und Transaktionsgrenzen prüfen, damit DB-Referenz und S3-Objekt nicht auseinanderlaufen.

### Redis

- [ ] Redis-Key-Format kompatibel halten: `paste:<id>` und `paste:<id>:accessCount`.
- [ ] TTL `30 Minuten` und Cache-Schwelle `> 10` Zugriffe gegen Legacy-Verhalten prüfen.
- [ ] Cache bei Änderungen und Löschungen invalidieren.
- [ ] Redis-Ausfälle weiterhin degradieren lassen, ohne Requests fehlschlagen zu lassen.

### Elasticsearch

- [ ] Gemeinsamen Query-Service an `ElasticPasteService` anbinden.
- [ ] Alle Dokumentfelder und Nested-User-Felder gegen das alte Mapping prüfen.
- [ ] Updates für Tags, Engagement, Stars und User sicher auslösen.
- [ ] Startup-Migrationen mit parallelen Instanzen testen; Reindexing und Alias-Swap dürfen keine Daten verlieren.
- [ ] Schreibvorgänge während eines Reindex-Laufs absichern: Wartungsmodus, Dual-Write oder finaler Sync vor Alias-Swap festlegen.
- [ ] Backfill für `indexed_in_elastic=false` als Spring-Boot-Job oder Admin-Command bereitstellen.
- [ ] Rollback-Strategie für versionierte Indizes dokumentieren.

## Datenbank und periodische Jobs

- [ ] Liquibase-Changesets für das bestehende MySQL-Production-Schema erstellen, ohne bestehende Daten zu verändern.
- [ ] Liquibase-Baseline-/Changelog-Strategie für bereits laufende Installationen dokumentieren.
- [ ] Entity-Nullability gegen das reale Production-Schema auditieren. Mehrere Legacy-Spalten sind nullable, während Kotlin-Entities aktuell `nullable=false` setzen.
- [ ] `AuthKey.scopes` gegen das Production-Schema prüfen: Dort ist `scopes` ein `TEXT`, nicht zwingend ein natives MySQL-`JSON`.
- [ ] ClickHouse-Flyway-Migrationen mit dem realen ClickHouse-Treiber und einer leeren Datenbank testen.
- [ ] Scheduled Job zum Löschen abgelaufener Pastes jede Minute portieren.
- [ ] Gelöschte Pastes vollständig aus SQL, Redis, S3 und Elasticsearch entfernen.

## CLI- und Betriebs-Parity

- [ ] Entscheiden, welche alten CLI-Kommandos als Spring Boot `ApplicationRunner`, Admin-Job oder separate Commands benötigt werden.
- [ ] Ersatz für `automigrate` bereitstellen: Liquibase-Deployment-Schritt.
- [ ] Ersatz für `automigrateelastic` bereitstellen: Kotlin-Startup-Migration oder gezielt startbarer Migrationsmodus.
- [ ] `syncelastic` als kontrollierten Backfill portieren.
- [ ] `syncminio` als kontrollierten Backfill portieren.
- [ ] `movesmallfilestodb` als kontrollierten Rückmigration-Job portieren.
- [ ] Health-, Readiness- und Liveness-Endpunkte ergänzen, insbesondere für MySQL, Redis, S3, Elasticsearch und ClickHouse.
- [ ] Observability ergänzen: Job-Fehler, Analytics-Drops, Elasticsearch-Backfill, S3-Fehler und OAuth-Fehler messen.
- [ ] Graceful Shutdown für Analytics-Flush und Hintergrundjobs prüfen.

## Plugin-System

- [ ] Plugin-Erkennung aus `plugins/`, `plugin.json`, Backend-Lifecycle-Hooks und Custom-Config-Mappings portieren.
- [ ] Frontend-Plugin-Assets unter `/assets/plugins/<name>` ausliefern.
- [ ] Plugin-Entrypoints in das ausgelieferte Frontend-HTML injizieren.

## Verifikation vor Umschaltung

- [ ] Eine Contract-Test-Suite erstellen, die altes und neues Backend mit denselben Requests aufruft und Status, Header, Content-Type und JSON-Struktur vergleicht.
- [ ] Golden Tests für alle oben genannten Routen ergänzen, inklusive anonymer, eingeloggter, Admin-, Blocked- und Awaiting-Access-Fälle.
- [ ] Golden Tests für Query-Parameter, Pagination-Header, Null-Felder, Defaultwerte und Fehlerantworten ergänzen.
- [ ] Tests mit Elasticsearch deaktiviert und aktiviert ausführen.
- [ ] Tests mit Redis deaktiviert und aktiviert ausführen.
- [ ] Tests mit S3 deaktiviert und aktiviert ausführen.
- [ ] Tests für OAuth-Redirect-URLs und Callback-Routen ergänzen.
- [ ] Tests für bestehende Production-S3-Referenzen, MySQL-Daten und Elasticsearch-Legacy-Indizes ergänzen.
- [ ] Erst nach grüner Contract-Suite Traffic auf `backend-kt` umschalten.

## Tests
- [ ] Unit Tests
  - [ ] Visibility Rules
  - [ ] Expiry Logic
  - [ ] Paste Key Generation
  - [ ] Rate Limiting
  - [ ] Permission Checks
  - [ ] Auth implementation
- [ ] Integration Tests using Testcontainers
  - [ ] S3/MinIO Integration
  - [ ] Redis Caching
  - [ ] Elasticsearch Queries
  - [ ] ClickHouse Analytics
  - [x] MySQL Database Interactions
  - [ ] OAuth2 Flows with Mock Providers
- [x] HTTP-Smoke-Tests für Snake-Case-Stats, App-Info, Public-User und Legacy-Raw-404 ergänzen.
- [ ] API-Contract-Tests für alle Routen ergänzen.
