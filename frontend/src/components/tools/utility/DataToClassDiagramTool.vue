<script setup lang="ts">
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import { inferSchemaGraph, toMermaidClassDiagram } from '@/utils/schema-diagrams.ts'
import { parseStructuredData, type StructuredInputFormat } from '@/utils/structured-data.ts'

const source = useStorage(
  'pastefy-utility-class-source',
  `{
  "project": {
    "id": "p_1",
    "name": "Pastefy",
    "owner": {
      "id": "u_1",
      "displayName": "Ada"
    },
    "members": [
      { "id": "u_2", "displayName": "Linus", "admin": true }
    ]
  }
}`,
)
const inputFormat = useStorage<StructuredInputFormat>('pastefy-utility-class-format', 'auto')
const rootEntity = useStorage('pastefy-utility-class-root', 'Root')
const includeTypes = useStorage('pastefy-utility-class-types', true)

const formatOptions = [
  { label: 'Auto detect', value: 'auto' as StructuredInputFormat },
  { label: 'JSON', value: 'json' as StructuredInputFormat },
  { label: 'YAML', value: 'yaml' as StructuredInputFormat },
  { label: 'CSV', value: 'csv' as StructuredInputFormat },
]

const diagram = computed(() => {
  try {
    const parsed = parseStructuredData(source.value, inputFormat.value)
    return toMermaidClassDiagram(inferSchemaGraph(parsed, rootEntity.value), {
      includeTypes: includeTypes.value,
    })
  } catch (error) {
    return `%% ${(error as Error).message || 'Could not infer a class diagram.'}`
  }
})
</script>

<template>
  <UtilityShell
    control-title="Data Model"
    control-description="Map nested JSON, YAML, or CSV structures to Mermaid class diagrams."
    result-title="Class Diagram"
    result-description="Generated Mermaid class diagram with classes, fields, and inferred relationships."
  >
    <template #controls>
      <div class="grid gap-3 md:grid-cols-2">
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Input format</label>
          <Select v-model="inputFormat" :options="formatOptions" option-label="label" option-value="value" fluid />
        </div>
        <div>
          <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Root class name</label>
          <InputText v-model="rootEntity" fluid />
        </div>
        <label class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900">
          <Checkbox v-model="includeTypes" binary />
          <span class="text-sm">Show inferred field types</span>
        </label>
      </div>

      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">Source data</label>
        <Textarea v-model="source" auto-resize rows="18" fluid />
      </div>
    </template>
    <template #result>
      <MermaidToolResult :code="diagram" file-name="class-diagram.mmd" />
    </template>
  </UtilityShell>
</template>
