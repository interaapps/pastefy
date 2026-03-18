import { load } from 'js-yaml'

export const parseYamlDocument = (source: string): unknown => load(source) ?? {}
