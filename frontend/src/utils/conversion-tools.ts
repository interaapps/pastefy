export type ConversionToolDefinition = {
  slug: string
  title: string
  shortTitle: string
  description: string
  icon: string
  sourceFileName: string
  sourceType: string
  targetFileName: string
  targetType: string
  category: 'Convert'
  keywords: string[]
  example: string
}

type ConversionResult = {
  output: string
}

const needsYamlQuotes = (value: string) =>
  value === '' ||
  /[:#\-\n{}\[\],&*!?|<>=@`'"]/.test(value) ||
  /^(true|false|null|~)$/i.test(value) ||
  /^[-+]?\d+(\.\d+)?$/.test(value)

const yamlScalar = (value: unknown): string => {
  if (value === null || value === undefined) return 'null'
  if (typeof value === 'number' || typeof value === 'boolean') return String(value)
  if (typeof value === 'string') return needsYamlQuotes(value) ? JSON.stringify(value) : value
  return JSON.stringify(value)
}

const toYaml = (value: unknown, indent = 0): string => {
  const prefix = '  '.repeat(indent)

  if (Array.isArray(value)) {
    if (value.length === 0) return `${prefix}[]`
    return value
      .map((entry) => {
        if (entry && typeof entry === 'object') {
          const nested = toYaml(entry, indent + 1)
          return `${prefix}-\n${nested}`
        }
        return `${prefix}- ${yamlScalar(entry)}`
      })
      .join('\n')
  }

  if (value && typeof value === 'object') {
    const entries = Object.entries(value)
    if (entries.length === 0) return `${prefix}{}`
    return entries
      .map(([key, entry]) => {
        if (entry && typeof entry === 'object') {
          return `${prefix}${key}:\n${toYaml(entry, indent + 1)}`
        }
        return `${prefix}${key}: ${yamlScalar(entry)}`
      })
      .join('\n')
  }

  return `${prefix}${yamlScalar(value)}`
}

const parseYamlScalar = (value: string): unknown => {
  const trimmed = value.trim()
  if (trimmed === '' || trimmed === 'null' || trimmed === '~') return null
  if (trimmed === 'true') return true
  if (trimmed === 'false') return false
  if (/^[-+]?\d+(\.\d+)?$/.test(trimmed)) return Number(trimmed)

  if (
    (trimmed.startsWith('"') && trimmed.endsWith('"')) ||
    (trimmed.startsWith("'") && trimmed.endsWith("'"))
  ) {
    try {
      return JSON.parse(trimmed.replace(/^'/, '"').replace(/'$/, '"'))
    } catch {
      return trimmed.slice(1, -1)
    }
  }

  return trimmed
}

type YamlLine = {
  indent: number
  content: string
}

const prepareYamlLines = (source: string) =>
  source
    .replace(/\t/g, '  ')
    .split('\n')
    .map((line) => {
      const commentless = line.replace(/\s+#.*$/, '')
      return {
        indent: commentless.match(/^ */)?.[0].length || 0,
        content: commentless.trimEnd(),
      }
    })
    .filter((line) => line.content.trim() !== '' && !line.content.trim().startsWith('#'))

const parseYamlBlock = (lines: YamlLine[], startIndex: number, indent: number): [unknown, number] => {
  const current = lines[startIndex]
  if (!current) return [null, startIndex]

  if (current.indent < indent) return [null, startIndex]

  if (current.content.trim().startsWith('- ')) {
    const items: unknown[] = []
    let index = startIndex

    while (index < lines.length) {
      const line = lines[index]
      if (line.indent < indent || !line.content.trim().startsWith('- ')) break
      if (line.indent !== indent) break

      const itemContent = line.content.trim().slice(2).trim()
      const next = lines[index + 1]

      if (itemContent === '') {
        if (next && next.indent > indent) {
          const [child, nextIndex] = parseYamlBlock(lines, index + 1, next.indent)
          items.push(child)
          index = nextIndex
          continue
        }

        items.push(null)
        index += 1
        continue
      }

      const colonIndex = itemContent.indexOf(':')
      if (colonIndex > -1) {
        const key = itemContent.slice(0, colonIndex).trim()
        const rest = itemContent.slice(colonIndex + 1).trim()
        const item: Record<string, unknown> = {}

        if (rest !== '') {
          item[key] = parseYamlScalar(rest)
        } else if (next && next.indent > indent) {
          const [child, nextIndex] = parseYamlBlock(lines, index + 1, next.indent)
          item[key] = child
          index = nextIndex
          items.push(item)
          continue
        } else {
          item[key] = null
        }

        if (next && next.indent > indent) {
          const [child, nextIndex] = parseYamlBlock(lines, index + 1, next.indent)
          if (child && typeof child === 'object' && !Array.isArray(child)) {
            Object.assign(item, child)
            index = nextIndex
            items.push(item)
            continue
          }
        }

        items.push(item)
        index += 1
        continue
      }

      items.push(parseYamlScalar(itemContent))
      index += 1
    }

    return [items, index]
  }

  const obj: Record<string, unknown> = {}
  let index = startIndex

  while (index < lines.length) {
    const line = lines[index]
    if (line.indent < indent || line.indent !== indent) break

    const trimmed = line.content.trim()
    const colonIndex = trimmed.indexOf(':')
    if (colonIndex === -1) {
      index += 1
      continue
    }

    const key = trimmed.slice(0, colonIndex).trim()
    const rest = trimmed.slice(colonIndex + 1).trim()
    const next = lines[index + 1]

    if (rest !== '') {
      obj[key] = parseYamlScalar(rest)
      index += 1
      continue
    }

    if (next && next.indent > indent) {
      const [child, nextIndex] = parseYamlBlock(lines, index + 1, next.indent)
      obj[key] = child
      index = nextIndex
      continue
    }

    obj[key] = null
    index += 1
  }

  return [obj, index]
}

const parseYaml = (source: string) => {
  const lines = prepareYamlLines(source)
  if (lines.length === 0) return {}
  const [parsed] = parseYamlBlock(lines, 0, lines[0].indent)
  return parsed
}

const flattenObject = (
  value: unknown,
  prefix = '',
  separator = '_',
  output: Record<string, string> = {},
) => {
  if (value === null || value === undefined) {
    output[prefix] = ''
    return output
  }

  if (typeof value !== 'object') {
    output[prefix] = String(value)
    return output
  }

  if (Array.isArray(value)) {
    output[prefix] = JSON.stringify(value)
    return output
  }

  Object.entries(value).forEach(([key, entry]) => {
    const nextKey = prefix ? `${prefix}${separator}${key}` : key
    if (entry && typeof entry === 'object' && !Array.isArray(entry)) {
      flattenObject(entry, nextKey, separator, output)
    } else {
      output[nextKey] = Array.isArray(entry) ? JSON.stringify(entry) : String(entry ?? '')
    }
  })

  return output
}

const parseEnv = (source: string) => {
  const output: Record<string, string> = {}
  source.split('\n').forEach((line) => {
    const trimmed = line.trim()
    if (!trimmed || trimmed.startsWith('#') || !trimmed.includes('=')) return
    const separator = trimmed.indexOf('=')
    const key = trimmed.slice(0, separator).trim()
    let value = trimmed.slice(separator + 1).trim()
    if (
      (value.startsWith('"') && value.endsWith('"')) ||
      (value.startsWith("'") && value.endsWith("'"))
    ) {
      value = value.slice(1, -1)
    }
    output[key] = value
  })
  return output
}

const toEnv = (value: unknown) => {
  const flattened = flattenObject(value, '', '_')
  return Object.entries(flattened)
    .map(([key, entry]) => `${key.toUpperCase()}=${/\s/.test(entry) ? JSON.stringify(entry) : entry}`)
    .join('\n')
}

const parseCsv = (source: string) => {
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

  for (let i = 0; i < source.length; i += 1) {
    const char = source[i]
    const next = source[i + 1]

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

const escapeCsvCell = (value: unknown) => {
  const stringValue = value === null || value === undefined ? '' : String(value)
  if (/[",\n]/.test(stringValue)) {
    return `"${stringValue.replace(/"/g, '""')}"`
  }
  return stringValue
}

const toCsv = (value: unknown) => {
  if (!Array.isArray(value)) {
    throw new Error('JSON to CSV expects an array of objects or values at the top level.')
  }

  if (value.length === 0) return ''

  if (value.every((entry) => typeof entry !== 'object' || entry === null || Array.isArray(entry))) {
    return value.map((entry) => escapeCsvCell(entry)).join('\n')
  }

  const objectRows = value as Record<string, unknown>[]
  const headers = Array.from(
    new Set(objectRows.flatMap((entry) => Object.keys(entry || {}))),
  )

  const lines = [
    headers.map(escapeCsvCell).join(','),
    ...objectRows.map((entry) => headers.map((header) => escapeCsvCell(entry?.[header])).join(',')),
  ]

  return lines.join('\n')
}

const toMarkdownTable = (headers: string[], rows: string[][]) => {
  if (headers.length === 0) return ''
  const headerLine = `| ${headers.join(' | ')} |`
  const separatorLine = `| ${headers.map(() => '---').join(' | ')} |`
  const bodyLines = rows.map((row) => `| ${headers.map((_, index) => row[index] ?? '').join(' | ')} |`)
  return [headerLine, separatorLine, ...bodyLines].join('\n')
}

const shellQuote = (value: string) => `'${value.replace(/'/g, `'\"'\"'`)}'`

type ParsedHttpRequest = {
  method: string
  url: string
  headers: Record<string, string>
  body: string
}

const parseHttpRequest = (source: string): ParsedHttpRequest => {
  const normalized = source.trim().replace(/\r\n/g, '\n')
  const [head, ...bodyParts] = normalized.split('\n\n')
  const lines = head.split('\n').filter(Boolean)
  const requestLine = lines.shift()

  if (!requestLine) {
    throw new Error('HTTP input must start with a request line like GET https://example.com.')
  }

  const [method, url] = requestLine.trim().split(/\s+/, 2)
  if (!method || !url) {
    throw new Error('HTTP request line must include method and URL.')
  }

  const headers: Record<string, string> = {}
  lines.forEach((line) => {
    const separator = line.indexOf(':')
    if (separator === -1) return
    headers[line.slice(0, separator).trim()] = line.slice(separator + 1).trim()
  })

  return {
    method: method.toUpperCase(),
    url,
    headers,
    body: bodyParts.join('\n\n').trim(),
  }
}

const shellSplit = (command: string) => {
  const tokens: string[] = []
  let current = ''
  let quote: "'" | '"' | null = null
  let escaping = false

  for (const char of command.trim()) {
    if (escaping) {
      current += char
      escaping = false
      continue
    }

    if (char === '\\' && quote !== "'") {
      escaping = true
      continue
    }

    if ((char === '"' || char === "'") && !quote) {
      quote = char
      continue
    }

    if (quote && char === quote) {
      quote = null
      continue
    }

    if (!quote && /\s/.test(char)) {
      if (current) {
        tokens.push(current)
        current = ''
      }
      continue
    }

    current += char
  }

  if (current) tokens.push(current)
  return tokens
}

const parseCurl = (source: string): ParsedHttpRequest => {
  const tokens = shellSplit(source)
  if (tokens[0] !== 'curl') {
    throw new Error('cURL input must start with the curl command.')
  }

  let method = 'GET'
  let url = ''
  const headers: Record<string, string> = {}
  const bodyParts: string[] = []

  for (let i = 1; i < tokens.length; i += 1) {
    const token = tokens[i]
    const next = tokens[i + 1]

    if (['-X', '--request'].includes(token) && next) {
      method = next.toUpperCase()
      i += 1
      continue
    }

    if (['-H', '--header'].includes(token) && next) {
      const separator = next.indexOf(':')
      if (separator > -1) {
        headers[next.slice(0, separator).trim()] = next.slice(separator + 1).trim()
      }
      i += 1
      continue
    }

    if (['-d', '--data', '--data-raw', '--data-binary'].includes(token) && next) {
      bodyParts.push(next)
      if (method === 'GET') method = 'POST'
      i += 1
      continue
    }

    if (!token.startsWith('-') && /^https?:\/\//.test(token)) {
      url = token
    }
  }

  if (!url) {
    throw new Error('Could not find a URL in the cURL command.')
  }

  return {
    method,
    url,
    headers,
    body: bodyParts.join('\n'),
  }
}

const toCurl = (request: ParsedHttpRequest) => {
  const segments = ['curl', '-X', request.method.toUpperCase(), shellQuote(request.url)]

  Object.entries(request.headers).forEach(([key, value]) => {
    segments.push('-H', shellQuote(`${key}: ${value}`))
  })

  if (request.body) {
    segments.push('-d', shellQuote(request.body))
  }

  return segments.join(' ')
}

const toHttp = (request: ParsedHttpRequest) => {
  const headerLines = Object.entries(request.headers).map(([key, value]) => `${key}: ${value}`)
  return [ `${request.method.toUpperCase()} ${request.url}`, ...headerLines, '', request.body ]
    .join('\n')
    .trim()
}

export const conversionTools: ConversionToolDefinition[] = [
  {
    slug: 'json-to-yaml',
    title: 'Convert JSON to YAML',
    shortTitle: 'JSON -> YAML',
    description: 'Turn API payloads or config objects into clean YAML for infra and docs workflows.',
    icon: 'arrows-exchange',
    sourceFileName: 'payload.json',
    sourceType: 'json',
    targetFileName: 'payload.yaml',
    targetType: 'yaml',
    category: 'Convert',
    keywords: ['json', 'yaml', 'config', 'conversion'],
    example: `{
  "service": "pastefy",
  "environment": "production",
  "features": {
    "readerMode": true,
    "presentationMode": true
  }
}`,
  },
  {
    slug: 'yaml-to-json',
    title: 'Convert YAML to JSON',
    shortTitle: 'YAML -> JSON',
    description: 'Turn deployment and config-style YAML into machine-friendly JSON.',
    icon: 'arrows-exchange',
    sourceFileName: 'docker-compose.yml',
    sourceType: 'yaml',
    targetFileName: 'docker-compose.json',
    targetType: 'json',
    category: 'Convert',
    keywords: ['yaml', 'json', 'compose', 'config'],
    example: `services:
  web:
    image: pastefy/web:latest
    ports:
      - "8080:8080"
  redis:
    image: redis:7`,
  },
  {
    slug: 'json-to-env',
    title: 'Convert JSON to .env',
    shortTitle: 'JSON -> .env',
    description: 'Flatten JSON configuration into environment variables for apps and deployments.',
    icon: 'binary-tree-2',
    sourceFileName: 'config.json',
    sourceType: 'json',
    targetFileName: '.env',
    targetType: 'properties',
    category: 'Convert',
    keywords: ['json', 'env', 'environment variables'],
    example: `{
  "app": {
    "name": "Pastefy",
    "env": "production"
  },
  "db": {
    "host": "postgres",
    "port": 5432
  }
}`,
  },
  {
    slug: 'env-to-json',
    title: 'Convert .env to JSON',
    shortTitle: '.env -> JSON',
    description: 'Turn environment variables into JSON for API payloads, docs, or quick inspection.',
    icon: 'binary-tree-2',
    sourceFileName: '.env',
    sourceType: 'properties',
    targetFileName: 'config.json',
    targetType: 'json',
    category: 'Convert',
    keywords: ['env', 'json', 'properties'],
    example: `APP_NAME=Pastefy
APP_ENV=production
DB_HOST=postgres
DB_PORT=5432`,
  },
  {
    slug: 'csv-to-json',
    title: 'Convert CSV to JSON',
    shortTitle: 'CSV -> JSON',
    description: 'Turn tabular CSV data into JSON arrays you can feed into APIs or tooling.',
    icon: 'table-export',
    sourceFileName: 'report.csv',
    sourceType: 'csv',
    targetFileName: 'report.json',
    targetType: 'json',
    category: 'Convert',
    keywords: ['csv', 'json', 'table'],
    example: `name,role,team
Ada,admin,platform
Linus,user,backend`,
  },
  {
    slug: 'json-to-csv',
    title: 'Convert JSON to CSV',
    shortTitle: 'JSON -> CSV',
    description: 'Turn JSON arrays into flat CSV output for spreadsheets and exports.',
    icon: 'table-export',
    sourceFileName: 'report.json',
    sourceType: 'json',
    targetFileName: 'report.csv',
    targetType: 'csv',
    category: 'Convert',
    keywords: ['json', 'csv', 'spreadsheet'],
    example: `[
  { "name": "Ada", "role": "admin", "team": "platform" },
  { "name": "Linus", "role": "user", "team": "backend" }
]`,
  },
  {
    slug: 'http-to-curl',
    title: 'Convert HTTP to cURL',
    shortTitle: 'HTTP -> cURL',
    description: 'Turn raw HTTP request files into cURL commands you can run in a shell.',
    icon: 'api',
    sourceFileName: 'request.http',
    sourceType: 'http',
    targetFileName: 'request.sh',
    targetType: 'http',
    category: 'Convert',
    keywords: ['http', 'curl', 'api'],
    example: `POST https://api.pastefy.app/api/v2/paste
Authorization: Bearer demo-token
Content-Type: application/json

{
  "title": "hello.txt",
  "content": "Hello World"
}`,
  },
  {
    slug: 'curl-to-http',
    title: 'Convert cURL to HTTP',
    shortTitle: 'cURL -> HTTP',
    description: 'Turn cURL commands into clean HTTP request files for docs and API tooling.',
    icon: 'api',
    sourceFileName: 'request.sh',
    sourceType: 'http',
    targetFileName: 'request.http',
    targetType: 'http',
    category: 'Convert',
    keywords: ['curl', 'http', 'request'],
    example: `curl -X POST 'https://api.pastefy.app/api/v2/paste' -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' -d '{"title":"hello.txt","content":"Hello World"}'`,
  },
  {
    slug: 'yaml-to-env',
    title: 'Convert YAML to .env',
    shortTitle: 'YAML -> .env',
    description: 'Flatten simple YAML configuration into environment variables for deployment use.',
    icon: 'binary-tree-2',
    sourceFileName: 'config.yaml',
    sourceType: 'yaml',
    targetFileName: '.env',
    targetType: 'properties',
    category: 'Convert',
    keywords: ['yaml', 'env', 'config'],
    example: `app:
  name: Pastefy
  env: production
db:
  host: postgres
  port: 5432`,
  },
  {
    slug: 'env-to-yaml',
    title: 'Convert .env to YAML',
    shortTitle: '.env -> YAML',
    description: 'Turn environment variables into YAML for docs and human-readable config snapshots.',
    icon: 'binary-tree-2',
    sourceFileName: '.env',
    sourceType: 'properties',
    targetFileName: 'config.yaml',
    targetType: 'yaml',
    category: 'Convert',
    keywords: ['env', 'yaml', 'config'],
    example: `APP_NAME=Pastefy
APP_ENV=production
DB_HOST=postgres
DB_PORT=5432`,
  },
  {
    slug: 'csv-to-markdown',
    title: 'Convert CSV to Markdown Table',
    shortTitle: 'CSV -> Markdown',
    description: 'Turn CSV exports into Markdown tables for docs, READMEs, and issues.',
    icon: 'table-export',
    sourceFileName: 'report.csv',
    sourceType: 'csv',
    targetFileName: 'table.md',
    targetType: 'markdown',
    category: 'Convert',
    keywords: ['csv', 'markdown', 'table', 'docs'],
    example: `name,role,team
Ada,admin,platform
Linus,user,backend`,
  },
  {
    slug: 'json-to-markdown',
    title: 'Convert JSON to Markdown Table',
    shortTitle: 'JSON -> Markdown',
    description: 'Turn arrays of JSON objects into clean Markdown tables.',
    icon: 'table-export',
    sourceFileName: 'report.json',
    sourceType: 'json',
    targetFileName: 'table.md',
    targetType: 'markdown',
    category: 'Convert',
    keywords: ['json', 'markdown', 'table', 'docs'],
    example: `[
  { "name": "Ada", "role": "admin", "team": "platform" },
  { "name": "Linus", "role": "user", "team": "backend" }
]`,
  },
  {
    slug: 'http-to-json',
    title: 'Convert HTTP to JSON',
    shortTitle: 'HTTP -> JSON',
    description: 'Turn raw HTTP request files into JSON objects for tooling and inspection.',
    icon: 'api',
    sourceFileName: 'request.http',
    sourceType: 'http',
    targetFileName: 'request.json',
    targetType: 'json',
    category: 'Convert',
    keywords: ['http', 'json', 'request'],
    example: `POST https://api.pastefy.app/api/v2/paste
Authorization: Bearer demo-token
Content-Type: application/json

{
  "title": "hello.txt",
  "content": "Hello World"
}`,
  },
  {
    slug: 'curl-to-json',
    title: 'Convert cURL to JSON',
    shortTitle: 'cURL -> JSON',
    description: 'Turn cURL commands into JSON request objects for downstream processing.',
    icon: 'api',
    sourceFileName: 'request.sh',
    sourceType: 'http',
    targetFileName: 'request.json',
    targetType: 'json',
    category: 'Convert',
    keywords: ['curl', 'json', 'request'],
    example: `curl -X POST 'https://api.pastefy.app/api/v2/paste' -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' -d '{"title":"hello.txt","content":"Hello World"}'`,
  },
]

export function findConversionTool(slug?: string) {
  return conversionTools.find((tool) => tool.slug === slug)
}

export function runConversion(tool: ConversionToolDefinition, source: string): ConversionResult {
  switch (tool.slug) {
    case 'json-to-yaml':
      return { output: toYaml(JSON.parse(source)) }
    case 'yaml-to-json':
      return { output: JSON.stringify(parseYaml(source), null, 2) }
    case 'json-to-env':
      return { output: toEnv(JSON.parse(source)) }
    case 'env-to-json':
      return { output: JSON.stringify(parseEnv(source), null, 2) }
    case 'csv-to-json': {
      const rows = parseCsv(source)
      if (rows.length === 0) return { output: '[]' }
      const [header, ...records] = rows
      const output = records.map((row) =>
        Object.fromEntries(header.map((key, index) => [key, row[index] ?? ''])),
      )
      return { output: JSON.stringify(output, null, 2) }
    }
    case 'json-to-csv':
      return { output: toCsv(JSON.parse(source)) }
    case 'http-to-curl':
      return { output: toCurl(parseHttpRequest(source)) }
    case 'curl-to-http':
      return { output: toHttp(parseCurl(source)) }
    case 'yaml-to-env':
      return { output: toEnv(parseYaml(source)) }
    case 'env-to-yaml':
      return { output: toYaml(parseEnv(source)) }
    case 'csv-to-markdown': {
      const rows = parseCsv(source)
      if (rows.length === 0) return { output: '' }
      const [header, ...records] = rows
      return { output: toMarkdownTable(header, records) }
    }
    case 'json-to-markdown': {
      const parsed = JSON.parse(source)
      if (!Array.isArray(parsed) || parsed.length === 0) {
        throw new Error('JSON to Markdown expects a non-empty array of objects.')
      }
      const headers = Array.from(new Set(parsed.flatMap((entry) => Object.keys(entry || {}))))
      const rows = parsed.map((entry) => headers.map((header) => String(entry?.[header] ?? '')))
      return { output: toMarkdownTable(headers, rows) }
    }
    case 'http-to-json':
      return { output: JSON.stringify(parseHttpRequest(source), null, 2) }
    case 'curl-to-json':
      return { output: JSON.stringify(parseCurl(source), null, 2) }
    default:
      throw new Error('Unsupported conversion tool.')
  }
}
