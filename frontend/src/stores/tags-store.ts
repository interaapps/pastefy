import { ref } from 'vue'
import { defineStore } from 'pinia'
import { client } from '@/main.ts'
import type { Tag } from '@/types/tags.ts'

export const useTagsStore = defineStore('tag-cache', () => {
  const tagsCache = ref<Record<string, Tag>>({})

  const fetchIfNeeded = async (tag: string) => {
    if (!tagsCache.value[tag])
      tagsCache.value[tag] = (await client.get(`/api/v2/public/tags/${tag}`)).data as Tag
  }

  return { tagsCache, fetchIfNeeded }
})
