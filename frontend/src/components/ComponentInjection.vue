<script lang="ts" setup>
import { useComponentInjectionStore } from '@/stores/component-injections.ts'

defineProps<{
  type: string
  value?: unknown
}>()

const injectionStore = useComponentInjectionStore()
</script>
<template>
  <template v-if="type in injectionStore.injections">
    <component
      v-for="(comp, ind) of injectionStore.injections[type]"
      :is="comp"
      :value="value"
      :key="ind"
    />
  </template>
</template>
