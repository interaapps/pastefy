<script setup lang="ts">
import Highlighted from '@/components/Highlighted.vue'
import MermaidViewer from '@/components/previews/MermaidViewer.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

withDefaults(
  defineProps<{
    code: string
    fileName?: string
    placeholder?: string
  }>(),
  {
    fileName: 'diagram.mmd',
    placeholder: 'Configure the tool to generate Mermaid output.',
  },
)
</script>

<template>
  <div class="space-y-4">
    <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
      <MermaidViewer v-if="code" :mermaid-code="code" />
      <div v-else class="p-4 text-sm text-neutral-500 dark:text-neutral-400">
        {{ placeholder }}
      </div>
    </div>

    <div class="overflow-hidden rounded-xl border border-neutral-200 bg-white dark:border-neutral-700 dark:bg-neutral-900">
      <Highlighted :contents="code || `%% ${placeholder}`" :file-name="fileName" />
    </div>
  </div>

  <UtilityResultActions :result="code" :file-name="fileName" />
</template>
