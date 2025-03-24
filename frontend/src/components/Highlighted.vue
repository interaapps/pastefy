<script setup lang="ts">
import hljs from 'highlight.js'
import { onMounted, ref } from 'vue'
import { findFromFileName } from '@/utils/lang-replacements.ts'

const props = defineProps<{
  contents: string
  fileName?: string
  lang?: string
  hideDivider?: boolean
  hideLineNumbering?: boolean
  hideColorPreview?: boolean
}>()

const contents = ref(props.contents)

const id = Math.random().toString(36).substring(7)

onMounted(async () => {
  const lang = findFromFileName(props.fileName || props.lang || '')

  if (!props.contents) {
    contents.value = ''
    return
  }

  if (props.contents.length < 15000) {
    if (lang && hljs.listLanguages().includes(lang)) {
      contents.value = hljs.highlight(props.contents, {
        language: lang,
      }).value
    } else {
      contents.value = hljs.highlightAuto(props.contents).value
    }
  }

  setTimeout(() => {
    document
      .querySelectorAll(`.higlighted-${id} .hljs-number, .higlighted-${id} .hljs-string`)
      .forEach((el) => {
        let val = el.textContent?.trim() || ''

        if (val.startsWith('"') && val.endsWith('"')) {
          val = val.substring(1, val.length - 1)
        }

        if (
          !props.hideColorPreview &&
          ((val.startsWith('#') &&
            (val.length === 7 || val.length === 9 || val.length === 4 || val.length === 5)) ||
            (val.startsWith('rgb') &&
              /rgb(a?)\(\s?[0-9]{1,3}\s?,\s?[0-9]{1,3}\s?,\s?[0-9]{1,3}\s?(,\s?[0-9]{1,3}\s?)?\)/.test(
                val,
              )))
        ) {
          const colorPreview = document.createElement('span')

          ;[
            'h-[0.75rem]',
            'w-[0.75rem]',
            'rounded-sm',
            'inline-block',
            'mr-0.5',
            'ml-0.5',
            'border-1',
            'border-neutral-200',
            'dark:border-neutral-700',
          ].forEach((c) => {
            colorPreview.classList.add(c)
          })

          colorPreview.style.backgroundColor = val
          el.prepend(colorPreview)
        }
      })
  }, 500)
})
</script>
<template>
  <div class="highlighted flex w-full overflow-auto text-sm" :class="`higlighted-${id}`">
    <div
      v-if="!hideLineNumbering"
      class="p-3 text-right select-none"
      :class="hideDivider ? ' ' : 'border-r-1 border-neutral-200 dark:border-neutral-700'"
    >
      <span
        class="block opacity-30"
        v-for="(_, line) of contents?.split('\n') || []"
        :key="line + 1"
        >{{ line + 1 }}</span
      >
    </div>
    <code class="w-full p-3">
      <pre v-html="contents" />
    </code>
  </div>
</template>
