export type SpecialPreviewType =
  | 'hcl'
  | 'log'
  | 'http'
  | 'github-actions'
  | 'ansible'
  | 'monitoring-rules'

export function resolveSpecialPreviewType(
  fileName?: string,
  contents?: string,
  type?: string,
): SpecialPreviewType | undefined {
  const name = (fileName || '').toLowerCase()
  const source = contents || ''
  const trimmed = source.trim()

  if (
    type === 'hcl' ||
    name.endsWith('.tf') ||
    name.endsWith('.tfvars') ||
    name.endsWith('.hcl')
  ) {
    return 'hcl'
  }

  if (
    type === 'log' ||
    name.endsWith('.log')
  ) {
    return 'log'
  }

  if (
    type === 'http' ||
    name.endsWith('.http') ||
    name.endsWith('.rest') ||
    trimmed.startsWith('curl ') ||
    /^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\s+https?:\/\//m.test(source)
  ) {
    return 'http'
  }

  if (
    (type === 'yaml' || type === 'json') &&
    (name.includes('workflow') || name.includes('action')) &&
    /\bon\s*:/i.test(source) &&
    /\bjobs\s*:/i.test(source)
  ) {
    return 'github-actions'
  }

  if (
    type === 'yaml' &&
    /\bhosts\s*:/i.test(source) &&
    (/\btasks\s*:/i.test(source) || /\broles\s*:/i.test(source))
  ) {
    return 'ansible'
  }

  if (
    ((type === 'yaml' || type === 'json') &&
      /\bgroups\s*:/i.test(source) &&
      /\brules\s*:/i.test(source) &&
      /\bexpr\s*:/i.test(source)) ||
    (type === 'json' && /"panels"\s*:/.test(source) && /"title"\s*:/.test(source))
  ) {
    return 'monitoring-rules'
  }

  return undefined
}
