<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'

const { t } = useTranslation()
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
  { get label() { return t('utility.mermaidThemeBuilderTool.options.base') }, value: 'base' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.default') }, value: 'default' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.dark') }, value: 'dark' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.forest') }, value: 'forest' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.neutral') }, value: 'neutral' },
]

const curveOptions = [
  { get label() { return t('utility.mermaidThemeBuilderTool.options.basis') }, value: 'basis' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.linear') }, value: 'linear' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.step') }, value: 'stepBefore' },
  { get label() { return t('utility.mermaidThemeBuilderTool.options.monotone') }, value: 'monotoneX' },
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
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('settings.theme') }}</label>
          <Select v-model="theme" :options="themeOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.curve') }}</label>
          <Select v-model="curve" :options="curveOptions" option-label="label" option-value="value" fluid />
        </div>
        <div class="md:col-span-2">
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.fontFamily') }}</label>
          <InputText v-model="fontFamily" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.primaryColor') }}</label>
          <InputText v-model="primaryColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.primaryTextColor') }}</label>
          <InputText v-model="primaryTextColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.borderColor') }}</label>
          <InputText v-model="primaryBorderColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.lineColor') }}</label>
          <InputText v-model="lineColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.tertiaryColor') }}</label>
          <InputText v-model="tertiaryColor" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.background') }}</label>
          <InputText v-model="background" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.nodeSpacing') }}</label>
          <InputNumber v-model="nodeSpacing" :min="0" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.rankSpacing') }}</label>
          <InputNumber v-model="rankSpacing" :min="0" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.padding') }}</label>
          <InputNumber v-model="diagramPadding" :min="0" fluid />
        </div>
      </div>

      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">{{ $t('utility.mermaidThemeBuilderTool.mermaidSource') }}</label>
        <Textarea v-model="source" auto-resize rows="16" fluid />
      </div>
    </template>

    <template #result>
      <MermaidToolResult
        :code="themedCode"
        file-name="themed-diagram.mmd"
        :placeholder="$t('utility.mermaidThemeBuilderTool.addMermaidCodeToPreviewTheThemedDiagram')"
      />
    </template>
  </UtilityShell>
</template>
