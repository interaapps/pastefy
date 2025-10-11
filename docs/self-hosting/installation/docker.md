# Docker Deployment

Docker is the easiest way to get Pastefy running quickly. This guide will show you how to run Pastefy in a Docker container.

---

## **1. Pull the Docker Image**

Pastefy is available on Docker Hub:

```bash
docker pull interaapps/pastefy
```

---

## **2. Run Pastefy with Docker**

You can start Pastefy using a single `docker run` command:

```bash
docker run -p 8080:80 \
  --env HTTP_SERVER_PORT=80 \
  --env HTTP_SERVER_CORS="*" \
  ...
  interaapps/pastefy
```

> You can find all environment variables in [Configuration](../configuration.md).

---

## **3. Verify Deployment**

1. Open your browser and navigate to `http://localhost:8080` (or your configured `SERVER_NAME`).
2. You should see the Pastefy home page.
3. Try creating a paste to confirm the database connection works.

---

## **4. Next Steps**

* Configure OAuth login: [Configuration](../oauth.md)
* Consider using Docker-Compose for more advanced deployments: [Docker-Compose](docker-compose.md)