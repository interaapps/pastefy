<script setup lang="ts">
import Button from 'primevue/button'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import ToolCard from '@/components/tools/ToolCard.vue'
import { useSEO } from '@/composables/seo.ts'
import {
  findToolCategory,
  getCategoryCount,
  getCategoryEntries,
  toolCategoryDefinitions,
  type ToolCategorySlug,
} from '@/utils/tool-categories.ts'

const route = useRoute()

const categorySlug = computed(() => route.params.category as ToolCategorySlug | undefined)
const category = computed(() => findToolCategory(categorySlug.value))
const tools = computed(() => (category.value ? getCategoryEntries(category.value.slug) : []))
const groupedTools = computed(() => ({
  preview: tools.value.filter((tool) => tool.kind === 'preview'),
  conversion: tools.value.filter((tool) => tool.kind === 'conversion'),
  utility: tools.value.filter((tool) => tool.kind === 'utility'),
}))
const relatedCategories = computed(() =>
  toolCategoryDefinitions
    .filter((entry) => entry.slug !== category.value?.slug && getCategoryCount(entry.slug) > 0)
    .filter((entry) => entry.scope === category.value?.scope || category.value?.scope === 'mixed')
    .slice(0, 6),
)

useSEO({
  title: computed(() => category.value?.seoTitle || 'Pastefy Tool Category'),
  description: computed(
    () =>
      category.value?.seoDescription ||
      'Browse focused Pastefy tools by category to preview, convert, or inspect developer formats.',
  ),
})
</script>

<template>
  <section v-if="category" class="mx-auto flex max-w-[1200px] flex-col gap-8">
    <div
      class="overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800 md:p-8"
    >
      <div class="grid gap-6 md:grid-cols-[1.7fr_1fr] md:items-end">
        <div class="flex flex-col gap-4">
          <span class="text-sm font-semibold uppercase tracking-[0.25em] text-neutral-500 dark:text-neutral-400">
            {{ category.eyebrow }}
          </span>
          <div class="flex flex-col gap-3">
            <h1 class="max-w-[16ch] text-4xl leading-none font-bold md:text-5xl">
              {{ category.title }}
            </h1>
            <p class="max-w-[68ch] text-base text-neutral-600 dark:text-neutral-300">
              {{ category.description }}
            </p>
          </div>
        </div>

        <div class="grid gap-3 rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium">Category summary</span>
            <i :class="`ti ti-${category.icon} text-xl opacity-60`" />
          </div>
          <div class="grid gap-2 text-sm text-neutral-600 dark:text-neutral-300">
            <p>{{ tools.length }} tools in this category.</p>
            <p>{{ groupedTools.preview.length }} previews, {{ groupedTools.conversion.length }} converters, {{ groupedTools.utility.length }} utilities.</p>
            <p>Built for share-ready workflows and cleaner developer exploration.</p>
          </div>
          <Button
            as="router-link"
            :to="{ name: 'tool-home' }"
            label="all tools"
            icon="ti ti-arrow-left"
            severity="contrast"
            class="mt-2"
          />
        </div>
      </div>
    </div>

    <section v-if="groupedTools.preview.length" class="flex flex-col gap-4">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Preview Tools</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Purpose-built editors and previews in the {{ category.eyebrow.toLowerCase() }} space.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ groupedTools.preview.length }} tools
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of groupedTools.preview" :key="tool.slug" :tool="tool" />
      </div>
    </section>

    <section v-if="groupedTools.conversion.length" class="flex flex-col gap-4">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Conversion Tools</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Format conversion workflows that belong to this category.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ groupedTools.conversion.length }} tools
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of groupedTools.conversion" :key="tool.slug" :tool="tool" />
      </div>
    </section>

    <section v-if="groupedTools.utility.length" class="flex flex-col gap-4">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Utility Tools</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Helpers, generators, and inspectors in this category.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ groupedTools.utility.length }} tools
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of groupedTools.utility" :key="tool.slug" :tool="tool" />
      </div>
    </section>

    <section v-if="relatedCategories.length" class="flex flex-col gap-4">
      <div>
        <h2 class="text-2xl font-bold">Related Categories</h2>
        <p class="text-sm text-neutral-500 dark:text-neutral-400">
          Nearby tool categories that fit similar workflows.
        </p>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <router-link
          v-for="entry of relatedCategories"
          :key="entry.slug"
          :to="{ name: 'tool-category', params: { category: entry.slug } }"
          class="rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
            >
              <i :class="`ti ti-${entry.icon} text-xl`" />
            </div>
            <div class="min-w-0">
              <div class="font-semibold">{{ entry.title }}</div>
              <p class="mt-1 text-sm text-neutral-600 dark:text-neutral-300">
                {{ entry.description }}
              </p>
            </div>
          </div>
        </router-link>
      </div>
    </section>
  </section>

  <section
    v-else
    class="mx-auto flex max-w-[900px] flex-col gap-4 rounded-xl border border-dashed border-neutral-200 bg-neutral-100 p-8 text-neutral-600 dark:border-neutral-700 dark:bg-neutral-800 dark:text-neutral-300"
  >
    <h1 class="text-2xl font-bold">Category not found</h1>
    <p>This tool category does not exist or does not have a public page yet.</p>
    <div>
      <Button as="router-link" :to="{ name: 'tool-home' }" label="back to tools" icon="ti ti-arrow-left" severity="contrast" />
    </div>
  </section>
</template>
