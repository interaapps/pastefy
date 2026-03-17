export type UtilityToolDefinition = {
  slug: string
  title: string
  shortTitle: string
  description: string
  icon: string
  category: 'Inspect' | 'Generate' | 'Security' | 'Automation' | 'Encode' | 'Time'
  keywords: string[]
}

export const utilityTools: UtilityToolDefinition[] = [
  {
    slug: 'jwt-inspector',
    title: 'JWT Inspector',
    shortTitle: 'JWT',
    description:
      'Decode JWTs, inspect claims, and check timestamps without sending the token anywhere.',
    icon: 'shield-lock',
    category: 'Security',
    keywords: ['jwt', 'token', 'claims', 'auth', 'bearer'],
  },
  {
    slug: 'hash-generator',
    title: 'Hash Generator',
    shortTitle: 'Hash',
    description:
      'Generate SHA hashes for text or payloads and compare lengths at a glance.',
    icon: 'fingerprint',
    category: 'Security',
    keywords: ['hash', 'sha256', 'checksum', 'digest'],
  },
  {
    slug: 'secret-generator',
    title: 'Secret Generator',
    shortTitle: 'Secrets',
    description:
      'Generate UUIDs, hex tokens, and Base64URL secrets for quick local testing and setup.',
    icon: 'sparkles',
    category: 'Generate',
    keywords: ['uuid', 'secret', 'token', 'generator', 'base64url'],
  },
  {
    slug: 'password-generator',
    title: 'Password Generator',
    shortTitle: 'Passwords',
    description:
      'Generate passwords with configurable character sets, length, count, and ambiguity rules.',
    icon: 'key',
    category: 'Security',
    keywords: ['password', 'secret', 'generator', 'security'],
  },
  {
    slug: 'cron-explainer',
    title: 'Cron Explainer',
    shortTitle: 'Cron',
    description:
      'Understand cron expressions with a readable breakdown of schedule fields and timing.',
    icon: 'clock-code',
    category: 'Automation',
    keywords: ['cron', 'schedule', 'automation', 'timing'],
  },
  {
    slug: 'base64-lab',
    title: 'Base64 Lab',
    shortTitle: 'Base64',
    description:
      'Encode and decode Base64 or Base64URL strings locally for payload and token workflows.',
    icon: 'binary',
    category: 'Encode',
    keywords: ['base64', 'base64url', 'encode', 'decode'],
  },
  {
    slug: 'url-encoder',
    title: 'URL Encoder / Decoder',
    shortTitle: 'URL Encode',
    description:
      'Encode and decode URL components and full query values without leaving the browser.',
    icon: 'link',
    category: 'Encode',
    keywords: ['url', 'encode', 'decode', 'querystring'],
  },
  {
    slug: 'timestamp-converter',
    title: 'Timestamp Converter',
    shortTitle: 'Timestamp',
    description:
      'Convert between Unix timestamps, ISO dates, and local times for debugging and docs.',
    icon: 'clock-hour-4',
    category: 'Time',
    keywords: ['timestamp', 'unix', 'iso', 'date', 'time'],
  },
  {
    slug: 'slug-generator',
    title: 'Slug Generator',
    shortTitle: 'Slug',
    description:
      'Generate slugs, snake_case, and other URL-friendly variants from plain text.',
    icon: 'writing',
    category: 'Generate',
    keywords: ['slug', 'kebab-case', 'snake_case', 'url'],
  },
  {
    slug: 'case-converter',
    title: 'Case Converter',
    shortTitle: 'Case',
    description:
      'Switch text between lowercase, uppercase, title case, camelCase, snake_case, and more.',
    icon: 'letter-case',
    category: 'Generate',
    keywords: ['case', 'camelCase', 'snake_case', 'kebab-case', 'text transform'],
  },
  {
    slug: 'query-string-parser',
    title: 'Query String Parser',
    shortTitle: 'Query',
    description:
      'Parse query strings into structured data and rebuild them cleanly.',
    icon: 'list-search',
    category: 'Inspect',
    keywords: ['query string', 'params', 'url', 'search params'],
  },
  {
    slug: 'html-entity-tool',
    title: 'HTML Entity Encoder / Decoder',
    shortTitle: 'HTML Entities',
    description:
      'Encode or decode HTML entities for templates, docs, and pasted snippets.',
    icon: 'brackets-angle',
    category: 'Encode',
    keywords: ['html entities', 'encode', 'decode', 'html'],
  },
  {
    slug: 'json-string-tool',
    title: 'JSON String Escape Tool',
    shortTitle: 'JSON String',
    description:
      'Escape text for JSON strings or unescape JSON string content back to plain text.',
    icon: 'quote',
    category: 'Encode',
    keywords: ['json', 'escape', 'string', 'unescape'],
  },
  {
    slug: 'regex-escape-tool',
    title: 'Regex Escape Tool',
    shortTitle: 'Regex Escape',
    description:
      'Escape plain text for regex patterns or unescape regex-special characters.',
    icon: 'regex',
    category: 'Encode',
    keywords: ['regex', 'escape', 'pattern'],
  },
  {
    slug: 'color-converter',
    title: 'Color Converter',
    shortTitle: 'Color',
    description:
      'Convert colors between HEX, RGB, and HSL with a live swatch preview.',
    icon: 'palette',
    category: 'Generate',
    keywords: ['color', 'hex', 'rgb', 'hsl'],
  },
  {
    slug: 'number-base-converter',
    title: 'Number Base Converter',
    shortTitle: 'Number Bases',
    description:
      'Convert values between binary, octal, decimal, and hexadecimal.',
    icon: 'binary-tree',
    category: 'Inspect',
    keywords: ['binary', 'hex', 'decimal', 'octal', 'base converter'],
  },
  {
    slug: 'text-sorter',
    title: 'Text Sorter & Deduper',
    shortTitle: 'Sorter',
    description:
      'Sort line-based lists, remove duplicates, and reverse ordering quickly.',
    icon: 'sort-ascending',
    category: 'Inspect',
    keywords: ['sort', 'dedupe', 'lines', 'text'],
  },
  {
    slug: 'lorem-ipsum-generator',
    title: 'Lorem Ipsum Generator',
    shortTitle: 'Lorem Ipsum',
    description:
      'Generate placeholder words, sentences, or paragraphs for UI and content mockups.',
    icon: 'text-plus',
    category: 'Generate',
    keywords: ['lorem ipsum', 'placeholder', 'content', 'generator'],
  },
  {
    slug: 'uuid-inspector',
    title: 'UUID Inspector',
    shortTitle: 'UUID',
    description:
      'Validate UUIDs and inspect their version, variant, and nil status.',
    icon: 'binary',
    category: 'Inspect',
    keywords: ['uuid', 'identifier', 'inspect', 'version'],
  },
  {
    slug: 'word-stats',
    title: 'Word / Line / Character Counter',
    shortTitle: 'Text Stats',
    description:
      'Count words, lines, characters, paragraphs, reading time, and other useful text metrics.',
    icon: 'align-box-left-middle',
    category: 'Inspect',
    keywords: ['word count', 'line count', 'character count', 'reading time', 'text stats'],
  },
]

export function findUtilityTool(slug?: string) {
  return utilityTools.find((tool) => tool.slug === slug)
}
