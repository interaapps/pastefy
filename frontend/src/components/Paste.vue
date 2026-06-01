<script lang="ts" setup>
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Popover from 'primevue/popover'
import { useAsyncState, useClipboard, useTitle } from '@vueuse/core'
import { client, eventBus } from '@/main.ts'

import { computed, defineAsyncComponent, ref, useTemplateRef, watch } from 'vue'
import { useCurrentPasteStore } from '@/stores/current-paste.ts'
import PasteVisibilityIcon from '@/components/PasteVisibilityIcon.vue'
import type { MultiPastePart, Paste } from '@/types/paste.ts'
import { getIconByFileName } from '@/utils/lang-information.ts'
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
import ComponentInjection from '@/components/ComponentInjection.vue'
import PasteSharingPopover from '@/components/popovers/PasteSharingPopover.vue'
import PasteStarButton from '@/components/paste/PasteStarButton.vue'
import { useTranslation } from 'i18next-vue'
import PasteAiSummary from '@/components/PasteAiSummary.vue'
import SeverityScore from '@/components/paste/SeverityScore.vue'
import PasteComments from '@/components/paste/PasteComments.vue'
import type { PasteCommentMarker } from '@/types/paste-comment.ts'
const Highlighted = defineAsyncComponent(() => import('@/components/Highlighted.vue'))

const props = defineProps<{
  pasteId: string
  asEmbed?: boolean
  hideTitle?: boolean
  hideActions?: boolean
  hideEmbedBottom?: boolean
}>()

const password = ref<string | undefined>(undefined)

const currentPasteStore = useCurrentPasteStore()

const clipboard = useClipboard()

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

const { isLoading, error } = useAsyncState(async () => {
  const pasteRes = (
    await client.get(`/api/v2/paste/${props.pasteId}`, {
      params: {
        from_frontend: 'true',
        with_ai_analysis: 'true',
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
  eventBus.emit('pageLoaded', 'paste')
}, undefined)

const { isLoading: aiSummaryLoading, execute: generateAiSummary } = useAsyncState(
  async () => {
    await client.post(`/api/v2/paste/${props.pasteId}/ai-analysis`)
  },
  undefined,
  { immediate: false },
)

const fetchTags = () => {
  let i = 0
  return paste.value?.tags?.filter(() => ++i < 8).forEach(tagsStore.fetchIfNeeded)
}

const setValues = () => {
  if (!paste.value) return

  content.value = paste.value.content
  title.value = paste.value.title

  currentFileName.value = paste.value.title

  useTitle(`${title.value || 'Untitled Paste'} | Pastefy`)

  if (paste.value.type === 'MULTI_PASTE' && (!paste.value.encrypted || decrypted.value)) {
    multiPasteParts.value = JSON.parse(content.value!) as MultiPastePart[]
    selectMultiPart(0)
  }

  emit('update-height')
  setTimeout(() => emit('update-height'), 500)
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
  decrypted.value = true
  setValues()
}

const copy = () => {
  clipboard.copy(content.value!)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}

const confirm = useConfirm()
const { t } = useTranslation()
const deletePaste = () => {
  confirm.require({
    message: t('paste.deleteConfirm'),
    header: t('paste.deleteTitle'),
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
  canDelete:
    currentUser.user?.type === 'ADMIN' ||
    (paste.value?.user_id && currentUser.user?.id === paste.value?.user_id),
  canEdit: paste.value?.user_id && currentUser.user?.id === paste.value?.user_id,
  showVisibility:
    currentUser.user?.type === 'ADMIN' ||
    (paste.value?.user_id && currentUser.user?.id === paste.value?.user_id),
  isAdmin: currentUser.user?.type === 'ADMIN',
}))

const newTab = (u: string) => window.open(u)

const config = useConfig()

const tagsPopover = useTemplateRef<PopoverMethods>('tagsPopover')
const sharePopover = useTemplateRef<PopoverMethods>('sharePopover')
const comments = useTemplateRef<InstanceType<typeof PasteComments>>('comments')
const lineCommentMarkers = ref<PasteCommentMarker[]>([])

const canPreview = computed(
  () =>
    [
      'markdown',
      'csv',
      'mermaid',
      'mmd',
      'diff',
      'ics',
      'regex',
      'cast',
      'json',
      'xml',
      'yaml',
      'toml',
      'properties',
      'ini',
      'hcl',
      'http',
      'log',
    ].includes(currentLang.value) ||
    currentFileName.value?.endsWith('.tf') ||
    currentFileName.value?.endsWith('.tfvars') ||
    currentFileName.value?.endsWith('.hcl') ||
    currentFileName.value?.endsWith('.html') ||
    currentFileName.value?.endsWith('.htm') ||
    currentFileName.value?.endsWith('.svg') ||
    currentFileName.value?.endsWith('.cast') ||
    currentFileName.value?.endsWith('.mmd') ||
    currentFileName.value?.endsWith('.mermaid') ||
    currentFileName.value?.endsWith('.geojson'),
)

const previewRequiresManualOpen = computed(
  () =>
    currentLang.value === 'json' ||
    currentLang.value === 'xml' ||
    currentLang.value === 'yaml' ||
    currentLang.value === 'toml' ||
    currentLang.value === 'properties' ||
    currentLang.value === 'ini' ||
    currentFileName.value?.endsWith('.html') ||
    currentFileName.value?.endsWith('.htm'),
)

const showPreview = ref(true)

watch(
  [currentLang, currentFileName],
  () => {
    showPreview.value = !previewRequiresManualOpen.value
  },
  { immediate: true },
)

const averageAiSeverity = computed(() => {
  const warnings = paste.value?.ai_info?.warnings || []
  if (!warnings.length) return 0

  return Math.round(warnings.reduce((sum, warning) => sum + warning.severity, 0) / warnings.length)
})
</script>
<template>
  <div v-if="appInfo.appInfo?.login_required_for_read && !currentUser.user">
    <span class="block text-center text-neutral-500">{{ $t('paste.loginReadRequired') }}</span>
  </div>
  <ErrorContainer v-else-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" />
  <main v-else-if="paste?.encrypted && !decrypted" class="flex flex-col items-center gap-3">
    <form
      class="mx-auto flex w-[24rem] max-w-full flex-col items-center gap-3 rounded-xl border border-neutral-200 p-3 dark:border-neutral-700"
      @submit.prevent="decrypt"
    >
      <i class="ti ti-lock text-5xl opacity-60" />
      <h2>{{ $t('paste.passwordProtected') }}</h2>
      <InputText
        autofocus
        fluid
        size="small"
        :placeholder="$t('auth.password')"
        v-model="password"
        type="password"
      />
      <Button
        type="submit"
        :label="$t('paste.decrypt')"
        fluid
        size="small"
        outlined
        severity="contrast"
      />
    </form>

    <Button
      severity="danger"
      @click="deletePaste"
      text
      rounded
      v-if="!asEmbed && permission.canDelete"
      :label="$t('common.delete')"
      size="small"
      v-shortkey="['ctrl', 'd']"
      @shortkey="deletePaste"
    />
  </main>
  <div v-else-if="paste" class="flex h-full w-full justify-center">
    <div class="flex w-full flex-col gap-2" :class="config.sideBarShown ? '' : 'xl:pl-[3.75rem]'">
      <div class="flex items-center justify-between gap-3" v-if="!hideTitle">
        <div class="flex items-center gap-3">
          <h1
            class="text-2xl font-bold break-all"
            id="paste-title"
            v-if="title"
            v-view-transition-name="`paste-${paste.id}-title`"
          >
            {{ title }}
          </h1>
          <div
            class="flex items-center gap-1 rounded-md bg-neutral-100 px-1 py-0.5 dark:bg-neutral-800"
            v-if="permission.showVisibility"
            v-animate-css="{ classes: 'fadeIn', delay: 500 }"
          >
            <PasteVisibilityIcon :visibility="paste.visibility" />
            <span class="text-sm font-normal">{{
              $t(`paste.visibility.${paste.visibility.toLowerCase()}`)
            }}</span>
          </div>
        </div>
        <a
          v-if="content && averageAiSeverity >= 5 && content.split('\n').length > 20"
          href="#paste-ai-summary"
        >
          <SeverityScore :severity="averageAiSeverity" single />
        </a>
      </div>
      <ComponentInjection type="paste-below-title" :value="paste" />
      <div class="relative flex w-full flex-col gap-3 md:flex-row-reverse">
        <div
          class="top-0 flex h-fit flex-wrap items-center md:sticky md:w-[3rem] md:flex-col"
          v-animate-css="'fadeIn'"
          v-if="!hideActions"
        >
          <ComponentInjection type="paste-actions-first" :value="paste" />
          <Button
            @click="copy"
            severity="contrast"
            text
            rounded
            :icon="`ti ${copied ? 'ti-check text-green-500' : 'ti-copy'} text-xl`"
            v-tooltip="{ value: `${$t('common.copy')} (⌘+⇧+C)`, showDelay: 500 }"
            v-shortkey.click="['meta', 'shift', 'c']"
            @shortkey="copy"
            :aria-label="$t('common.copy')"
          />
          <PasteStarButton v-if="paste" :paste="paste" :paste-id="props.pasteId" />

          <Button
            v-if="!asEmbed"
            @click="comments?.openDialog()"
            severity="contrast"
            text
            rounded
            icon="ti ti-messages text-xl"
            v-tooltip="{ value: $t('comments.title'), showDelay: 500 }"
            :aria-label="$t('comments.title')"
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
            :aria-label="$t('components.paste.openInCodebox')"
          />
          <!-- <Button severity="contrast" text rounded icon="ti ti-bookmark text-xl" /> -->
          <Button
            @click="() => currentPasteStore.forkFrom(paste!)"
            severity="contrast"
            text
            rounded
            v-if="!asEmbed"
            icon="ti ti-git-fork text-xl"
            v-tooltip="{ value: `${$t('common.fork')} (ctrl+F)`, showDelay: 500 }"
            v-shortkey.click="['ctrl', 'f']"
            @shortkey="() => currentPasteStore.forkFrom(paste!)"
            :aria-label="$t('common.fork')"
          />
          <Button
            @click="() => currentPasteStore.editFrom(paste!, password)"
            severity="contrast"
            text
            rounded
            v-if="!asEmbed && permission.canEdit"
            icon="ti ti-pencil text-xl"
            v-tooltip="{ value: `${$t('common.edit')} (ctrl+E)`, showDelay: 500 }"
            v-shortkey.click="['ctrl', 'e']"
            @shortkey="() => currentPasteStore.editFrom(paste!, password)"
            :aria-label="$t('common.edit')"
          />
          <Button
            @click="(e) => sharePopover!.toggle(e)"
            v-if="!asEmbed"
            severity="contrast"
            text
            rounded
            icon="ti ti-share-3 text-xl"
            v-tooltip="{ value: $t('common.share'), showDelay: 500 }"
            :aria-label="$t('common.share')"
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
            v-tooltip="{ value: `${$t('common.raw')} (ctrl+R)`, showDelay: 500 }"
            v-shortkey.click="['ctrl', 'r']"
            @shortkey="newTab(paste.raw_url!)"
            :aria-label="$t('common.raw')"
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
            :aria-label="$t('common.download')"
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
            :aria-label="$t('common.tags')"
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
            :aria-label="$t('common.delete')"
          />
          <Button
            as="router-link"
            :to="{ name: 'paste-analytics', params: { paste: paste.id } }"
            severity="contrast"
            text
            rounded
            v-if="
              appInfo.appInfo?.analytics_enabled &&
              !asEmbed &&
              (permission.canEdit || permission.isAdmin)
            "
            icon="ti ti-chart-line text-xl"
            v-tooltip="{ value: 'Analytics', showDelay: 500 }"
            aria-label="Analytics"
          />
          <Button
            severity="contrast"
            @click="generateAiSummary()"
            :loading="aiSummaryLoading"
            text
            rounded
            v-if="!asEmbed && permission.isAdmin"
            icon="ti ti-sparkle text-xl"
            :aria-label="$t('paste.generateAiSummary')"
          />
          <ComponentInjection type="paste-actions-before-profile" :value="paste" />
          <component
            :is="asEmbed ? 'a' : 'router-link'"
            v-if="paste?.user?.avatar"
            :to="{ name: 'user', params: { user: paste.user.name } }"
            :href="router.resolve({ name: 'user', params: { user: paste.user.name } }).href"
            :target="asEmbed ? '_blank' : undefined"
            class="ml-2 block md:mt-2 md:ml-0"
            v-tooltip="{ value: paste.user.name || paste.user.display_name, showDelay: 500 }"
          >
            <img
              class="block h-[1.8rem] w-[1.8rem] rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
              :src="paste?.user?.avatar"
              alt="profile picture"
            />
          </component>
          <ComponentInjection type="paste-actions-last" :value="paste" />
        </div>
        <div class="w-full space-y-5 overflow-hidden">
          <div
            class="group relative w-full rounded-xl border border-neutral-200 bg-neutral-100 dark:border-neutral-700 dark:bg-neutral-800"
            :style="{
              'view-transition-name': `paste-${paste.id}-highlighted`,
            }"
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
                  class="rounded-none whitespace-pre"
                  text
                  :severity="multiPartIndex === index ? 'primary' : 'contrast'"
                  @click="selectMultiPart(index)"
                />
              </div>
            </div>

            <div
              v-if="canPreview"
              class="absolute right-1 z-1000 flex gap-1 opacity-0 transition-all group-hover:opacity-100"
              :class="paste.type === 'MULTI_PASTE' ? 'top-[2.57rem]' : 'top-1'"
            >
              <Button
                v-if="showPreview"
                :label="$t('paste.showCode')"
                size="small"
                severity="secondary"
                @click="showPreview = false"
                class="border border-neutral-200 bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-800"
              />
              <Button
                v-else
                :label="$t('paste.showPreview')"
                size="small"
                severity="secondary"
                @click="showPreview = true"
                class="border border-neutral-200 bg-neutral-50 dark:border-neutral-700 dark:bg-neutral-800"
              />
            </div>

            <template v-if="content">
              <PastePreview
                v-if="canPreview && showPreview"
                :contents="content"
                :file-name="currentFileName"
                :type="currentLang"
              />
              <Highlighted
                v-else
                :file-name="currentFileName"
                :key="multiPartIndex"
                :contents="content"
                :line-comment-markers="paste.type === 'PASTE' && !asEmbed ? lineCommentMarkers : []"
                :enable-line-comments="paste.type === 'PASTE' && !asEmbed"
                @line-click="
                  (event, line, target) => comments?.openLineComment(event, line, target)
                "
              />
            </template>

            <div
              v-if="asEmbed && !hideEmbedBottom"
              class="flex overflow-auto border-t border-neutral-200 p-0.5 dark:border-neutral-700"
            >
              <Button
                :label="$t('paste.viewOnPastefy')"
                as="a"
                :href="`${origin}/${paste.id}`"
                target="_blank"
                text
                size="small"
                severity="contrast"
              />
            </div>
          </div>
          <PasteAiSummary v-if="paste.ai_info" :ai-info="paste.ai_info" />
        </div>
      </div>

      <ComponentInjection type="paste-below-code" :value="paste" />
    </div>
  </div>

  <PasteSharingPopover
    ref="sharePopover"
    v-if="paste"
    :paste="paste"
    :content
    :asEmbed
    :currentFileName="currentFileName!"
    :pasteId
  />

  <PasteComments
    v-if="paste && !asEmbed"
    ref="comments"
    :paste-id="props.pasteId"
    :paste-user-id="paste.user_id"
    :paste-contents="content"
    :paste-file-name="currentFileName"
    :enable-line-comments="paste.type === 'PASTE'"
    @markers-updated="lineCommentMarkers = $event"
  />

  <Popover ref="tagsPopover">
    <div v-if="paste?.tags?.length" class="flex max-w-[20rem] flex-wrap gap-2">
      <component
        :is="asEmbed ? 'a' : 'router-link'"
        v-for="tag of paste.tags"
        :to="{ name: 'tag', params: { tag } }"
        :href="router.resolve({ name: 'tag', params: { tag } }).href"
        :target="asEmbed ? '_blank' : undefined"
        :key="tag"
        class="flex items-center gap-1 rounded-md border border-neutral-200 bg-neutral-100 p-0.5 px-1 text-sm hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-700"
      >
        <i
          v-if="tagsStore.tagsCache[tag]?.icon"
          :class="`ti ti-${tagsStore.tagsCache[tag]?.icon}`"
        />
        <span>{{ tagsStore.tagsCache[tag]?.display_name || tag }}</span>
      </component>
      <ComponentInjection type="paste-tags-popover-inner-after" :value="paste" />
    </div>
  </Popover>
</template>
