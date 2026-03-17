import { computed, toValue, watchEffect, type MaybeRefOrGetter } from 'vue'
import { useTitle } from '@vueuse/core'

type SEOInput = {
  title: MaybeRefOrGetter<string>
  description: MaybeRefOrGetter<string>
}

export function useSEO(input: SEOInput) {
  const resolvedTitle = computed(() => toValue(input.title))
  const resolvedDescription = computed(() => toValue(input.description))

  useTitle(resolvedTitle)

  watchEffect(() => {
    if (typeof document === 'undefined') return

    const ensureMeta = (selector: string, attributes: Record<string, string>) => {
      let tag = document.head.querySelector(selector) as HTMLMetaElement | null
      if (!tag) {
        tag = document.createElement('meta')
        Object.entries(attributes).forEach(([key, value]) => tag!.setAttribute(key, value))
        document.head.appendChild(tag)
      }
      return tag
    }

    ensureMeta('meta[name="description"]', { name: 'description' }).setAttribute(
      'content',
      resolvedDescription.value,
    )
    ensureMeta('meta[property="og:title"]', { property: 'og:title' }).setAttribute(
      'content',
      resolvedTitle.value,
    )
    ensureMeta('meta[property="og:description"]', { property: 'og:description' }).setAttribute(
      'content',
      resolvedDescription.value,
    )
    ensureMeta('meta[name="twitter:title"]', { name: 'twitter:title' }).setAttribute(
      'content',
      resolvedTitle.value,
    )
    ensureMeta('meta[name="twitter:description"]', { name: 'twitter:description' }).setAttribute(
      'content',
      resolvedDescription.value,
    )
  })
}
