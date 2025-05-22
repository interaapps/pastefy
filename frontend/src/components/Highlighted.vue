<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { findFromFileName } from '@/utils/lang-replacements.ts'
import { highlight } from '@/utils/highlight.ts'
import '@/utils/highlight-imports.ts'
import highlightWorker from '@/utils/workers/highlight.worker.ts?worker'
import CopyButton from '@/components/CopyButton.vue'

const props = defineProps<{
  contents: string
  fileName?: string
  lang?: string
  hideDivider?: boolean
  hideLineNumbering?: boolean
  hideColorPreview?: boolean
  showCopyButton?: boolean
  getCopyContents?: () => Promise<string>
}>()

const highlightedContents = ref<string | undefined>(undefined)

const id = Math.random().toString(36).substring(7)

const addColorIndicators = () => {
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
}

const initHighlight = () => {
  const lang = findFromFileName(props.fileName || props.lang || '')

  if (!props.contents) {
    highlightedContents.value = ''
    return
  }

  if ('Worker' in window && props.contents.length > 10000) {
    const worker = new highlightWorker()

    worker.onmessage = (e) => {
      const { highlighted } = e.data
      highlightedContents.value = highlighted
      worker.terminate()
      addColorIndicators()
    }

    worker.postMessage({
      contents: props.contents,
      lang,
    })
  } else {
    highlightedContents.value = highlight(props.contents, lang)
    addColorIndicators()
  }
}

watch(
  () => props.contents,
  () => {
    initHighlight()
  },
)

onMounted(async () => {
  initHighlight()
})
</script>
<template>
  <div
    class="highlighted highlighted-group relative flex w-full overflow-auto text-sm"
    :class="`higlighted-${id}`"
  >
    <CopyButton
      v-if="showCopyButton"
      :contents
      class="highlighted-group-show absolute top-2 right-2 opacity-0 transition-all"
      :getCopyContents
    />
    <div
      v-if="!hideLineNumbering"
      class="p-3 text-right select-none"
      :class="hideDivider ? ' ' : 'border-r-1 border-neutral-200 dark:border-neutral-700'"
    >
      <span
        class="block opacity-30"
        v-for="(_, line) of (highlightedContents !== undefined
          ? highlightedContents
          : contents
        )?.split('\n') || []"
        :key="line + 1"
        >{{ line + 1 }}</span
      >
    </div>
    <code class="w-full p-3">
      <pre v-if="highlightedContents !== undefined" v-html="highlightedContents" />
      <pre v-else>{{ contents }}</pre>
    </code>
  </div>
</template>
<style>
.highlighted-group {
  .highlighted-group-show {
    opacity: 0;
  }
  &:hover {
    .highlighted-group-show {
      opacity: 1;
    }
  }
}
</style>
