<script setup lang="ts">
import Button from 'primevue/button'
import ColorPicker from 'primevue/colorpicker'
import InputText from 'primevue/inputtext'
import { computed } from 'vue'
import { useClipboard, useStorage } from '@vueuse/core'
import UtilityShell from '@/components/tools/utility/UtilityShell.vue'
import UtilityResultActions from '@/components/tools/utility/UtilityResultActions.vue'

const clipboard = useClipboard()
const input = useStorage('pastefy-utility-color-input', '0ea5e9')

const normalizedHex = computed(() => {
  const raw = input.value.replace('#', '').trim()
  if (raw.length === 3) return raw.split('').map((char) => `${char}${char}`).join('')
  return raw.slice(0, 6).padEnd(6, '0')
})

const rgb = computed(() => ({
  r: parseInt(normalizedHex.value.slice(0, 2), 16),
  g: parseInt(normalizedHex.value.slice(2, 4), 16),
  b: parseInt(normalizedHex.value.slice(4, 6), 16),
}))

const hsl = computed(() => {
  const r = rgb.value.r / 255
  const g = rgb.value.g / 255
  const b = rgb.value.b / 255
  const max = Math.max(r, g, b)
  const min = Math.min(r, g, b)
  const l = (max + min) / 2
  const d = max - min
  let h = 0
  let s = 0

  if (d !== 0) {
    s = d / (1 - Math.abs(2 * l - 1))
    switch (max) {
      case r:
        h = 60 * (((g - b) / d) % 6)
        break
      case g:
        h = 60 * ((b - r) / d + 2)
        break
      default:
        h = 60 * ((r - g) / d + 4)
    }
  }

  return {
    h: Math.round((h + 360) % 360),
    s: Math.round(s * 100),
    l: Math.round(l * 100),
  }
})

const result = computed(() =>
  JSON.stringify(
    {
      hex: `#${normalizedHex.value}`,
      rgb: `rgb(${rgb.value.r}, ${rgb.value.g}, ${rgb.value.b})`,
      hsl: `hsl(${hsl.value.h}, ${hsl.value.s}%, ${hsl.value.l}%)`,
    },
    null,
    2,
  ),
)
</script>

<template>
  <UtilityShell>
    <template #controls>
      <label class="text-sm font-medium">Hex color</label>
      <div class="grid gap-3 md:grid-cols-[1fr_auto]">
        <InputText v-model="input" fluid />
        <ColorPicker v-model="input" />
      </div>
    </template>

    <template #result>
      <div class="mb-4 h-32 rounded-xl border border-neutral-200 dark:border-neutral-700" :style="{ backgroundColor: `#${normalizedHex}` }" />
      <div class="grid gap-3">
        <div
          v-for="entry of [
            ['HEX', `#${normalizedHex}`],
            ['RGB', `rgb(${rgb.r}, ${rgb.g}, ${rgb.b})`],
            ['HSL', `hsl(${hsl.h}, ${hsl.s}%, ${hsl.l}%)`],
          ]"
          :key="entry[0]"
          class="rounded-xl border border-neutral-200 bg-white p-4 dark:border-neutral-700 dark:bg-neutral-900"
        >
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="font-medium">{{ entry[0] }}</span>
            <Button @click="clipboard.copy(String(entry[1]))" icon="ti ti-copy" severity="contrast" text />
          </div>
          <code class="block break-all text-sm">{{ entry[1] }}</code>
        </div>
      </div>
    </template>

    <template #footer>
      <UtilityResultActions :result="result" file-name="color-conversion.json" />
    </template>
  </UtilityShell>
</template>
