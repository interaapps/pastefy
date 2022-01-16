# Petrel Code Editor
![](/.gitlab/screenshots/1.png)

# Installation
## NPM
```js
npm install petrel

import { CodeEditor } from 'petrel'
```
## Module
```js
import { CodeEditor } from 'https://js.intera.dev/petrel/1.0.5/index.js'
```
## JS
```html
<script src='https://js.intera.dev/petrel/1.0.5.js'></script>
```
# Styles
## NPM & Webpack
```js
require('petrel/css/dark.css')
```
## Web
```html
<link rel="stylesheet" href="https://js.intera.dev/petrel/1.0.5/css/dark.css">
```
# Example
```html

<head>
    <link rel="stylesheet" href="https://js.intera.dev/petrel/1.0.5/css/dark.css">
    <!-- For the following example I'm using highlightjs so I import the styles for it as well. Theme: Pastefy -->
    <link rel="stylesheet" href="https://js.intera.dev/petrel/1.0.5/css/highlight/pastefy.css">
</head>

<div id="editor"></div>
<script type="module">
    import { CodeEditor, JavaScriptAutoComplete } from 'https://js.intera.dev/petrel/1.0.5/index.js'

    const codeEditor = new CodeEditor(element)

    // Using highlightjs for this example
    codeEditor.setHighlighter(code => hljs.highlight("javascript", code).value)

    // Sets the content of the editor
    codeEditor.setValue(`console.log("Welcome to Petrel!")`)

    // Using JS autocompletion
    codeEditor.setAutoCompleteHandler(new JavaScriptAutoComplete())

    // Creates the editor
    codeEditor.create()
</script>
```

# Options
```js
new CodeEditor(..., {
    closeKeys: {
        '<', '>' // If < is being typed into the editor, the editor will autocomplete it with >
    },
    placeholder: 'Enter code in here',
    value: "Hello World",
    readonly: false,
    tabShortcutsEnabled: true,
    tabSize: 4
})

```

# Custom styling
If you want to style Petrel yourself you might look into css/dark.css as an example.