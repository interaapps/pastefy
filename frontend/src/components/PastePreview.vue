<script setup lang="ts">
import { defineAsyncComponent } from 'vue'
import SVGViewer from '@/components/previews/SVGViewer.vue'

// const LaTeXViewer = defineAsyncComponent(() => import('@/components/previews/LaTeXViewer.vue'))
const MarkdownViewer = defineAsyncComponent(
  () => import('@/components/previews/MarkdownViewer.vue'),
)
const CSVViewer = defineAsyncComponent(() => import('@/components/previews/CSVViewer.vue'))
const MermaidViewer = defineAsyncComponent(() => import('@/components/previews/MermaidViewer.vue'))
const GeoJSONViewer = defineAsyncComponent(() => import('@/components/previews/GeoJSONViewer.vue'))
const DiffViewer = defineAsyncComponent(() => import('@/components/previews/DiffViewer.vue'))
const ICSViewer = defineAsyncComponent(() => import('@/components/previews/ICSViewer.vue'))
const RegexViewer = defineAsyncComponent(() => import('@/components/previews/RegexViewer.vue'))
const JSONViewer = defineAsyncComponent(() => import('@/components/previews/JSONViewer.vue'))
const HTMLViewer = defineAsyncComponent(() => import('@/components/previews/HTMLViewer.vue'))
const XMLViewer = defineAsyncComponent(() => import('@/components/previews/XMLViewer.vue'))
const ConfigViewer = defineAsyncComponent(() => import('@/components/previews/ConfigViewer.vue'))
const YAMLViewer = defineAsyncComponent(() => import('@/components/previews/YAMLViewer.vue'))
const AsciinemaViewer = defineAsyncComponent(
  () => import('@/components/previews/AsciinemaViewer.vue'),
)

defineProps<{
  fileName?: string
  contents: string
  type: string
  inEditor?: boolean
}>()
</script>
<template>
  <CSVViewer v-if="type === 'csv'" :csv="contents" :inEditor />
  <MarkdownViewer v-else-if="type === 'markdown'" :markdown="contents" :inEditor />
  <MermaidViewer
    v-else-if="
      type === 'mermaid' ||
      type === 'mmd' ||
      fileName?.endsWith('.mmd') ||
      fileName?.endsWith('.mermaid')
    "
    :mermaid-code="contents"
    :inEditor
  />
  <SVGViewer v-else-if="fileName?.endsWith('.svg')" :svg="contents" :inEditor />
  <GeoJSONViewer v-else-if="fileName?.endsWith('.geojson')" :geo-json="contents" :inEditor />
  <DiffViewer v-else-if="type === 'diff'" :diff="contents" :inEditor />
  <ICSViewer v-else-if="type === 'ics'" :ics="contents" :inEditor />
  <RegexViewer v-else-if="type === 'regex'" :regex="contents" :inEditor />
  <JSONViewer v-else-if="type === 'json' || fileName?.endsWith('.json')" :json="contents" />
  <HTMLViewer
    v-else-if="type === 'html' || fileName?.endsWith('.html') || fileName?.endsWith('.htm')"
    :html="contents"
  />
  <XMLViewer
    v-else-if="
      type === 'xml' ||
      fileName?.endsWith('.xml') ||
      fileName?.endsWith('.xhtml') ||
      fileName?.endsWith('.iml')
    "
    :xml="contents"
  />
  <YAMLViewer
    v-else-if="type === 'yaml' || fileName?.endsWith('.yml') || fileName?.endsWith('.yaml')"
    :yaml="contents"
  />
  <ConfigViewer
    v-else-if="type === 'toml' || fileName?.endsWith('.toml')"
    :contents="contents"
    :file-name="fileName"
    type="toml"
  />
  <ConfigViewer
    v-else-if="
      type === 'properties' ||
      type === 'ini' ||
      fileName?.endsWith('.properties') ||
      fileName?.endsWith('.ini') ||
      fileName === '.env'
    "
    :contents="contents"
    :file-name="fileName"
    :type="type === 'ini' || fileName?.endsWith('.ini') ? 'ini' : 'properties'"
  />
  <AsciinemaViewer
    v-else-if="type === 'cast' || fileName?.endsWith('.cast')"
    :cast="contents"
    :inEditor
  />
  <!-- <LaTeXViewer v-else-if="type === 'tex'" :latex="contents" /> -->
</template>
