<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { preloadNumberVoices, speakNumber, stopSpeak } from '../../utils/speech'
import { useHotkeys } from '../../composables/useHotkeys'
import FeedbackOverlay from '../../components/FeedbackOverlay.vue'
import ParentJudge from '../../components/ParentJudge.vue'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const current = ref(1)
const reading = ref(false)
const testing = ref(false)
const missing = ref(null)
const showJudge = ref(false)
const feedback = ref('')

const display = computed(() => {
  if (testing.value && missing.value === current.value) return '?'
  return current.value
})

const hints = computed(() => {
  if (showJudge.value || feedback.value) return []
  const list = [
    { keys: '空格', label: '发音' },
    { keys: '←/→', label: '切换' },
    { keys: 'T', label: testing.value ? '退出测试' : '测试' },
    { keys: 'L', label: reading.value ? '停止领读' : '领读' },
    { keys: 'Esc', label: '返回' },
  ]
  return list
})

onMounted(() => {
  preloadNumberVoices()
})

onUnmounted(() => stopSpeak())

async function leadRead() {
  if (reading.value) return
  stopSpeak()
  reading.value = true
  testing.value = false
  missing.value = null
  for (let n = 1; n <= 100; n++) {
    if (!reading.value) break
    current.value = n
    await speakNumber(n)
    await sleep(80)
  }
  reading.value = false
}

function stopRead() {
  reading.value = false
  stopSpeak()
}

function toggleLead() {
  if (reading.value) stopRead()
  else leadRead()
}

function startTest() {
  stopRead()
  testing.value = true
  showJudge.value = false
  feedback.value = ''
  pickMissing()
}

function pickMissing() {
  missing.value = Math.floor(Math.random() * 100) + 1
  current.value = missing.value
}

function endTest() {
  testing.value = false
  missing.value = null
  showJudge.value = false
}

function toggleTest() {
  if (testing.value) endTest()
  else startTest()
}

async function onNumberClick() {
  if (showJudge.value || feedback.value) return
  if (testing.value && current.value === missing.value) {
    showJudge.value = true
    return
  }
  await speakNumber(current.value)
}

function prev() {
  if (reading.value || showJudge.value) return
  current.value = current.value <= 1 ? 100 : current.value - 1
}

function next() {
  if (reading.value || showJudge.value) return
  current.value = current.value >= 100 ? 1 : current.value + 1
}

function onCorrect() {
  showJudge.value = false
  feedback.value = 'success'
}

function onWrong() {
  showJudge.value = false
  feedback.value = 'fail'
}

function onFeedbackDone() {
  feedback.value = ''
  if (testing.value) pickMissing()
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms))
}

useHotkeys(() => {
  if (showJudge.value || feedback.value) {
    return {}
  }
  return {
    ' ': onNumberClick,
    Space: onNumberClick,
    ArrowLeft: prev,
    ArrowRight: next,
    ArrowUp: prev,
    ArrowDown: next,
    t: toggleTest,
    T: toggleTest,
    KeyT: toggleTest,
    l: toggleLead,
    L: toggleLead,
    KeyL: toggleLead,
    Escape: () => router.push('/'),
    Backspace: () => router.push('/'),
  }
})
</script>

<template>
  <div class="phone-shell">
    <div class="page">
      <div class="topbar">
        <button class="icon-btn" @click="router.push('/')">←</button>
        <h1>快乐数数</h1>
        <button class="test-btn" @click="toggleTest">
          {{ testing ? '退出测试' : '测试' }}
        </button>
      </div>

      <div class="stage panel">
        <button class="number floaty" @click="onNumberClick">{{ display }}</button>
        <div class="caption">{{ testing ? '宝宝来念出这个数字吧' : '空格键 / 点数字听真人发音' }}</div>
      </div>

      <div class="controls">
        <button class="btn btn-ghost" :disabled="reading" @click="prev">← 上一个</button>
        <button class="btn btn-ghost" :disabled="reading" @click="next">下一个 →</button>
      </div>

      <div class="lead">
        <button v-if="!reading" class="btn btn-sky" @click="leadRead">🔊 领读 1→100</button>
        <button v-else class="btn btn-pink" @click="stopRead">停止领读</button>
      </div>

      <KeyboardHints :items="hints" />

      <ParentJudge
        v-if="showJudge"
        @correct="onCorrect"
        @wrong="onWrong"
        @cancel="endTest"
      />
      <FeedbackOverlay v-if="feedback" :type="feedback" @done="onFeedbackDone" />
    </div>
  </div>
</template>

<style scoped>
.test-btn {
  background: linear-gradient(180deg, #ffb4d0, var(--pink));
  color: white;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 800;
  cursor: pointer;
}

.stage {
  text-align: center;
  padding: 40px 16px;
  margin-bottom: 16px;
  background: linear-gradient(180deg, #eefaff, #fff);
}

.number {
  width: 180px;
  height: 180px;
  border-radius: 40px;
  background: linear-gradient(180deg, #9adcf8, var(--sky));
  color: white;
  font-size: 5rem;
  font-weight: 900;
  box-shadow: 0 12px 0 #2f97c4;
  cursor: pointer;
}

.caption {
  margin-top: 18px;
  font-weight: 800;
  color: var(--ink-soft);
}

.controls {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.lead {
  margin-top: 14px;
}

.lead .btn {
  width: 100%;
}
</style>
