<script setup lang="ts">
import type { Paste } from '@/types/paste.ts'
import Highlighted from '@/components/Highlighted.vue'
import { computed } from 'vue'
import CSVViewer from '@/components/previews/CSVViewer.vue'
import { useConfig } from '@/composables/config.ts'
import { getIconByFileName } from '@/utils/lang-information.ts'

const props = defineProps<{
  paste: Paste
  previewMaxHeight?: string
  selected?: boolean
}>()

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
    `${(import.meta.env.VITE_APP_BASE_URL as string) || ''}/${props.paste.id}/raw`,
    {
      headers: {
        Authorization: `Bearer ${config.value.apiKey}`,
      },
    },
  ).then((t) => t.text())
}

const icon = computed(() => getIconByFileName(props.paste.title))
</script>
<template>
  <router-link
    :to="{ name: 'paste', params: { paste: paste.id } }"
    class="relative rounded-xl border transition-all"
    :class="
      selected
        ? 'border-neutral-300 bg-neutral-200 hover:bg-neutral-300 dark:border-neutral-600 dark:bg-neutral-700 dark:hover:bg-neutral-600'
        : 'border-neutral-200 bg-neutral-100 hover:bg-neutral-200 dark:border-neutral-700 dark:bg-neutral-800 dark:hover:bg-neutral-700'
    "
  >
    <div class="flex justify-between gap-2 p-3 pb-0">
      <div v-if="paste.encrypted">
        <i class="ti ti-lock" />
        <span class="font-bold"> Encrypted paste </span>
      </div>
      <span
        class="flex items-center gap-1 truncate font-bold"
        v-else-if="paste.title.trim()"
        v-view-transition-name="`paste-${paste.id}-title`"
      >
        <i :class="`ti ti-${paste.type === 'MULTI_PASTE' ? 'files' : icon}`" />
        <span class="truncate">{{ paste.title }}</span>
      </span>
      <span class="italic opacity-40" v-else> no title </span>

      <div>
        <span class="truncate text-sm opacity-40" v-if="paste.created_at">
          {{ new Date(paste.created_at.replace(' ', 'T')).toLocaleString() }}
        </span>
      </div>
    </div>
    <div
      :style="{
        'view-transition-name': `paste-${paste.id}-highlighted`,
      }"
    >
      <div class="p-3" v-if="paste.encrypted">
        <span class="font-italic text-sm opacity-60">No preview available</span>
      </div>
      <div
        v-else-if="paste.title?.endsWith('.csv')"
        class="mx-3 mt-1 overflow-hidden rounded-t-md border border-b-0 border-neutral-200 dark:border-neutral-700"
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
      />
    </div>

    <div class="absolute right-0 bottom-0 m-1 flex items-center gap-2">
      <span
        v-if="paste.tags?.includes('codebox')"
        class="flex items-center gap-1 rounded-md border border-black/10 bg-black/5 px-1.5 backdrop-blur-2xl"
      >
        <i class="ti ti-box" />
        Codebox
      </span>
      <span
        v-else-if="paste.type === 'MULTI_PASTE'"
        class="flex rounded-md border border-black/10 bg-black/5 px-1.5 backdrop-blur-2xl"
      >
        Multi-Paste
      </span>

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
    </div>
  </router-link>
</template>
