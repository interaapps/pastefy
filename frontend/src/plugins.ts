export type Plugin = {
  init?: () => void
}

export const plugins = new Map<string, Plugin>()

export function createPlugin(name: string, plugin: Plugin) {
  plugins.set(name, plugin)
}

export function getEnabledPlugins() {
  return plugins.values()
}
