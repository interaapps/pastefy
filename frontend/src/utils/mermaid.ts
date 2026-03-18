const stripMarkdownInline = (value: string) =>
  value
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '$1')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '$1')
    .replace(/`([^`]+)`/g, '$1')
    .replace(/\*\*([^*]+)\*\*/g, '$1')
    .replace(/__([^_]+)__/g, '$1')
    .replace(/\*([^*]+)\*/g, '$1')
    .replace(/_([^_]+)_/g, '$1')
    .replace(/~~([^~]+)~~/g, '$1')

export const normalizeMermaidText = (value: unknown, fallback = 'Untitled') => {
  const normalized = String(value ?? '')
    .replace(/\r?\n+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  const markdownSafe = stripMarkdownInline(normalized).trim()
  return markdownSafe || fallback
}

export const toMermaidQuotedLabel = (value: unknown, fallback = 'Untitled') =>
  JSON.stringify(normalizeMermaidText(value, fallback))

export const toMermaidSafeIdentifier = (value: unknown, fallback = 'node') => {
  const normalized = normalizeMermaidText(value, fallback)
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')

  if (!normalized) return fallback
  return /^[0-9]/.test(normalized) ? `${fallback}_${normalized}` : normalized
}

export const toMermaidBareLabel = (value: unknown, fallback = 'label') => {
  const normalized = normalizeMermaidText(value, fallback)
    .replace(/["'`[\]{}()<>:|]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  return normalized || fallback
}
