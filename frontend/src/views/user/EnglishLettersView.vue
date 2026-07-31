<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { speak } from '../../utils/speech'
import { useHotkeys } from '../../composables/useHotkeys'
import SpeakerButton from '../../components/SpeakerButton.vue'
import FeedbackOverlay from '../../components/FeedbackOverlay.vue'
import ParentJudge from '../../components/ParentJudge.vue'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const list = ref([])
const index = ref(0)
const testing = ref(false)
const showJudge = ref(false)
const feedback = ref('')
const hidden = ref(false)

const current = computed(() => list.value[index.value] || null)

const hints = computed(() => {
  if (showJudge.value || feedback.value) return []
  return [
    { keys: '空格', label: testing.value ? '打开判断' : '发音' },
    { keys: '←/→', label: '切换' },
    { keys: 'T', label: testing.value ? '退出测试' : '测试' },
    { keys: 'Esc', label: '返回' },
  ]
})

onMounted(async () => {
  list.value = await api.getEnglish('letter')
})

function next() {
  if (!list.value.length) return
  index.value = (index.value + 1) % list.value.length
  if (testing.value) hidden.value = true
}

function prev() {
  if (!list.value.length) return
  index.value = (index.value - 1 + list.value.length) % list.value.length
  if (testing.value) hidden.value = true
}

function startTest() {
  testing.value = true
  hidden.value = true
  showJudge.value = false
}

function endTest() {
  testing.value = false
  hidden.value = false
  showJudge.value = false
}

function toggleTest() {
  if (testing.value) endTest()
  else startTest()
}

function askJudge() {
  if (testing.value) showJudge.value = true
}

async function playOrJudge() {
  if (showJudge.value || feedback.value) return
  if (testing.value && hidden.value) {
    askJudge()
    return
  }
  if (current.value) await speak(current.value.word, { lang: 'en-US' })
}

function onCorrect() {
  showJudge.value = false
  feedback.value = 'success'
  hidden.value = false
}

function onWrong() {
  showJudge.value = false
  feedback.value = 'fail'
}

function onFeedbackDone() {
  feedback.value = ''
  if (testing.value) next()
}

useHotkeys(() => {
  if (showJudge.value || feedback.value) {
    return {}
  }
  return {
    ' ': playOrJudge,
    Space: playOrJudge,
    ArrowLeft: prev,
    ArrowRight: next,
    ArrowUp: prev,
    ArrowDown: next,
    t: toggleTest,
    T: toggleTest,
    KeyT: toggleTest,
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
        <h1>字母宝宝</h1>
        <button class="test-btn" @click="toggleTest">
          {{ testing ? '退出测试' : '测试' }}
        </button>
      </div>

      <div v-if="current" class="stage panel">
        <div class="emoji floaty">{{ current.emoji || '🔤' }}</div>
        <div class="letter-row">
          <div class="letter" @click="playOrJudge">
            {{ testing && hidden ? '?' : current.word }}
          </div>
          <SpeakerButton v-if="!(testing && hidden)" :text="current.word" lang="en-US" />
        </div>
        <div class="phonetic">{{ testing && hidden ? '宝宝来念' : current.phonetic }}</div>
        <div class="meaning">{{ testing && hidden ? '空格打开家长判断' : `${current.meaning} · ${current.example}` }}</div>
      </div>

      <div class="nav">
        <button class="btn btn-ghost" @click="prev">← 上一个</button>
        <button class="btn btn-mint" @click="next">下一个 →</button>
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
  padding: 28px 16px;
  margin-bottom: 16px;
}

.emoji {
  font-size: 3.2rem;
  margin-bottom: 8px;
}

.letter-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.letter {
  font-size: 5.5rem;
  font-weight: 900;
  color: var(--mint-deep);
  line-height: 1;
  min-width: 1.2em;
  cursor: pointer;
}

.phonetic {
  margin-top: 10px;
  color: var(--sky-deep);
  font-weight: 800;
  font-size: 1.2rem;
}

.meaning {
  margin-top: 8px;
  color: var(--ink-soft);
  font-weight: 700;
}

.nav {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 12px;
  margin-top: auto;
}
</style>
