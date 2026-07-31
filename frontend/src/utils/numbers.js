const DIGITS = ['零', '一', '二', '三', '四', '五', '六', '七', '八', '九']

/** 将 1–100 转为中文读法（学龄前常用） */
export function numberToChinese(n) {
  const num = Number(n)
  if (!Number.isInteger(num) || num < 0 || num > 100) {
    return String(n)
  }
  if (num <= 10) {
    return num === 10 ? '十' : DIGITS[num]
  }
  if (num < 20) {
    return '十' + DIGITS[num % 10]
  }
  if (num < 100) {
    const tens = Math.floor(num / 10)
    const ones = num % 10
    return DIGITS[tens] + '十' + (ones ? DIGITS[ones] : '')
  }
  return '一百'
}

/** 预生成 1–100 读法表 */
export const NUMBER_READINGS = Object.fromEntries(
  Array.from({ length: 100 }, (_, i) => {
    const n = i + 1
    return [n, numberToChinese(n)]
  }),
)
