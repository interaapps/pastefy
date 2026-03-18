<script setup lang="ts">
import domtoimage from 'dom-to-image'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed, ref, useTemplateRef } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidViewer from '@/components/previews/MermaidViewer.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'

const source = useStorage(
  'pastefy-utility-mermaid-export-source',
  `flowchart LR
  Client --> API
  API --> DB
  API --> Cache`,
)
const fileName = useStorage('pastefy-utility-mermaid-export-file-name', 'diagram')
const format = useStorage<'png' | 'svg'>('pastefy-utility-mermaid-export-format', 'png')
const scale = useStorage<number>('pastefy-utility-mermaid-export-scale', 2)
const padding = useStorage<number>('pastefy-utility-mermaid-export-padding', 24)
const background = useStorage('pastefy-utility-mermaid-export-background', '#ffffff')
const watermark = useStorage('pastefy-utility-mermaid-export-watermark', 'Created with Pastefy')
const exportAreaRef = useTemplateRef<HTMLDivElement>('exportAreaRef')
const isExporting = ref(false)

const formatOptions = [
  { label: 'PNG', value: 'png' as const },
  { label: 'SVG', value: 'svg' as const },
]

const download = async () => {
  const node = exportAreaRef.value
  if (!node) return
  isExporting.value = true
  try {
    const safeName = (fileName.value || 'diagram').replace(/[^a-zA-Z0-9-_]+/g, '-')
    if (format.value === 'svg') {
      const dataUrl = await (domtoimage as any).toSvg(node, {
        bgcolor: background.value,
        style: {
          transform: `scale(${scale.value})`,
          transformOrigin: 'top left',
          width: `${node.offsetWidth}px`,
          height: `${node.offsetHeight}px`,
        },
        width: node.offsetWidth * scale.value,
        height: node.offsetHeight * scale.value,
      })
      const link = document.createElement('a')
      link.download = `${safeName}.svg`
      link.href = dataUrl
      link.click()
      return
    }

    const blob = await domtoimage.toBlob(node, {
      bgcolor: background.value,
      style: {
        transform: `scale(${scale.value})`,
        transformOrigin: 'top left',
        width: `${node.offsetWidth}px`,
        height: `${node.offsetHeight}px`,
      },
      width: node.offsetWidth * scale.value,
      height: node.offsetHeight * scale.value,
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.download = `${safeName}.png`
    link.href = url
    link.click()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
  } finally {
    isExporting.value = false
  }
}

const outputCode = computed(() => source.value)

const copyMermaid = async () => {
  await navigator.clipboard?.writeText(outputCode.value)
}
</script>

<template>
  <UtilityShell
    control-title="Export Setup"
    control-description="Render a Mermaid diagram and export it as PNG or SVG with padding, background, and watermark options."
    result-title="Export Preview"
    result-description="Preview the export surface exactly as it will be rendered."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Filename</label>
          <InputText v-model="fileName" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Format</label>
          <Select
            v-model="format"
            :options="formatOptions"
            option-label="label"
            option-value="value"
            fluid
          />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Scale</label>
          <InputNumber v-model="scale" :min="1" :max="4" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Padding</label>
          <InputNumber v-model="padding" :min="0" :max="120" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400"
            >Background</label
          >
          <InputText v-model="background" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Watermark</label>
          <InputText v-model="watermark" fluid />
        </div>
      </div>
      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400"
          >Mermaid code</label
        >
        <Textarea v-model="source" auto-resize rows="18" fluid />
      </div>
    </template>
    <template #result>
      <div
        ref="exportAreaRef"
        class="overflow-hidden rounded-xl border border-neutral-200 dark:border-neutral-700"
        :style="{ padding: `${padding}px`, backgroundColor: background }"
      >
        <MermaidViewer :mermaid-code="source" />
        <div v-if="watermark" class="px-5 pb-2 text-right text-xs font-medium opacity-50">
          {{ watermark }}
        </div>
      </div>
    </template>
    <template #footer>
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-t border-neutral-200 bg-white px-4 py-3 dark:border-neutral-700 dark:bg-neutral-900"
      >
        <div class="text-sm text-neutral-500 dark:text-neutral-400">
          PNG and SVG export use the current preview surface including padding and watermark.
        </div>
        <div class="flex flex-wrap gap-2">
          <Button
            @click="download"
            :loading="isExporting"
            :label="`download ${format}`"
            icon="ti ti-download"
            severity="contrast"
          />
          <Button
            @click="copyMermaid"
            label="copy mermaid"
            icon="ti ti-copy"
            severity="contrast"
            outlined
          />
        </div>
      </div>
    </template>
  </UtilityShell>
</template>
