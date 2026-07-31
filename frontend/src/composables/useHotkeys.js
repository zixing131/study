import { onMounted, onUnmounted } from 'vue'

function isTypingTarget(el) {
  if (!el) return false
  const tag = el.tagName
  return tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || el.isContentEditable
}

/**
 * @param {() => Record<string, (e: KeyboardEvent) => void>} getHandlers
 *   key 支持：' ', 'Space', 'ArrowLeft', 'Enter', 'Escape', 't', 'Digit0'..'Digit9', 'KeyA'.. 等
 */
export function useHotkeys(getHandlers) {
  function onKeyDown(e) {
    if (e.defaultPrevented) return
    if (e.metaKey || e.ctrlKey || e.altKey) return
    if (isTypingTarget(e.target)) return

    const handlers = typeof getHandlers === 'function' ? getHandlers() : getHandlers
    if (!handlers) return

    const candidates = [
      e.code,
      e.key,
      e.key?.length === 1 ? e.key.toLowerCase() : '',
      e.code === 'Space' ? ' ' : '',
    ].filter(Boolean)

    for (const name of candidates) {
      const fn = handlers[name]
      if (typeof fn === 'function') {
        e.preventDefault()
        fn(e)
        return
      }
    }
  }

  onMounted(() => window.addEventListener('keydown', onKeyDown))
  onUnmounted(() => window.removeEventListener('keydown', onKeyDown))
}
