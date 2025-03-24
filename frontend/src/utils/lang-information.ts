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
  go: {
    icon: 'brand-golang',
  },
  json: {
    icon: 'braces',
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
}

export function getInformationByFileName(file: string) {
  return langInformation[findFromFileName(file) as keyof typeof langInformation]
}

const fileNameIcons = {
  '.codebox': 'box',
  Dockerfile: 'brand-docker',
} as Record<string, string>

export function getIconByFileName(file: string) {
  if (file in fileNameIcons) return fileNameIcons[file]
  return langInformation[findFromFileName(file) as keyof typeof langInformation]?.icon || 'file'
}
