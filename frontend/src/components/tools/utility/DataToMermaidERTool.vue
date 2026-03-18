<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import Checkbox from 'primevue/checkbox'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import { inferSchemaGraph, toMermaidERDiagram } from '@/utils/schema-diagrams.ts'
import { parseStructuredData, type StructuredInputFormat } from '@/utils/structured-data.ts'

const source = useStorage(
  'pastefy-utility-data-er-source',
  `{
  "users": [
    {
      "id": 1,
      "name": "Ada",
      "profile": { "avatar": "ada.png", "bio": "Engineer" },
      "posts": [
        { "id": 10, "title": "Hello", "published": true }
      ]
    }
  ]
}`,
)
const inputFormat = useStorage<StructuredInputFormat>('pastefy-utility-data-er-format', 'auto')
const rootEntity = useStorage('pastefy-utility-data-er-root', 'Dataset')
const includeTypes = useStorage('pastefy-utility-data-er-types', true)

const formatOptions = [
  { label: 'Auto detect', value: 'auto' as StructuredInputFormat },
  { label: 'JSON', value: 'json' as StructuredInputFormat },
  { label: 'YAML', value: 'yaml' as StructuredInputFormat },
  { label: 'CSV', value: 'csv' as StructuredInputFormat },
]

const diagram = computed(() => {
  try {
    const parsed = parseStructuredData(source.value, inputFormat.value)
    return toMermaidERDiagram(inferSchemaGraph(parsed, rootEntity.value), {
      includeTypes: includeTypes.value,
    })
  } catch (error) {
    return `%% ${(error as Error).message || 'Could not infer an ER diagram.'}`
  }
})
</script>

<template>
  <UtilityShell
    control-title="Schema Input"
    control-description="Infer entities and relationships from JSON, YAML, or CSV schema-like data."
    result-title="ER Diagram"
    result-description="Generated Mermaid ER diagram based on inferred entities and relationships."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Input format</label>
          <Select v-model="inputFormat" :options="formatOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Root entity name</label>
          <InputText v-model="rootEntity" fluid />
        </div>
        <label class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900">
          <Checkbox v-model="includeTypes" binary />
          <span class="text-sm">Show inferred field types</span>
        </label>
      </div>
      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Schema-like data</label>
        <Textarea v-model="source" auto-resize rows="18" fluid />
      </div>
    </template>
    <template #result>
      <MermaidToolResult :code="diagram" file-name="er-diagram.mmd" />
    </template>
  </UtilityShell>
</template>
