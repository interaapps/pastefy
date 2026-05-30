import type { TFunction } from 'i18next'

type ToolCatalogKind = 'preview' | 'conversion' | 'utility'

type TranslatableTool = {
  slug: string
  title: string
  shortTitle: string
  description: string
  category: string
}

type TranslatableCategory = {
  slug: string
  title: string
  eyebrow: string
  description: string
  seoTitle: string
  seoDescription: string
}

const toKey = (value: string) =>
  value
    .replace(/[^a-zA-Z0-9]+(.)/g, (_, character: string) => character.toUpperCase())
    .replace(/^[A-Z]/, (character) => character.toLowerCase())

export const localizeTool = <T extends TranslatableTool>(
  kind: ToolCatalogKind,
  tool: T,
  t: TFunction,
): T => ({
  ...tool,
  title: t(`catalog.${kind}.${tool.slug}.title`, { defaultValue: tool.title }),
  shortTitle: t(`catalog.${kind}.${tool.slug}.shortTitle`, { defaultValue: tool.shortTitle }),
  description: t(`catalog.${kind}.${tool.slug}.description`, { defaultValue: tool.description }),
  category: t(`catalog.labels.${toKey(tool.category)}`, { defaultValue: tool.category }),
})

export const localizeCategory = <T extends TranslatableCategory>(category: T, t: TFunction): T => ({
  ...category,
  title: t(`catalog.category.${category.slug}.title`, { defaultValue: category.title }),
  eyebrow: t(`catalog.category.${category.slug}.eyebrow`, { defaultValue: category.eyebrow }),
  description: t(`catalog.category.${category.slug}.description`, {
    defaultValue: category.description,
  }),
  seoTitle: t(`catalog.category.${category.slug}.seoTitle`, { defaultValue: category.seoTitle }),
  seoDescription: t(`catalog.category.${category.slug}.seoDescription`, {
    defaultValue: category.seoDescription,
  }),
})
