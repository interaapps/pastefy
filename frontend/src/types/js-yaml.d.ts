declare module 'js-yaml' {
  export function load(source: string): unknown
  export function loadAll(source: string): unknown[]
}
