<script setup lang="ts">
import DOMPurify from 'dompurify'
import { computed } from 'vue'

const props = defineProps<{
  html: string
}>()

const sanitizedHtml = computed(() =>
  DOMPurify.sanitize(props.html, {
    USE_PROFILES: { html: true },
  }),
)
</script>

<template>
  <div class="overflow-hidden text-sm">
    <div
      class="border-b border-neutral-200 bg-neutral-50 px-4 py-3 text-xs font-semibold tracking-[0.2em] uppercase opacity-60 dark:border-neutral-700 dark:bg-neutral-900/70"
    >
      HTML Preview
    </div>
    <iframe
      class="h-[36rem] w-full bg-white"
      sandbox="allow-same-origin"
      :srcdoc="sanitizedHtml"
      title="HTML Preview"
    />
  </div>
</template>
