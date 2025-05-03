<script setup lang="ts">
import ComponentInjection from '@/components/ComponentInjection.vue'
</script>
<template>
  <div class="flex h-full w-full flex-col items-center justify-center gap-2">
    <h1 class="mono text-5xl font-bold md:text-6xl">== 404 ==</h1>
    <p class="text-sm opacity-60">Page not found</p>
    <ComponentInjection type="not-found-page" />
  </div>
</template>
