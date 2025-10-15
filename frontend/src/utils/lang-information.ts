import { findFromFileName } from '@/utils/lang-replacements.ts'

export const langInformation = {
  java: {
    icon: 'coffee',
  },
  javascript: {
    icon: 'brand-javascript',
  },
  typescript: {
    icon: 'brand-typescript',
  },
  kotlin: {
    icon: 'brand-kotlin',
  },
  go: {
    icon: 'brand-golang',
  },
  json: {
    icon: 'file-code-2',
  },
  jsx: {
    icon: 'brand-react',
  },
  markdown: {
    icon: 'markdown',
  },
  yaml: {
    icon: 'adjustments-alt',
  },
  tsx: {
    icon: 'brand-react',
  },
  html: {
    icon: 'file-type-html',
  },
  xml: {
    icon: 'file-type-xml',
  },
  svg: {
    icon: 'file-vector',
  },
  php: {
    icon: 'file-type-php',
  },
  csv: {
    icon: 'file-type-csv',
  },
  lua: {
    icon: 'script',
  },
  python: {
    icon: 'brand-python',
  },
  rust: {
    icon: 'brand-rust',
  },
  swift: {
    icon: 'brand-swift',
  },
  powershell: {
    icon: 'brand-powershell',
  },
  csharp: {
    icon: 'brand-c-sharp',
  },
  shell: {
    icon: 'terminal',
  },
  sql: {
    icon: 'file-type-sql',
  },
  geojson: {
    icon: 'map-route',
  },
  mermaid: {
    icon: 'chart-arcs',
  },
  mmd: {
    icon: 'chart-arcs',
  },
  ics: {
    icon: 'calendar',
  },
  regex: {
    icon: 'regex',
  },
  cast: {
    icon: 'movie',
  },
  diff: {
    icon: 'layers-difference',
  },
}

export function getInformationByFileName(file: string) {
  return langInformation[findFromFileName(file) as keyof typeof langInformation]
}

const fileNameIcons = {
  '.codebox': 'box',
  Dockerfile: 'brand-docker',
  'docker-compose.yml': 'stack',
  'docker-compose.yaml': 'stack',
  Makefile: 'settings',
} as Record<string, string>

export function getIconByFileName(file: string) {
  if (file in fileNameIcons) return fileNameIcons[file]

  const ext = file.split('.').pop()!

  return (
    langInformation[ext as keyof typeof langInformation]?.icon ||
    langInformation[findFromFileName(file) as keyof typeof langInformation]?.icon ||
    'file-code'
  )
}
