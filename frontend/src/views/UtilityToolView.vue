<script setup lang="ts">
import Button from 'primevue/button'
import { computed, defineAsyncComponent, provide, ref } from 'vue'
import { useRoute } from 'vue-router'

import ToolFocusExitButton from '@/components/tools/ToolFocusExitButton.vue'
import ToolRelatedLinks from '@/components/tools/ToolRelatedLinks.vue'
import { useSEO } from '@/composables/seo.ts'
import { findUtilityTool, utilityTools } from '@/utils/utility-tools.ts'

const route = useRoute()
const focusMode = ref(false)
provide('tool-focus-mode', focusMode)

const tool = computed(() => findUtilityTool(String(route.params.tool || '')))

useSEO({
  title: computed(() =>
    tool.value ? `${tool.value.title} • Pastefy Tools` : 'Pastefy Tools • Utilities',
  ),
  description: computed(() =>
    tool.value
      ? `${tool.value.description} Use this Pastefy utility locally in the browser and create a paste from the result when needed.`
      : 'Use Pastefy utility tools for inspection, generation, and automation workflows.',
  ),
})

const toolComponents = {
  'data-to-mermaid': defineAsyncComponent(
    () => import('@/components/tools/utility/DataToMermaidTool.vue'),
  ),
  'jwt-inspector': defineAsyncComponent(
    () => import('@/components/tools/utility/JwtInspectorTool.vue'),
  ),
  'hash-generator': defineAsyncComponent(
    () => import('@/components/tools/utility/HashGeneratorTool.vue'),
  ),
  'secret-generator': defineAsyncComponent(
    () => import('@/components/tools/utility/SecretGeneratorTool.vue'),
  ),
  'password-generator': defineAsyncComponent(
    () => import('@/components/tools/utility/PasswordGeneratorTool.vue'),
  ),
  'cron-explainer': defineAsyncComponent(
    () => import('@/components/tools/utility/CronExplainerTool.vue'),
  ),
  'base64-lab': defineAsyncComponent(() => import('@/components/tools/utility/Base64Tool.vue')),
  'url-encoder': defineAsyncComponent(
    () => import('@/components/tools/utility/UrlEncoderTool.vue'),
  ),
  'timestamp-converter': defineAsyncComponent(
    () => import('@/components/tools/utility/TimestampConverterTool.vue'),
  ),
  'slug-generator': defineAsyncComponent(
    () => import('@/components/tools/utility/SlugGeneratorTool.vue'),
  ),
  'case-converter': defineAsyncComponent(
    () => import('@/components/tools/utility/CaseConverterTool.vue'),
  ),
  'query-string-parser': defineAsyncComponent(
    () => import('@/components/tools/utility/QueryStringTool.vue'),
  ),
  'html-entity-tool': defineAsyncComponent(
    () => import('@/components/tools/utility/HtmlEntityTool.vue'),
  ),
  'json-string-tool': defineAsyncComponent(
    () => import('@/components/tools/utility/JsonStringTool.vue'),
  ),
  'regex-escape-tool': defineAsyncComponent(
    () => import('@/components/tools/utility/RegexEscapeTool.vue'),
  ),
  'color-converter': defineAsyncComponent(
    () => import('@/components/tools/utility/ColorConverterTool.vue'),
  ),
  'number-base-converter': defineAsyncComponent(
    () => import('@/components/tools/utility/NumberBaseTool.vue'),
  ),
  'text-sorter': defineAsyncComponent(
    () => import('@/components/tools/utility/TextSorterTool.vue'),
  ),
  'lorem-ipsum-generator': defineAsyncComponent(
    () => import('@/components/tools/utility/LoremIpsumTool.vue'),
  ),
  'uuid-inspector': defineAsyncComponent(
    () => import('@/components/tools/utility/UuidInspectorTool.vue'),
  ),
  'word-stats': defineAsyncComponent(
    () => import('@/components/tools/utility/WordStatsTool.vue'),
  ),
} as const

const currentComponent = computed(() =>
  tool.value ? toolComponents[tool.value.slug as keyof typeof toolComponents] : undefined,
)

const relatedTools = computed(() =>
  utilityTools.filter((entry) => entry.slug !== tool.value?.slug).slice(0, 3),
)

const relatedToolLinks = computed(() =>
  relatedTools.value.map((entry) => ({
    slug: entry.slug,
    icon: entry.icon,
    title: entry.shortTitle,
    meta: entry.category,
    to: {
      name: 'tool-utility',
      params: {
        tool: entry.slug,
      },
    },
  })),
)
</script>

<template>
  <section
    v-if="tool"
    class="mx-auto flex max-w-[1400px] flex-col gap-6 bg-white dark:bg-[#121212]"
    :class="focusMode ? 'fixed inset-0 z-[120] max-w-none h-[100dvh] overflow-hidden p-4 md:p-6' : ''"
  >
    <ToolFocusExitButton :visible="focusMode" @close="focusMode = false" />

    <div
      v-if="!focusMode"
      class="overflow-hidden rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800 md:p-8"
    >
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div class="flex max-w-[55rem] flex-col gap-3">
          <div class="flex items-center gap-3">
            <span
              class="inline-flex items-center gap-2 rounded-md border border-neutral-200 bg-white px-3 py-1 text-sm text-neutral-700 dark:border-neutral-700 dark:bg-neutral-900 dark:text-neutral-200"
            >
              <i :class="`ti ti-${tool.icon}`" />
              {{ tool.category }}
            </span>
          </div>

          <div class="flex flex-col gap-2">
            <h1 class="text-3xl leading-tight font-bold md:text-4xl">{{ tool.title }}</h1>
            <p class="max-w-[70ch] text-base text-neutral-600 dark:text-neutral-300">
              {{ tool.description }}
            </p>
          </div>
        </div>

        <div class="flex flex-wrap gap-2">
          <Button
            @click="focusMode = !focusMode"
            :label="focusMode ? 'exit focus mode' : 'focus mode'"
            :icon="`ti ${focusMode ? 'ti-minimize' : 'ti-maximize'}`"
            severity="contrast"
            outlined
          />
          <Button
            as="router-link"
            :to="{ name: 'tool-home', hash: '#utilities' }"
            label="all utilities"
            icon="ti ti-layout-grid"
            severity="contrast"
            outlined
          />
        </div>
      </div>
    </div>

    <component :is="currentComponent" v-if="currentComponent" :class="focusMode ? 'min-h-0 flex-1' : ''" />

    <ToolRelatedLinks v-if="!focusMode" :items="relatedToolLinks" />
  </section>

  <section
    v-else
    class="mx-auto flex max-w-[50rem] flex-col gap-4 rounded-xl border border-neutral-200 bg-neutral-100 p-6 dark:border-neutral-700 dark:bg-neutral-800"
  >
    <h1 class="text-2xl font-bold">Utility tool not found</h1>
    <p class="text-neutral-600 dark:text-neutral-300">
      This utility tool does not exist. Open the tools index to pick one of the supported utilities.
    </p>
    <div>
      <Button as="router-link" :to="{ name: 'tool-home', hash: '#utilities' }" label="back to utilities" />
    </div>
  </section>
</template>
