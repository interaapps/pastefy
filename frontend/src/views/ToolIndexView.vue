<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { computed, ref } from 'vue'
import { previewTools } from '@/utils/preview-tools.ts'
import { conversionTools } from '@/utils/conversion-tools.ts'
import { utilityTools } from '@/utils/utility-tools.ts'
import { useSEO } from '@/composables/seo.ts'

useSEO({
  title: 'Pastefy Tools • Preview, Convert, and Share',
  description:
    'Use Pastefy tools to preview structured files, convert formats like JSON and YAML, and create a paste only when you are ready to share.',
})

const search = ref('')

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

const filteredPreviewTools = computed(() => previewTools.filter((tool) => matchesSearch(tool)))
const filteredConversionTools = computed(() =>
  conversionTools.filter((tool) => matchesSearch(tool)),
)
const filteredUtilityTools = computed(() => utilityTools.filter((tool) => matchesSearch(tool)))
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
          v-for="tool of tools"
          :key="tool.slug"
          :to="{ name: 'tool-preview', params: { tool: tool.slug } }"
          class="group flex h-full flex-col justify-between rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex flex-col gap-4">
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
                >
                  <i :class="`ti ti-${tool.icon} text-2xl`" />
                </div>
                <div>
                  <h3 class="font-semibold">{{ tool.shortTitle }}</h3>
                  <p class="text-xs uppercase tracking-[0.2em] text-neutral-400">
                    {{ tool.fileName }}
                  </p>
                </div>
              </div>
              <i
                class="ti ti-arrow-up-right text-lg text-neutral-300 transition-all group-hover:text-neutral-500 dark:group-hover:text-neutral-300"
              />
            </div>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ tool.description }}</p>
          </div>

          <div class="mt-5 flex flex-wrap gap-2">
            <span
              v-for="keyword of tool.keywords.slice(0, 3)"
              :key="keyword"
              class="rounded-full bg-neutral-100 px-2.5 py-1 text-xs text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300"
            >
              {{ keyword }}
            </span>
          </div>
        </router-link>
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
        <router-link
          v-for="tool of filteredConversionTools"
          :key="tool.slug"
          :to="{ name: 'tool-conversion', params: { tool: tool.slug } }"
          class="group flex h-full flex-col justify-between rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex flex-col gap-4">
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
                >
                  <i :class="`ti ti-${tool.icon} text-2xl`" />
                </div>
                <div>
                  <h3 class="font-semibold">{{ tool.shortTitle }}</h3>
                  <p class="text-xs uppercase tracking-[0.2em] text-neutral-400">
                    {{ tool.sourceFileName }} -> {{ tool.targetFileName }}
                  </p>
                </div>
              </div>
              <i
                class="ti ti-arrow-up-right text-lg text-neutral-300 transition-all group-hover:text-neutral-500 dark:group-hover:text-neutral-300"
              />
            </div>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ tool.description }}</p>
          </div>

          <div class="mt-5 flex flex-wrap gap-2">
            <span
              v-for="keyword of tool.keywords.slice(0, 3)"
              :key="keyword"
              class="rounded-full bg-neutral-100 px-2.5 py-1 text-xs text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300"
            >
              {{ keyword }}
            </span>
          </div>
        </router-link>
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
        <router-link
          v-for="tool of filteredUtilityTools"
          :key="tool.slug"
          :to="{ name: 'tool-utility', params: { tool: tool.slug } }"
          class="group flex h-full flex-col justify-between rounded-xl border border-neutral-200 bg-neutral-100 p-5 transition-all hover:-translate-y-0.5 hover:bg-white dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-900"
        >
          <div class="flex flex-col gap-4">
            <div class="flex items-start justify-between gap-3">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-12 w-12 items-center justify-center rounded-xl bg-white text-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
                >
                  <i :class="`ti ti-${tool.icon} text-2xl`" />
                </div>
                <div>
                  <h3 class="font-semibold">{{ tool.shortTitle }}</h3>
                  <p class="text-xs uppercase tracking-[0.2em] text-neutral-400">
                    {{ tool.category }}
                  </p>
                </div>
              </div>
              <i
                class="ti ti-arrow-up-right text-lg text-neutral-300 transition-all group-hover:text-neutral-500 dark:group-hover:text-neutral-300"
              />
            </div>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ tool.description }}</p>
          </div>

          <div class="mt-5 flex flex-wrap gap-2">
            <span
              v-for="keyword of tool.keywords.slice(0, 3)"
              :key="keyword"
              class="rounded-full bg-neutral-100 px-2.5 py-1 text-xs text-neutral-600 dark:bg-neutral-800 dark:text-neutral-300"
            >
              {{ keyword }}
            </span>
          </div>
        </router-link>
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
