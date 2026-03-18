import type { RouteLocationRaw } from 'vue-router'

import { conversionTools } from '@/utils/conversion-tools.ts'
import { previewTools } from '@/utils/preview-tools.ts'
import { utilityTools } from '@/utils/utility-tools.ts'

export type ToolCategorySlug =
  | 'docs'
  | 'data'
  | 'visual'
  | 'mermaid'
  | 'api'
  | 'infra'
  | 'operations'
  | 'server'
  | 'deploy'
  | 'automation'
  | 'security'
  | 'terminal'
  | 'convert'
  | 'inspect'
  | 'generate'
  | 'encode'
  | 'time'
  | 'visualize'

export type ToolCategoryDefinition = {
  slug: ToolCategorySlug
  title: string
  icon: string
  scope: 'preview' | 'utility' | 'conversion' | 'mixed'
  eyebrow: string
  description: string
  seoTitle: string
  seoDescription: string
}

export type ToolCatalogEntry = {
  kind: 'preview' | 'conversion' | 'utility'
  slug: string
  title: string
  shortTitle: string
  description: string
  icon: string
  category: string
  keywords: string[]
  meta: string
  to: RouteLocationRaw
}

export const toolCategoryDefinitions: ToolCategoryDefinition[] = [
  {
    slug: 'docs',
    title: 'Docs Tools',
    icon: 'file-text',
    scope: 'preview',
    eyebrow: 'Documentation',
    description: 'Tools for markdown articles, diffs, and shareable documentation workflows.',
    seoTitle: 'Pastefy Docs Tools',
    seoDescription:
      'Explore Pastefy documentation tools for Markdown articles, diffs, and readable share modes built for developer docs.',
  },
  {
    slug: 'data',
    title: 'Data Tools',
    icon: 'braces',
    scope: 'preview',
    eyebrow: 'Structured Data',
    description: 'Inspect JSON, YAML, XML, CSV, and other structured formats with richer previews.',
    seoTitle: 'Pastefy Data Tools',
    seoDescription:
      'Use Pastefy data tools to inspect and preview JSON, YAML, XML, CSV, and other structured developer formats.',
  },
  {
    slug: 'visual',
    title: 'Visual Tools',
    icon: 'sparkles',
    scope: 'preview',
    eyebrow: 'Visual Preview',
    description: 'Preview HTML, SVG, Mermaid, and spatial formats in a more visual editing workflow.',
    seoTitle: 'Pastefy Visual Preview Tools',
    seoDescription:
      'Use Pastefy visual tools to preview HTML, SVG, Mermaid diagrams, GeoJSON maps, and other visual formats.',
  },
  {
    slug: 'mermaid',
    title: 'Mermaid Tools',
    icon: 'chart-arrows-vertical',
    scope: 'mixed',
    eyebrow: 'Mermaid',
    description:
      'Find Mermaid-focused editors, theme builders, exporters, and diagram generators in one place.',
    seoTitle: 'Pastefy Mermaid Tools',
    seoDescription:
      'Use Pastefy Mermaid tools to edit diagrams, build themes, export images, and generate Mermaid charts, ER diagrams, class diagrams, and infra views.',
  },
  {
    slug: 'api',
    title: 'API Tools',
    icon: 'api',
    scope: 'preview',
    eyebrow: 'API Workflow',
    description: 'Work with HTTP requests, payloads, and API-facing developer content in focused tools.',
    seoTitle: 'Pastefy API Tools',
    seoDescription:
      'Use Pastefy API tools to inspect HTTP requests, payload formats, and API-oriented developer snippets before sharing.',
  },
  {
    slug: 'infra',
    title: 'Infrastructure Tools',
    icon: 'building-warehouse',
    scope: 'preview',
    eyebrow: 'Infrastructure',
    description: 'Preview Terraform, config files, and infrastructure-focused snippets in a cleaner workspace.',
    seoTitle: 'Pastefy Infrastructure Tools',
    seoDescription:
      'Use Pastefy infrastructure tools for Terraform, environment files, configs, and infrastructure-oriented workflows.',
  },
  {
    slug: 'operations',
    title: 'Operations Tools',
    icon: 'activity-heartbeat',
    scope: 'mixed',
    eyebrow: 'Operations',
    description:
      'Inspect logs, alerting rules, and production-facing operational data with tools built for day-two work.',
    seoTitle: 'Pastefy Operations Tools',
    seoDescription:
      'Use Pastefy operations tools to inspect logs, alert rules, and production-focused operational content and diagnostics.',
  },
  {
    slug: 'server',
    title: 'Server Config Tools',
    icon: 'server',
    scope: 'utility',
    eyebrow: 'Server Config',
    description:
      'Generate deploy-ready Caddy, NGINX, and Apache website configs with the common options you usually need.',
    seoTitle: 'Pastefy Server Config Tools',
    seoDescription:
      'Use Pastefy server config tools to generate Caddy, NGINX, and Apache configs for static sites and reverse proxies.',
  },
  {
    slug: 'deploy',
    title: 'Deployment Tools',
    icon: 'rocket',
    scope: 'utility',
    eyebrow: 'Deployment',
    description:
      'Generate Dockerfiles, Compose stacks, startup scripts, and systemd units for common deployment workflows.',
    seoTitle: 'Pastefy Deployment Tools',
    seoDescription:
      'Use Pastefy deployment tools to generate Dockerfiles, Compose files, startup scripts, and systemd services.',
  },
  {
    slug: 'automation',
    title: 'Automation Tools',
    icon: 'clock-code',
    scope: 'mixed',
    eyebrow: 'Automation',
    description: 'Build around workflows, schedules, GitHub Actions, and automation-ready developer content.',
    seoTitle: 'Pastefy Automation Tools',
    seoDescription:
      'Use Pastefy automation tools for cron schedules, GitHub Actions, Ansible, and automation-focused developer workflows.',
  },
  {
    slug: 'security',
    title: 'Security Tools',
    icon: 'shield-lock',
    scope: 'mixed',
    eyebrow: 'Security',
    description: 'Inspect tokens, generate secrets, and work with security-sensitive text locally in the browser.',
    seoTitle: 'Pastefy Security Tools',
    seoDescription:
      'Use Pastefy security tools to inspect JWTs, generate passwords and secrets, and handle sensitive data locally.',
  },
  {
    slug: 'terminal',
    title: 'Terminal Tools',
    icon: 'terminal-2',
    scope: 'preview',
    eyebrow: 'Terminal',
    description: 'Preview terminal sessions and terminal-shaped share content with cleaner rendering.',
    seoTitle: 'Pastefy Terminal Tools',
    seoDescription:
      'Use Pastefy terminal tools to preview terminal recordings and terminal-style developer content before sharing.',
  },
  {
    slug: 'convert',
    title: 'Conversion Tools',
    icon: 'arrows-exchange',
    scope: 'conversion',
    eyebrow: 'Convert',
    description: 'Convert between common developer formats like JSON, YAML, CSV, .env, HTTP, and cURL.',
    seoTitle: 'Pastefy Conversion Tools',
    seoDescription:
      'Use Pastefy conversion tools to convert JSON, YAML, CSV, .env, HTTP, cURL, and other developer formats.',
  },
  {
    slug: 'inspect',
    title: 'Inspect Tools',
    icon: 'search',
    scope: 'utility',
    eyebrow: 'Inspect',
    description: 'Analyze, compare, and inspect text, diffs, identifiers, and structured input quickly.',
    seoTitle: 'Pastefy Inspect Tools',
    seoDescription:
      'Use Pastefy inspect tools to analyze diffs, UUIDs, query strings, number bases, and text-heavy developer data.',
  },
  {
    slug: 'generate',
    title: 'Generate Tools',
    icon: 'sparkles',
    scope: 'utility',
    eyebrow: 'Generate',
    description: 'Generate slugs, placeholder content, colors, values, and other useful developer-ready output.',
    seoTitle: 'Pastefy Generate Tools',
    seoDescription:
      'Use Pastefy generate tools to create slugs, sample content, colors, secrets, and other useful developer output.',
  },
  {
    slug: 'encode',
    title: 'Encoding Tools',
    icon: 'binary',
    scope: 'utility',
    eyebrow: 'Encode',
    description: 'Encode and decode URL, Base64, HTML entity, JSON string, and regex-friendly content.',
    seoTitle: 'Pastefy Encoding Tools',
    seoDescription:
      'Use Pastefy encoding tools for Base64, URL encoding, HTML entities, JSON string escaping, and regex escaping.',
  },
  {
    slug: 'time',
    title: 'Time Tools',
    icon: 'clock-hour-4',
    scope: 'utility',
    eyebrow: 'Time',
    description: 'Convert timestamps and reason about schedules with lightweight browser-first tools.',
    seoTitle: 'Pastefy Time Tools',
    seoDescription:
      'Use Pastefy time tools to convert timestamps and inspect scheduling data in a simple browser-based workspace.',
  },
  {
    slug: 'visualize',
    title: 'Visualization Tools',
    icon: 'chart-bar',
    scope: 'utility',
    eyebrow: 'Visualize',
    description: 'Turn developer data into diagrams, charts, class views, ER models, and Mermaid exports.',
    seoTitle: 'Pastefy Visualization Tools',
    seoDescription:
      'Use Pastefy visualization tools to turn SQL, JSON, YAML, CSV, Markdown, and infra files into Mermaid diagrams and charts.',
  },
]

export const previewCatalogEntries: ToolCatalogEntry[] = previewTools.map((tool) => ({
  kind: 'preview',
  slug: tool.slug,
  title: tool.title,
  shortTitle: tool.shortTitle,
  description: tool.description,
  icon: tool.icon,
  category: tool.category,
  keywords: tool.keywords,
  meta: tool.fileName,
  to: { name: 'tool-preview', params: { tool: tool.slug } },
}))

export const conversionCatalogEntries: ToolCatalogEntry[] = conversionTools.map((tool) => ({
  kind: 'conversion',
  slug: tool.slug,
  title: tool.title,
  shortTitle: tool.shortTitle,
  description: tool.description,
  icon: tool.icon,
  category: tool.category,
  keywords: tool.keywords,
  meta: `${tool.sourceFileName} -> ${tool.targetFileName}`,
  to: { name: 'tool-conversion', params: { tool: tool.slug } },
}))

export const utilityCatalogEntries: ToolCatalogEntry[] = utilityTools.map((tool) => ({
  kind: 'utility',
  slug: tool.slug,
  title: tool.title,
  shortTitle: tool.shortTitle,
  description: tool.description,
  icon: tool.icon,
  category: tool.category,
  keywords: tool.keywords,
  meta: tool.category,
  to: { name: 'tool-utility', params: { tool: tool.slug } },
}))

export const allToolCatalogEntries: ToolCatalogEntry[] = [
  ...previewCatalogEntries,
  ...conversionCatalogEntries,
  ...utilityCatalogEntries,
]

export const categorySlugByName = {
  Docs: 'docs',
  Data: 'data',
  Visual: 'visual',
  Mermaid: 'mermaid',
  API: 'api',
  Infra: 'infra',
  Operations: 'operations',
  Server: 'server',
  Deploy: 'deploy',
  Automation: 'automation',
  Security: 'security',
  Terminal: 'terminal',
  Convert: 'convert',
  Inspect: 'inspect',
  Generate: 'generate',
  Encode: 'encode',
  Time: 'time',
  Visualize: 'visualize',
} as const satisfies Record<string, ToolCategorySlug>

export const toolCategoryNameBySlug = Object.fromEntries(
  Object.entries(categorySlugByName).map(([name, slug]) => [slug, name]),
) as Record<ToolCategorySlug, string>

export const findToolCategory = (slug?: string) =>
  toolCategoryDefinitions.find((category) => category.slug === slug)

export const getCategoryEntries = (slug: ToolCategorySlug) => {
  if (slug === 'mermaid') {
    return allToolCatalogEntries.filter((entry) => {
      const haystack = `${entry.slug} ${entry.title} ${entry.description} ${entry.keywords.join(' ')}`
        .toLowerCase()
      return haystack.includes('mermaid')
    })
  }

  const categoryName = toolCategoryNameBySlug[slug]
  return allToolCatalogEntries.filter((entry) => entry.category === categoryName)
}

export const getCategoryCount = (slug: ToolCategorySlug) => getCategoryEntries(slug).length
