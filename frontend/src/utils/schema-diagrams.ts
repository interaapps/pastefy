import {
  isRecord,
  normalizePrimitive,
  singularize,
  toPascalCase,
} from '@/utils/structured-data.ts'
import {
  toMermaidBareLabel,
  toMermaidQuotedLabel,
  toMermaidSafeIdentifier,
} from '@/utils/mermaid.ts'

export type SchemaField = {
  name: string
  type: string
}

export type SchemaRelation = {
  from: string
  to: string
  label: string
  kind: 'one' | 'many'
}

export type SchemaEntity = {
  name: string
  fields: SchemaField[]
}

export type SchemaGraph = {
  entities: SchemaEntity[]
  relations: SchemaRelation[]
}

const mergeType = (existing: string | undefined, next: string) => {
  if (!existing || existing === next) return next
  const types = new Set([...existing.split(' | '), next])
  return Array.from(types).join(' | ')
}

const primitiveType = (value: unknown) => {
  if (value === null || value === undefined) return 'null'
  if (Array.isArray(value)) return 'array'
  if (isRecord(value)) return 'object'
  return typeof value
}

const collectEntity = (
  name: string,
  value: unknown,
  entities: Map<string, Map<string, string>>,
  relations: SchemaRelation[],
) => {
  const entityName = toPascalCase(name || 'Root')
  const fieldMap = entities.get(entityName) || new Map<string, string>()
  entities.set(entityName, fieldMap)

  if (!isRecord(value)) {
    fieldMap.set('value', mergeType(fieldMap.get('value'), primitiveType(value)))
    return
  }

  Object.entries(value).forEach(([key, entry]) => {
    if (Array.isArray(entry)) {
      if (!entry.length || entry.every((item) => !isRecord(item))) {
        const firstType = entry.length ? primitiveType(entry[0]) : 'unknown'
        fieldMap.set(key, mergeType(fieldMap.get(key), `${firstType}[]`))
        return
      }

      const childName = toPascalCase(singularize(key))
      relations.push({
        from: entityName,
        to: childName,
        label: key,
        kind: 'many',
      })
      entry.forEach((item) => collectEntity(childName, item, entities, relations))
      return
    }

    if (isRecord(entry)) {
      const childName = toPascalCase(key)
      relations.push({
        from: entityName,
        to: childName,
        label: key,
        kind: 'one',
      })
      collectEntity(childName, entry, entities, relations)
      return
    }

    fieldMap.set(key, mergeType(fieldMap.get(key), primitiveType(normalizePrimitive(entry))))
  })
}

export const inferSchemaGraph = (value: unknown, rootName = 'Root'): SchemaGraph => {
  const entities = new Map<string, Map<string, string>>()
  const relations: SchemaRelation[] = []

  if (Array.isArray(value)) {
    const entityName = toPascalCase(singularize(rootName))
    value.forEach((entry) => collectEntity(entityName, entry, entities, relations))
  } else {
    collectEntity(rootName, value, entities, relations)
  }

  return {
    entities: Array.from(entities.entries()).map(([name, fields]) => ({
      name,
      fields: Array.from(fields.entries())
        .map(([fieldName, type]) => ({ name: fieldName, type }))
        .sort((a, b) => a.name.localeCompare(b.name)),
    })),
    relations: relations.filter(
      (relation, index, arr) =>
        arr.findIndex(
          (entry) =>
            entry.from === relation.from &&
            entry.to === relation.to &&
            entry.label === relation.label &&
            entry.kind === relation.kind,
        ) === index,
    ),
  }
}

export const toMermaidERDiagram = (
  graph: SchemaGraph,
  options: {
    includeTypes?: boolean
  } = {},
) => {
  const lines = ['erDiagram']

  graph.entities.forEach((entity) => {
    const entityName = toMermaidSafeIdentifier(entity.name, 'Entity')
    lines.push(`  ${entityName} {`)
    entity.fields.forEach((field) => {
      lines.push(
        `    ${options.includeTypes === false ? 'field' : toMermaidSafeIdentifier(field.type, 'field')} ${toMermaidSafeIdentifier(field.name, 'field')}`,
      )
    })
    lines.push('  }')
  })

  graph.relations.forEach((relation) => {
    lines.push(
      `  ${toMermaidSafeIdentifier(relation.from, 'Entity')} ${relation.kind === 'many' ? '||--o{' : '||--||'} ${toMermaidSafeIdentifier(relation.to, 'Entity')} : ${toMermaidQuotedLabel(relation.label, 'relation')}`,
    )
  })

  return lines.join('\n')
}

export const toMermaidClassDiagram = (
  graph: SchemaGraph,
  options: {
    includeTypes?: boolean
  } = {},
) => {
  const lines = ['classDiagram']

  graph.entities.forEach((entity) => {
    const entityName = toMermaidSafeIdentifier(entity.name, 'Entity')
    lines.push(`  class ${entityName} {`)
    entity.fields.forEach((field) => {
      lines.push(
        `    ${options.includeTypes === false ? toMermaidSafeIdentifier(field.name, 'field') : `${toMermaidSafeIdentifier(field.type, 'field')} ${toMermaidSafeIdentifier(field.name, 'field')}`}`,
      )
    })
    lines.push('  }')
  })

  graph.relations.forEach((relation) => {
    lines.push(
      `  ${toMermaidSafeIdentifier(relation.from, 'Entity')} ${relation.kind === 'many' ? '"1" *-- "*" ' : '"1" *-- "1" '}${toMermaidSafeIdentifier(relation.to, 'Entity')} : ${toMermaidBareLabel(relation.label, 'relation')}`,
    )
  })

  return lines.join('\n')
}
