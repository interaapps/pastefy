<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Button from 'primevue/button'
import Message from 'primevue/message'
import Codemirror from 'codemirror-editor-vue3'
import { computed, ref, watch } from 'vue'
import { useAsyncState, useClipboard, useStorage } from '@vueuse/core'
import { useRoute, useRouter } from 'vue-router'
import type CodeMirror from 'codemirror'
import CodeMirrorLib from 'codemirror'
import emmet from '@emmetio/codemirror-plugin'

import 'codemirror/addon/display/placeholder.js'
import 'codemirror/addon/edit/closebrackets.js'
import 'codemirror/addon/edit/closetag.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/edit/matchtags.js'
import 'codemirror/mode/meta.js'
import 'codemirror/addon/mode/loadmode.js'

import { client } from '@/main.ts'
import PastePreview from '@/components/PastePreview.vue'
import ToolFocusExitButton from '@/components/tools/ToolFocusExitButton.vue'
import ToolPinButton from '@/components/tools/ToolPinButton.vue'
import ToolRelatedLinks from '@/components/tools/ToolRelatedLinks.vue'
import { findPreviewTool, previewTools } from '@/utils/preview-tools.ts'
import codemirrorLanguageImports from '@/utils/codemirror-language-imports.ts'
import { findFromFileName } from '@/utils/lang-replacements.ts'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
import type { Paste, PasteVisibility } from '@/types/paste.ts'
import { useSEO } from '@/composables/seo.ts'

emmet(CodeMirrorLib)

const route = useRoute()
const router = useRouter()
const currentPaste = useCurrentPasteStore()
const clipboard = useClipboard()
const showEditor = ref(true)
const isExpanded = ref(false)

const drafts = useStorage<Record<string, string>>('pastefy-tool-drafts', {})
const fileNames = useStorage<Record<string, string>>('pastefy-tool-file-names', {})
const visibilityStates = useStorage<Record<string, PasteVisibility>>('pastefy-tool-visibilities', {})

const tool = computed(() => findPreviewTool(String(route.params.tool || '')))

useSEO({
  title: computed(() =>
    tool.value ? `${tool.value.title} • Pastefy Tools` : 'Pastefy Tools • Preview Lab',
  ),
  description: computed(() =>
    tool.value
      ? `${tool.value.description} Edit ${tool.value.fileName} live and preview the result before creating a paste.`
      : 'Open a Pastefy preview tool to edit and inspect structured content before sharing it.',
  ),
})

const visibilityOptions = [
  {
    label: 'Unlisted',
    value: 'UNLISTED',
  },
  {
    label: 'Public',
    value: 'PUBLIC',
  },
  {
    label: 'Private',
    value: 'PRIVATE',
  },
]

const ensureToolState = (slug: string, nextTool: NonNullable<typeof tool.value>) => {
  if (!drafts.value[slug]) {
    drafts.value = {
      ...drafts.value,
      [slug]: nextTool.example,
    }
  }

  if (!fileNames.value[slug]) {
    fileNames.value = {
      ...fileNames.value,
      [slug]: nextTool.fileName,
    }
  }

  if (!visibilityStates.value[slug]) {
    visibilityStates.value = {
      ...visibilityStates.value,
      [slug]: 'UNLISTED',
    }
  }
}

watch(
  tool,
  (nextTool) => {
    if (!nextTool) return
    ensureToolState(nextTool.slug, nextTool)
  },
  { immediate: true },
)

const currentContents = computed({
  get() {
    if (!tool.value) return ''
    ensureToolState(tool.value.slug, tool.value)
    return drafts.value[tool.value.slug] || tool.value.example
  },
  set(value: string) {
    if (!tool.value) return
    drafts.value = {
      ...drafts.value,
      [tool.value.slug]: value,
    }
  },
})

const currentFileName = computed({
  get() {
    if (!tool.value) return ''
    ensureToolState(tool.value.slug, tool.value)
    return fileNames.value[tool.value.slug] || tool.value.fileName
  },
  set(value: string) {
    if (!tool.value) return
    fileNames.value = {
      ...fileNames.value,
      [tool.value.slug]: value,
    }
  },
})

const currentVisibility = computed<PasteVisibility>({
  get() {
    if (!tool.value) return 'UNLISTED'
    ensureToolState(tool.value.slug, tool.value)
    return visibilityStates.value[tool.value.slug] || 'UNLISTED'
  },
  set(value) {
    if (!tool.value) return
    visibilityStates.value = {
      ...visibilityStates.value,
      [tool.value.slug]: value,
    }
  },
})

const cmOptions = ref<CodeMirror.EditorConfiguration>({
  mode: 'markdown',
  theme: 'pastefy',
  autofocus: false,
  lineNumbers: true,
  autoCloseBrackets: true,
  autoCloseTags: true,
  matchBrackets: true,
})

const previewType = computed(() => tool.value?.type || 'markdown')
const normalizedFileName = computed(
  () => currentFileName.value.trim() || tool.value?.fileName || 'snippet.txt',
)

const editorStats = computed(() => {
  const source = currentContents.value
  return {
    characters: source.length,
    lines: source === '' ? 0 : source.split('\n').length,
  }
})

const inferredTag = computed(() => {
  const lang = findFromFileName(normalizedFileName.value)
  return lang ? `lang-${lang}` : undefined
})

const loadEditorMode = async () => {
  if (!tool.value) return

  let extension = normalizedFileName.value.split('.').pop()
  if (normalizedFileName.value === '.env') extension = 'properties'
  if (extension === 'geojson') extension = 'json'
  if (extension === 'md') extension = 'markdown'
  if (extension === 'htm') extension = 'html'

  const modeByType: Record<string, string> = {
    markdown: 'markdown',
    json: 'javascript',
    html: 'htmlmixed',
    xml: 'xml',
    yaml: 'yaml',
    csv: 'spreadsheet',
    diff: 'diff',
    http: 'http',
    properties: 'properties',
    log: 'shell',
  }

  let mode = extension ? CodeMirrorLib.findModeByExtension(extension)?.mode : undefined
  if (!mode) mode = modeByType[previewType.value] || 'markdown'

  if (mode && codemirrorLanguageImports[mode]) {
    await codemirrorLanguageImports[mode]()
  }

  cmOptions.value.mode = mode || 'markdown'
}

watch([normalizedFileName, previewType], loadEditorMode, { immediate: true })

const copySource = async () => {
  await clipboard.copy(currentContents.value)
}

const resetExample = () => {
  if (!tool.value) return
  currentContents.value = tool.value.example
  currentFileName.value = tool.value.fileName
}

const openInPasteEditor = async () => {
  if (!tool.value) return

  currentPaste.clear()
  currentPaste.type = 'PASTE'
  currentPaste.title = normalizedFileName.value
  currentPaste.contents = currentContents.value
  currentPaste.visibility = currentVisibility.value
  currentPaste.tags = inferredTag.value ? [inferredTag.value] : []

  await router.push({ name: 'home-page' })
}

const { execute: createPaste, isLoading: isCreatingPaste } = useAsyncState(
  async () => {
    if (!tool.value || !currentContents.value.trim()) return

    const res = await client.post('/api/v2/paste', {
      title: normalizedFileName.value,
      content: currentContents.value,
      encrypted: false,
      visibility: currentVisibility.value,
      type: 'PASTE',
      tags: inferredTag.value ? [inferredTag.value] : [],
      ai: true,
    })

    const createdPaste = res.data.paste as Paste
    await router.push({
      name: 'paste',
      params: {
        paste: createdPaste.id,
      },
    })
  },
  undefined,
  {
    immediate: false,
  },
)

const relatedTools = computed(() => {
  if (!tool.value) return previewTools.slice(0, 3)
  return previewTools.filter((entry) => entry.slug !== tool.value?.slug).slice(0, 3)
})

const relatedToolLinks = computed(() =>
  relatedTools.value.map((entry) => ({
    slug: entry.slug,
    icon: entry.icon,
    title: entry.shortTitle,
    meta: entry.fileName,
    to: {
      name: 'tool-preview',
      params: {
        tool: entry.slug,
      },
    },
  })),
)
</script>

<template>
  <section
    v-if="tool"
    class="mx-auto flex max-w-[1400px] flex-col gap-6 bg-white dark:bg-[#121212]"
    :class="isExpanded ? 'fixed inset-0 z-[120] max-w-none h-[100dvh] overflow-hidden p-4 md:p-6' : ''"
  >
    <ToolFocusExitButton :visible="isExpanded" @close="isExpanded = false" />

    <div
      v-if="!isExpanded"
      class="overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800 md:p-8"
    >
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div class="flex max-w-[55rem] flex-col gap-3">
          <div class="flex items-center gap-3">
            <span
              class="inline-flex items-center gap-2 rounded-md border border-neutral-200 bg-white px-3 py-1 text-sm text-neutral-700 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
            >
              <i :class="`ti ti-${tool.icon}`" />
              {{ tool.category }}
            </span>
            <span class="text-sm text-neutral-500 dark:text-neutral-400">
              {{ tool.fileName }}
            </span>
          </div>

          <div class="flex flex-col gap-2">
            <h1 class="text-3xl leading-tight font-bold md:text-4xl">{{ tool.title }}</h1>
            <p class="max-w-[70ch] text-base text-neutral-600 dark:text-neutral-300">
              {{ tool.description }}
            </p>
          </div>
        </div>

        <div class="flex flex-wrap items-center justify-end gap-2">
          <Button
            as="router-link"
            :to="{ name: 'tool-home' }"
            label="all tools"
            icon="ti ti-layout-grid"
            severity="contrast"
            outlined
          />
          <Button
            @click="resetExample"
            label="reset example"
            icon="ti ti-restore"
            severity="contrast"
            outlined
          />
          <ToolPinButton kind="preview" :slug="tool.slug" :label="false" />
          <Button
            @click="isExpanded = !isExpanded"
            :icon="`ti ${isExpanded ? 'ti-minimize' : 'ti-maximize'}`"
            severity="contrast"
            outlined
            :aria-label="isExpanded ? 'Exit focus mode' : 'Focus mode'"
          />
        </div>
      </div>
    </div>

    <div class="grid gap-5" :class="[showEditor ? 'xl:grid-cols-[1.02fr_0.98fr]' : 'grid-cols-1', isExpanded ? 'min-h-0 flex-1' : '']">
      <div
        v-if="showEditor"
        class="flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
        :class="isExpanded ? 'min-h-0 h-full' : 'min-h-[42rem]'"
      >
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <div>
            <h2 class="font-semibold">Editor</h2>
            <p class="text-sm text-neutral-500 dark:text-neutral-400">
              Tweak the source, keep the preview live, and publish only when you want.
            </p>
          </div>

          <div class="flex items-center gap-2 text-sm text-neutral-500 dark:text-neutral-400">
            <span>{{ editorStats.lines }} lines</span>
            <span class="opacity-40">•</span>
            <span>{{ editorStats.characters }} chars</span>
            <Button
              @click="showEditor = false"
              icon="ti ti-layout-sidebar-left-collapse"
              severity="contrast"
              text
              aria-label="Hide editor"
            />
          </div>
        </div>

        <div class="grid gap-3 border-b border-neutral-200 p-4 dark:border-neutral-700 md:grid-cols-[1fr_12rem]">
          <InputText v-model="currentFileName" placeholder="Filename" fluid />
          <Select
            v-model="currentVisibility"
            :options="visibilityOptions"
            option-label="label"
            option-value="value"
            placeholder="Visibility"
          />
        </div>

        <div class="min-h-0 flex-1 overflow-hidden">
          <Codemirror
            v-model:value="currentContents"
            :options="cmOptions"
            height="100%"
            width="100%"
            placeholder="Start typing to see the preview..."
          />
        </div>

        <div class="flex flex-wrap items-center justify-between gap-3 border-t border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <div class="flex flex-wrap gap-2">
            <Button
              @click="copySource"
              label="copy source"
              icon="ti ti-copy"
              severity="contrast"
              outlined
            />
            <Button
              @click="openInPasteEditor"
              label="open in paste editor"
              icon="ti ti-edit"
              severity="contrast"
              outlined
            />
          </div>

          <Button
            @click="createPaste()"
            :disabled="!currentContents.trim()"
            :loading="isCreatingPaste"
            label="create paste"
            icon="ti ti-send"
            severity="contrast"
          />
        </div>
      </div>

      <div
        class="flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
        :class="isExpanded ? 'min-h-0 h-full' : 'min-h-[42rem]'"
      >
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <div>
            <h2 class="font-semibold">Preview</h2>
            <p class="text-sm text-neutral-500 dark:text-neutral-400">
              This uses the same preview components as the main Pastefy paste views.
            </p>
          </div>

          <div class="flex flex-wrap gap-2">
            <Button
              v-if="!showEditor"
              @click="showEditor = true"
              icon="ti ti-layout-sidebar-left-expand"
              severity="contrast"
              text
              aria-label="Show editor"
            />
            <span
              v-for="keyword of tool.keywords.slice(0, 2)"
              :key="keyword"
              class="rounded-full bg-neutral-100 px-2.5 py-1 text-xs text-neutral-500 dark:bg-neutral-800 dark:text-neutral-300"
            >
              {{ keyword }}
            </span>
          </div>
        </div>

        <div class="min-h-0 flex-1 overflow-auto p-4">
          <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
            <PastePreview
              :contents="currentContents"
              :file-name="normalizedFileName"
              :type="previewType"
              in-editor
            />
          </div>
        </div>

        <div class="border-t border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <Message severity="secondary" size="small" variant="simple">
            Nothing gets published from this page until you press
            <strong>create paste</strong>.
          </Message>
        </div>
      </div>
    </div>

    <ToolRelatedLinks v-if="!isExpanded" :items="relatedToolLinks" />
  </section>

  <section v-else class="mx-auto flex max-w-[50rem] flex-col gap-4 rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800">
    <h1 class="text-2xl font-bold">Tool not found</h1>
    <p class="text-neutral-600 dark:text-neutral-300">
      This preview tool does not exist. Open the tools index to pick one of the supported preview
      pages.
    </p>
    <div>
      <Button as="router-link" :to="{ name: 'tool-home' }" label="back to tools" />
    </div>
  </section>
</template>
