<script setup lang="ts">
import Button from 'primevue/button'
import { computed } from 'vue'

import { usePinnedTools, type PinnedToolKind } from '@/composables/pinned-tools.ts'

const props = withDefaults(
  defineProps<{
    kind: PinnedToolKind
    slug: string
    label?: boolean
    text?: boolean
    outlined?: boolean
    size?: 'small' | 'large'
  }>(),
  {
    label: true,
    text: false,
    outlined: true,
  },
)

const pinnedTools = usePinnedTools()
const pinned = computed(() => pinnedTools.isPinned(props.kind, props.slug))
const buttonLabel = computed(() => (pinned.value ? 'pinned' : 'pin tool'))

const toggle = () => {
  pinnedTools.togglePinned(props.kind, props.slug)
}
</script>

<template>
  <Button
    @click.stop.prevent="toggle"
    :label="label ? buttonLabel : undefined"
    icon="ti ti-pin"
    severity="contrast"
    :text="text"
    :outlined="outlined && !pinned"
    :size="size"
    :class="pinned ? '!bg-neutral-900 !text-white dark:!bg-neutral-100 dark:!text-neutral-900' : ''"
    :aria-label="buttonLabel"
  />
</template>
