# Database migrations

MySQL and ClickHouse migrations are deliberately separated:

- Liquibase owns MySQL changes. Its root changelog is `changelog/db.changelog-master.yaml`. It intentionally contains no changesets yet.
- Flyway owns ClickHouse changes. Versioned SQL files live in `migration/clickhouse/`.

Both migration paths are disabled by default so regular application startup never changes production schemas implicitly.

Apply MySQL migrations in the deployment migration step with:

```bash
LIQUIBASE_ENABLED=true
```

Apply ClickHouse migrations in a separate deployment migration step with:

```bash
ANALYTICS_CLICKHOUSE_MIGRATIONS_ENABLED=true
ANALYTICS_CLICKHOUSE_JDBC_URL=jdbc:clickhouse://clickhouse:8123/default
```

The ClickHouse migration runner does not require analytics event processing to be enabled.
