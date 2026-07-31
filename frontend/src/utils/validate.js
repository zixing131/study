export function isBlank(v) {
  return v == null || String(v).trim() === ''
}

export function asInt(v, fallback = 0) {
  const n = Number(v)
  return Number.isFinite(n) ? Math.trunc(n) : fallback
}

/** @returns {string|null} 错误文案，通过则 null */
export function required(v, label) {
  return isBlank(v) ? `${label}不能为空` : null
}

export function nonNegInt(v, label = '排序') {
  if (v === '' || v == null) return `${label}不能为空`
  const n = Number(v)
  if (!Number.isInteger(n) || n < 0) return `${label}须为大于等于 0 的整数`
  return null
}

/**
 * @param {Record<string, string|null|undefined>} rules
 * @returns {{ ok: boolean, errors: Record<string, string>, first: string }}
 */
export function collectErrors(rules) {
  const errors = {}
  let first = ''
  for (const [key, msg] of Object.entries(rules)) {
    if (msg) {
      errors[key] = msg
      if (!first) first = msg
    }
  }
  return { ok: !first, errors, first }
}
