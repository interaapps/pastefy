import hljsCore from 'highlight.js/lib/core'
export const hljs = hljsCore

const escapeHtml = (str: string) => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

export function highlight(contents: string, lang?: string, maxLength = 25000): string {
  if (contents.length < maxLength) {
    console.log({ a: lang, b: hljs.listLanguages(), lang, l: hljs.listLanguages() })
    if (lang && hljs.listLanguages().includes(lang)) {
      return hljs.highlight(contents, {
        language: lang,
      }).value
    } else if (contents.length < 25000) {
      // Detecting a language is expensive, so only do it for small files
      return hljs.highlightAuto(contents).value
    }
  }

  return escapeHtml(contents)
}
