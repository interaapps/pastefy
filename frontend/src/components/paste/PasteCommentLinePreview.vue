<script setup lang="ts">
import Button from 'primevue/button'
import { computed, ref } from 'vue'
import Highlighted from '@/components/Highlighted.vue'

const props = defineProps<{
  contents: string
  fileName?: string
  lineFrom: number
  lineTo?: number
}>()

const expanded = ref(false)
const previewLines = computed(() =>
  props.contents.split('\n').slice(props.lineFrom - 1, props.lineTo || props.lineFrom),
)
const hasMore = computed(() => previewLines.value.length > 3)
const inlineContents = computed(() =>
  (expanded.value ? previewLines.value : previewLines.value.slice(0, 3)).join('\n'),
)
</script>

<template>
  <div class="mt-2 overflow-hidden rounded-lg bg-neutral-100 dark:bg-neutral-800">
    <Highlighted
      :contents="inlineContents"
      :file-name
      :starting-line-number="lineFrom - 1"
      hide-color-preview
      small
      show-copy-button
    />
    <Button
      v-if="hasMore"
      :label="$t(expanded ? 'comments.showLess' : 'comments.showMore')"
      size="small"
      severity="contrast"
      text
      class="w-full rounded-none border-t border-neutral-200 dark:border-neutral-700"
      @click="expanded = !expanded"
    />
  </div>
</template>
