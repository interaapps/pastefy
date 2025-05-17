<script setup lang="ts">
import { onMounted, ref } from 'vue'

const props = defineProps<{
  latex: string
}>()

const outhtml = ref('')
onMounted(() => {})
</script>
<template>
  <div class="latex-preview overflow-hidden p-5 text-sm">
    <div id="lat" />
    <div v-html="outhtml" />
  </div>
</template>
