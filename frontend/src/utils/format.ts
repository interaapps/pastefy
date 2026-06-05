const relativeTimeFormatter = new Intl.RelativeTimeFormat(undefined, { numeric: 'auto' })
const localDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
})
const relativeTimeUnits: Array<{ unit: Intl.RelativeTimeFormatUnit; seconds: number }> = [
  { unit: 'year', seconds: 60 * 60 * 24 * 365 },
  { unit: 'month', seconds: 60 * 60 * 24 * 30 },
  { unit: 'week', seconds: 60 * 60 * 24 * 7 },
  { unit: 'day', seconds: 60 * 60 * 24 },
  { unit: 'hour', seconds: 60 * 60 },
  { unit: 'minute', seconds: 60 },
  { unit: 'second', seconds: 1 },
]

export const parseApiDate = (value?: string) => {
  const trimmed = value?.trim()
  if (!trimmed || trimmed.startsWith('0000-00-00')) return null

  let normalized = trimmed.replace(' ', 'T')
  if (!/[zZ]|[+-]\d{2}:?\d{2}$/.test(normalized)) {
    normalized += 'Z'
  }

  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export const formatRelativeDate = (value?: string) => {
  const date = parseApiDate(value)
  if (!date) return ''

  const diffSeconds = (date.getTime() - Date.now()) / 1000
  const unit =
    relativeTimeUnits.find((candidate) => Math.abs(diffSeconds) >= candidate.seconds) ||
    relativeTimeUnits[relativeTimeUnits.length - 1]

  return relativeTimeFormatter.format(Math.round(diffSeconds / unit.seconds), unit.unit)
}

export const formatLocalDateTime = (value?: string) => {
  const date = parseApiDate(value)
  return date ? localDateTimeFormatter.format(date) : ''
}

export const formatCompactNumber = (value?: number) => {
  if (value === undefined || value === null) return ''

  const abs = Math.abs(value)
  const format = (divisor: number, suffix: string) => {
    const compact = value / divisor
    const maximumFractionDigits = Math.abs(compact) < 10 && abs % divisor !== 0 ? 1 : 0
    return `${new Intl.NumberFormat(undefined, { maximumFractionDigits }).format(compact)}${suffix}`
  }

  if (abs >= 1_000_000_000) return format(1_000_000_000, 'b')
  if (abs >= 1_000_000) return format(1_000_000, 'm')
  if (abs >= 1_000) return format(1_000, 'k')
  return new Intl.NumberFormat().format(value)
}
