<script setup lang="ts">
import type { Paste } from '@/types/paste.ts'
import Highlighted from '@/components/Highlighted.vue'
import { computed, onMounted } from 'vue'
import CSVViewer from '@/components/previews/CSVViewer.vue'
import { useConfig } from '@/composables/config.ts'
import { getIconByFileName } from '@/utils/lang-information.ts'
import { useTagsStore } from '@/stores/tags-store.ts'
import SmallCardTag from '@/components/SmallCardTag.vue'
import { formatCompactNumber, formatLocalDateTime, formatRelativeDate } from '@/utils/format.ts'

const props = defineProps<{
  paste: Paste
  previewMaxHeight?: string
  selected?: boolean
  linkClass?: string
}>()

const tagsStore = useTagsStore()

const pasteContents = computed(() => {
  if (props.paste.type === 'MULTI_PASTE') {
    try {
      let json = props.paste.content.trim()

      // Json is being stripped when over 100chars so complete the json
      if (json.endsWith('"}...')) json += ']'
      if (json.endsWith('\\"...')) json += '"}]'
      if (json.endsWith('"...')) json += '}]'
      if (json.endsWith('...')) json += '"}]'

      return JSON.parse(json)[0]?.contents
    } catch {
      return ''
    }
  }

  return props.paste.content
})

const config = useConfig()
const getCopyContents = async () => {
  return await fetch(
    `${(import.meta.env.VITE_APP_BASE_Url as string) || ''}/${props.paste.id}/raw`,
    {
      headers: {
        Authorization: `Bearer ${config.value.apiKey}`,
      },
    },
  ).then((t) => t.text())
}

const icon = computed(() => getIconByFileName(props.paste.title))

onMounted(() => {
  tagsStore.fetchMultipleTagsIfNeeded(props.paste.tags)
})
</script>
<template>
  <router-link
    :to="{ name: 'paste', params: { paste: paste.id } }"
    class="transition-al w-fulll relative flex flex-col justify-between gap-3 rounded-xl border py-3"
    :class="
      (linkClass ? `${linkClass} ` : '') +
      (selected
        ? 'border-neutral-300 bg-neutral-200 hover:bg-neutral-300 dark:border-neutral-600 dark:bg-neutral-700 dark:hover:bg-neutral-600'
        : 'border-neutral-200 bg-neutral-100 hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-700')
    "
  >
    <div class="space-y-3 px-3">
      <div class="flex w-full justify-between gap-2">
        <div v-if="paste.encrypted">
          <i class="ti ti-lock" />
          <span class="font-bold">{{ $t('paste.encryptedPaste') }}</span>
        </div>
        <span
          class="flex items-center gap-1 truncate font-bold"
          v-else-if="paste.title.trim()"
          v-view-transition-name="`paste-${paste.id}-title`"
        >
          <i :class="`ti ti-${paste.type === 'MULTI_PASTE' ? 'files' : icon}`" />
          <span class="truncate">{{ paste.title }}</span>
        </span>
        <span class="italic opacity-40" v-else>{{ $t('paste.noTitle') }}</span>
      </div>

      <div
        class="min-h-[9.5rem] overflow-hidden rounded-md bg-neutral-200 dark:bg-neutral-900"
        :style="{
          'view-transition-name': `paste-${paste.id}-highlighted`,
        }"
      >
        <div class="p-3" v-if="paste.encrypted">
          <span class="font-italic text-sm opacity-60">{{ $t('paste.noPreview') }}</span>
        </div>
        <div
          v-else-if="paste.title?.endsWith('.csv')"
          class="overflow-hidden rounded-t-md"
          :class="previewMaxHeight || 'max-h-[9.5rem]'"
        >
          <CSVViewer :csv="pasteContents" />
        </div>
        <Highlighted
          v-else
          class="overflow-hidden"
          :class="previewMaxHeight || 'max-h-[9.5rem]'"
          hide-divider
          :key="pasteContents"
          :file-name="paste.title"
          :contents="pasteContents"
          :show-copy-button="paste.type !== 'MULTI_PASTE'"
          :getCopyContents
          small
        />
      </div>
    </div>

    <div class="flex w-full flex-wrap items-center gap-1 px-3" v-if="paste.tags?.length">
      <SmallCardTag
        v-for="tag of paste.tags.slice(0, 4)"
        :key="tag"
        :icon="tagsStore.tagsCache[tag]?.icon"
        :label="(tagsStore.tagsCache[tag]?.display_name || tag).substring(0, 15)"
        in-box
      />
    </div>

    <div class="flex w-full flex-wrap items-center justify-between gap-1 px-3">
      <div class="flex flex-wrap items-center gap-2">
        <img
          v-if="paste.user"
          :src="paste.user.avatar"
          alt="profile picture"
          class="h-[1.4rem] w-[1.4rem] rounded-full border border-neutral-200 object-cover dark:border-neutral-700"
          v-tooltip.bottom="{
            value: `${paste.user.display_name} - @${paste.user.name}`,
            showDelay: 500,
          }"
        />

        <SmallCardTag
          v-if="paste.view_count !== undefined"
          icon="eye"
          :label="formatCompactNumber(paste.view_count)"
        />
        <SmallCardTag
          v-if="paste.comment_count !== undefined"
          icon="messages"
          :label="formatCompactNumber(paste.comment_count)"
        />
        <SmallCardTag
          v-if="paste.star_count !== undefined"
          icon="star"
          :label="formatCompactNumber(paste.star_count)"
        />
      </div>
      <span
        class="truncate text-sm opacity-40"
        v-if="paste.created_at"
        :title="formatLocalDateTime(paste.created_at)"
      >
        {{ formatRelativeDate(paste.created_at) }}
      </span>
    </div>
  </router-link>
</template>
