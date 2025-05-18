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

mermaid.initialize({
  startOnLoad: false,
  fontFamily: "'Plus Jakarta Sans Variable', sans-serif",
  theme:
    config.value.theme === 'dark'
      ? 'dark'
      : config.value.theme === 'system'
        ? window.matchMedia('(prefers-color-scheme: dark)').matches
          ? 'dark'
          : 'default'
        : 'default',
})

const mermaidHTML = ref('Loading')

const render = async () => {
  mermaidHTML.value = ''
  const out = await mermaid.render('mermaid-preview', props.mermaidCode)
  mermaidHTML.value = out.svg
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
    <div v-html="mermaidHTML" class="flex justify-center overflow-auto" :style="{ zoom }" />

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
