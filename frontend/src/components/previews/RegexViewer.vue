<template>
  <div>
    <Highlighted
      :contents="regex"
      lang="regex"
      class="border-b-1 border-neutral-200 dark:border-neutral-700"
    />
    <div class="flex w-full flex-col gap-5 p-5">
      <div class="flex flex-col gap-3">
        <div class="flex items-center justify-between gap-2">
          <label class="text-lg font-semibold text-neutral-700 dark:text-neutral-300">
            Test Input
          </label>
          <InputText v-model="flags" placeholder="z. B. g, i, m" size="small" class="w-[5rem]" />
        </div>
        <div class="relative">
          <div
            class="pointer-events-none absolute h-full w-full px-(--p-textarea-padding-x) py-(--p-textarea-padding-y) whitespace-pre"
            v-html="highlightedResult || '&nbsp;'"
          />
          <Textarea
            v-model="testInput"
            fluid
            placeholder="insert your test string here"
            class="relative z-10 bg-transparent text-transparent caret-neutral-500"
            style="font-size: 1rem"
            auto-resize
          />
        </div>
      </div>

      <p v-if="error" class="mono mt-2 text-sm text-red-500">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Highlighted from '@/components/Highlighted.vue'

const props = defineProps<{
  regex: string
  inEditor?: boolean
}>()

const flags = ref('g')
const testInput = ref('')

const error = computed(() => {
  try {
    const re = new RegExp(props.regex, flags.value)
    testInput.value.replace(re, '')
    return null
  } catch (e: unknown) {
    return (e as { message: string }).message || e
  }
})
const highlightedResult = computed(() => {
  const re = new RegExp(props.regex, flags.value)
  if (!testInput.value) return ''

  return testInput.value
    .replace(
      /[&<>"']/g,
      (s) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[s]!,
    )
    .replace(re, (match) => `<mark class="rounded-sm bg-primary-100">${match}</mark>`)
})
</script>
