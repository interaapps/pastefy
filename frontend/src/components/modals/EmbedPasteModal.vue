<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import Highlighted from '@/components/Highlighted.vue'
import { computed } from 'vue'

defineProps<{
  pasteId: string
}>()

const visible = defineModel<boolean>('visible')

const origin = window.location.origin

const embedCode = computed(
  () => `<div id="pastefy"></div>
${'<'}script type="module">
  import { embedPastefy } from '${origin}/embed.js'
  embedPastefy(document.getElementById('pastefy'), 'P3fKIHq3'${origin !== 'https://pastefy.app' ? `, "${origin}"` : ''})
</${'script>'}`,
)
const iframeEmbedCode = computed(
  () => `<iframe class="paste" src="${origin}/P3fKIHq3/embed" style="width: 700px; border: none"></iframe>
<${'script'}>
  /** @type {HTMLIFrameElement} */
  const iframe = document.querySelector('.paste')
  if (iframe) {
    iframe.onload = () => {
      window.addEventListener('message', e => {
        if (e.data && 'height' in e.data) {
          iframe.style.height = e.data.height + 'px'
        }
      })
    }
  }
</${'script'}>`,
)
</script>
<template>
  <Dialog v-model:visible="visible" modal header="Embed" class="w-[60rem] max-w-full">
    <h2 class="mb-2 text-lg font-bold">Embed via script</h2>
    <div
      class="mb-5 rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
    >
      <Highlighted file-name=".html" :contents="embedCode" />
    </div>

    <h2 class="mb-2 text-lg font-bold">Embed via iframe tag</h2>
    <div
      class="mb-6 rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
    >
      <Highlighted file-name=".html" :contents="iframeEmbedCode" />
    </div>

    <h2 class="mb-2 text-lg font-bold">Preview</h2>
    <div class="h-[20rem] rounded-xl border border-neutral-200 p-6 dark:border-neutral-700">
      <iframe :src="`/${pasteId}/embed`" class="h-full w-full" />
    </div>
  </Dialog>
</template>
