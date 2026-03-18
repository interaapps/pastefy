<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { computed, ref } from 'vue'

import ToolCard from '@/components/tools/ToolCard.vue'
import { useSEO } from '@/composables/seo.ts'
import { usePinnedTools } from '@/composables/pinned-tools.ts'
import {
  conversionCatalogEntries,
  getCategoryCount,
  previewCatalogEntries,
  toolCategoryDefinitions,
  utilityCatalogEntries,
} from '@/utils/tool-categories.ts'
import { previewTools } from '@/utils/preview-tools.ts'

useSEO({
  title: 'Pastefy Tools • Preview, Convert, and Share',
  description:
    'Use Pastefy tools to preview structured files, convert formats like JSON and YAML, and create a paste only when you are ready to share.',
})

const search = ref('')
const pinnedToolsStore = usePinnedTools()

const normalizedSearch = computed(() => search.value.trim().toLowerCase())

const matchesSearch = (
  entry: { title?: string; shortTitle?: string; description: string; keywords: string[] },
) => {
  if (!normalizedSearch.value) return true

  return [
    entry.title || '',
    entry.shortTitle || '',
    entry.description,
    ...entry.keywords,
  ]
    .join(' ')
    .toLowerCase()
    .includes(normalizedSearch.value)
}

const groupedTools = computed(() => {
  return Object.entries(
    previewTools.reduce<Record<string, typeof previewTools>>((groups, tool) => {
      if (!matchesSearch(tool)) return groups
      groups[tool.category] ||= []
      groups[tool.category].push(tool)
      return groups
    }, {}),
  ).filter(([, tools]) => tools.length > 0)
})

const filteredPreviewTools = computed(() => previewCatalogEntries.filter((tool) => matchesSearch(tool)))
const filteredConversionTools = computed(() =>
  conversionCatalogEntries.filter((tool) => matchesSearch(tool)),
)
const filteredUtilityTools = computed(() => utilityCatalogEntries.filter((tool) => matchesSearch(tool)))
const categoryLinks = computed(() =>
  toolCategoryDefinitions
    .filter((category) => getCategoryCount(category.slug) > 0)
    .filter((category) => {
      if (!normalizedSearch.value) return true
      return `${category.title} ${category.description} ${category.eyebrow}`
        .toLowerCase()
        .includes(normalizedSearch.value)
    }),
)
const pinnedTools = computed(() => pinnedToolsStore.pinnedTools.value)
</script>

<template>
  <section class="mx-auto flex max-w-[1200px] flex-col gap-8">
    <div
      class="overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800 md:p-8"
    >
      <div class="grid gap-6 md:grid-cols-[1.5fr_1fr] md:items-end">
        <div class="flex flex-col gap-4">
          <span class="text-sm font-semibold uppercase tracking-[0.25em] text-neutral-500 dark:text-neutral-400">
            Pastefy Tools
          </span>
          <div class="flex flex-col gap-3">
            <h1 class="max-w-[18ch] text-4xl leading-none font-bold md:text-5xl">
              Preview, convert, and shape code before you share it.
            </h1>
            <p class="max-w-[62ch] text-base text-neutral-600 dark:text-neutral-300">
              Work with Markdown, JSON, YAML, HTML, DevOps configs, API requests, and more in
              focused tools built on top of Pastefy's preview system. Stay in a lightweight
              split-view workflow and create a paste only when the result is ready.
            </p>
          </div>
        </div>

        <div class="grid gap-3 rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium">What you can do here</span>
            <i class="ti ti-sparkles text-xl opacity-60" />
          </div>
          <div class="grid gap-2 text-sm text-neutral-600 dark:text-neutral-300">
            <p>Preview structured formats with dedicated viewers.</p>
            <p>Convert data between common developer formats.</p>
            <p>Create a real paste only when the output is ready to share.</p>
          </div>
          <Button
            as="router-link"
            :to="{ name: 'tool-preview', params: { tool: previewTools[0]?.slug } }"
            label="open a preview tool"
            icon="ti ti-arrow-right"
            severity="contrast"
            class="mt-2"
          />
        </div>
      </div>
    </div>

    <div class="rounded-xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-800">
      <div class="grid gap-3 md:grid-cols-[1fr_auto] md:items-center">
        <InputText
          v-model="search"
          fluid
          placeholder="Search tools, formats, and converters..."
        />
        <span class="text-sm text-neutral-500 dark:text-neutral-400">
          {{ filteredPreviewTools.length + filteredConversionTools.length + filteredUtilityTools.length }} results
        </span>
      </div>
    </div>

    <section v-if="pinnedTools.length" class="flex flex-col gap-4">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Pinned Tools</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Your saved shortcuts live here and stay on this browser via local storage.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ pinnedTools.length }} pinned
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of pinnedTools" :key="`${tool.kind}:${tool.slug}`" :tool="tool" />
      </div>
    </section>

    <section class="flex flex-col gap-4">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Browse by Category</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Jump straight into focused tool collections with dedicated SEO pages.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ categoryLinks.length }} categories
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <router-link
          v-for="category of categoryLinks"
          :key="category.slug"
          :to="{ name: 'tool-category', params: { category: category.slug } }"
          class="rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
            >
              <i :class="`ti ti-${category.icon} text-2xl`" />
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2">
                <h3 class="font-semibold">{{ category.title }}</h3>
                <span class="text-xs text-neutral-400">
                  {{ getCategoryCount(category.slug) }}
                </span>
              </div>
              <p class="mt-1 text-sm text-neutral-600 dark:text-neutral-300">
                {{ category.description }}
              </p>
            </div>
          </div>
        </router-link>
      </div>
    </section>

    <section id="preview-tools" class="flex flex-col gap-4 scroll-mt-24">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Preview Tools</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Live editors with the same preview components used across Pastefy.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ filteredPreviewTools.length }} tools
        </span>
      </div>
    </section>

    <section
      v-for="[category, tools] of groupedTools"
      :key="category"
      class="flex flex-col gap-4"
    >
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">{{ category }}</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Purpose-built editors and previews for {{ category.toLowerCase() }} workflows.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ tools.length }} tools
        </span>
      </div>

      <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <router-link
          :to="{ name: 'tool-category', params: { category: category.toLowerCase() } }"
          class="md:col-span-2 xl:col-span-3 inline-flex items-center gap-2 text-sm text-neutral-500 transition-colors hover:text-neutral-800 dark:text-neutral-400 dark:hover:text-neutral-200"
        >
          Browse {{ category.toLowerCase() }} category page
          <i class="ti ti-arrow-right text-base" />
        </router-link>
        <ToolCard
          v-for="tool of tools"
          :key="tool.slug"
          :tool="{
            kind: 'preview',
            slug: tool.slug,
            title: tool.title,
            shortTitle: tool.shortTitle,
            description: tool.description,
            icon: tool.icon,
            category: tool.category,
            keywords: tool.keywords,
            meta: tool.fileName,
            to: { name: 'tool-preview', params: { tool: tool.slug } },
          }"
        />
      </div>
    </section>

    <section id="conversions" class="flex flex-col gap-4 scroll-mt-24">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Conversions</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Format converters for the most useful structured data and API workflows.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ filteredConversionTools.length }} tools
        </span>
      </div>

      <div v-if="filteredConversionTools.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of filteredConversionTools" :key="tool.slug" :tool="tool" />
      </div>

      <div
        v-else
        class="rounded-xl border border-dashed border-neutral-200 p-6 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-400"
      >
        No conversion tools match your search yet.
      </div>
    </section>

    <section id="utilities" class="flex flex-col gap-4 scroll-mt-24">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h2 class="text-2xl font-bold">Utilities</h2>
          <p class="text-sm text-neutral-500 dark:text-neutral-400">
            Inspectors, generators, and helper tools that are not previews or converters.
          </p>
        </div>
        <span
          class="rounded-full border border-neutral-200 px-3 py-1 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-300"
        >
          {{ filteredUtilityTools.length }} tools
        </span>
      </div>

      <div v-if="filteredUtilityTools.length" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <ToolCard v-for="tool of filteredUtilityTools" :key="tool.slug" :tool="tool" />
      </div>

      <div
        v-else
        class="rounded-xl border border-dashed border-neutral-200 p-6 text-sm text-neutral-500 dark:border-neutral-700 dark:text-neutral-400"
      >
        No utility tools match your search yet.
      </div>
    </section>
  </section>
</template>
