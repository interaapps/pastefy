<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import Checkbox from 'primevue/checkbox'
import Highlighted from '@/components/Highlighted.vue'
import { computed, type Ref, ref } from 'vue'

const props = defineProps<{
  pasteId: string
}>()

const visible = defineModel<boolean>('visible')

const origin = window.location.origin

const hideTitle = ref(false)
const hideActions = ref(false)
const addPadding = ref(false)
const settingsQuery = computed(() => {
  const query: Record<string, string> = {}
  if (hideTitle.value) query.hideTitle = 'true'
  if (hideActions.value) query.hideActions = 'true'
  if (addPadding.value) query.addPadding = 'true'

  return Object.keys(query).length ? '?' + new URLSearchParams(query).toString() : ''
})
const embedCode = computed(
  () => `<div id="pastefy"></div>
${'<'}script type="module">
  import { embedPastefy } from '${origin}/embed.js'
  embedPastefy(document.getElementById('pastefy'), 'P3fKIHq3'${
    origin !== 'https://pastefy.app'
      ? `, ${JSON.stringify({
          hideTitle: hideTitle.value || undefined,
          hideActions: hideActions.value || undefined,
        })}, "${origin}"`
      : ''
  })
</${'script>'}`,
)
const iframeEmbedCode = computed(
  () => `<iframe class="paste" src="${origin}/${props.pasteId}/embed${settingsQuery.value}" style="width: 700px; border: none; border-radius: 11px"></iframe>
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
  <Dialog v-model:visible="visible" modal header="Embed" class="w-[80rem] max-w-full">
    <div class="gap-4 md:grid md:grid-cols-2">
      <div class="md:h-full md:overflow-auto">
        <h2 class="mb-2 text-lg font-bold">Options</h2>
        <div
          class="mb-5 flex flex-wrap gap-5 rounded-xl border border-neutral-200 bg-neutral-100 p-5 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <div class="flex items-center gap-3">
            <label for="hide-title">Hide Title</label>
            <Checkbox input-id="hide-title" binary v-model="hideTitle" />
          </div>
          <div class="flex items-center gap-3">
            <label for="hide-actions">Hide Actions</label>
            <Checkbox input-id="hide-actions" binary v-model="hideActions" />
          </div>
          <div class="flex items-center gap-3">
            <label for="add-padding">Add Padding</label>
            <Checkbox input-id="add-padding" binary v-model="addPadding" />
          </div>
        </div>

        <h2 class="mb-2 text-lg font-bold">Embed via script</h2>
        <div
          class="mb-5 rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <Highlighted show-copy-button file-name=".html" :contents="embedCode" />
        </div>

        <h2 class="mb-2 text-lg font-bold">Embed via iframe tag</h2>
        <div
          class="mb-6 rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
        >
          <Highlighted show-copy-button file-name=".html" :contents="iframeEmbedCode" />
        </div>
      </div>

      <div class="md:h-full md:overflow-auto">
        <h2 class="mb-2 text-lg font-bold">Preview</h2>
        <div
          class="h-[20rem] rounded-xl border border-neutral-200 p-6 md:h-full dark:border-neutral-700"
        >
          <iframe :src="`/${pasteId}/embed${settingsQuery}`" class="h-full w-full" />
        </div>
      </div>
    </div>
  </Dialog>
</template>
