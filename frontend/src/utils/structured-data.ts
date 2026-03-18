import { parseYamlDocument } from '@/utils/yaml.ts'

export type StructuredInputFormat = 'auto' | 'json' | 'yaml' | 'csv'
export type FlatRow = Record<string, string | number | boolean | null>

export const detectStructuredFormat = (value: string): Exclude<StructuredInputFormat, 'auto'> => {
  const trimmed = value.trim()
  if (!trimmed) return 'json'
  if (trimmed.startsWith('{') || trimmed.startsWith('[')) return 'json'
  const firstLine = trimmed.split(/\r?\n/, 1)[0] || ''
  if (firstLine.includes(',') && !firstLine.includes(':')) return 'csv'
  return 'yaml'
}

export const parseCsvRows = (raw: string) => {
  const rows: string[][] = []
  let current = ''
  let row: string[] = []
  let inQuotes = false

  const pushCell = () => {
    row.push(current)
    current = ''
  }

  const pushRow = () => {
    if (row.length > 0 || current !== '') {
      pushCell()
      rows.push(row)
      row = []
    }
  }

  for (let i = 0; i < raw.length; i += 1) {
    const char = raw[i]
    const next = raw[i + 1]

    if (char === '"') {
      if (inQuotes && next === '"') {
        current += '"'
        i += 1
      } else {
        inQuotes = !inQuotes
      }
      continue
    }

    if (char === ',' && !inQuotes) {
      pushCell()
      continue
    }

    if ((char === '\n' || char === '\r') && !inQuotes) {
      if (char === '\r' && next === '\n') i += 1
      pushRow()
      continue
    }

    current += char
  }

  if (current !== '' || row.length > 0) pushRow()
  return rows
}

export const csvToObjects = (raw: string): FlatRow[] => {
  const rows = parseCsvRows(raw)
  if (!rows.length) return []
  const [headers, ...body] = rows
  return body.map((row) =>
    headers.reduce<FlatRow>((acc, header, index) => {
      acc[header] = row[index] ?? ''
      return acc
    }, {}),
  )
}

export const isRecord = (value: unknown): value is Record<string, unknown> =>
  value !== null && typeof value === 'object' && !Array.isArray(value)

export const normalizePrimitive = (value: unknown) => {
  if (value === null || value === undefined) return null
  if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'string') {
    return value
  }
  return JSON.stringify(value)
}

export const flattenRecord = (
  value: unknown,
  prefix = '',
  output: FlatRow = {},
): FlatRow => {
  if (Array.isArray(value)) {
    output[prefix || 'value'] = value.every((entry) => !isRecord(entry))
      ? value.map((entry) => normalizePrimitive(entry)).join(', ')
      : JSON.stringify(value)
    return output
  }

  if (!isRecord(value)) {
    output[prefix || 'value'] = normalizePrimitive(value)
    return output
  }

  Object.entries(value).forEach(([key, entry]) => {
    const nextKey = prefix ? `${prefix}.${key}` : key
    if (isRecord(entry)) {
      flattenRecord(entry, nextKey, output)
      return
    }

    if (Array.isArray(entry)) {
      output[nextKey] = entry.every((item) => !isRecord(item))
        ? entry.map((item) => normalizePrimitive(item)).join(', ')
        : JSON.stringify(entry)
      return
    }

    output[nextKey] = normalizePrimitive(entry)
  })

  return output
}

export const normalizeToRows = (value: unknown): FlatRow[] => {
  if (Array.isArray(value)) {
    if (!value.length) return []
    return value.map((entry, index) =>
      isRecord(entry)
        ? flattenRecord(entry)
        : {
            index: index + 1,
            value: normalizePrimitive(entry),
          },
    )
  }

  if (isRecord(value)) {
    const entries = Object.entries(value)
    const arrayEntries = entries.filter(([, entry]) => Array.isArray(entry))

    if (arrayEntries.length === 1) {
      return normalizeToRows(arrayEntries[0][1])
    }

    if (entries.length && entries.every(([, entry]) => isRecord(entry))) {
      return entries.map(([key, entry]) => ({
        key,
        ...flattenRecord(entry),
      }))
    }

    return [flattenRecord(value)]
  }

  return [{ value: normalizePrimitive(value) }]
}

export const parseStructuredData = (raw: string, format: StructuredInputFormat): unknown => {
  const resolvedFormat = format === 'auto' ? detectStructuredFormat(raw) : format

  if (resolvedFormat === 'json') {
    return JSON.parse(raw)
  }

  if (resolvedFormat === 'yaml') {
    return parseYamlDocument(raw)
  }

  return csvToObjects(raw)
}

export const toPascalCase = (value: string) =>
  value
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .split(/[^a-zA-Z0-9]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join('')

export const singularize = (value: string) => {
  if (value.endsWith('ies')) return `${value.slice(0, -3)}y`
  if (value.endsWith('ses')) return value.slice(0, -2)
  if (value.endsWith('s') && !value.endsWith('ss')) return value.slice(0, -1)
  return value
}
