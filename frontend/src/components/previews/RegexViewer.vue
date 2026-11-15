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
            {{ $t('previews.regexViewer.testInput') }}
          </label>
          <InputText v-model="flags" :placeholder="$t('previews.regexViewer.flags')" size="small" class="w-[5rem]" />
        </div>
        <div class="relative">
          <div
            class="pointer-events-none absolute h-full w-full px-(--p-textarea-padding-x) py-(--p-textarea-padding-y) whitespace-pre-wrap"
            v-html="highlightedResult || '&nbsp;'"
          />
          <Textarea
            v-model="testInput"
            fluid
            :placeholder="$t('previews.regexViewer.insertYourTestStringHere')"
            class="relative z-10 bg-transparent text-transparent caret-neutral-500"
            style="font-size: 1rem"
            auto-resize
            wrap="hard"
          />
        </div>
      </div>

      <p v-if="error" class="mono mt-2 text-sm text-red-500">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import RegexWorker from '@/utils/workers/regex.worker.ts?worker'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Highlighted from '@/components/Highlighted.vue'

const props = defineProps<{
  regex: string
  inEditor?: boolean
}>()

const flags = ref('g')
const testInput = ref('')

const error = ref('')
const highlightedResult = ref('')

const stopWatching = watch(
  [() => props.regex, flags, testInput],
  ([pattern, currentFlags, input], _previous, onCleanup) => {
    error.value = ''
    highlightedResult.value = ''

    // Untrusted expressions can backtrack indefinitely, so never run them on the UI thread.
    const worker = new RegexWorker()
    const stop = () => {
      clearTimeout(timeout)
      worker.terminate()
    }
    const timeout = setTimeout(() => {
      stop()
      error.value = 'The regular expression exceeded the time limit.'
    }, 1000)

    worker.onmessage = (event: MessageEvent<{ highlighted: string; error: string }>) => {
      stop()
      highlightedResult.value = event.data.highlighted
      error.value = event.data.error
    }
    worker.onerror = () => {
      stop()
      error.value = 'Could not evaluate this regular expression.'
    }
    onCleanup(stop)
    worker.postMessage({ pattern, flags: currentFlags, input })
  },
  { immediate: true },
)

onBeforeUnmount(stopWatching)
</script>
