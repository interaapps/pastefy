---
title: Asciinema Integration
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

# 🎬 Asciinema Integration

<br>

<iframe ref="iframe" class="paste" src="https://pastefy.app/ar9ehz8w/embed?hideActions=true" style="width: 700px; border: none; border-radius: 11px"></iframe>

Pastefy supports **Asciinema recordings**, making it easy to share terminal sessions with your friends, colleagues, or publicly. This feature is perfect for tutorials, demonstrations, or saving a terminal workflow for later.

## What is Asciinema?

[Asciinema](https://asciinema.org/) is an open-source tool for recording terminal sessions and sharing them online. Unlike screen recordings, Asciinema stores the session as text and commands, allowing others to **play it back interactively**.

## How Pastefy Supports Asciinema

With Pastefy, you can:

- Upload `.cast` files directly.
- Share them via a unique Pastefy link.
- Authenticate via your Pastefy account for private or public recordings.

### Supported Commands

```bash
# Record a terminal session
asciinema rec test.cast

# Stop recording and save
# Upload the recording to Pastefy
asciinema upload test.cast
````

### Authentication with Pastefy

To link your Asciinema recordings to your Pastefy account:

#### 1.
configure: nano ~/.config/asciinema/config
```toml
[api]
url = https://pastefy.app
```

#### 2. auth via cli
```bash
# Authenticate Asciinema with Pastefy
asciinema auth

# Set your Pastefy API key for Asciinema
echo YOUR_PASTEFY_API_KEY > ~/.config/asciinema/install-id

# Upload your recording
asciinema upload test.cast
```

### Example Paste

You can see an example recording on Pastefy [here](https://pastefy.app/ar9ehz8w).

---

With this integration, **sharing terminal demos has never been easier**. Pastefy keeps your recordings organized, easy to share, and accessible anywhere.
