import { load, loadAll } from 'js-yaml'

export const parseYamlDocument = (source: string): unknown => load(source) ?? {}

export const parseYamlDocuments = (source: string): unknown[] => {
  const documents = loadAll(source)
  return documents.filter((document) => document !== undefined)
}
