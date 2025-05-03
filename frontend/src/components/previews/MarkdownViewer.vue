<script setup lang="ts">
import markdownit from 'markdown-it'
import { computed, onMounted, ref, watch } from 'vue'
import { useDebounceFn } from '@vueuse/core'

const props = defineProps<{
  markdown: string
}>()

const md = markdownit({
  html: false,
  breaks: true,
  highlight: (str, lang) => {
    const container = document.createElement('div')

    'border-neutral-300 dark:border-neutral-700 border rounded-md mb-8'.split(' ').forEach((c) => {
      container.classList.add(c.trim())
    })

    const pastefyHighlighted = document.createElement('pastefy-highlighted')

    pastefyHighlighted.setAttribute('language', `.${lang}`)
    pastefyHighlighted.setAttribute('contents', str?.trim() || '')
    pastefyHighlighted.setAttribute('show-copy-button', 'true')

    container.appendChild(pastefyHighlighted)
    return container.outerHTML
  },
})

const output = ref('')

const render = () => {
  output.value = md.render(props.markdown)
}

onMounted(() => {
  render()
})

watch(
  () => props.markdown,
  useDebounceFn(() => {
    render()
  }, 500),
)
</script>
<template>
  <div class="markdown-prev" v-html="output" />
</template>
