<script lang="ts" setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import QRCode from 'qrcode'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import FloatLabel from 'primevue/floatlabel'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'

const props = defineProps<{
  text: string
}>()

const visible = defineModel<boolean>('visible')
const toast = useToast()
const qrCodeDataURL = ref<string | undefined>(undefined)

const exportFormats = [
  { label: 'PNG', value: 'image/png' },
  { label: 'JPEG', value: 'image/jpeg' },
  { label: 'SVG', value: 'image/svg+xml' },
]

const colorPresets = [
  { label: 'Classic', dark: '#000000', light: '#ffffff' },
  { label: 'Midnight', dark: '#0f172a', light: '#e2e8f0' },
  { label: 'Ocean', dark: '#0f766e', light: '#ecfeff' },
  { label: 'Amber', dark: '#9a3412', light: '#fff7ed' },
  { label: 'Rose', dark: '#9f1239', light: '#fff1f2' },
]

const exportFormat = ref<'image/png' | 'image/jpeg' | 'image/svg+xml'>('image/png')
const width = ref(720)
const margin = ref(2)
const darkColor = ref('#000000')
const lightColor = ref('#ffffff')
const showBackground = ref(true)
const includeQuietZone = ref(true)
const fileName = ref('pastefy-qr')
const logoDataUrl = ref<string | undefined>(undefined)
const logoSize = ref(22)
const logoPadding = ref(12)
const logoBackground = ref(true)

const canCopyImage = computed(
  () => typeof navigator !== 'undefined' && 'clipboard' in navigator && 'ClipboardItem' in window,
)

const downloadExtension = computed(() => {
  if (exportFormat.value === 'image/jpeg') return 'jpg'
  if (exportFormat.value === 'image/svg+xml') return 'svg'
  return 'png'
})

const previewCardStyle = computed(() => ({
  backgroundColor: showBackground.value ? lightColor.value : 'transparent',
}))

const colorPreviewStyle = computed(() => ({
  background: showBackground.value
    ? `linear-gradient(135deg, ${lightColor.value} 0%, ${lightColor.value} 50%, ${darkColor.value} 50%, ${darkColor.value} 100%)`
    : `linear-gradient(135deg, transparent 0%, transparent 50%, ${darkColor.value} 50%, ${darkColor.value} 100%)`,
}))

const previewWrapperStyle = computed(() => ({
  width: `${Math.max(180, width.value)}px`,
  height: `${Math.max(180, width.value)}px`,
}))

const logoWrapperStyle = computed(() => {
  const resolvedSize = Math.max(10, logoSize.value)
  const resolvedPadding = logoBackground.value ? Math.max(0, logoPadding.value) : 0

  return {
    width: `${resolvedSize}%`,
    height: `${resolvedSize}%`,
    padding: `${resolvedPadding}px`,
    backgroundColor: logoBackground.value
      ? showBackground.value
        ? lightColor.value
        : '#ffffff'
      : 'transparent',
  }
})

const applyPreset = (preset?: { dark: string; light: string }) => {
  if (!preset) return
  darkColor.value = preset.dark
  lightColor.value = preset.light
}

const updateLogo = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file) return
  if (!file.type.startsWith('image/')) {
    toast.add({
      severity: 'error',
      summary: 'Invalid file',
      detail: 'Please choose an image file for the logo.',
      life: 4000,
    })
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    if (typeof reader.result === 'string') {
      logoDataUrl.value = reader.result
    }
  }
  reader.readAsDataURL(file)
  input.value = ''
}

const clearLogo = () => {
  logoDataUrl.value = undefined
}

const buildOptions = () => ({
  type:
    exportFormat.value === 'image/svg+xml'
      ? 'image/png'
      : (exportFormat.value as 'image/png' | 'image/jpeg'),
  width: Math.max(180, width.value),
  margin: includeQuietZone.value ? Math.max(0, margin.value) : 0,
  color: {
    dark: darkColor.value,
    light: showBackground.value ? lightColor.value : '#0000',
  },
})

const renderQRCode = async () => {
  try {
    if (exportFormat.value === 'image/svg+xml') {
      qrCodeDataURL.value = await QRCode.toString(props.text, {
        type: 'svg',
        width: Math.max(180, width.value),
        margin: includeQuietZone.value ? Math.max(0, margin.value) : 0,
        color: {
          dark: darkColor.value,
          light: showBackground.value ? lightColor.value : '#0000',
        },
      }).then((svg) => `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`)
      return
    }

    qrCodeDataURL.value = await QRCode.toDataURL(props.text, buildOptions())
  } catch (error) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'QR generation failed',
      detail: 'The QR code could not be generated.',
      life: 4000,
    })
  }
}

const fetchBlob = async () => {
  if (!qrCodeDataURL.value) throw new Error('QR code is not ready yet.')
  const response = await fetch(qrCodeDataURL.value)
  return await response.blob()
}

const download = async () => {
  try {
    const blob = await fetchBlob()
    const link = document.createElement('a')
    const objectUrl = URL.createObjectURL(blob)

    link.download = `${fileName.value || 'pastefy-qr'}.${downloadExtension.value}`
    link.href = objectUrl
    link.click()

    setTimeout(() => URL.revokeObjectURL(objectUrl), 1000)
  } catch (error) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'Download failed',
      detail: 'The QR code could not be downloaded.',
      life: 4000,
    })
  }
}

const copyImage = async () => {
  if (!canCopyImage.value || exportFormat.value === 'image/svg+xml') return

  try {
    const blob = await fetchBlob()
    await navigator.clipboard.write([
      new ClipboardItem({
        [blob.type]: blob,
      }),
    ])
    toast.add({
      severity: 'success',
      summary: 'Copied',
      detail: 'The QR code was copied to your clipboard.',
      life: 2500,
    })
  } catch (error) {
    console.error(error)
    toast.add({
      severity: 'error',
      summary: 'Copy failed',
      detail: 'Your browser did not allow copying the QR image.',
      life: 4000,
    })
  }
}

watch(
  [
    () => props.text,
    visible,
    exportFormat,
    width,
    margin,
    darkColor,
    lightColor,
    showBackground,
    includeQuietZone,
  ],
  () => {
    renderQRCode()
  },
)

onMounted(() => renderQRCode())
</script>
<template>
  <Dialog v-model:visible="visible" modal header="QR Code" class="w-[75rem] max-w-full">
    <div class="grid gap-5 xl:grid-cols-[minmax(0,1fr)_23rem]">
      <div
        class="flex min-h-[24rem] items-start justify-center rounded-2xl border border-neutral-200 bg-neutral-100 p-5 dark:border-neutral-700 dark:bg-neutral-900"
      >
        <div
          v-if="qrCodeDataURL"
          class="relative flex items-center justify-start rounded-[2rem] border border-neutral-200 p-6 shadow-sm dark:border-neutral-700"
          :style="previewCardStyle"
        >
          <img
            class="block rounded-md"
            :style="previewWrapperStyle"
            :src="qrCodeDataURL"
            alt="QR Code"
          />
          <div
            v-if="logoDataUrl"
            class="absolute overflow-hidden rounded-2xl shadow-sm"
            :style="logoWrapperStyle"
          >
            <img :src="logoDataUrl" alt="QR Logo" class="h-full w-full rounded-xl object-contain" />
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Export</h3>
          <div class="space-y-7">
            <FloatLabel>
              <InputText v-model="fileName" fluid />
              <label>File name</label>
            </FloatLabel>
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
              <InputNumber v-model="width" :min="180" :max="2048" fluid />
              <label>Size (px)</label>
            </FloatLabel>
            <FloatLabel>
              <InputNumber v-model="margin" :min="0" :max="20" fluid />
              <label>Quiet zone</label>
            </FloatLabel>
          </div>
        </section>

        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Colors</h3>
          <div class="space-y-7">
            <Select
              :options="colorPresets"
              option-label="label"
              placeholder="Apply preset"
              fluid
              @update:model-value="applyPreset"
            >
              <template #option="{ option }">
                <div class="flex items-center gap-3">
                  <div class="flex gap-1">
                    <span
                      class="block h-4 w-4 rounded-full border border-neutral-300"
                      :style="{ backgroundColor: option.dark }"
                    />
                    <span
                      class="block h-4 w-4 rounded-full border border-neutral-300"
                      :style="{ backgroundColor: option.light }"
                    />
                  </div>
                  <span>{{ option.label }}</span>
                </div>
              </template>
            </Select>
            <div class="rounded-xl border border-neutral-200 p-3 dark:border-neutral-700">
              <div class="mb-2 flex items-center justify-between text-xs uppercase opacity-60">
                <span>Current palette</span>
                <span>{{ darkColor }} / {{ showBackground ? lightColor : 'transparent' }}</span>
              </div>
              <div
                class="h-12 rounded-lg border border-neutral-200 dark:border-neutral-700"
                :style="colorPreviewStyle"
              />
            </div>
            <FloatLabel>
              <InputText v-model="darkColor" fluid />
              <label>Foreground color</label>
            </FloatLabel>
            <div
              class="flex items-center gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
            >
              <span
                class="block h-8 w-8 rounded-full border border-neutral-300"
                :style="{ backgroundColor: darkColor }"
              />
              <div>
                <p class="text-sm font-medium">Foreground preview</p>
                <p class="text-xs opacity-60">QR modules and scan pattern</p>
              </div>
            </div>
            <FloatLabel>
              <InputText v-model="lightColor" fluid :disabled="!showBackground" />
              <label>Background color</label>
            </FloatLabel>
            <div
              class="flex items-center gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
            >
              <span
                class="block h-8 w-8 rounded-full border border-neutral-300"
                :style="{ backgroundColor: showBackground ? lightColor : 'transparent' }"
              />
              <div>
                <p class="text-sm font-medium">Background preview</p>
                <p class="text-xs opacity-60">
                  {{ showBackground ? 'Card and QR background color' : 'Transparent background' }}
                </p>
              </div>
            </div>
            <div class="grid gap-3">
              <label
                class="flex items-center justify-between gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
              >
                <span class="text-sm font-medium">Show background</span>
                <Checkbox v-model="showBackground" binary />
              </label>
              <label
                class="flex items-center justify-between gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
              >
                <span class="text-sm font-medium">Include quiet zone</span>
                <Checkbox v-model="includeQuietZone" binary />
              </label>
            </div>
          </div>
        </section>

        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-8 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Logo</h3>
          <div class="space-y-7">
            <label
              class="flex cursor-pointer items-center justify-center rounded-xl border border-dashed border-neutral-300 px-4 py-5 text-sm font-medium text-neutral-600 transition hover:border-neutral-400 hover:bg-neutral-50 dark:border-neutral-700 dark:text-neutral-300 dark:hover:bg-neutral-800"
            >
              <input type="file" accept="image/*" class="hidden" @change="updateLogo" />
              {{ logoDataUrl ? 'Replace logo' : 'Upload logo' }}
            </label>
            <div v-if="logoDataUrl" class="flex justify-end">
              <Button
                size="small"
                text
                severity="danger"
                icon="ti ti-trash"
                label="Remove Logo"
                @click="clearLogo"
              />
            </div>
            <FloatLabel>
              <InputNumber v-model="logoSize" :min="10" :max="35" suffix="%" fluid />
              <label>Logo size</label>
            </FloatLabel>
            <FloatLabel>
              <InputNumber v-model="logoPadding" :min="0" :max="32" fluid />
              <label>Logo padding</label>
            </FloatLabel>
            <label
              class="flex items-center justify-between gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
            >
              <span class="text-sm font-medium">Logo background plate</span>
              <Checkbox v-model="logoBackground" binary />
            </label>
          </div>
        </section>

        <section
          class="rounded-2xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <h3 class="mb-3 text-sm font-semibold tracking-[0.2em] uppercase opacity-60">Content</h3>
          <p
            class="rounded-xl border border-dashed border-neutral-300 p-3 text-sm break-all text-neutral-600 dark:border-neutral-700 dark:text-neutral-300"
          >
            {{ text }}
          </p>
        </section>
      </div>
    </div>

    <template #footer>
      <div
        class="flex w-full flex-col gap-3 border-t border-neutral-200 pt-5 md:flex-row md:items-center md:justify-between dark:border-neutral-700"
      >
        <span class="text-sm text-neutral-500 dark:text-neutral-400">
          Export as {{ downloadExtension }} at {{ Math.max(180, width) }}px.
        </span>
        <div class="flex flex-wrap justify-end gap-2">
          <Button
            v-if="canCopyImage && exportFormat !== 'image/svg+xml'"
            label="Copy Image"
            icon="ti ti-copy"
            severity="contrast"
            outlined
            @click="copyImage"
          />
          <Button label="Download" icon="ti ti-download" @click="download" />
        </div>
      </div>
    </template>
  </Dialog>
</template>
