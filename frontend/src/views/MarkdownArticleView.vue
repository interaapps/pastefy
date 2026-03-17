<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useAsyncState, useTitle } from '@vueuse/core'
import { useRoute } from 'vue-router'
import { useRouteQuery } from '@vueuse/router'
import Button from 'primevue/button'
import Message from 'primevue/message'
import CryptoJS from 'crypto-js'
import { client, eventBus } from '@/main.ts'
import type { MultiPastePart, Paste } from '@/types/paste.ts'
import ErrorContainer from '@/components/ErrorContainer.vue'
import LoadingContainer from '@/components/LoadingContainer.vue'
import MarkdownViewer from '@/components/previews/MarkdownViewer.vue'
import { findFromFileName } from '@/utils/lang-replacements.ts'

const route = useRoute()
const selectedPart = useRouteQuery<string>('part', '')

const password = ref<string | undefined>(undefined)
const decrypted = ref(false)
const paste = ref<Paste | undefined>(undefined)
const title = ref<string>('')
const markdown = ref<string>('')

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

const activeMarkdownPart = computed(() => {
  if (paste.value?.type !== 'MULTI_PASTE') return undefined

  return (
    markdownParts.value.find((part) => part.name === selectedPart.value) || markdownParts.value[0]
  )
})

const isMarkdownPaste = computed(() => {
  if (!paste.value) return false
  if (paste.value.type === 'PASTE') return findFromFileName(paste.value.title || '') === 'markdown'
  return markdownParts.value.length > 0
})

const formatDate = (value?: string) => {
  if (!value) return ''
  return new Date(value.replace(' ', 'T')).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const toArticleLabel = (value?: string) => {
  if (!value) return 'Untitled Article'
  return value.replace(/\.md$/i, '')
}

const setValues = () => {
  if (!paste.value) return
  if (paste.value.type === 'MULTI_PASTE') {
    const part = activeMarkdownPart.value
    title.value = toArticleLabel(part?.name || paste.value.title || 'Untitled Article')
    markdown.value = part?.contents || ''
  } else {
    title.value = toArticleLabel(paste.value.title || 'Untitled Article')
    markdown.value = paste.value.content
  }
  useTitle(`${title.value} | Pastefy`)
}

watch(activeMarkdownPart, () => {
  if (paste.value?.type === 'MULTI_PASTE' && activeMarkdownPart.value) {
    setValues()
  }
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
  setValues()
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

  if (pasteRes.type === 'MULTI_PASTE' && !selectedPart.value) {
    selectedPart.value = markdownParts.value[0]?.name || ''
  }

  const hashPw = window.location.hash?.replace('#', '')
  if (hashPw && paste.value.encrypted) {
    password.value = hashPw
    decrypt()
  } else {
    setValues()
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
      <h1 class="text-2xl font-bold">This article is encrypted</h1>
      <p class="text-sm text-neutral-500 dark:text-neutral-400">
        Add the password to the URL hash or unlock it here to view the markdown article.
      </p>
      <input
        v-model="password"
        type="password"
        placeholder="Password"
        class="w-full rounded-xl border border-neutral-300 bg-white px-4 py-3 outline-none dark:border-neutral-700 dark:bg-neutral-800"
      />
      <Button type="submit" label="Unlock Article" />
    </form>
  </main>

  <main v-else-if="paste" class="mx-auto max-w-[56rem]">
    <div v-if="!isMarkdownPaste" class="space-y-4">
      <Message severity="warn">
        This article view is only available for markdown pastes.
      </Message>
      <Button
        as="router-link"
        :to="{ name: 'paste', params: { paste: paste.id } }"
        label="Open Regular Paste View"
        icon="ti ti-arrow-right"
      />
    </div>

    <article
      v-else
      class="overflow-hidden rounded-[2rem] border border-neutral-200 bg-white shadow-sm dark:border-neutral-800 dark:bg-neutral-900"
    >
      <header
        class="border-b border-neutral-200 bg-linear-to-br from-neutral-50 via-white to-neutral-100 px-6 py-8 dark:border-neutral-800 dark:from-neutral-900 dark:via-neutral-900 dark:to-neutral-800/60 md:px-10 md:py-10"
      >
        <div class="mb-4 flex flex-wrap items-center gap-3 text-sm text-neutral-500 dark:text-neutral-400">
          <span
            class="inline-flex items-center gap-2 rounded-full border border-neutral-300 bg-white px-3 py-1 font-semibold tracking-[0.2em] uppercase dark:border-neutral-700 dark:bg-neutral-900"
          >
            <i class="ti ti-article" />
            Markdown Article
          </span>
          <span v-if="paste.created_at">{{ formatDate(paste.created_at) }}</span>
        </div>

        <h1 class="max-w-4xl text-4xl font-black tracking-tight text-balance md:text-5xl">
          {{ title }}
        </h1>

        <div
          v-if="paste.user"
          class="mt-6 flex items-center gap-3 text-sm text-neutral-600 dark:text-neutral-300"
        >
          <img
            :src="paste.user.avatar"
            :alt="paste.user.display_name"
            class="h-10 w-10 rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
          />
          <div>
            <p class="font-semibold">{{ paste.user.display_name }}</p>
            <router-link
              :to="{ name: 'user', params: { user: paste.user.name } }"
              class="opacity-70 hover:underline"
            >
              @{{ paste.user.name }}
            </router-link>
          </div>
        </div>

        <div class="mt-6 flex flex-wrap gap-2">
          <Button
            v-if="paste.type === 'MULTI_PASTE'"
            as="router-link"
            :to="{ name: 'paste', params: { paste: paste.id } }"
            label="Open Multi-Paste View"
            icon="ti ti-files"
            outlined
            severity="contrast"
          />
          <Button
            v-else
            as="router-link"
            :to="{ name: 'paste', params: { paste: paste.id } }"
            label="Open Code View"
            icon="ti ti-code"
            outlined
            severity="contrast"
          />
          <Button
            as="a"
            :href="paste.raw_url"
            target="_blank"
            label="Raw Markdown"
            icon="ti ti-file-text"
            text
            severity="contrast"
          />
        </div>
      </header>

      <div class="px-2 py-4 md:px-6 md:py-8">
        <div
          v-if="paste.type === 'MULTI_PASTE' && markdownParts.length > 1"
          class="mb-6 flex flex-wrap gap-2 px-2 md:px-4"
        >
          <Button
            v-for="part in markdownParts"
            :key="part.name"
            :label="toArticleLabel(part.name)"
            size="small"
            :severity="part.name === activeMarkdownPart?.name ? 'primary' : 'contrast'"
            :outlined="part.name !== activeMarkdownPart?.name"
            @click="selectedPart = part.name"
          />
        </div>
        <div class="markdown-article">
          <MarkdownViewer :markdown="markdown" />
        </div>
      </div>
    </article>
  </main>
</template>
