import hljs from "highlight.js";
import LANGUAGE_REPLACEMENTS from "@/assets/data/langReplacements";

function copyStringToClipboard(str) {
    var el = document.createElement('textarea');
    el.value = str;
    el.setAttribute('readonly', '');
    el.style = {position: 'absolute', left: '-9999px'};
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
}


class Toast {
    constructor(text = "", color = "#17fc2e", background = "#222530") {
        this.text = text
        this.color = color
        this.background = background
        this.element = null
        this.timer = null
        this.timeout = 2600
        this.useHTML = false
        this.element = document.createElement("span");
        this.element.id = 'snackbar'
        if (this.useHTML)
            this.element.innerHTML = this.text
        else
            this.element.textContent = this.text

        this.onclose = () => {
        }
        this.onopen = () => {
        }
    }

    open() {
        if (this.useHTML)
            this.element.innerHTML = this.text
        else
            this.element.textContent = this.text
        this.element.style.color = this.color
        this.element.style.backgroundColor = this.background
        this.element.classList.add('show');

        document.body.appendChild(this.element)

        this.onopen()

        this.timer = setTimeout(() => {
            this.timer = null
            this.close()
        }, this.timeout)
    }

    close() {
        if (this.timer != null)
            clearTimeout(this.timer)
        this.element.classList.remove('show');
        this.onclose()
        setTimeout(() => document.body.removeChild(this.element), 300)
    }
}

let bottomMargin = 24

function showSnackBar(text = "", color = "#17fc2e", background = "#222530", open = true) {
    const snackbar = new Toast(text, color, background)

    if (open) {
        snackbar.onopen = () => {
            snackbar.element.style.bottom = bottomMargin + "px"
            bottomMargin += snackbar.element.clientHeight + 8
        }

        snackbar.open()

        snackbar.onclose = () => {
            bottomMargin -= snackbar.element.clientHeight + 8
        }
    }

    return snackbar
}

export function buildSearchAndFilterQuery(q) {
    const query = {}

    if (q.includes("=")) {
        const split = q.split("=")
        query[`filter[${ split[0] }]`] = split[1]
    } else if (q) {
        query.search = q
    }

    return query
}

const languages = [...hljs.listLanguages(), "text"];

export function getLanguageByFileName(fileName) {
    const fileNameExtension = fileName.split(".");
    let ending = fileNameExtension[fileNameExtension.length - 1];
    const originalEnding = ending

    let language = null;

    for (let replace in LANGUAGE_REPLACEMENTS) {
        if (ending == replace) {
            ending = LANGUAGE_REPLACEMENTS[replace];
            break;
        }
    }

    if (languages.includes(ending)) {
        language = ending;
    }

    return [ending, language, originalEnding || ""]
}

export function estimateTitle(contents) {
    let matched = contents.match(/^#(#?)(#?)(#?)(#?) ([^\n]*)(\n.*)?/s)
    if (matched !== null) {
        return `${matched[5]}.md`
    }

    // Match HTML
    if (contents.startsWith('<!DOCTYPE html>')) {
        return `index.html`
    }
    if (contents.includes('<div>')) {
        return `file.html`
    }

    if (contents.startsWith('<template>')) {
        return `Component.vue`
    }
    // Match Python
    if (contents.includes('if __name__ == "__main__":')) {
        return `main.py`
    }

    // Match java
    matched = contents.match(/^\s?(package) .*;.*(public|private)\s(class|@?interface|abstract class|enum)\s([a-zA-Z-0-9]*)\s?.*{.*/s)
    if (matched !== null) {
        return `${matched[4]}.java`
    }

    // Match PHP
    matched = contents.match(/^\s?<\?php.*\n(class|interface|abstract class|enum|trait)\s([a-zA-Z-0-9]*)\s?.*{.*/s)
    if (matched !== null) {
        return `${matched[2]}.php`
    }
    // Match PHP
    if (contents.includes('<?php')) {
        return `index.php`
    }

    // Match CSharp
    matched = contents.match(/\s?namespace\s.*{.*\s(class|@?interface|abstract class|enum)\s([a-zA-Z-0-9]*)\s?.*{.*/s)
    if (matched !== null) {
        return `${matched[2]}.cs`
    }
    // Match Javascript
    matched = contents.match(/.*export\sdefault\s(class|function)\s([a-zA-Z-0-9]*)\s?{/s)
    if (matched !== null) {
        return `${matched[2]}.js`
    }
    if (contents.includes('console.log(') || contents.includes('document.write(')) {
        return `.js`
    }

    // Match Lua
    if (contents.startsWith('loadstring(')) {
        return `addon.lua`
    }

    // Match Go
    matched = contents.match(/^package main.*func\smain\s?\(.*/s)
    if (matched !== null) {
        return `main.go`
    }
    // Match C
    matched = contents.match(/^#include <studio\.h>.*int\smain\s?\(/s)
    if (matched !== null) {
        return `main.c`
    }
    // Match CPP
    matched = contents.match(/^#include <iostream>.*int\smain\s?\(/s)
    if (matched !== null) {
        return `main.cpp`
    }
    if (contents.includes('std::cout')) {
        return `file.cpp`
    }

    if (contents.startsWith('FROM') && (contents.includes('CMD') || contents.includes('RUN'))) {
        return `Dockerfile`
    }

    if (contents.startsWith('<svg') || contents.startsWith('<?xml') && contents.includes('<svg')) {
        return `file.svg`
    }

    // Match JSON
    matched = contents.match(/^{.*?:.*?}$/s)
    if (matched !== null) {
        if (contents.includes('"name"') && contents.includes('"version"')) {
            if (contents.includes('"require"'))
                return 'composer.json'
            if (contents.includes('"lockfileVersion"'))
                return 'package-lock.json'
            if (contents.includes('"dependencies"'))
                return 'package.json'
        }
        return `data.json`
    }

    return ''
}

export default {copyStringToClipboard, showSnackBar, buildSearchAndFilterQuery, Toast}