import { findFromFileName } from '@/utils/lang-replacements.ts'

export type SharePreviewType = 'csv' | 'mermaid' | 'geojson' | 'svg' | 'diff' | 'cast'

export function resolveSharePreviewType(fileName?: string): SharePreviewType | undefined {
  if (!fileName) return undefined

  if (fileName.endsWith('.svg')) return 'svg'
  if (fileName.endsWith('.geojson')) return 'geojson'
  if (fileName.endsWith('.mmd') || fileName.endsWith('.mermaid')) return 'mermaid'
  if (fileName.endsWith('.cast')) return 'cast'

  const lang = findFromFileName(fileName)

  if (lang === 'csv') return 'csv'
  if (lang === 'diff') return 'diff'
  if (lang === 'cast') return 'cast'
  if (lang === 'geojson') return 'geojson'
  if (lang === 'mermaid' || lang === 'mmd') return 'mermaid'

  return undefined
}

export function getSharePreviewMeta(type: SharePreviewType) {
  const map = {
    csv: {
      label: 'CSV Table',
      description: 'A clean tabular view for sharing datasets and quick exports.',
      icon: 'table',
    },
    mermaid: {
      label: 'Diagram View',
      description: 'A presentation-friendly view for Mermaid charts and diagrams.',
      icon: 'chart-arcs',
    },
    geojson: {
      label: 'Map View',
      description: 'A focused map presentation for shared GeoJSON content.',
      icon: 'map-route',
    },
    svg: {
      label: 'SVG View',
      description: 'A standalone visual presentation for vector artwork and diagrams.',
      icon: 'file-vector',
    },
    diff: {
      label: 'Diff View',
      description: 'A comparison-focused layout for reviewing and sharing changes.',
      icon: 'layers-difference',
    },
    cast: {
      label: 'Terminal Replay',
      description: 'A clean playback view for shared terminal recordings.',
      icon: 'movie',
    },
  } as const

  return map[type]
}

export function toSharePreviewLabel(fileName?: string) {
  if (!fileName) return 'Untitled'
  return fileName.replace(/\.(csv|mmd|mermaid|geojson|svg|diff|patch|cast)$/i, '')
}
