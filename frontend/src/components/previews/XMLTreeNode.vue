<script setup lang="ts">
import { computed } from 'vue'

defineOptions({
  name: 'XMLTreeNode',
})

export type XMLNodeEntry = {
  name: string
  path: string
  attributes: Array<{ name: string; value: string }>
  text?: string
  children: XMLNodeEntry[]
}

const props = defineProps<{
  node: XMLNodeEntry
  depth?: number
  search?: string
  isExpanded: (path: string) => boolean
  setExpanded: (path: string, expanded: boolean) => void
}>()

const depth = computed(() => props.depth || 0)
const needle = computed(() => props.search?.trim().toLowerCase() || '')
const hasChildren = computed(() => props.node.children.length > 0)

const selfText = computed(() =>
  [
    props.node.name,
    props.node.path,
    props.node.text || '',
    props.node.attributes.map((attr) => `${attr.name}=${attr.value}`).join(' '),
  ]
    .join(' ')
    .toLowerCase(),
)

const hasMatchingDescendant = computed(() => {
  if (!needle.value) return false

  const walk = (node: XMLNodeEntry): boolean => {
    const combined = [
      node.name,
      node.path,
      node.text || '',
      node.attributes.map((attr) => `${attr.name}=${attr.value}`).join(' '),
    ]
      .join(' ')
      .toLowerCase()

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
</script>

<template>
  <div v-if="isVisible" class="font-[JetBrains_Mono_Variable] text-[0.92rem]">
    <div
      class="rounded-lg px-2 py-1 transition-colors hover:bg-neutral-100/80 dark:hover:bg-neutral-800/70"
      :style="{ paddingLeft: `${depth * 18 + 8}px` }"
    >
      <div class="flex items-start gap-2">
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
            <span class="text-sky-700 dark:text-sky-300">&lt;{{ node.name }}</span>
            <span
              v-for="attribute in node.attributes"
              :key="`${node.path}-${attribute.name}`"
              class="flex flex-wrap gap-1"
            >
              <span class="text-amber-700 dark:text-amber-300">{{ attribute.name }}</span>
              <span class="opacity-50">=</span>
              <span class="text-emerald-700 dark:text-emerald-300">"{{ attribute.value }}"</span>
            </span>
            <span class="text-sky-700 dark:text-sky-300">
              {{ hasChildren || node.text ? '>' : '/>' }}
            </span>
            <span
              v-if="node.text"
              class="truncate text-neutral-600 dark:text-neutral-300"
            >
              {{ node.text }}
            </span>
            <span v-if="hasChildren" class="text-xs opacity-50">
              {{ node.children.length }} children
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="hasChildren && expanded">
      <XMLTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :depth="depth + 1"
        :search="search"
        :is-expanded="isExpanded"
        :set-expanded="setExpanded"
      />
      <div
        class="px-2 py-1 font-[JetBrains_Mono_Variable] text-[0.92rem] text-sky-700 opacity-70 dark:text-sky-300"
        :style="{ paddingLeft: `${(depth + 1) * 18 + 8}px` }"
      >
        &lt;/{{ node.name }}&gt;
      </div>
    </div>
  </div>
</template>
