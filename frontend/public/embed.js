export function embedPastefy(parentEl, pasteId, origin = 'https://pastefy.app') {
  const iframe = document.createElement('iframe')

  iframe.src = `${origin}/${pasteId}/embed`

  if (iframe) {
    iframe.onload = () => {
      window.addEventListener('message', (e) => {
        if (e.data && 'height' in e.data) {
          iframe.style.height = e.data.height + 'px'
        }
      })
    }
  }

  parentEl.appendChild(iframe)
}
