<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import Codemirror from 'codemirror-editor-vue3'
import { computed, ref, watch } from 'vue'
import { useAsyncState, useClipboard, useStorage } from '@vueuse/core'
import { useRoute, useRouter } from 'vue-router'
import type CodeMirror from 'codemirror'
import CodeMirrorLib from 'codemirror'

import 'codemirror/addon/display/placeholder.js'
import 'codemirror/addon/edit/closebrackets.js'
import 'codemirror/addon/edit/closetag.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/edit/matchtags.js'
import 'codemirror/mode/meta.js'
import 'codemirror/addon/mode/loadmode.js'

import { client } from '@/main.ts'
import PastePreview from '@/components/PastePreview.vue'
import Highlighted from '@/components/Highlighted.vue'
import ToolFocusExitButton from '@/components/tools/ToolFocusExitButton.vue'
import ToolRelatedLinks from '@/components/tools/ToolRelatedLinks.vue'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
import type { Paste } from '@/types/paste.ts'
import codemirrorLanguageImports from '@/utils/codemirror-language-imports.ts'
import {
  conversionTools,
  findConversionTool,
  runConversion,
} from '@/utils/conversion-tools.ts'
import { findFromFileName } from '@/utils/lang-replacements.ts'
import { useSEO } from '@/composables/seo.ts'

const route = useRoute()
const router = useRouter()
const currentPaste = useCurrentPasteStore()
const clipboard = useClipboard()
const showEditor = ref(true)
const isExpanded = ref(false)

const drafts = useStorage<Record<string, string>>('pastefy-conversion-drafts', {})
const outputNames = useStorage<Record<string, string>>('pastefy-conversion-output-files', {})
const viewMode = ref<'preview' | 'code'>('code')

const tool = computed(() => findConversionTool(String(route.params.tool || '')))

useSEO({
  title: computed(() =>
    tool.value ? `${tool.value.title} • Pastefy Tools` : 'Pastefy Tools • Conversion',
  ),
  description: computed(() =>
    tool.value
      ? `${tool.value.description} Convert ${tool.value.sourceFileName} into ${tool.value.targetFileName} and share the result when you are ready.`
      : 'Use Pastefy conversion tools to transform structured data and create a paste from the result.',
  ),
})

const ensureToolState = (slug: string, nextTool: NonNullable<typeof tool.value>) => {
  if (!drafts.value[slug]) {
    drafts.value = {
      ...drafts.value,
      [slug]: nextTool.example,
    }
  }

  if (!outputNames.value[slug]) {
    outputNames.value = {
      ...outputNames.value,
      [slug]: nextTool.targetFileName,
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

const currentOutputFileName = computed({
  get() {
    if (!tool.value) return ''
    ensureToolState(tool.value.slug, tool.value)
    return outputNames.value[tool.value.slug] || tool.value.targetFileName
  },
  set(value: string) {
    if (!tool.value) return
    outputNames.value = {
      ...outputNames.value,
      [tool.value.slug]: value,
    }
  },
})

const sourceMode = ref<CodeMirror.EditorConfiguration['mode']>('markdown')

const loadSourceMode = async () => {
  if (!tool.value) return
  let extension = tool.value.sourceFileName.split('.').pop()
  if (tool.value.sourceFileName === '.env') extension = 'properties'
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
  if (!mode) mode = modeByType[tool.value.sourceType] || 'markdown'

  if (mode && codemirrorLanguageImports[mode]) {
    await codemirrorLanguageImports[mode]()
  }

  sourceMode.value = mode || 'markdown'
}

watch(tool, loadSourceMode, { immediate: true })

const sourceOptions = computed<CodeMirror.EditorConfiguration>(() => ({
  mode: sourceMode.value,
  theme: 'pastefy',
  autofocus: false,
  lineNumbers: true,
  autoCloseBrackets: true,
  autoCloseTags: true,
  matchBrackets: true,
}))

const conversionState = computed(() => {
  if (!tool.value) {
    return {
      output: '',
      error: undefined as string | undefined,
    }
  }

  try {
    return {
      output: runConversion(tool.value, currentContents.value).output,
      error: undefined,
    }
  } catch (error) {
    return {
      output: '',
      error: error instanceof Error ? error.message : 'Could not convert this input.',
    }
  }
})

const outputLang = computed(() => findFromFileName(currentOutputFileName.value || '') || tool.value?.targetType)

const resetExample = () => {
  if (!tool.value) return
  currentContents.value = tool.value.example
  currentOutputFileName.value = tool.value.targetFileName
}

const copyOutput = async () => {
  if (!conversionState.value.output) return
  await clipboard.copy(conversionState.value.output)
}

const openInPasteEditor = async () => {
  if (!tool.value || !conversionState.value.output) return

  currentPaste.clear()
  currentPaste.type = 'PASTE'
  currentPaste.title = currentOutputFileName.value.trim() || tool.value.targetFileName
  currentPaste.contents = conversionState.value.output
  currentPaste.tags = outputLang.value ? [`lang-${outputLang.value}`] : []

  await router.push({ name: 'home-page' })
}

const { execute: createPaste, isLoading: isCreatingPaste } = useAsyncState(
  async () => {
    if (!tool.value || !conversionState.value.output) return

    const res = await client.post('/api/v2/paste', {
      title: currentOutputFileName.value.trim() || tool.value.targetFileName,
      content: conversionState.value.output,
      encrypted: false,
      visibility: 'UNLISTED',
      type: 'PASTE',
      tags: outputLang.value ? [`lang-${outputLang.value}`] : [],
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

const relatedTools = computed(() =>
  conversionTools.filter((entry) => entry.slug !== tool.value?.slug).slice(0, 3),
)

const relatedToolLinks = computed(() =>
  relatedTools.value.map((entry) => ({
    slug: entry.slug,
    icon: entry.icon,
    title: entry.shortTitle,
    meta: `${entry.sourceFileName} -> ${entry.targetFileName}`,
    to: {
      name: 'tool-conversion',
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
              Conversion
            </span>
            <span class="text-sm text-neutral-500 dark:text-neutral-400">
              {{ tool.sourceFileName }} -> {{ tool.targetFileName }}
            </span>
          </div>

          <div class="flex flex-col gap-2">
            <h1 class="text-3xl leading-tight font-bold md:text-4xl">{{ tool.title }}</h1>
            <p class="max-w-[70ch] text-base text-neutral-600 dark:text-neutral-300">
              {{ tool.description }}
            </p>
          </div>
        </div>

        <div class="flex flex-wrap gap-2">
          <Button
            @click="isExpanded = !isExpanded"
            :label="isExpanded ? 'exit focus mode' : 'focus mode'"
            :icon="`ti ${isExpanded ? 'ti-minimize' : 'ti-maximize'}`"
            severity="contrast"
            outlined
          />
          <Button
            as="router-link"
            :to="{ name: 'tool-home', hash: '#conversions' }"
            label="all conversions"
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
            <h2 class="font-semibold">Source</h2>
            <p class="text-sm text-neutral-500 dark:text-neutral-400">
              Paste or edit the original content you want to transform.
            </p>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-sm text-neutral-500 dark:text-neutral-400">{{ tool.sourceFileName }}</span>
            <Button
              @click="showEditor = false"
              icon="ti ti-layout-sidebar-left-collapse"
              severity="contrast"
              text
              aria-label="Hide editor"
            />
          </div>
        </div>

        <div class="min-h-0 flex-1 overflow-hidden">
          <Codemirror
            v-model:value="currentContents"
            :options="sourceOptions"
            height="100%"
            width="100%"
            placeholder="Paste your source content here..."
          />
        </div>
      </div>

      <div
        class="flex flex-col overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
        :class="isExpanded ? 'min-h-0 h-full' : 'min-h-[42rem]'"
      >
        <div class="flex flex-wrap items-center justify-between gap-3 border-b border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <div>
            <h2 class="font-semibold">Output</h2>
            <p class="text-sm text-neutral-500 dark:text-neutral-400">
              Converted result with preview and quick actions.
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
            <Button
              label="preview"
              size="small"
              :severity="viewMode === 'preview' ? 'contrast' : 'secondary'"
              @click="viewMode = 'preview'"
            />
            <Button
              label="code"
              size="small"
              :severity="viewMode === 'code' ? 'contrast' : 'secondary'"
              @click="viewMode = 'code'"
            />
          </div>
        </div>

        <div class="border-b border-neutral-200 p-4 dark:border-neutral-700">
          <InputText v-model="currentOutputFileName" placeholder="Output filename" fluid />
        </div>

        <div class="min-h-0 flex-1 overflow-auto">
          <Message v-if="conversionState.error" severity="error" class="mb-4">
            {{ conversionState.error }}
          </Message>

          <template v-else>
            <div v-if="viewMode === 'preview'" class="overflow-hidden">
              <PastePreview
                :contents="conversionState.output"
                :file-name="currentOutputFileName"
                :type="tool.targetType"
                in-editor
              />
            </div>

            <div v-else class="overflow-hidden">
              <Highlighted
                :contents="conversionState.output"
                :file-name="currentOutputFileName"
              />
            </div>
          </template>
        </div>

        <div class="flex flex-wrap items-center justify-between gap-3 border-t border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900">
          <div class="flex flex-wrap gap-2">
            <Button
              @click="copyOutput"
              :disabled="!conversionState.output"
              label="copy output"
              icon="ti ti-copy"
              severity="contrast"
              outlined
            />
            <Button
              @click="openInPasteEditor"
              :disabled="!conversionState.output"
              label="open in paste editor"
              icon="ti ti-edit"
              severity="contrast"
              outlined
            />
          </div>

          <Button
            @click="createPaste()"
            :disabled="!conversionState.output"
            :loading="isCreatingPaste"
            label="create paste"
            icon="ti ti-send"
            severity="contrast"
          />
        </div>
      </div>
    </div>

    <ToolRelatedLinks v-if="!isExpanded" :items="relatedToolLinks" />
  </section>

  <section v-else class="mx-auto flex max-w-[50rem] flex-col gap-4 rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800">
    <h1 class="text-2xl font-bold">Conversion tool not found</h1>
    <p class="text-neutral-600 dark:text-neutral-300">
      This conversion tool does not exist. Open the tools index to pick one of the supported
      converters.
    </p>
    <div>
      <Button as="router-link" :to="{ name: 'tool-home', hash: '#conversions' }" label="back to conversions" />
    </div>
  </section>
</template>
