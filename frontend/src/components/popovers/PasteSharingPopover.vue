<script setup lang="ts">
import Popover from 'primevue/popover'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputGroup from 'primevue/inputgroup'
import type { Paste } from '@/types/paste.ts'
import EmbedPasteModal from '@/components/modals/EmbedPasteModal.vue'
import ScreenshotPaste from '@/components/modals/ScreenshotPaste.vue'
import QRModal from '@/components/modals/QRModal.vue'
import { computed, ref, useTemplateRef } from 'vue'
import type { PopoverMethods } from 'primevue'
import { useClipboard } from '@vueuse/core'
import CopyButton from '@/components/CopyButton.vue'

const pasteEmbedOpened = ref(false)
const pasteScreenshotOpened = ref(false)

const clipboard = useClipboard()

const props = defineProps<{
  paste: Paste
  currentFileName: string
  asEmbed?: boolean
  content?: string
  pasteId: string
}>()

const pasteUrl = computed(() => `${origin}/${props.paste?.id}`)

const share = () => {
  navigator.share({
    title: props.paste.title,
    text: 'Here is a paste I want to share with you',
    url: pasteUrl.value,
  })
}

const qrCodeShown = ref(false)

const sharePopover = useTemplateRef<PopoverMethods>('sharePopover')

defineExpose({
  toggle: (e: Event) => sharePopover.value!.toggle(e),
  show: (e: Event) => sharePopover.value!.show(e),
  hide: () => sharePopover.value!.hide(),
})
</script>

<template>
  <Popover ref="sharePopover">
    <div class="flex w-[16rem] max-w-full flex-col gap-2" v-if="paste">
      <div class="flex gap-2 rounded-lg border border-neutral-200 p-1 dark:border-neutral-700">
        <div class="flex w-[7rem] flex-col overflow-hidden rounded-md bg-black text-white">
          <span class="p-1 px-2 text-[8px]">{{ paste.title }}</span>
          <div class="flex w-full items-end justify-between">
            <div class="pb-1 pl-2">
              <img src="/icons/logo-dark.svg" class="w-[2.6rem]" />
            </div>

            <div class="mono h-[2rem] w-[3.3rem] rounded-tl-md bg-neutral-700 p-1 text-[6px]">
              {{ paste.content.substring(0, 100) }}}
            </div>
          </div>
        </div>

        <div>
          <span class="overflow-hidden text-sm">{{ paste.title }}</span> <br />
          <span class="text-[10px] opacity-60">Share code with Pastefy.</span>
        </div>
      </div>
      <InputGroup>
        <InputText
          size="small"
          :value="pasteUrl"
          class="border-r-0 border-gray-200 select-all dark:border-neutral-700"
          readonly
        />
        <CopyButton :contents="pasteUrl" />
      </InputGroup>

      <div class="flex w-full flex-col">
        <Button
          @click="share"
          severity="contrast"
          text
          fluid
          size="small"
          class="justify-start"
          icon="ti ti-share-2 text-lg"
          aria-label="Share"
          label="share"
        />
        <Button
          @click="
            () => {
              pasteScreenshotOpened = true
              sharePopover!.hide()
            }
          "
          severity="contrast"
          text
          fluid
          size="small"
          class="justify-start"
          icon="ti ti-camera text-lg"
          aria-label="Create Screenshot"
          label="screenshot"
        />
        <Button
          @click="
            () => {
              pasteEmbedOpened = true
              sharePopover!.hide()
            }
          "
          severity="contrast"
          size="small"
          class="justify-start"
          text
          fluid
          icon="ti ti-code text-lg"
          aria-label="Embed"
          label="embed"
        />
        <Button
          @click="
            () => {
              qrCodeShown = true
              sharePopover!.hide()
            }
          "
          severity="contrast"
          size="small"
          class="justify-start"
          text
          fluid
          icon="ti ti-qrcode text-lg"
          label="qr code"
        />
      </div>
    </div>
  </Popover>

  <ScreenshotPaste
    :id="paste?.id"
    :title="currentFileName"
    v-if="paste && !asEmbed"
    :file-name="currentFileName"
    :content="content!"
    v-model:visible="pasteScreenshotOpened"
  />
  <EmbedPasteModal v-model:visible="pasteEmbedOpened" :paste-id="pasteId" />
  <QRModal :text="pasteUrl" v-model:visible="qrCodeShown" />
</template>
