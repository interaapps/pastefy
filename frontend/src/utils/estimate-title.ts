import hljs from 'highlight.js'
export function estimateTitle(contents: string) {
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
  matched = contents.match(
    /^\s?(package) .*;.*(public|private)\s(class|@?interface|abstract class|enum)\s([a-zA-Z-0-9]*)\s?.*{.*/s,
  )
  if (matched !== null) {
    return `${matched[4]}.java`
  }

  // Match PHP
  matched = contents.match(
    /^\s?<\?php.*\n(class|interface|abstract class|enum|trait)\s([a-zA-Z-0-9]*)\s?.*{.*/s,
  )
  if (matched !== null) {
    return `${matched[2]}.php`
  }
  // Match PHP
  if (contents.includes('<?php')) {
    return `index.php`
  }

  // Match CSharp
  matched = contents.match(
    /\s?namespace\s.*{.*\s(class|@?interface|abstract class|enum)\s([a-zA-Z-0-9]*)\s?.*{.*/s,
  )
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
  if (
    contents.includes('loadstring(') ||
    contents.includes('game.Players') ||
    contents.includes('game:GetService') ||
    contents.includes('game:HttpGet') ||
    contents.includes('--[[') ||
    contents.startsWith('local ')
  ) {
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

  if (contents.startsWith('<svg') || (contents.startsWith('<?xml') && contents.includes('<svg'))) {
    return `file.svg`
  }

  // Match JSON
  matched = contents.match(/^{.*?:.*?}$/s)
  if (matched !== null) {
    if (contents.includes('"name"') && contents.includes('"version"')) {
      if (contents.includes('"require"')) return 'composer.json'
      if (contents.includes('"lockfileVersion"')) return 'package-lock.json'
      if (contents.includes('"dependencies"')) return 'package.json'
    }
    return `data.json`
  }

  try {
    if (contents.length < 7000) {
      const auto = hljs.highlightAuto(contents)

      const languages = {
        javascript: 'script.js',
        typescript: 'script.ts',
        coffeescript: 'script.js',
        java: 'Program.java',
        groovy: 'Program.groovy',
        kotlin: 'Program.kt',
        'php-template': 'component.php',
        php: 'index.php',
        lua: 'extension.lua',
        r: 'file.r',
        ini: 'config.ini',
        toml: 'config.toml',
        diff: 'changes.diff',
        properties: '.env',
        json: 'data.json',
        scss: 'styles.scss',
        css: 'styles.css',
        less: 'styles.less',
        shell: 'script.sh',
        bash: 'script.bash',
        apache: '.htaccess',
        rust: 'program.rs',
        yaml: 'data.yml',
        'python-repl': 'app.py',
        python: 'app.py',
        dockerfile: 'Dockerfile',
        sql: 'db.sql',
        markdown: 'document.md',
        vbscript: 'program.vbs',
        swift: 'app.swift',
        cpp: 'app.cpp',
        c: 'app.c',
        csharp: 'App.cs',
        http: 'request.http',
        perl: 'app.pl',
        nginx: 'config.nginx',
        objectivec: 'program.m',
        go: 'app.go',
        dart: 'component.dart',
      } as Record<string, string>

      if (auto.language && auto.language in languages) {
        return languages[auto.language]
      }
      if (auto.secondBest?.language && auto.secondBest.language in languages) {
        return languages[auto.secondBest?.language]
      }
    }
  } catch {
    //
  }

  return ''
}
