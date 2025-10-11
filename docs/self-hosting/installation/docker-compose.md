# Docker-Compose Deployment

Using Docker-Compose is a convenient way to run Pastefy together with its dependencies like MySQL, Redis, or MinIO in a single configuration.

---

## **1. Copy the docker-compose**

```yaml
version: '3.3'

services:
  db:
    image: mariadb:10.11
    volumes:
      - dbvol:/var/lib/mysql

    environment:
      MYSQL_ROOT_PASSWORD: pastefy
      MYSQL_DATABASE: pastefy
      MYSQL_USER: pastefy
      MYSQL_PASSWORD: pastefy

  pastefy:
    depends_on:
      - db
    image: interaapps/pastefy:latest
    ports:
      - "9999:80"

    environment:
      HTTP_SERVER_PORT: 80
      HTTP_SERVER_CORS: "*"
      DATABASE_DRIVER: mysql
      DATABASE_NAME: pastefy
      DATABASE_USER: pastefy
      DATABASE_PASSWORD: pastefy
      DATABASE_HOST: db
      DATABASE_PORT: 3306
      SERVER_NAME: "http://localhost:9999"
      # There is INTERAAPPS, GOOGLE, GITHUB, DISCORD, TWITCH
      OAUTH2_PROVIDER_CLIENT_ID:
      OAUTH2_PROVIDER_CLIENT_SECRET:

volumes:
  dbvol:
```

---

## **2. Using the Provided `docker-compose.yml`**

```bash
docker-compose up -d
```

This will start Pastefy along with the required services (database, Redis, etc.) in detached mode.

---

## **3. Customize Environment Variables**

Before starting, you might want to configure some environment variables:

```yaml
services:
  pastefy:
    image: interaapps/pastefy
    ports:
      - "8080:80"
    environment:
      HTTP_SERVER_PORT: 80
      HTTP_SERVER_CORS: "*"
      ...
```

* Change `SERVER_NAME` to your domain if self-hosting publicly.
* For OAuth login, add the respective environment variables under `environment`.

---

## **4. Starting & Stopping Pastefy**

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f
```

> Tip: Use `docker-compose restart pastefy` to restart only the Pastefy service after changing environment variables.

---

## **5. Next Steps**

* Verify that Pastefy is running at `http://localhost:8080`.
* Configure OAuth logins if needed: [Configuration](../oauth.md)