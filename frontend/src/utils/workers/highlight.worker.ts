import '@/utils/highlight-imports.ts'
import { highlight } from '@/utils/highlight.ts'

onmessage = (e) => {
  const { contents, lang } = e.data
  postMessage({ highlighted: highlight(contents, lang, 250000) })
}
