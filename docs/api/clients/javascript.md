# Pastefy JavaScript/Typescript Client

A TypeScript/JavaScript client for interacting with the [Pastefy.app](https://pastefy.app) API (v2). Access pastes, folders, tags, users, and more with full TypeScript typing.

## 🚀 Installation

```bash
npm install @interaapps/pastefy
```


## 📦 Usage

<ClientTabs>

<ClientTab title="JavaScript" lang="javascript">

```javascript
import { PastefyClient } from '@interaapps/pastefy';

const client = new PastefyClient('YOUR_API_TOKEN');

// Fetch a paste
const paste = await client.getPaste('paste-id');
console.log(paste.title);

// Create a new paste
const newPaste = await client.createPaste({
  title: 'Hello World',
  content: 'This is a sample paste.',
  visibility: 'PUBLIC',
});
console.log(newPaste.id);
```

</ClientTab>

<ClientTab title="TypeScript" lang="ts">

```ts
import { PastefyClient, CreatePasteRequest } from '@interaapps/pastefy';

const client = new PastefyClient('YOUR_API_TOKEN');

const paste: CreatePasteRequest = {
  title: 'Hello TypeScript',
  content: 'Pastefy TS client',
  visibility: 'UNLISTED',
};

const newPaste = await client.createPaste(paste);
console.log(newPaste.id);
```

</ClientTab>

</ClientTabs>


## 🔐 Authentication

<ClientTabs>

<ClientTab title="Initialize Client" lang="ts">

```ts
const client = new PastefyClient('YOUR_API_TOKEN');
```

</ClientTab>

<ClientTab title="Update Token" lang="ts">

```ts
client.setApiToken('NEW_API_TOKEN');
```

</ClientTab>

</ClientTabs>

## 📚 API Reference

### 📝 Pastes

<ClientCollapsible title="Pastes Methods">

| Method                                          | Description                    |
| ----------------------------------------------- | ------------------------------ |
| `createPaste(paste: CreatePasteRequest)`        | Create a new paste             |
| `editPaste(id: string, data: EditPasteRequest)` | Edit an existing paste         |
| `getPastes(query: PasteFilters)`                | Get pastes of the current user |
| `getPaste(id: string)`                          | Fetch a single paste           |
| `getPasteRaw(id: string)`                       | Fetch raw content of a paste   |
| `deletePaste(id: string)`                       | Delete a paste                 |
| `starPaste(id: string)`                         | Star a paste                   |
| `unstarPaste(id: string)`                       | Unstar a paste                 |
| `getPublicPastes(query: PasteFilters)`          | Fetch public pastes            |
| `getPublicTrendingPastes(query: PasteFilters)`  | Fetch trending public pastes   |
| `getLatestPublicPastes(query: PasteFilters)`    | Fetch latest public pastes     |
| `getUserPastes(query: PasteFilters)`            | Fetch pastes of a user         |
| `getStarredPastes(query: PasteFilters)`         | Fetch starred pastes           |

</ClientCollapsible>

### 📁 Folders

<ClientCollapsible title="Folders Methods">

| Method                                    | Description                      |
| ----------------------------------------- | -------------------------------- |
| `createFolder(data: CreateFolderRequest)` | Create a folder                  |
| `getFolder(id: string, options?)`         | Get folder info                  |
| `getFolders(query: Filters)`              | List all folders                 |
| `getUserFolders(query?)`                  | List folders of the current user |

</ClientCollapsible>

### 👤 Users

<ClientCollapsible title="Users Methods">

| Method                                 | Description                      |
| -------------------------------------- | -------------------------------- |
| `getCurrentUser()`                     | Get your current user info       |
| `getUserOverview()`                    | Get overview of the current user |
| `getPublicUser(id: string)`            | Fetch public info of a user      |
| `getUser(id: string)`                  | Admin: fetch user info           |
| `getUsers(query: string)`              | Admin: list users                |
| `editUser(id: string, data: EditUser)` | Admin: edit a user               |
| `deleteUser(id: string)`               | Admin: delete a user             |

</ClientCollapsible>

### 🔑 API Keys

<ClientCollapsible title="API Keys Methods">

| Method                     | Description          |
| -------------------------- | -------------------- |
| `createApiKey()`           | Create a new API key |
| `getApiKeys()`             | List API keys        |
| `deleteApiKey(id: string)` | Delete an API key    |

</ClientCollapsible>

### 🏷 Tags

<ClientCollapsible title="Tags Methods">

| Method                    | Description          |
| ------------------------- | -------------------- |
| `getTags(query: Filters)` | List tags            |
| `getTag(id: string)`      | Fetch a specific tag |

</ClientCollapsible>

---

## ❗ Exceptions

<ClientCollapsible title="Handling Exceptions">

```ts
try {
  await client.getPaste('invalid-id');
} catch (e) {
  if (e instanceof NotFoundException) {
    console.error('Paste not found');
  }
}
```

* `AuthenticationException`
* `AwaitingAccessException`
* `BlockedException`
* `FeatureDisabledException`
* `NotFoundException`
* `PastePrivateException`
* `PermissionsDeniedException`

</ClientCollapsible>

---

## 🧪 Example: Create and Fetch Pastes

<ClientCollapsible title="Full Example">

```javascript
const paste = await client.createPaste({
  title: 'My First Paste',
  content: 'Welcome to Pastefy!',
  visibility: 'UNLISTED',
  tags: ['welcome', 'test'],
});

console.log(`Paste created at: https://pastefy.app/${paste.id}`);

const fetched = await client.getPaste(paste.id);
console.log(fetched.content);
```

</ClientCollapsible>


## Inofficial clients
- [Unofficial JavaScript client for Pastefy API by dxkyy](https://github.com/dxkyy/pastefy-api)