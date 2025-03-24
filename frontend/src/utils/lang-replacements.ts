export const LANG_REPLACEMENTS = {
  md: 'markdown',
  js: 'javascript',
  jsx: 'javascript',
  jsm: 'javascript',
  ts: 'typescript',
  tsm: 'typescript',
  tsx: 'typescript',
  html: 'xml',
  svg: 'xml',
  htm: 'xml',
  xhtml: 'xml',
  iml: 'xml',
  vue: 'xml',
  yml: 'yaml',
  py: 'python',
  css: 'scss',
  sass: 'yaml',
  cs: 'csharp',
  env: 'properties',
  txt: 'text',
  h: 'c',
  ino: 'c',
  go: 'go',
  rs: 'rust',
  php: 'php',
  sh: 'shell',
  bat: 'batch',
  ps1: 'powershell',
} as Record<string, string>

const fileNameReplacements = {
  '.codebox': 'json',
} as Record<string, string>

export function findFromFileName(fileName: string): string | undefined {
  if (fileName in fileNameReplacements) return fileNameReplacements[fileName]

  const ext = fileName.split('.').pop()!
  return LANG_REPLACEMENTS[ext] ?? ext
}
