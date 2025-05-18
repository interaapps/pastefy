<script setup lang="ts">
import DOMPurify from 'dompurify'
import { onMounted, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

const props = defineProps<{
  svg: string
}>()

const src = ref('')

const render = async () => {
  const pure = DOMPurify.sanitize(props.svg, {
    USE_PROFILES: { svg: true },
  })
  const blob = new Blob([pure], { type: 'image/svg+xml' })
  const url = URL.createObjectURL(blob)
  src.value = url
}
onMounted(() => {
  render()
})
watch(
  () => props.svg,
  useDebounceFn(() => {
    render()
  }, 500),
)
</script>
<template>
  <div class="mermaid-preview-group relative w-full overflow-auto p-5 text-sm">
    <img alt="Preview of the file" :src />
  </div>
</template>
