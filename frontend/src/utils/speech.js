import { NUMBER_READINGS, numberToChinese } from './numbers'

let preferredVoice = null
let currentAudio = null
/** 浏览器侧 Audio 对象缓存，避免重复创建元素 */
const audioCache = new Map()

function pickVoice(langPrefix = 'zh') {
  const voices = window.speechSynthesis?.getVoices?.() || []
  const matched = voices.find((v) => v.lang?.toLowerCase().startsWith(langPrefix))
  return matched || voices[0] || null
}

if (typeof window !== 'undefined' && window.speechSynthesis) {
  window.speechSynthesis.onvoiceschanged = () => {
    preferredVoice = pickVoice('zh')
  }
}

function isEnglish(lang = '') {
  return String(lang).toLowerCase().startsWith('en')
}

/**
 * 走本地服务端缓存接口（后端落盘后不再请求有道）
 * - 英语：type=1 英式 / type=2 美式
 * - 中文：lang=zh
 */
export function localVoiceUrl(text, { lang = 'zh-CN', type = 2 } = {}) {
  const params = new URLSearchParams({
    text: String(text).trim(),
    lang: isEnglish(lang) ? 'en' : 'zh',
    type: String(type),
  })
  return `/api/audio/voice?${params.toString()}`
}

export function localNumberVoiceUrl(n) {
  return `/api/audio/number?n=${Number(n)}`
}

function getCachedAudio(url) {
  if (audioCache.has(url)) {
    const cached = audioCache.get(url)
    try { cached.currentTime = 0 } catch { /* ignore */ }
    return cached
  }
  const audio = new Audio(url)
  audio.preload = 'auto'
  audioCache.set(url, audio)
  return audio
}

function speakCached(url, fallbackText, fallbackLang) {
  stopSpeak()
  return new Promise((resolve) => {
    const audio = getCachedAudio(url)
    currentAudio = audio
    let settled = false
    const finish = () => {
      if (settled) return
      settled = true
      audio.onended = null
      audio.onerror = null
      if (currentAudio === audio) currentAudio = null
      resolve()
    }
    const fallback = () => {
      if (settled) return
      settled = true
      audio.onended = null
      audio.onerror = null
      if (currentAudio === audio) currentAudio = null
      speakBrowser(fallbackText, { lang: fallbackLang }).then(resolve)
    }
    audio.onended = finish
    audio.onerror = fallback
    const play = audio.play()
    if (play?.catch) {
      play.catch(fallback)
    }
  })
}

function speakBrowser(text, { lang = 'zh-CN', rate = 0.9, pitch = 1.1 } = {}) {
  if (!text || !window.speechSynthesis) return Promise.resolve()
  window.speechSynthesis.cancel()
  return new Promise((resolve) => {
    const utter = new SpeechSynthesisUtterance(String(text))
    utter.lang = lang
    utter.rate = rate
    utter.pitch = pitch
    const voice = pickVoice(lang.slice(0, 2)) || preferredVoice
    if (voice) utter.voice = voice
    utter.onend = () => resolve()
    utter.onerror = () => resolve()
    window.speechSynthesis.speak(utter)
  })
}

/**
 * @param {string} text
 * @param {{ lang?: string, rate?: number, pitch?: number, youdaoType?: 1|2, engine?: 'youdao'|'browser' }} options
 */
export function speak(text, options = {}) {
  const {
    lang = 'zh-CN',
    rate = 0.9,
    pitch = 1.1,
    youdaoType = 2,
    engine = 'youdao',
  } = options
  if (!text) return Promise.resolve()
  if (engine === 'browser') {
    return speakBrowser(text, { lang, rate, pitch })
  }
  const url = localVoiceUrl(text, { lang, type: youdaoType })
  return speakCached(url, text, isEnglish(lang) ? 'en-US' : 'zh-CN')
}

/** 1–100 数字真人发音（服务端缓存） */
export function speakNumber(n) {
  const reading = NUMBER_READINGS[n] || numberToChinese(n)
  return speakCached(localNumberVoiceUrl(n), reading, 'zh-CN')
}

/** 预热浏览器侧 1–100（文件已在服务端缓存时几乎瞬时） */
export function preloadNumberVoices() {
  for (let n = 1; n <= 100; n++) {
    const url = localNumberVoiceUrl(n)
    if (!audioCache.has(url)) {
      const audio = new Audio()
      audio.preload = 'auto'
      audio.src = url
      audioCache.set(url, audio)
    }
  }
}

export function stopSpeak() {
  window.speechSynthesis?.cancel?.()
  if (currentAudio) {
    currentAudio.pause()
    try { currentAudio.currentTime = 0 } catch { /* ignore */ }
    currentAudio = null
  }
}

/** 简单成功音效（WebAudio） */
export function playSuccessSound() {
  playToneSequence([523.25, 659.25, 783.99], 0.12, 'triangle')
}

/** 简单失败音效 */
export function playFailSound() {
  playToneSequence([300, 220], 0.18, 'sawtooth')
}

function playToneSequence(freqs, duration, type = 'sine') {
  try {
    const ctx = new (window.AudioContext || window.webkitAudioContext)()
    freqs.forEach((freq, i) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = type
      osc.frequency.value = freq
      gain.gain.value = 0.0001
      osc.connect(gain)
      gain.connect(ctx.destination)
      const start = ctx.currentTime + i * duration
      gain.gain.exponentialRampToValueAtTime(0.18, start + 0.02)
      gain.gain.exponentialRampToValueAtTime(0.0001, start + duration)
      osc.start(start)
      osc.stop(start + duration + 0.02)
    })
    setTimeout(() => ctx.close(), (freqs.length + 1) * duration * 1000)
  } catch {
    // ignore
  }
}
