<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { numberToChinese } from '../../utils/numbers'
import { speak } from '../../utils/speech'
import { useHotkeys } from '../../composables/useHotkeys'
import FeedbackOverlay from '../../components/FeedbackOverlay.vue'
import ParentJudge from '../../components/ParentJudge.vue'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const a = ref(1)
const b = ref(1)
const op = ref('+')
const answer = ref('')
const showJudge = ref(false)
const feedback = ref('')

const expression = computed(() => `${a.value} ${op.value} ${b.value}`)

const hints = computed(() => {
  if (showJudge.value || feedback.value) return []
  return [
    { keys: '空格', label: '读题' },
    { keys: '0-9', label: '输入答案' },
    { keys: 'Enter', label: '判断' },
    { keys: 'N', label: '换题' },
    { keys: 'Backspace', label: '清空' },
    { keys: 'Esc', label: '返回' },
  ]
})

function nextQuestion() {
  op.value = Math.random() > 0.5 ? '+' : '-'
  if (op.value === '+') {
    a.value = rand(0, 10)
    b.value = rand(0, 10 - a.value)
  } else {
    a.value = rand(0, 10)
    b.value = rand(0, a.value)
  }
  answer.value = ''
  showJudge.value = false
}

function rand(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function press(n) {
  if (showJudge.value || feedback.value) return
  if (answer.value.length >= 2) return
  answer.value += String(n)
}

function clearAns() {
  answer.value = ''
}

function backspaceAns() {
  answer.value = answer.value.slice(0, -1)
}

async function readQuestion() {
  const left = numberToChinese(a.value)
  const right = numberToChinese(b.value)
  const text = op.value === '+'
    ? `${left}加${right}等于几`
    : `${left}减${right}等于几`
  await speak(text)
}

function submit() {
  if (answer.value === '') return
  showJudge.value = true
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
  nextQuestion()
}

useHotkeys(() => {
  if (showJudge.value || feedback.value) {
    return {}
  }
  const handlers = {
    ' ': readQuestion,
    Space: readQuestion,
    Enter: submit,
    n: nextQuestion,
    N: nextQuestion,
    KeyN: nextQuestion,
    Backspace: () => {
      if (answer.value) backspaceAns()
      else router.push('/')
    },
    Escape: () => router.push('/'),
    Delete: clearAns,
  }
  for (let i = 0; i <= 9; i++) {
    handlers[String(i)] = () => press(i)
    handlers[`Digit${i}`] = () => press(i)
    handlers[`Numpad${i}`] = () => press(i)
  }
  return handlers
})

nextQuestion()
</script>

<template>
  <div class="phone-shell">
    <div class="page">
      <div class="topbar">
        <button class="icon-btn" @click="router.push('/')">←</button>
        <h1>加减运算</h1>
        <button class="icon-btn" @click="readQuestion">🔊</button>
      </div>

      <div class="stage panel">
        <div class="expr">{{ expression }} = ?</div>
        <div class="answer">{{ answer || '□' }}</div>
        <p class="hint">空格读题 · 数字键作答 · Enter 判断</p>
      </div>

      <div class="pad">
        <button v-for="n in 10" :key="n - 1" class="key" @click="press(n - 1)">{{ n - 1 }}</button>
        <button class="key ghost" @click="clearAns">清空</button>
        <button class="key ok" @click="submit">判断</button>
      </div>

      <button class="btn btn-sky skip" @click="nextQuestion">换一题</button>
      <KeyboardHints :items="hints" />

      <ParentJudge
        v-if="showJudge"
        @correct="onCorrect"
        @wrong="onWrong"
        @cancel="showJudge = false"
      />
      <FeedbackOverlay
        v-if="feedback"
        :type="feedback"
        @done="onFeedbackDone"
      />
    </div>
  </div>
</template>

<style scoped>
.stage {
  text-align: center;
  padding: 28px 16px;
  margin-bottom: 16px;
}

.expr {
  font-family: var(--font-display);
  font-size: 2.4rem;
  color: var(--ink);
}

.answer {
  margin-top: 14px;
  font-size: 3.4rem;
  font-weight: 900;
  color: var(--sun-deep);
  min-height: 1.2em;
}

.hint {
  margin-top: 10px;
  color: var(--ink-soft);
  font-weight: 700;
}

.pad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.key {
  height: 56px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 6px 0 #ecdccc;
  font-weight: 900;
  font-size: 1.3rem;
  cursor: pointer;
}

.key:active {
  transform: translateY(3px);
  box-shadow: 0 2px 0 #ecdccc;
}

.key.ghost {
  background: #fff4ea;
  font-size: 1rem;
}

.key.ok {
  background: linear-gradient(180deg, #a8ecc8, var(--mint-deep));
  color: white;
  box-shadow: 0 6px 0 #3aa872;
}

.skip {
  margin-top: 14px;
  width: 100%;
}
</style>
