---
title: File Previews
---
<script setup>

import { useTemplateRef, watch } from 'vue'

const iframe = useTemplateRef('iframe')

watch(iframe, () => {
  if (iframe.value) {
    iframe.value.onload = () => {
      window.addEventListener('message', e => {
        if (e.data && 'height' in e.data) {
          iframe.value.style.height = e.data.height + 'px'
        }
      })
    }
  }
})
</script>



# 📂 File Previews

<br>

<iframe ref="iframe" class="paste" src="https://pastefy.app/LyyRgxA4/embed?hideActions=true" style="width: 700px; border: none; border-radius: 11px"></iframe>


Pastefy goes beyond plain text — it automatically detects and renders **rich previews** for many file types.  
This makes it easy to visualize your data, diagrams, and formatted content directly in the browser.

## ✨ Supported Formats

Here’s what Pastefy can currently preview:

| Format | Description |
|:--|:--|
| **Markdown (.md)** | Renders Markdown beautifully, with full syntax highlighting. |
| **Mermaid (.mermaid / .mmd)** | Generates flowcharts, sequence diagrams, and graphs directly from text. |
| **SVG (.svg)** | Displays inline SVG images with full resolution. |
| **CSV (.csv)** | Converts CSV files into readable, scrollable tables. |
| **GeoJSON (.geojson)** | Displays geographic data visually on an embedded map. |
| **Diff (.diff)** | Highlights code changes with color-coded additions and deletions. |
| **Calendar (.ics)** | Parses and previews calendar events. |
| **Regex (.regex)** | Provides a structured view for regular expressions. |
| **Asciinema (.cast)** | Embeds and plays terminal recordings interactively. |

🧠 **Example:** You can see several of these in action on  
👉 [https://pastefy.app/EByZpNoS](https://pastefy.app/EByZpNoS)

## 💡 How It Works

Pastefy automatically detects the file type by the **file extension**, or

When a supported format is detected, the paste view switches to an appropriate **interactive renderer**, allowing you to explore or visualize your data right in the browser — no extra setup required.

## ⚙️ Developer Notes
- Raw mode is still available — just click the **"Raw"** button on any paste.


With Pastefy’s previews, your code, diagrams, and data become **instantly shareable and readable** — no external tools needed.
