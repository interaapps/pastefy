# Develop Plugins

## File structure
```
- pastefy-app.jar
- .env
...
- plugins/
    - my-plugin/
        - plugin.json
        - frontend/
            - plugin.js
        - backend:
            - backend.jar
```

## plugin.json
```json
{
  "name": "my-plugin",
  "frontend": {
    "folder": "frontend",
    "entrypoint": "plugin.js"
  },
  "backend": {
    "file": "backend/backend.jar",
    "main": "com.example.MyPlugin"
  }
}
```

## Plain Frontend
```js
const pastefy = window.pastefy;
const { h } = pastefy.vueFunctions

pastefy.createPlugin('my-plugin', {
    async init() {
        pastefy.componentInjections.registerInjection('paste-below-title', ({ value }) => {
            console.log(value.title)
            return h('div', { class: 'bar', innerHTML: 'hello' })
        })
    }
})
```