import { ref } from 'vue'
import { defineStore } from 'pinia'
import { client } from '@/main.ts'
import type { Tag } from '@/types/tags.ts'

export const useTagsStore = defineStore('tag-cache', () => {
  const tagsCache = ref<Record<string, Tag>>({})

  const fetching = ref<string[]>([])

  const fetchIfNeeded = async (tag: string) => {
    if (fetching.value.includes(tag)) return

    fetching.value.push(tag)

    if (!tagsCache.value[tag]) {
      tagsCache.value[tag] = (await client.get(`/api/v2/public/tags/${tag}`)).data as Tag
    }
    fetching.value.splice(fetching.value.indexOf(tag), 1)
  }

  const fetchMultipleTagsIfNeeded = (tags?: string[], max = 99) => {
    let i = 0
    return tags?.filter(() => ++i < max).forEach(fetchIfNeeded)
  }

  return { tagsCache, fetchIfNeeded, fetchMultipleTagsIfNeeded }
})
