const escapeHtml = (value: string) =>
  value.replace(
    /[&<>"']/g,
    (character) =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[character]!,
  )

onmessage = (event: MessageEvent<{ pattern: string; flags: string; input: string }>) => {
  try {
    const { pattern, flags, input } = event.data
    const regex = new RegExp(pattern, flags)
    let offset = 0
    let highlighted = ''

    // Match the original text and escape each segment before generating markup.
    input.replace(regex, (match: string, ...args: unknown[]) => {
      const hasGroups = typeof args[args.length - 1] === 'object'
      const index = args[args.length - (hasGroups ? 3 : 2)] as number
      highlighted += escapeHtml(input.slice(offset, index))
      highlighted += `<mark class="rounded-sm bg-primary-100">${escapeHtml(match)}</mark>`
      offset = index + match.length
      return match
    })
    highlighted += escapeHtml(input.slice(offset))
    postMessage({ highlighted, error: '' })
  } catch (error) {
    postMessage({ highlighted: '', error: (error as Error).message })
  }
}
