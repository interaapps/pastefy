<script setup lang="ts">
import mermaid from 'mermaid'
import Button from 'primevue/button'
import { onMounted, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'
import { useConfig } from '@/composables/config.ts'

const props = defineProps<{
  mermaidCode: string
}>()

const config = useConfig()

const getBaseConfig = () => ({
  startOnLoad: false,
  securityLevel: 'strict' as const,
  // Keep Mermaid's protected defaults and prevent diagrams from changing the sanitizer.
  secure: [...(mermaid.mermaidAPI.defaultConfig.secure ?? []), 'dompurifyConfig'],
  fontFamily: "'Plus Jakarta Sans Variable', sans-serif",
  theme: (config.value.theme === 'dark'
    ? 'dark'
    : config.value.theme === 'system'
      ? window.matchMedia('(prefers-color-scheme: dark)').matches
        ? 'dark'
        : 'default'
      : 'default') as 'dark' | 'default',
})

mermaid.initialize(getBaseConfig())

const mermaidHTML = ref('Loading')
const mermaidError = ref('')

const render = async () => {
  mermaidHTML.value = ''
  mermaidError.value = ''

  try {
    mermaid.mermaidAPI.reset()
    mermaid.initialize(getBaseConfig())

    // Let Mermaid handle untrusted directives through its own security checks.
    const out = await mermaid.render(
      `mermaid-preview-${Math.random().toString().replace('.', '')}`,
      props.mermaidCode,
    )
    mermaidHTML.value = out.svg
  } catch (error) {
    mermaidError.value = (error as Error)?.message || 'Could not render this Mermaid diagram.'
  } finally {
    mermaid.mermaidAPI.reset()
    mermaid.initialize(getBaseConfig())
  }
}
onMounted(() => {
  render()
})
watch(
  () => props.mermaidCode,
  useDebounceFn(() => {
    render()
  }, 500),
)

const zoom = ref(1)
</script>
<template>
  <div class="mermaid-preview-group relative overflow-auto p-5 text-sm">
    <div
      v-if="mermaidError"
      class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700 dark:border-red-900/70 dark:bg-red-950/40 dark:text-red-200"
    >
      {{ mermaidError }}
    </div>
    <div v-else v-html="mermaidHTML" class="flex justify-center overflow-auto" :style="{ zoom }" />

    <div class="mermaid-preview-group-show absolute right-5 bottom-5 flex gap-1">
      <Button icon="ti ti-minus" size="small" severity="secondary" outlined @click="zoom -= 0.1" />
      <Button icon="ti ti-plus" size="small" severity="secondary" outlined @click="zoom += 0.1" />
    </div>
  </div>
</template>

<style>
.mermaid-preview-group {
  .mermaid-preview-group-show {
    transition: 0.3s;
    opacity: 0;
  }
  &:hover {
    .mermaid-preview-group-show {
      opacity: 1;
    }
  }
}
</style>
