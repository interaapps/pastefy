<script setup lang="ts">
defineProps<{
  items: Array<{
    slug: string
    icon: string
    title: string
    meta: string
    to: {
      name: string
      params?: Record<string, string>
      hash?: string
    }
  }>
}>()
</script>

<template>
  <section class="grid gap-4 md:grid-cols-3">
    <router-link
      v-for="item of items"
      :key="item.slug"
      :to="item.to"
      class="rounded-xl border border-neutral-200 bg-neutral-100 p-4 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
    >
      <div class="flex items-center gap-3">
        <div
          class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
        >
          <i :class="`ti ti-${item.icon}`" />
        </div>
        <div>
          <h3 class="font-semibold">{{ item.title }}</h3>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">{{ item.meta }}</p>
        </div>
      </div>
    </router-link>
  </section>
</template>
