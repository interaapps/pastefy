<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'YAMLTreeNode',
})

export type YAMLNodeEntry = {
  label: string
  value?: string
  path: string
  lineNumber: number
  kind: 'mapping' | 'sequence' | 'comment' | 'scalar'
  children: YAMLNodeEntry[]
}

const props = defineProps<{
  node: YAMLNodeEntry
  depth?: number
  search?: string
  isExpanded: (path: string) => boolean
  setExpanded: (path: string, expanded: boolean) => void
}>()

const depth = computed(() => props.depth || 0)
const needle = computed(() => props.search?.trim().toLowerCase() || '')
const hasChildren = computed(() => props.node.children.length > 0)

const selfText = computed(() =>
  [props.node.label, props.node.value || '', props.node.path].join(' ').toLowerCase(),
)

const hasMatchingDescendant = computed(() => {
  if (!needle.value) return false

  const walk = (node: YAMLNodeEntry): boolean => {
    const combined = [node.label, node.value || '', node.path].join(' ').toLowerCase()
    if (combined.includes(needle.value)) return true
    return node.children.some(walk)
  }

  return props.node.children.some(walk)
})

const isVisible = computed(() => {
  if (!needle.value) return true
  return selfText.value.includes(needle.value) || hasMatchingDescendant.value
})

const expanded = computed(() => {
  if (!hasChildren.value) return false
  if (needle.value && hasMatchingDescendant.value) return true
  return props.isExpanded(props.node.path)
})

const toggle = () => {
  if (!hasChildren.value) return
  props.setExpanded(props.node.path, !expanded.value)
}

const labelClass = computed(() => {
  if (props.node.kind === 'comment') return 'opacity-50'
  if (props.node.kind === 'sequence') return 'text-sky-700 dark:text-sky-300'
  return 'text-amber-700 dark:text-amber-300'
})

const valueClass = computed(() => {
  if (props.node.kind === 'comment') return 'opacity-50'
  return 'text-emerald-700 dark:text-emerald-300'
})
</script>

<template>
  <div v-if="isVisible" class="font-[JetBrains_Mono_Variable] text-[0.92rem]">
    <div
      class="flex items-start gap-2 rounded-lg px-2 py-1 transition-colors hover:bg-neutral-100/80 dark:hover:bg-neutral-800/70"
      :style="{ paddingLeft: `${depth * 18 + 8}px` }"
    >
      <button
        v-if="hasChildren"
        class="mt-0.5 flex h-5 w-5 items-center justify-center rounded text-neutral-500 hover:bg-neutral-200 dark:hover:bg-neutral-700"
        @click="toggle"
      >
        <i :class="`ti ti-chevron-${expanded ? 'down' : 'right'} text-sm`" />
      </button>
      <span v-else class="block h-5 w-5" />

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-x-2 gap-y-1">
          <span class="text-xs opacity-40">{{ node.lineNumber }}</span>
          <span :class="labelClass">{{ node.label }}</span>
          <span
            v-if="node.kind === 'mapping' && node.value !== undefined && node.value !== ''"
            class="opacity-40"
          >
            :
          </span>
          <span
            v-if="node.kind === 'sequence' && node.value !== undefined && node.value !== ''"
            class="opacity-40"
          >
            -
          </span>
          <span v-if="node.value" :class="valueClass">{{ node.value }}</span>
          <span v-if="hasChildren" class="text-xs opacity-50">{{ node.children.length }} children</span>
        </div>
      </div>
    </div>

    <div v-if="hasChildren && expanded">
      <YAMLTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :depth="depth + 1"
        :search="search"
        :is-expanded="isExpanded"
        :set-expanded="setExpanded"
      />
    </div>
  </div>
</template>
