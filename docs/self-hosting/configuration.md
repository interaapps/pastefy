# `.env Configuration`

Pastefy uses environment variables to configure backend settings, authentication, storage, and integrations. This guide lists all available `.env` variables, grouped by category, with descriptions and defaults.

---

## **1. Server Settings** (Required)

| Variable           | Description                             | Default / Notes      |
|--------------------|-----------------------------------------|----------------------|
| `HTTP_SERVER_PORT` | Port the HTTP server listens on         | `80`                 |
| `HTTP_SERVER_CORS` | Allowed CORS origins                    | `*`                  |
| `SERVER_NAME`      | The public URL of your Pastefy instance | `http://example.com` |

---

## **2. Database Settings** (Required)

::: info
Currently only MySQL is supported.
:::

| Variable               | Description                             | Default / Notes |
|------------------------|-----------------------------------------|-----------------|
| `DATABASE_DRIVER`      | Database type (Currently only `mysql`)  | `mysql`         |
| `DATABASE_NAME`        | Database name                           | `pastefy`       |
| `DATABASE_USER`        | Database username                       | `pastefy`       |
| `DATABASE_PASSWORD`    | Database password                       | `pastefy`       |
| `DATABASE_HOST`        | Database host                           | `localhost`     |
| `DATABASE_PORT`        | Database port                           | `3306`          |
| `PASTEFY_AUTOMIGRATE`  | Enable automatic DB migration           | `true`          |

### Extra: Optional MySQL Performance Tuning (Optional)
| Variable                                           | Description                                 | Default / Notes |
|----------------------------------------------------|---------------------------------------------|-----------------|
| `DATABASE_CUSTOMPARAMS_CACHE_PREP_STMTS`           | Cache prepared statements                   | `true`          |
| `DATABASE_CUSTOMPARAMS_PREP_STMT_CACHE_SIZE`       | Prepared statement cache size               | `250`           |
| `DATABASE_CUSTOMPARAMS_PREP_STMT_CACHE_SQL_LIMIT`  | Max SQL length for prepared statement cache | `2048`          |
| `DATABASE_CUSTOMPARAMS_USE_SERVER_PREP_STMTS`      | Use server-side prepared statements         | `true`          |
| `DATABASE_CUSTOMPARAMS_CACHE_RESULT_SET_METADATA`  | Cache result set metadata                   | `true`          |
| `DATABASE_CUSTOMPARAMS_CACHE_SERVER_CONFIGURATION` | Cache server configuration                  | `true`          |
| `DATABASE_CUSTOMPARAMS_MAINTAIN_TIME_STATS`        | Maintain time statistics                    | `true`          |

---

## **3. OAuth / Login Providers** (Optional)

::: tip
You are not required to use a login provider. Pastefy can be used without login as well. 
And you can configure multiple providers at the same time if wanted.
:::

| Variable                          | Description                            | Example / Notes                |
|-----------------------------------|----------------------------------------|--------------------------------|
| `OAUTH2_INTERAAPPS_CLIENT_ID`     | Client ID for Interaapps login         | `dan3q9n`                      |
| `OAUTH2_INTERAAPPS_CLIENT_SECRET` | Client secret for Interaapps login     | `ASDFASDF`                     |
| `OAUTH2_TWITCH_CLIENT_ID`         | Client ID for Twitch login             | `your-twitch-id`               |
| `OAUTH2_TWITCH_CLIENT_SECRET`     | Client secret for Twitch login         | `your-twitch-secret`           |
| `OAUTH2_GITHUB_CLIENT_ID`         | Client ID for GitHub login             | `your-github-id`               |
| `OAUTH2_GITHUB_CLIENT_SECRET`     | Client secret for GitHub login         | `your-github-secret`           |
| `OAUTH2_GOOGLE_CLIENT_ID`         | Client ID for Google login             | `your-google-id`               |
| `OAUTH2_GOOGLE_CLIENT_SECRET`     | Client secret for Google login         | `your-google-secret`           |
| `OAUTH2_DISCORD_CLIENT_ID`        | Client ID for Discord login            | `your-discord-id`              |
| `OAUTH2_DISCORD_CLIENT_SECRET`    | Client secret for Discord login        | `your-discord-secret`          |
| `OAUTH2_OIDC_CLIENT_ID`           | Client ID for custom OIDC provider     | `your-client-id`               |
| `OAUTH2_OIDC_CLIENT_SECRET`       | Client secret for custom OIDC provider | `your-secret`                  |
| `OAUTH2_OIDC_AUTH_ENDPOINT`       | OAuth2 authorization endpoint          | `https://example.com/auth`     |
| `OAUTH2_OIDC_TOKEN_ENDPOINT`      | OAuth2 token endpoint                  | `https://example.com/token`    |
| `OAUTH2_OIDC_USERINFO_ENDPOINT`   | OIDC userinfo endpoint                 | `https://example.com/userinfo` |

---

## **4. Pastefy Access & Features** (Optional)

| Variable                        | Description                              | Default |
|---------------------------------|------------------------------------------|---------|
| `PASTEFY_LOGIN_REQUIRED`        | Require login for all actions            | `false` |
| `PASTEFY_LOGIN_REQUIRED_CREATE` | Require login to create pastes           | `false` |
| `PASTEFY_LOGIN_REQUIRED_READ`   | Require login to read pastes             | `false` |
| `PASTEFY_GRANT_ACCESS_REQUIRED` | Require admin approval for new accounts  | `false` |
| `PASTEFY_LIST_PASTES`           | Enable `/paste` route listing all pastes | `false` |
| `PASTEFY_PUBLIC_STATS`          | Make `/app/stats` public                 | `false` |
| `PASTEFY_PUBLIC_PASTES`         | Show public pastes section               | `false` |
| `PASTEFY_META_TAGS`             | Meta tags for the instance               | `none`  |
| `PASTEFY_CUSTOM_BODY`           | Custom HTML body content                 | `none`  |
| `PASTEFY_CUSTOM_HEADER`         | Custom HTML header content               | `none`  |
| `PASTEFY_PAGINATION_PAGE_LIMIT` | Pastes per page in pagination            | `none`  |
| `PASTEFY_DEV`                   | Enable development mode                  | `false` |

---

## **5. Redis** (Optional)

| Variable     | Description       | Examples |
|--------------|-------------------|----------|
| `REDIS_HOST` | Redis server host | `-`      |
| `REDIS_PORT` | Redis server port | `-`      |

---

## **6. MinIO / Object Storage** (Optional)

::: warning
Pastefy does not auto migrate your database to use MinIO/S3. You need to set it up manually and migrate existing pastes if needed. Set up a pastefy instance with MinIO/S3 from the start to avoid migration if you plan to grow.
:::

| Variable           | Description                   | Examples                    |
|--------------------|-------------------------------|-----------------------------|
| `MINIO_SERVER`     | MinIO / S3 server URL         | `https://minio.example.com` |
| `MINIO_BUCKET`     | Bucket name for Pastefy files | `pastefy`                   |
| `MINIO_ACCESS_KEY` | Access key                    | `-`                         |
| `MINIO_SECRET_KEY` | Secret key                    | `-`                         |
---

## **7. Elasticsearch** (Optional)

::: warning
Pastefy does not auto migrate your database to use Elasticsearch. You need to set it up manually and migrate existing pastes if needed. Set up a pastefy instance with Elasticsearch from the start to avoid migration if you plan to grow.
:::

| Variable                 | Description                | Example                 |
|--------------------------|----------------------------|-------------------------|
| `ELASTICSEARCH_URL`      | Elasticsearch server URL   | `http://localhost:9200` |
| `ELASTICSEARCH_USER`     | Username for Elasticsearch | `elastic`               |
| `ELASTICSEARCH_PASSWORD` | Password for Elasticsearch | `changeme`              |
| `ELASTICSEARCH_API_KEY`  | API key for Elasticsearch  | `xxx`                   |

---

## **8. AI / Integrations** (Optional)

| Variable             | Description                             | Notes |
|----------------------|-----------------------------------------|-------|
| `AI_ANTHROPIC_TOKEN` | API token for Anthropics AI integration | -     |

---

### ✅ Notes

* Can be used in `.env` or as Docker environment variables.
* Make sure `SERVER_NAME` matches your public URL for OAuth redirects.
* Combine with Docker-Compose or manual deployment depending on your setup.