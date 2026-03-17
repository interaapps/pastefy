<script setup lang="ts">
import { computed, ref, useTemplateRef, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import domtoimage from 'dom-to-image'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import FloatLabel from 'primevue/floatlabel'
import InputGroup from 'primevue/inputgroup'
import InputGroupAddon from 'primevue/inputgroupaddon'
import InputNumber from 'primevue/inputnumber'
import Select from 'primevue/select'
import Highlighted from '@/components/Highlighted.vue'

const visible = defineModel<boolean>('visible')

const screenshotAreaRef = useTemplateRef<HTMLDivElement>('screenshotAreaRef')
const toast = useToast()

const props = defineProps<{
  content: string
  title?: string
  id?: string
  fileName?: string
}>()

const backgrounds = [
  { name: 'Ocean', value: 'bg-gradient-to-r from-sky-700 via-cyan-700 to-blue-900' },
  { name: 'Forest', value: 'bg-gradient-to-r from-emerald-700 via-teal-700 to-cyan-900' },
  { name: 'Sunset', value: 'bg-gradient-to-r from-orange-600 via-rose-600 to-fuchsia-800' },
  { name: 'Amber', value: 'bg-gradient-to-r from-amber-500 via-orange-600 to-red-800' },
  { name: 'Graphite', value: 'bg-gradient-to-r from-neutral-800 to-neutral-950' },
  { name: 'Paper', value: 'bg-white' },
  { name: 'Black', value: 'bg-black' },
  { name: 'Transparent', value: 'bg-transparent' },
]

const themes = [
  { label: 'Dark', value: 'dark' },
  { label: 'Light', value: 'light' },
]

const exportFormats = [
  { label: 'PNG', value: 'png' },
  { label: 'JPEG', value: 'jpeg' },
]

const scaleOptions = [
  { label: '1x', value: 1 },
  { label: '2x', value: 2 },
  { label: '3x', value: 3 },
]

const background = ref(backgrounds[0].value)
const theme = ref<'dark' | 'light'>('dark')
const exportFormat = ref<'png' | 'jpeg'>('png')
const exportScale = ref(2)
const width = ref(880)
const padding = ref(32)
const showTitleBar = ref(true)
const showLineNumbers = ref(true)

const totalLines = computed(() => Math.max(props.content?.split('\n').length || 1, 1))
const fromLine = ref(1)
const toLine = ref(totalLines.value)

watch(
  totalLines,
  (lineCount) => {
    fromLine.value = Math.min(Math.max(1, fromLine.value), lineCount)
    toLine.value = Math.min(Math.max(fromLine.value, toLine.value), lineCount)
  },
  { immediate: true },
)

const selectedLines = computed(() => {
  const lines = props.content?.split('\n') || []
  const start = Math.max(fromLine.value - 1, 0)
  const end = Math.max(toLine.value, fromLine.value)
  return lines.slice(start, end).join('\n')
})

const fileLabel = computed(() => props.fileName || props.title || 'paste')
const screenshotThemeClass = computed(() => (theme.value === 'dark' ? 'dark text-white' : ''))
const codeShellClass = computed(() =>
  theme.value === 'dark'
    ? 'border-neutral-600 bg-neutral-800/85 text-white'
    : 'border-neutral-200 bg-white/92 text-neutral-900 shadow-sm',
)

const canCopyImage = computed(
  () => typeof navigator !== 'undefined' && 'clipboard' in navigator && 'ClipboardItem' in window,
)

const getImageOptions = () => {
  const node = screenshotAreaRef.value
  if (!node) return undefined

  const scaledWidth = node.offsetWidth * exportScale.value
  const scaledHeight = node.offsetHeight * exportScale.value

  return {
    width: scaledWidth,
    height: scaledHeight,
    bgcolor:
      exportFormat.value === 'jpeg' && background.value === 'bg-transparent'
        ? theme.value === 'dark'
          ? '#0a0a0a'
          : '#ffffff'
        : undefined,
    style: {
      transform: `scale(${exportScale.value})`,
      transformOrigin: 'top left',
      width: `${node.offsetWidth}px`,
      height: `${node.offsetHeight}px`,
    },
  }
}

const createBlob = async () => {
  const node = screenshotAreaRef.value
  if (!node) throw new Error('Screenshot area is not ready yet.')

  const options = getImageOptions()

  if (exportFormat.value === 'jpeg') {
    const dataUrl = await domtoimage.toJpeg(node, {
      ...options,
      quality: 0.96,
    })
    const response = await fetch(dataUrl)
    return {
      blob: await response.blob(),
      extension: 'jpg',
      mime: 'image/jpeg',
    }
  }

  return {
    blob: await domtoimage.toBlob(node, options),
    extension: 'png',
    mime: 'image/png',
  }
}

const download = async () => {
  try {
    const { blob, extension } = await createBlob()
    const link = document.createElement('a')
    const objectUrl = URL.createObjectURL(blob)

    link.download = `${props.fileName?.replace(/\./g, '-') || 'paste-screenshot'}.${extension}`
    link.href = objectUrl
    link.click()

    setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
  } catch (error) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'Export failed',
      detail: 'The screenshot could not be generated.',
      life: 4000,
    })
  }
}

const copyImage = async () => {
  if (!canCopyImage.value) return

  try {
    const { blob, mime } = await createBlob()
    await navigator.clipboard.write([
      new ClipboardItem({
        [mime]: blob,
      }),
    ])
    toast.add({
      severity: 'success',
      summary: 'Copied',
      detail: 'The screenshot was copied to your clipboard.',
      life: 2500,
    })
  } catch (error) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'Copy failed',
      detail: 'Your browser did not allow copying the image.',
      life: 4000,
    })
  }
}
</script>
<template>
  <Dialog v-model:visible="visible" modal header="Screenshot" class="w-[75rem] max-w-full">
    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_23rem]">
      <div
        class="overflow-hidden rounded-2xl border border-neutral-200 bg-neutral-100 p-4 dark:border-neutral-700 dark:bg-neutral-900"
      >
        <div class="flex min-h-[24rem] items-center justify-center overflow-auto rounded-xl">
          <div
            ref="screenshotAreaRef"
            class="relative transition-all"
            :class="[background, screenshotThemeClass]"
            :style="{
              width: `${Math.max(320, width)}px`,
              padding: `${Math.max(0, padding)}px`,
            }"
          >
            <div
              class="overflow-hidden rounded-[1.25rem] border backdrop-blur-xl"
              :class="codeShellClass"
            >
              <div
                v-if="showTitleBar"
                class="flex w-full items-center justify-between px-4 pt-4 pb-1"
                :class="theme === 'dark' ? 'text-white' : 'text-neutral-900'"
              >
                <div class="flex gap-1.5">
                  <div
                    v-for="ignored of [1, 2, 3]"
                    :key="ignored"
                    class="h-[11px] w-[11px] rounded-full"
                    :class="theme === 'dark' ? 'bg-white/20' : 'bg-neutral-300'"
                  />
                </div>
                <span class="truncate px-3 text-xs font-medium opacity-60">{{ fileLabel }}</span>
                <div class="w-[55px]" />
              </div>

              <Highlighted
                class="w-full overflow-hidden"
                hide-divider
                :contents="selectedLines"
                :file-name="fileName"
                :starting-line-number="Math.max(fromLine - 1, 0)"
                :hide-line-numbering="!showLineNumbers"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Style</h3>
          <div class="space-y-7">
            <FloatLabel>
              <Select
                v-model="theme"
                :options="themes"
                option-label="label"
                option-value="value"
                fluid
              />
              <label>Theme</label>
            </FloatLabel>
            <FloatLabel>
              <Select
                v-model="background"
                :options="backgrounds"
                option-label="name"
                option-value="value"
                fluid
              >
                <template #value="{ value }">
                  <div class="flex items-center gap-2">
                    <div
                      class="h-[1rem] w-[1rem] rounded-full border border-neutral-400"
                      :class="value"
                    />
                    <span>{{ backgrounds.find((option) => option.value === value)?.name }}</span>
                  </div>
                </template>
                <template #option="{ option }">
                  <div class="flex items-center gap-2">
                    <div
                      class="h-[1rem] w-[1rem] rounded-full border border-neutral-400"
                      :class="option.value"
                    />
                    <span>{{ option.name }}</span>
                  </div>
                </template>
              </Select>
              <label>Background</label>
            </FloatLabel>
            <FloatLabel>
              <InputNumber v-model="width" :min="320" :max="2200" fluid />
              <label>Canvas width (px)</label>
            </FloatLabel>
            <FloatLabel>
              <InputNumber v-model="padding" :min="0" :max="160" fluid />
              <label>Outer padding (px)</label>
            </FloatLabel>
          </div>
        </section>

        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Content</h3>
          <div class="space-y-7">
            <FloatLabel>
              <InputGroup>
                <InputNumber v-model="fromLine" :min="1" :max="totalLines" fluid />
                <InputGroupAddon class="bg-transparent">to</InputGroupAddon>
                <InputNumber v-model="toLine" :min="fromLine" :max="totalLines" fluid />
              </InputGroup>
              <label>Line range</label>
            </FloatLabel>
            <div class="grid gap-3">
              <label
                class="flex items-center justify-between gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
              >
                <span class="text-sm font-medium">Show title bar</span>
                <Checkbox v-model="showTitleBar" binary />
              </label>
              <label
                class="flex items-center justify-between gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
              >
                <span class="text-sm font-medium">Show line numbers</span>
                <Checkbox v-model="showLineNumbers" binary />
              </label>
            </div>
          </div>
        </section>

        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Export</h3>
          <div class="space-y-7">
            <FloatLabel>
              <Select
                v-model="exportFormat"
                :options="exportFormats"
                option-label="label"
                option-value="value"
                fluid
              />
              <label>Format</label>
            </FloatLabel>
            <FloatLabel>
              <Select
                v-model="exportScale"
                :options="scaleOptions"
                option-label="label"
                option-value="value"
                fluid
              />
              <label>Sharpness</label>
            </FloatLabel>
            <p class="text-xs text-neutral-500 dark:text-neutral-400">
              Higher sharpness creates larger files but produces crisper screenshots.
            </p>
          </div>
        </section>
      </div>
    </div>

    <template #footer>
      <div
        class="flex w-full flex-col gap-3 border-t border-neutral-200 pt-5 md:flex-row md:items-center md:justify-between dark:border-neutral-700"
      >
        <span class="text-sm text-neutral-500 dark:text-neutral-400">
          {{ totalLines }} total lines, exporting {{ Math.max(toLine - fromLine + 1, 0) }} lines.
        </span>
        <div class="flex flex-wrap justify-end gap-2">
          <Button
            v-if="canCopyImage"
            label="Copy Image"
            icon="ti ti-copy"
            severity="contrast"
            outlined
            @click="copyImage"
          />
          <Button label="Download" @click="download" icon="ti ti-download" />
        </div>
      </div>
    </template>
  </Dialog>
</template>
