<script setup lang="ts">
import Checkbox from 'primevue/checkbox'
import Textarea from 'primevue/textarea'
import { computed } from 'vue'
import { useStorage } from '@vueuse/core'

import MermaidToolResult from '@/components/tools/utility/MermaidToolResult.vue'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'

type TableColumn = {
  name: string
  type: string
  primaryKey: boolean
}

type Relation = {
  from: string
  to: string
  fromField: string
  toField: string
}

const sanitizeEntityName = (value: string) => {
  const sanitized = value
    .replace(/["`]/g, '')
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')
  return sanitized || 'entity'
}

const sanitizeTypeForMermaid = (value: string) => {
  const sanitized = value
    .trim()
    .toLowerCase()
    .replace(/["`]/g, '')
    .replace(/[^a-zA-Z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
  return sanitized || 'field'
}

const source = useStorage(
  'pastefy-utility-sql-er-source',
  `CREATE TABLE users (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE posts (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES users(id),
  title TEXT NOT NULL
);`,
)
const includeTypes = useStorage('pastefy-utility-sql-er-types', true)

const diagram = computed(() => {
  try {
    const tableMatches = source.value.matchAll(
      /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?([^\s(]+)\s*\(([\s\S]*?)\);/gi,
    )
    const tables = new Map<string, TableColumn[]>()
    const relations: Relation[] = []

    for (const match of tableMatches) {
      const tableName = sanitizeEntityName(match[1])
      const block = match[2]
      const columns: TableColumn[] = []

      block
        .split(/,\n|,\r\n|,\s*(?=[a-zA-Z_"`])/)
        .map((line) => line.trim())
        .filter(Boolean)
        .forEach((line) => {
          const fkInline = line.match(/REFERENCES\s+([^\s(]+)\s*\(([^)]+)\)/i)
          if (fkInline) {
            const columnName = line.split(/\s+/)[0].replace(/["`]/g, '')
            relations.push({
              from: tableName,
              to: sanitizeEntityName(fkInline[1]),
              fromField: columnName,
              toField: fkInline[2].replace(/["`]/g, ''),
            })
          }

          if (/^(CONSTRAINT|PRIMARY KEY|FOREIGN KEY|UNIQUE|KEY)\b/i.test(line)) {
            const fkConstraint = line.match(/FOREIGN\s+KEY\s*\(([^)]+)\)\s+REFERENCES\s+([^\s(]+)\s*\(([^)]+)\)/i)
            if (fkConstraint) {
              relations.push({
                from: tableName,
                to: sanitizeEntityName(fkConstraint[2]),
                fromField: fkConstraint[1].replace(/["`]/g, ''),
                toField: fkConstraint[3].replace(/["`]/g, ''),
              })
            }
            return
          }

          const columnMatch = line.match(/^["`]?([a-zA-Z0-9_]+)["`]?\s+(.+)$/i)
          if (!columnMatch) return

          const typeSegment = columnMatch[2]
            .replace(/\bREFERENCES\b[\s\S]*$/i, '')
            .trim()
          const typeMatch = typeSegment.match(
            /^(.*?)(?=\s+(?:NOT\s+NULL|NULL|DEFAULT|PRIMARY\s+KEY|REFERENCES|CHECK|UNIQUE|CONSTRAINT|COLLATE|COMMENT|GENERATED|AUTO_INCREMENT)\b|$)/i,
          )
          const sqlType = (typeMatch?.[1] || typeSegment).trim()

          columns.push({
            name: columnMatch[1],
            type: sqlType.replace(/\s+/g, ' '),
            primaryKey: /\bPRIMARY\s+KEY\b/i.test(line),
          })
        })

      tables.set(tableName, columns)
    }

    const lines = ['erDiagram']
    tables.forEach((columns, table) => {
      lines.push(`  ${table} {`)
      columns.forEach((column) => {
        lines.push(
          `    ${includeTypes.value ? sanitizeTypeForMermaid(column.type) : 'field'} ${column.name}${column.primaryKey ? ' PK' : ''}`,
        )
      })
      lines.push('  }')
    })
    relations.forEach((relation) => {
      lines.push(`  ${relation.to} ||--o{ ${relation.from} : "${relation.toField} -> ${relation.fromField}"`)
    })

    return lines.join('\n')
  } catch (error) {
    return `%% ${(error as Error).message || 'Could not parse the SQL schema.'}`
  }
})
</script>

<template>
  <UtilityShell
    control-title="SQL Schema"
    control-description="Parse CREATE TABLE statements into Mermaid ER diagrams with tables, fields, and foreign keys."
    result-title="ER Diagram"
    result-description="Generated Mermaid ER diagram from the SQL schema."
  >
    <template #controls>
      <label class="flex items-center gap-2 rounded-xl border border-neutral-200 bg-white px-3 py-2 dark:border-neutral-700 dark:bg-neutral-900">
        <Checkbox v-model="includeTypes" binary />
        <span class="text-sm">Show SQL field types</span>
      </label>
      <div>
        <label class="mb-1 block text-sm text-neutral-500 dark:text-neutral-400">SQL schema</label>
        <Textarea v-model="source" auto-resize rows="20" fluid />
      </div>
    </template>
    <template #result>
      <MermaidToolResult :code="diagram" file-name="schema-er.mmd" />
    </template>
  </UtilityShell>
</template>
