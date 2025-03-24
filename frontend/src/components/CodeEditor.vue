<script setup lang="ts">
import { findFromFileName } from '@/utils/lang-replacements.ts'

const value = defineModel()

// @ts-ignore
import { CodeEditor } from 'petrel'
import hljs from 'highlight.js'
import { useTemplateRef, watch } from 'vue'

const props = defineProps<{
  fileName?: string
}>()

let editor: CodeEditor | undefined = undefined

const editorRef = useTemplateRef<HTMLDivElement>('editorRef')

const DEFAULT_HIGHLIGHTER = (v: string) =>
  v
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')

const initCodeEditor = (div: HTMLDivElement) => {
  editor = new CodeEditor(div)
  editor.create()
  editor.textAreaElement.placeholder = 'Paste in here'
  updateHighlighter()
}
const updateHighlighter = () => {}

watch(() => props.fileName, updateHighlighter)
watch(editorRef, () => (editorRef.value ? initCodeEditor(editorRef.value!) : null))
</script>
<template>
  <div ref="editorRef" />
</template>
