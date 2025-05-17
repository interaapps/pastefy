# New Frontend checklist
- [ ] Transparent Background for iframe embed
- [x] Authentication
- [x] Search (Private + Global)
- [x] Folders
- [x] Admin panel
- [x] Api-Keys
- [x] Meta Headers
- [ ] Settings
  - [x] Theme
- [x] Fix Trending algo
- [ ] Previews
  - [x] Markdown (Also in editor)
  - [ ] SVG
  - [ ] JSON / YAML (https://www.npmjs.com/package/vue3-json-viewer or CodeMirror)
  - [ ] XML
  - [ ] HTML
  - [ ] Plain Text
  - [ ] Latex
  - [ ] Mermaid
  - [x] CSV
  - [ ] SQL
    - [ ] MySQL Emulator https://github.com/i-van/mysql-emulator
    - [ ] Postgres Emulator https://www.npmjs.com/package/pgmock
    - [ ] SQLite Emulator https://sql.js.org/
- [ ] Comments on pastes (Also on lines)
- [ ] Paste List Entry copy button (fetches full paste first)
- [ ] File Upload
  - [ ] Image
  - [ ] Audio
  - [ ] PDF


## Frontend Plugins
```js
const myPlugin = createPastefyPlugin('my-plugin')
// ...
```

.env
```properties
PASTEFY_PLUGINS_FRONTEND=https://.../built_file.js
```