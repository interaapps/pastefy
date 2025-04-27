<script lang="ts" setup>
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Popover from 'primevue/popover'
import InputGroup from 'primevue/inputgroup'
import { useAsyncState, useClipboard, useTitle } from '@vueuse/core'
import { client } from '@/main.ts'

import { computed, ref, useTemplateRef } from 'vue'
import Highlighted from '@/components/Highlighted.vue'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
import ScreenshotPaste from '@/components/modals/ScreenshotPaste.vue'
import PasteVisibilityIcon from '@/components/PasteVisibilityIcon.vue'
import type { MultiPastePart, Paste } from '@/types/paste.ts'
import { getIconByFileName } from '@/utils/lang-information.ts'
import EmbedPasteModal from '@/components/modals/EmbedPasteModal.vue'
import CryptoJS from 'crypto-js'
import { findFromFileName } from '@/utils/lang-replacements.ts'
import { useConfirm } from 'primevue/useconfirm'
import router from '@/router'
import { useAppInfoStore } from '@/stores/app-info.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import { useCurrentUserStore } from '@/stores/current-user.ts'
import PastePreview from '@/components/PastePreview.vue'
import { useConfig } from '@/composables/config.ts'
import { useTagsStore } from '@/stores/tags-store.ts'
import type { PopoverMethods } from 'primevue'

const props = defineProps<{
  pasteId: string
  asEmbed?: boolean
}>()

const password = ref<string | undefined>(undefined)

const currentPasteStore = useCurrentPasteStore()

const clipboard = useClipboard()

const pasteScreenshotOpened = ref(false)
const pasteEmbedOpened = ref(false)

const content = ref<string | undefined>()
const title = ref<string | undefined>()

const currentFileName = ref<string | undefined>()

const multiPasteParts = ref<MultiPastePart[] | undefined>(undefined)
const multiPartIndex = ref(0)

const emit = defineEmits(['update-height'])

const selectMultiPart = (index: number) => {
  content.value = multiPasteParts.value![index]?.contents ?? ''
  currentFileName.value = multiPasteParts.value![index]?.name ?? ''
  multiPartIndex.value = index
  emit('update-height')
}
const origin = window.location.origin

const paste = ref<Paste | undefined>(undefined)

const tagsStore = useTagsStore()

const { isLoading: starLoading, execute: star } = useAsyncState(
  async () => {
    if (paste.value?.starred) {
      await client.delete(`/api/v2/paste/${props.pasteId}/star`)
      paste.value.starred = false
    } else {
      await client.post(`/api/v2/paste/${props.pasteId}/star`)
      paste.value!.starred = true
    }
  },
  undefined,
  { immediate: false },
)

const { isLoading, error } = useAsyncState(async () => {
  const pasteRes = (
    await client.get(`/api/v2/paste/${props.pasteId}`, {
      params: {
        from_frontend: 'true',
      },
    })
  ).data as Paste

  paste.value = pasteRes
  if (password.value) {
    decrypt()
  } else {
    setValues()
  }

  const hashPw = window.location.hash?.replace('#', '')
  if (hashPw) {
    password.value = hashPw
    decrypt()
  }
}, undefined)

const fetchTags = () => paste.value?.tags?.forEach(tagsStore.fetchIfNeeded)

const setValues = () => {
  if (!paste.value) return

  content.value = paste.value.content
  title.value = paste.value.title

  currentFileName.value = paste.value.title

  useTitle(`${title.value || 'Untitled Paste'} | Pastefy`)

  if (paste.value.type === 'MULTI_PASTE') {
    multiPasteParts.value = JSON.parse(content.value!) as MultiPastePart[]
    selectMultiPart(0)
  }

  emit('update-height')
}

const copied = ref(false)

const decrypted = ref(false)

const decrypt = () => {
  if (!paste.value) return

  const decryptedContent = CryptoJS.AES.decrypt(paste.value.content, password.value!).toString(
    CryptoJS.enc.Utf8,
  )
  if (decryptedContent === '') return

  paste.value.title = CryptoJS.AES.decrypt(paste.value.title, password.value!).toString(
    CryptoJS.enc.Utf8,
  )
  paste.value.content = decryptedContent
  setValues()
  decrypted.value = true
}

const copy = () => {
  clipboard.copy(content.value!)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}

const confirm = useConfirm()
const deletePaste = () => {
  confirm.require({
    message: 'Are you sure you want to delete this paste?',
    header: 'Delete paste',
    accept: async () => {
      await client.delete(`/api/v2/paste/${props.pasteId}`)
      router.push({ name: 'home-page' })
    },
  })
}

const download = () => {
  const element = document.createElement('a')
  element.setAttribute(
    'href',
    `data:text/plain;charset=utf-8,${encodeURIComponent(content.value!)}`,
  )
  element.setAttribute('download', currentFileName.value!)
  element.style.display = 'none'
  document.body.appendChild(element)
  element.click()
  document.body.removeChild(element)
}

const currentLang = computed(() => findFromFileName(currentFileName.value!) as string)
const appInfo = useAppInfoStore()

const currentUser = useCurrentUserStore()

const permission = computed(() => ({
  canDelete: paste.value?.user_id && currentUser.user?.id === paste.value?.user_id,
  canEdit: paste.value?.user_id && currentUser.user?.id === paste.value?.user_id,
  showVisibility: paste.value?.user_id && currentUser.user?.id === paste.value?.user_id,
}))

const newTab = (u: string) => window.open(u)

const config = useConfig()

const tagsPopover = useTemplateRef<PopoverMethods>('tagsPopover')
const sharePopover = useTemplateRef<PopoverMethods>('sharePopover')

const pasteUrl = computed(() => `${origin}/${paste.value?.id}`)

const share = () => {
  navigator.share({
    title: paste.value?.title,
    text: 'Here is a paste I want to share with you',
    url: pasteUrl.value,
  })
}
</script>
<template>
  <div v-if="appInfo.appInfo?.login_required_for_read && !currentUser.user">
    <span class="block text-center text-neutral-500"> Login is required to view pastes </span>
  </div>
  <ErrorContainer v-else-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" />
  <main v-else-if="paste?.encrypted && !decrypted" class="flex flex-col items-center gap-3">
    <form
      class="mx-auto flex w-[24rem] max-w-full flex-col items-center gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
      @submit.prevent="decrypt"
    >
      <i class="ti ti-lock text-5xl opacity-60" />
      <h2>Paste is password protected</h2>
      <InputText
        autofocus
        fluid
        size="small"
        placeholder="password"
        v-model="password"
        type="password"
      />
      <Button type="submit" label="decrypt" fluid size="small" outlined severity="contrast" />
    </form>

    <Button
      severity="danger"
      @click="deletePaste"
      text
      rounded
      v-if="!asEmbed && permission.canDelete"
      label="delete"
      size="small"
      v-shortkey="['ctrl', 'd']"
      @shortkey="deletePaste"
    />
  </main>
  <div v-else-if="paste" class="flex h-full w-full justify-center">
    <div class="flex w-full flex-col gap-2" :class="config.sideBarShown ? '' : 'xl:pl-[3.75rem]'">
      <div class="flex items-center gap-3">
        <h1 class="text-2xl font-bold break-all" id="paste-title" v-if="title">{{ title }}</h1>
        <div
          class="flex items-center gap-1 rounded-md bg-neutral-100 px-1 py-0.5 dark:bg-neutral-800"
          v-if="permission.showVisibility"
        >
          <PasteVisibilityIcon :visibility="paste.visibility" />
          <span class="text-sm font-normal">{{ paste.visibility }}</span>
        </div>
      </div>
      <div id="below-title" />
      <div class="relative flex w-full flex-col gap-3 md:flex-row-reverse">
        <div class="top-0 flex h-fit flex-wrap items-center md:sticky md:w-[3rem] md:flex-col">
          <Button
            @click="copy"
            severity="contrast"
            text
            rounded
            :icon="`ti ${copied ? 'ti-check text-green-500' : 'ti-copy'} text-xl`"
            v-tooltip="{ value: 'Copy (⌘+⇧+C)', showDelay: 500 }"
            v-shortkey.click="['meta', 'shift', 'c']"
            @shortkey="copy"
            aria-label="Copy"
          />
          <Button
            v-if="currentUser.user?.logged_in"
            @click="() => star()"
            severity="contrast"
            text
            rounded
            :icon="`ti ti-star text-xl ${paste.starred ? 'text-yellow-500' : ''}`"
            v-tooltip="{ value: 'Star', showDelay: 500 }"
            :loading="starLoading"
            aria-label="Star"
          />
          <Button
            v-if="paste.tags?.includes('codebox')"
            target="_blank"
            as="a"
            :href="`https://box.pastefy.app/${paste.id}`"
            severity="contrast"
            text
            rounded
            :icon="`ti ti-box text-xl`"
            v-tooltip="{ value: 'Open in Codebox', showDelay: 500 }"
            aria-label="Open in Codebox"
          />
          <!-- <Button severity="contrast" text rounded icon="ti ti-bookmark text-xl" /> -->
          <Button
            @click="() => currentPasteStore.forkFrom(paste!)"
            severity="contrast"
            text
            rounded
            v-if="!asEmbed"
            icon="ti ti-git-fork text-xl"
            v-tooltip="{ value: 'Fork (ctrl+F)', showDelay: 500 }"
            v-shortkey.click="['ctrl', 'f']"
            @shortkey="() => currentPasteStore.forkFrom(paste!)"
            aria-label="Fork"
          />
          <Button
            @click="() => currentPasteStore.editFrom(paste!, password)"
            severity="contrast"
            text
            rounded
            v-if="!asEmbed && permission.canEdit"
            icon="ti ti-pencil text-xl"
            v-tooltip="{ value: 'Edit (ctrl+E)', showDelay: 500 }"
            v-shortkey.click="['ctrl', 'e']"
            @shortkey="() => currentPasteStore.editFrom(paste!, password)"
            aria-label="Edit"
          />
          <Button
            @click="(e) => sharePopover!.toggle(e)"
            v-if="!asEmbed"
            severity="contrast"
            text
            rounded
            icon="ti ti-share-3 text-xl"
            v-tooltip="{ value: 'Share', showDelay: 500 }"
            @shortkey="pasteScreenshotOpened = true"
            aria-label="Share"
          />
          <Button
            as="a"
            v-if="!paste.encrypted"
            :href="`${paste.raw_url}${paste.type === 'MULTI_PASTE' ? `?part=${currentFileName}` : ''}`"
            target="_blank"
            severity="contrast"
            class="px-1"
            label="RAW"
            text
            rounded
            style="letter-spacing: -1.5px"
            :pt="{
              label: 'text-sm font-bold',
            }"
            v-tooltip="{ value: 'Show raw code (ctrl+R)', showDelay: 500 }"
            v-shortkey.click="['ctrl', 'r']"
            @shortkey="newTab(paste.raw_url!)"
            aria-label="Show Raw"
          />
          <Button
            @click="download"
            severity="contrast"
            text
            rounded
            icon="ti ti-download text-xl"
            v-tooltip="{ value: 'Download (ctrl+S)', showDelay: 500 }"
            v-shortkey.click="['ctrl', 's']"
            @shortkey="download"
            aria-label="Download"
          />
          <Button
            v-if="paste?.tags?.length"
            @click="
              (e) => {
                fetchTags()
                tagsPopover?.toggle(e)
              }
            "
            severity="contrast"
            text
            rounded
            icon="ti ti-label text-xl rotate-[-45deg]"
            v-tooltip="{ value: 'Tags', showDelay: 500 }"
            aria-label="Tags"
          />
          <Button
            severity="contrast"
            @click="deletePaste"
            text
            rounded
            v-if="!asEmbed && permission.canDelete"
            icon="ti ti-trash text-xl"
            v-tooltip="{ value: 'Delete (ctrl+⌫)', showDelay: 500 }"
            v-shortkey="['ctrl', 'backspace']"
            @shortkey="deletePaste"
            aria-label="Delete"
          />
          <router-link
            v-if="paste?.user?.avatar"
            :to="{ name: 'user', params: { user: paste.user.name } }"
            class="ml-2 block md:mt-2 md:ml-0"
            v-tooltip="{ value: paste.user.name || paste.user.display_name, showDelay: 500 }"
          >
            <img
              class="block h-[1.8rem] w-[1.8rem] rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
              :src="paste?.user?.avatar"
              alt="profile picture"
            />
          </router-link>
        </div>
        <div class="w-full overflow-hidden">
          <div
            class="w-full rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
          >
            <div
              v-if="multiPasteParts"
              class="flex overflow-auto border-b border-neutral-200 dark:border-neutral-700"
            >
              <div
                v-for="(part, index) of multiPasteParts"
                class="border-r border-neutral-200 dark:border-neutral-700"
                :key="part.name"
              >
                <Button
                  :label="part.name"
                  size="small"
                  :icon="`ti ti-${getIconByFileName(part.name)}`"
                  class="rounded-none"
                  text
                  :severity="multiPartIndex === index ? 'primary' : 'contrast'"
                  @click="selectMultiPart(index)"
                />
              </div>
            </div>

            <template v-if="content">
              <PastePreview
                v-if="['markdown', 'csv'].includes(currentLang)"
                :contents="content"
                :file-name="currentFileName"
                :type="currentLang"
              />
              <Highlighted
                v-else
                :file-name="currentFileName"
                :key="multiPartIndex"
                :contents="content"
              />
            </template>

            <div
              v-if="asEmbed"
              class="flex overflow-auto border-t border-neutral-200 p-0.5 dark:border-neutral-700"
            >
              <Button
                label="view on pastefy"
                as="a"
                :href="`${origin}/${paste.id}`"
                target="_blank"
                text
                size="small"
                severity="contrast"
              />
            </div>
          </div>
        </div>
      </div>

      <div id="below-code" />
    </div>
  </div>

  <ScreenshotPaste
    :id="paste?.id"
    :title="currentFileName"
    v-if="paste && !asEmbed"
    :file-name="currentFileName"
    :content="content!"
    v-model:visible="pasteScreenshotOpened"
  />
  <EmbedPasteModal v-model:visible="pasteEmbedOpened" :paste-id="pasteId" />

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
        <InputText size="small" :value="pasteUrl" class="select-all" readonly />
        <Button
          size="small"
          icon="ti ti-copy"
          severity="contrast"
          @click="clipboard.copy(pasteUrl)"
        />
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
          v-tooltip="{ value: 'Screenshot (ctrl+shift+S)', showDelay: 500 }"
          v-shortkey.click="['ctrl', 'shift', 's']"
          @shortkey="pasteScreenshotOpened = true"
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
          v-tooltip="{ value: 'Embed', showDelay: 500 }"
          aria-label="Embed"
          label="embed"
        />
      </div>
    </div>
  </Popover>
  <Popover ref="tagsPopover">
    <div v-if="paste?.tags?.length" class="flex flex-wrap gap-2">
      <router-link
        v-for="tag of paste.tags"
        :to="{ name: 'tag', params: { tag } }"
        :key="tag"
        class="flex items-center gap-1 rounded-md border border-neutral-200 bg-neutral-100 p-0.5 px-1 text-sm hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-700"
      >
        <i
          v-if="tagsStore.tagsCache[tag]?.icon"
          :class="`ti ti-${tagsStore.tagsCache[tag]?.icon}`"
        />
        <span>{{ tagsStore.tagsCache[tag]?.display_name || tag }}</span>
      </router-link>
    </div>
  </Popover>
</template>
