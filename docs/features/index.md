---
title: Pastefy Features
---

# Pastefy Features

Pastefy is an **open source alternative to Gists or Pastebin** — built for developers and teams who want a simple, self-hostable way to share code, configuration, and text securely.

You can try it right now on the public instance:  
👉 [**pastefy.app**](https://pastefy.app)

## 🚀 What Makes Pastefy Special

Pastefy combines the simplicity of Pastebin with the flexibility of a modern web app.  
Whether you’re just sharing a quick script, storing configuration files, or integrating it into your CI/CD pipeline — Pastefy adapts to your workflow.

::: info
**You can use Pastefy to...**
- Quickly share code snippets or logs
- Organize projects with folders
- Record and share terminal sessions (via [Asciinema](./integrations/asciinema.md))
- Visualize data files like CSV, Markdown, Mermaid diagrams, and more  
- Integrate pastes directly into your apps via the [REST API](../api/index.md)
- Use it for scripting platforms or game modifications
:::

---

## 💡 Key Features

### 🗂️ Paste Management
- **Raw Preview** and **Copy Button**
- **Fork pastes** with one click
- **Public / Private / Unlisted** 
- **Client-Encrypted pastes**
- **Syntax Highlighting** for 200+ languages
- **Expiration**
- **Custom API Integrations**
- **Folders** for organization
- **Full-screen mode**
- **Dark / Light themes**

### 💾 Developer Tools
- **API v2** with SDKs for [JavaScript](https://github.com/interaapps/js-api-clients?tab=readme-ov-file#pastefy), [Java](https://github.com/interaapps/pastefy-java-apiclient), and [Go](https://github.com/interaapps/pastefy-go-api)
- **CLI & cURL upload**
  ```bash
  curl -F f=@file.txt pastefy.app
  ````

* **OAuth2 Login** (Google, GitHub, Discord, Twitch, InteraApps, or custom OIDC)
* **Admin Panel** for user and paste management

### 💫 Previews

Pastefy supports rich previews for various file types:

* 📝 [Markdown](./previews)
* 🧭 Mermaid Diagrams
* 🖼️ SVG
* 📊 CSV
* 🌍 GeoJSON
* 🧩 Diff / Regex
* ⏺️ [Asciinema Recordings](./integrations/asciinema.md)

### Sharing pastes
* **Embed pastes** in websites or blogs
* **Share via QR code**
* **Create beautiful Code Screenshots**
* **OS sharing** (Twitter, Facebook, LinkedIn)
* **RAW links** for direct access

### ⚙️ Extensions & Integrations

* **VS Code Extension** — create and manage pastes from your editor
* **Raycast Extension** — share snippets instantly from macOS
* **Asciinema Integration** — record terminal sessions and upload them directly
* **OAuth2 Providers** — Google, GitHub, Discord, Twitch, InteraApps, or custom OIDC

### ☁️ Deployment

Pastefy is built to run anywhere:

* 🐳 [Docker](/self-hosting/installation/docker.md)
* 🧩 [Docker Compose](/self-hosting/installation/docker-compose.md)
* ☸️ Kubernetes
* 💻 Manual setup (Java + Node)
* Optional Redis, MinIO, and Elasticsearch integrations


## 🧰 Build With Pastefy

Pastefy isn’t just for sharing — it’s a **platform**.
You can build apps, bots, or internal tools around it using the REST API.

::: tip
If you just want to try it out, visit
👉 [**pastefy.app**](https://pastefy.app)
and start pasting — no setup required!
:::
