<script lang="ts" setup>
import Dialog from 'primevue/dialog'
import QRCode from 'qrcode'
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  text: string
}>()

const visible = defineModel<boolean>('visible')
const qrCodeDataURL = ref<string | undefined>(undefined)

const renderQRCode = async () => {
  QRCode.toDataURL(
    props.text,
    {
      type: 'image/jpeg',
      width: 700,
      color: {
        dark: '#000',
        light: '#FFF',
      },
    },
    function (err: unknown, url: string) {
      if (err) throw err

      qrCodeDataURL.value = url
    },
  )
}

watch(
  () => props.text,
  () => {
    renderQRCode()
  },
)
watch(
  () => visible,
  () => {
    renderQRCode()
  },
)

onMounted(() => renderQRCode())
</script>
<template>
  <Dialog v-model:visible="visible" modal header="QR Code" class="w-[30rem] max-w-full">
    <div
      v-if="qrCodeDataURL"
      class="align-items-center justify-content-center relative flex w-full rounded-xl border-1 border-neutral-300 p-3 dark:border-neutral-700"
    >
      <div class="absolute top-0 right-0 p-2">
        <a :href="qrCodeDataURL" download="">
          <i class="ti ti-download scale-active icon-button" />
        </a>
      </div>
      <img class="w-10rem h-10rem border-round-md block" :src="qrCodeDataURL" alt="" />
    </div>
  </Dialog>
</template>
