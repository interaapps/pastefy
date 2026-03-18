<script setup lang="ts">
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'

const source = useStorage(
  'pastefy-utility-mermaid-theme-source',
  `flowchart LR
  User([User]) --> API[API]
  API --> DB[(Database)]
  API --> Cache[(Cache)]`,
)

const theme = useStorage('pastefy-utility-mermaid-theme-name', 'base')
const fontFamily = useStorage('pastefy-utility-mermaid-theme-font', "'Plus Jakarta Sans Variable', sans-serif")
const primaryColor = useStorage('pastefy-utility-mermaid-primary-color', '#dbeafe')
const primaryTextColor = useStorage('pastefy-utility-mermaid-primary-text', '#111827')
const primaryBorderColor = useStorage('pastefy-utility-mermaid-primary-border', '#2563eb')
const lineColor = useStorage('pastefy-utility-mermaid-line-color', '#475569')
const tertiaryColor = useStorage('pastefy-utility-mermaid-tertiary-color', '#f5f3ff')
const background = useStorage('pastefy-utility-mermaid-background', '#ffffff')
const curve = useStorage('pastefy-utility-mermaid-curve', 'basis')
const nodeSpacing = useStorage('pastefy-utility-mermaid-node-spacing', 40)
const rankSpacing = useStorage('pastefy-utility-mermaid-rank-spacing', 50)
const diagramPadding = useStorage('pastefy-utility-mermaid-diagram-padding', 8)

const themeOptions = [
  { label: 'Base', value: 'base' },
  { label: 'Default', value: 'default' },
  { label: 'Dark', value: 'dark' },
  { label: 'Forest', value: 'forest' },
  { label: 'Neutral', value: 'neutral' },
]

const curveOptions = [
  { label: 'Basis', value: 'basis' },
  { label: 'Linear', value: 'linear' },
  { label: 'Step', value: 'stepBefore' },
  { label: 'Monotone', value: 'monotoneX' },
]

const themedCode = computed(() => {
  const resolvedFontFamily = fontFamily.value.trim() || 'sans-serif'
  const init = {
    theme: theme.value,
    themeVariables: {
      fontFamily: resolvedFontFamily,
      primaryColor: primaryColor.value,
      primaryTextColor: primaryTextColor.value,
      primaryBorderColor: primaryBorderColor.value,
      lineColor: lineColor.value,
      tertiaryColor: tertiaryColor.value,
      background: background.value,
    },
    flowchart: {
      curve: curve.value,
      nodeSpacing: Number(nodeSpacing.value) || 40,
      rankSpacing: Number(rankSpacing.value) || 50,
      diagramPadding: Number(diagramPadding.value) || 8,
    },
  }

  return `%%{init: ${JSON.stringify(init)}}%%\n${source.value}`
})
</script>

<template>
  <UtilityShell
    control-title="Theme"
    control-description="Tune Mermaid colors, spacing, fonts, and flowchart styling, then export the themed diagram code."
    result-title="Themed Diagram"
    result-description="Preview the themed Mermaid output and reuse the generated init directive anywhere."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Theme</label>
          <Select v-model="theme" :options="themeOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Curve</label>
          <Select v-model="curve" :options="curveOptions" option-label="label" option-value="value" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Font family</label>
          <InputText v-model="fontFamily" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Primary color</label>
          <InputText v-model="primaryColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Primary text color</label>
          <InputText v-model="primaryTextColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Border color</label>
          <InputText v-model="primaryBorderColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Line color</label>
          <InputText v-model="lineColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Tertiary color</label>
          <InputText v-model="tertiaryColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Background</label>
          <InputText v-model="background" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Node spacing</label>
          <InputNumber v-model="nodeSpacing" :min="0" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Rank spacing</label>
          <InputNumber v-model="rankSpacing" :min="0" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Padding</label>
          <InputNumber v-model="diagramPadding" :min="0" fluid />
        </div>
      </div>

      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Mermaid source</label>
        <Textarea v-model="source" auto-resize rows="16" fluid />
      </div>
    </template>

    <template #result>
      <MermaidToolResult
        :code="themedCode"
        file-name="themed-diagram.mmd"
        placeholder="Add Mermaid code to preview the themed diagram."
      />
    </template>
  </UtilityShell>
</template>
