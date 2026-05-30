<script setup lang="ts">
import { useTranslation } from 'i18next-vue'
import { computed, ref } from 'vue'
import { useAsyncState, useTitle } from '@vueuse/core'
import { useRoute } from 'vue-router'
import Button from 'primevue/button'
import Message from 'primevue/message'
import CryptoJS from 'crypto-js'
import { client, eventBus } from '@/main.ts'
import type { MultiPastePart, Paste } from '@/types/paste.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import CopyButton from '@/components/CopyButton.vue'
import InputText from 'primevue/inputtext'
import InputGroup from 'primevue/inputgroup'
import { findFromFileName } from '@/utils/lang-replacements.ts'
import { resolveSharePreviewType, toSharePreviewLabel } from '@/utils/share-preview.ts'

const { t } = useTranslation()
const route = useRoute()
const password = ref<string | undefined>(undefined)
const decrypted = ref(false)
const paste = ref<Paste | undefined>(undefined)

useTitle('Reader & Share Modes | Pastefy')

const multiPasteParts = computed(() => {
  if (!paste.value || paste.value.type !== 'MULTI_PASTE') return []

  try {
    return JSON.parse(paste.value.content || '[]') as MultiPastePart[]
  } catch {
    return []
  }
})

const markdownParts = computed(() =>
  multiPasteParts.value.filter((part) => findFromFileName(part.name || '') === 'markdown'),
)

const presentationParts = computed(() =>
  multiPasteParts.value.filter((part) => resolveSharePreviewType(part.name)),
)

const modes = computed(() => {
  if (!paste.value) return []

  const origin = window.location.origin
  const items: Array<{
    key: string
    label: string
    description: string
    icon: string
    url: string
  }> = [
    {
      key: 'default',
      get label() { return t('views.readerShareModesView.options.defaultPaste') },
      get description() { return t('views.readerShareModesView.descriptions.theFullPastefyPastePageWithCodeActionsMetadataAndSharingTools') },
      icon: 'file-code',
      url: `${origin}/${paste.value.id}`,
    },
    {
      key: 'raw',
      get label() { return t('views.readerShareModesView.options.rawFile') },
      get description() { return t('views.readerShareModesView.descriptions.thePlainRawFileUrlForDirectDownloadsScriptsAndIntegrations') },
      icon: 'file-text',
      url: paste.value.raw_url || `${origin}/${paste.value.id}/raw`,
    },
  ]

  if (paste.value.type === 'PASTE' && findFromFileName(paste.value.title || '') === 'markdown') {
    items.push({
      key: 'article',
      get label() { return t('views.readerShareModesView.options.articleView') },
      get description() { return t('views.readerShareModesView.descriptions.aReaderFriendlyLayoutForSharingMarkdownLikeaPolishedArticle') },
      icon: 'article',
      url: `${origin}/${paste.value.id}/article`,
    })
  }

  if (paste.value.type === 'PASTE' && resolveSharePreviewType(paste.value.title)) {
    items.push({
      key: 'presentation',
      get label() { return t('views.readerShareModesView.options.presentationView') },
      get description() { return t('views.readerShareModesView.descriptions.aFocusedPreviewPageForTablesDiagramsMapsSvgsDiffsJsonHtmlAndTerminalReplays') },
      icon: 'presentation',
      url: `${origin}/${paste.value.id}/presentation`,
    })
  }

  if (paste.value.type === 'MULTI_PASTE' && markdownParts.value.length) {
    for (const part of markdownParts.value) {
      items.push({
        key: `article-${part.name}`,
        label: `Article: ${toSharePreviewLabel(part.name)}`,
        get description() { return t('views.readerShareModesView.descriptions.aDirectArticleLinkToThisMarkdownFileInsideTheMultiPaste') },
        icon: 'article',
        url: `${origin}/${paste.value.id}/article?part=${encodeURIComponent(part.name)}`,
      })
    }
  }

  if (paste.value.type === 'MULTI_PASTE' && presentationParts.value.length) {
    for (const part of presentationParts.value) {
      items.push({
        key: `presentation-${part.name}`,
        label: `Presentation: ${toSharePreviewLabel(part.name)}`,
        get description() { return t('views.readerShareModesView.descriptions.aDirectPresentationLinkToThisPreviewableFileInsideTheMultiPaste') },
        icon: 'presentation',
        url: `${origin}/${paste.value.id}/presentation?part=${encodeURIComponent(part.name)}`,
      })
    }
  }

  return items
})

const decrypt = () => {
  if (!paste.value || !password.value) return

  const decryptedContent = CryptoJS.AES.decrypt(paste.value.content, password.value).toString(
    CryptoJS.enc.Utf8,
  )
  if (decryptedContent === '') return

  paste.value.title = CryptoJS.AES.decrypt(paste.value.title, password.value).toString(
    CryptoJS.enc.Utf8,
  )
  paste.value.content = decryptedContent
  decrypted.value = true
}

const { isLoading, error } = useAsyncState(async () => {
  const pasteRes = (
    await client.get(`/api/v2/paste/${route.params.paste as string}`, {
      params: {
        from_frontend: 'true',
      },
    })
  ).data as Paste

  paste.value = pasteRes

  const hashPw = window.location.hash?.replace('#', '')
  if (hashPw && paste.value.encrypted) {
    password.value = hashPw
    decrypt()
  }

  eventBus.emit('pageLoaded', 'paste')
}, undefined)
</script>

<template>
  <ErrorContainer v-if="error" :error="error as any" />
  <LoadingContainer v-else-if="isLoading" class="flex items-center justify-center p-3" />

  <main v-else-if="paste?.encrypted && !decrypted" class="mx-auto max-w-[42rem]">
    <form
      class="flex flex-col items-center gap-3 rounded-3xl border border-neutral-200 bg-white/80 p-6 text-center shadow-sm dark:border-neutral-700 dark:bg-neutral-900/70"
      @submit.prevent="decrypt"
    >
      <i class="ti ti-lock text-5xl opacity-60" />
      <h1 class="text-2xl font-bold">{{ $t('views.readerShareModesView.thisShareSheetIsEncrypted') }}</h1>
      <p class="text-sm text-neutral-500 dark:text-neutral-400">
        Unlock the paste first to see all available reader and share modes.
      </p>
      <input
        v-model="password"
        type="password"
        :placeholder="$t('auth.password')"
        class="w-full rounded-xl border border-neutral-300 bg-white px-4 py-3 outline-none dark:border-neutral-700 dark:bg-neutral-800"
      />
      <Button type="submit" :label="$t('common.unlock')" />
    </form>
  </main>

  <main v-else-if="paste" class="mx-auto max-w-[1100px]">
    <section
      class="mb-6 overflow-hidden rounded-[2rem] border border-neutral-200 bg-white shadow-sm dark:border-neutral-800 dark:bg-neutral-900"
    >
      <header
        class="space-y-8 border-b border-neutral-200 bg-linear-to-br from-neutral-50 via-white to-neutral-100 px-6 py-8 md:px-10 md:py-10 dark:border-neutral-800 dark:from-neutral-900 dark:via-neutral-900 dark:to-neutral-800/60"
      >
        <div class="flex flex-wrap items-center gap-3 text-sm text-neutral-500 dark:text-neutral-400">
          <span
            class="inline-flex items-center gap-2 rounded-full border border-neutral-300 bg-white px-3 py-1 font-semibold tracking-[0.2em] uppercase dark:border-neutral-700 dark:bg-neutral-900"
          >
            <i class="ti ti-share-3" />
            {{ $t('views.readerShareModesView.readerShareModes') }}
          </span>
          <span>{{ paste.type === 'MULTI_PASTE' ? 'Multi-Paste' : 'Single Paste' }}</span>
        </div>

        <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div class="space-y-3">
            <h1 class="max-w-4xl text-4xl font-black tracking-tight md:text-5xl">
              {{ paste.title }}
            </h1>
            <p class="max-w-2xl text-sm text-neutral-600 dark:text-neutral-300">
              Pick the best way to present this paste, then copy the exact URL you want to share.
            </p>
          </div>

          <div class="flex flex-wrap gap-2">
            <Button
              as="router-link"
              :to="{ name: 'paste', params: { paste: paste.id } }"
              :label="$t('views.readerShareModesView.openPaste')"
              icon="ti ti-code"
              outlined
              severity="contrast"
            />
          </div>
        </div>
      </header>
    </section>

    <Message v-if="modes.length === 0" severity="warn">
      No special reader or share modes are available for this paste yet.
    </Message>

    <section v-else class="grid gap-4 md:grid-cols-2">
      <article
        v-for="mode in modes"
        :key="mode.key"
        class="rounded-2xl border border-neutral-200 bg-white p-5 shadow-sm dark:border-neutral-800 dark:bg-neutral-900"
      >
        <div class="mb-4 flex items-start justify-between gap-3">
          <div>
            <div class="mb-2 inline-flex items-center gap-2 text-sm font-semibold">
              <i :class="`ti ti-${mode.icon}`" />
              <span>{{ mode.label }}</span>
            </div>
            <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ mode.description }}</p>
          </div>
        </div>

        <InputGroup class="mb-4">
          <InputText
            :value="mode.url"
            readonly
            class="border-r-0 border-gray-200 select-all dark:border-neutral-700"
          />
          <CopyButton :contents="mode.url" />
        </InputGroup>

        <div class="flex flex-wrap gap-2">
          <Button as="a" :href="mode.url" target="_blank" :label="$t('common.open')" icon="ti ti-external-link" />
        </div>
      </article>
    </section>
  </main>
</template>
