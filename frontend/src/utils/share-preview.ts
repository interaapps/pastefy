import { findFromFileName } from '@/utils/lang-replacements.ts'

export type SharePreviewType =
  | 'csv'
  | 'mermaid'
  | 'geojson'
  | 'svg'
  | 'diff'
  | 'cast'
  | 'html'
  | 'json'
  | 'xml'
  | 'yaml'
  | 'toml'
  | 'config'

export function resolveSharePreviewType(fileName?: string): SharePreviewType | undefined {
  if (!fileName) return undefined

  if (fileName.endsWith('.svg')) return 'svg'
  if (fileName.endsWith('.geojson')) return 'geojson'
  if (fileName.endsWith('.mmd') || fileName.endsWith('.mermaid')) return 'mermaid'
  if (fileName.endsWith('.cast')) return 'cast'
  if (fileName.endsWith('.html') || fileName.endsWith('.htm')) return 'html'
  if (fileName.endsWith('.xml') || fileName.endsWith('.xhtml') || fileName.endsWith('.iml'))
    return 'xml'
  if (fileName.endsWith('.yml') || fileName.endsWith('.yaml')) return 'yaml'
  if (fileName.endsWith('.toml')) return 'toml'
  if (fileName.endsWith('.ini') || fileName.endsWith('.properties') || fileName === '.env')
    return 'config'

  const lang = findFromFileName(fileName)

  if (lang === 'csv') return 'csv'
  if (lang === 'diff') return 'diff'
  if (lang === 'cast') return 'cast'
  if (lang === 'geojson') return 'geojson'
  if (lang === 'mermaid' || lang === 'mmd') return 'mermaid'
  if (lang === 'json') return 'json'
  if (lang === 'xml') return 'xml'
  if (lang === 'yaml') return 'yaml'
  if (lang === 'toml') return 'toml'
  if (lang === 'properties' || lang === 'ini') return 'config'

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
    json: {
      label: 'JSON View',
      description: 'A readable structured view for APIs, configs, and data payloads.',
      icon: 'braces',
    },
    html: {
      label: 'HTML View',
      description: 'A rendered view for standalone HTML snippets and mini pages.',
      icon: 'file-type-html',
    },
    xml: {
      label: 'XML View',
      description: 'A structured XML explorer with searchable tree and formatted source.',
      icon: 'file-type-xml',
    },
    yaml: {
      label: 'YAML View',
      description: 'A cleaner config-focused reader for YAML files with quick searching.',
      icon: 'adjustments-alt',
    },
    toml: {
      label: 'TOML View',
      description: 'A readable config presentation for TOML-based files and manifests.',
      icon: 'settings-code',
    },
    config: {
      label: 'Config View',
      description: 'A focused inspector for ini, properties, and environment-style files.',
      icon: 'sliders-horizontal',
    },
  } as const

  return map[type]
}

export function toSharePreviewLabel(fileName?: string) {
  if (!fileName) return 'Untitled'
  return fileName.replace(
    /\.(csv|mmd|mermaid|geojson|svg|diff|patch|cast|json|html|htm|xml|xhtml|iml|ya?ml|toml|ini|properties)$/i,
    '',
  )
}
