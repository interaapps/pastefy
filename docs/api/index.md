# Pastefy API

The **Pastefy API** allows you to programmatically access and manage content on [Pastefy.app](https://pastefy.app). With the API, you can create, edit, fetch, and delete pastes, manage folders and tags, and interact with user accounts securely.

## 🌐 Base URL

All API requests use the following base URL:

```
https://pastefy.app/api/v2
```

When self-hosted, replace `pastefy.app` with your instance's domain.


## 🔑 Authentication

Most endpoints require authentication via an **API token**. Pass the token using the `Authorization: Bearer <token>` header.

Get api key here: [https://pastefy.app/apikeys](https://pastefy.app/apikeys)

```http
GET /api/... HTTP/1.1
Host: pastefy.app
Authorization: Bearer YOUR_API_TOKEN
```

::: info
Some public endpoints, like fetching trending pastes or public user information, may not require authentication. 
BUT that might change in the future for some endpoints specifically.
:::

## 📦 Some of the API Features

- **Pastes**
  - Create, edit, delete, and fetch pastes
  - Access raw paste content
  - Star and unstar pastes
  - Filter and fetch public pastes (trending/latest)

- **Folders**
  - Organize pastes into folders
  - Fetch user or public folders

- **Users**
  - Get current user info and overview
  - Fetch public user profiles
  - Admin endpoints for managing users

- **Tags**
  - List and fetch tags

- **API Keys**
  - Create, list, and delete API keys for programmatic access

## 📚 Documentation

For detailed API usage, endpoints, and examples, visit the **Pastefy Client Documentation**:

* [JavaScript Client](/api/clients/javascript.md)
* [Java Clients](/api/clients/java.md)
* [Go Client](/api/clients/go.md)

> Even if you don’t use any of those languages, the client docs show all available endpoints, types, and request parameters.


## ⚡ Quick Tips

* Use **filters** to query pastes or folders efficiently.
* API responses include clear error messages; handle exceptions like `NotFoundException` or `PermissionsDeniedException`.