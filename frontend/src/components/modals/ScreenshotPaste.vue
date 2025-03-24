<script setup lang="ts">
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import FloatLabel from 'primevue/floatlabel'
import Select from 'primevue/select'
import InputNumber from 'primevue/inputnumber'
import Highlighted from '@/components/Highlighted.vue'
import { ref, useTemplateRef } from 'vue'
import domtoimage from 'dom-to-image'
const visible = defineModel<boolean>('visible')

const screenshotAreaRef = useTemplateRef<HTMLDivElement>('screenshotAreaRef')

const props = defineProps<{
  content: string
  title?: string
  id?: string
  fileName?: string
}>()

const backgrounds = [
  { name: 'blue', value: 'bg-gradient-to-r from-blue-800 to-indigo-900' },
  { name: 'green', value: 'bg-gradient-to-r from-green-800 to-blue-900' },
  { name: 'red', value: 'bg-gradient-to-r from-red-800 to-pink-900' },
  { name: 'yellow', value: 'bg-gradient-to-r from-yellow-800 to-orange-900' },
  { name: 'purple', value: 'bg-gradient-to-r from-purple-800 to-pink-900' },
  { name: 'gray', value: 'bg-gradient-to-r from-neutral-800 to-neutral-900' },
  { name: 'transparent', value: 'bg-transparent' },
  { name: 'black', value: 'bg-black' },
  { name: 'white', value: 'bg-white' },
]

const background = ref(backgrounds[0].value)

const width = ref(800)

const download = () => {
  domtoimage.toSvg(screenshotAreaRef.value!).then(function (dataUrl: string) {
    const link = document.createElement('a')
    link.download = `${props.fileName?.replace(/\./g, '-') || 'paste-screenshot'}.png`
    link.href = dataUrl
    link.click()
  })
}
</script>
<template>
  <Dialog v-model:visible="visible" modal header="Screenshot" class="w-[60rem]">
    <div class="mb-8 flex min-h-[20rem] items-center justify-center overflow-auto">
      <div
        ref="screenshotAreaRef"
        class="dark relative bg-gradient-to-r p-8 text-white"
        :class="background"
        :style="{
          width: `${width}px`,
        }"
      >
        <div class="backdrop-blur-1xl rounded-lg border border-neutral-600 bg-neutral-800/80">
          <div class="flex w-full items-center justify-between p-3 pb-0">
            <div class="flex gap-1.5">
              <div
                v-for="ignored of [1, 2, 3]"
                class="h-[11px] w-[11px] rounded-full bg-white/20"
                :key="ignored"
              />
            </div>
            <span class="text-xs font-light opacity-30">{{ title }}</span>
            <div class="w-[55px]" />
          </div>
          <Highlighted
            class="w-full overflow-hidden"
            hide-divider
            :contents="content"
            :file-name="fileName"
          />
        </div>
      </div>
    </div>
    <template #footer>
      <div class="flex w-full justify-between pt-2">
        <div class="flex gap-2">
          <FloatLabel>
            <Select
              v-model="background"
              :options="backgrounds"
              option-label="name"
              option-value="value"
            >
              <template #value="{ value }">
                <div class="flex items-center gap-2">
                  <div
                    class="h-[1rem] w-[1rem] rounded-full border border-neutral-700"
                    :class="value"
                  />
                  <span>{{ backgrounds.find((b) => b.value === value)?.name }}</span>
                </div>
              </template>
              <template #option="{ option }">
                <div class="flex items-center gap-2">
                  <div
                    class="h-[1rem] w-[1rem] rounded-full border border-neutral-700"
                    :class="option.value"
                  />
                  <span>{{ option.name }}</span>
                </div>
              </template>
            </Select>
            <label>background</label>
          </FloatLabel>
          <FloatLabel>
            <InputNumber v-model="width" />
            <label>width (px)</label>
          </FloatLabel>
        </div>
        <Button label="download" @click="download" outlined icon="ti ti-download" />
      </div>
    </template>
  </Dialog>
</template>
