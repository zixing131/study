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
const flipped = ref(false)

const current = computed(() => list.value[index.value] || null)

const hints = computed(() => {
  if (showJudge.value || feedback.value) return []
  return [
    { keys: '空格', label: testing.value ? '打开判断' : '发音' },
    { keys: 'F', label: '翻转释义' },
    { keys: '←/→', label: '切换' },
    { keys: 'T', label: testing.value ? '退出测试' : '测试' },
    { keys: 'Esc', label: '返回' },
  ]
})

onMounted(async () => {
  list.value = await api.getEnglish('word')
})

function next() {
  if (!list.value.length) return
  index.value = (index.value + 1) % list.value.length
  flipped.value = false
}

function prev() {
  if (!list.value.length) return
  index.value = (index.value - 1 + list.value.length) % list.value.length
  flipped.value = false
}

function startTest() {
  testing.value = true
  flipped.value = false
}

function endTest() {
  testing.value = false
  flipped.value = false
  showJudge.value = false
}

function toggleTest() {
  if (testing.value) endTest()
  else startTest()
}

function onCardClick() {
  if (testing.value) {
    showJudge.value = true
  } else {
    flipped.value = !flipped.value
  }
}

async function playOrJudge() {
  if (showJudge.value || feedback.value) return
  if (testing.value) {
    showJudge.value = true
    return
  }
  if (current.value) await speak(current.value.word, { lang: 'en-US' })
}

function onCorrect() {
  showJudge.value = false
  feedback.value = 'success'
  flipped.value = true
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
    f: () => { flipped.value = !flipped.value },
    F: () => { flipped.value = !flipped.value },
    KeyF: () => { flipped.value = !flipped.value },
    Enter: onCardClick,
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
        <h1>单词卡片</h1>
        <button class="test-btn" @click="toggleTest">
          {{ testing ? '退出测试' : '测试' }}
        </button>
      </div>

      <div v-if="current" class="card panel" @click="onCardClick">
        <div class="emoji floaty">{{ current.emoji || '📘' }}</div>
        <div class="word">{{ testing && !flipped ? '???' : current.word }}</div>
        <div class="row">
          <span class="phonetic">{{ testing && !flipped ? '' : current.phonetic }}</span>
          <SpeakerButton
            v-if="!(testing && !flipped)"
            :text="current.word"
            lang="en-US"
            size="md"
            @click.stop
          />
        </div>
        <div class="meaning">
          {{ testing && !flipped ? '宝宝说出英文单词' : current.meaning }}
        </div>
        <div class="example">{{ testing && !flipped ? '空格打开家长判断' : current.example }}</div>
      </div>

      <div class="nav">
        <button class="btn btn-ghost" @click="prev">← 上一张</button>
        <button class="btn btn-sun" @click="next">下一张 →</button>
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

.card {
  text-align: center;
  padding: 32px 18px;
  margin-bottom: 16px;
  cursor: pointer;
  background: linear-gradient(180deg, #fff, #fff7ef);
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.emoji {
  font-size: 4rem;
}

.word {
  margin-top: 10px;
  font-size: 3rem;
  font-weight: 900;
  color: var(--sun-deep);
  text-transform: lowercase;
}

.row {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.phonetic {
  color: var(--sky-deep);
  font-weight: 800;
}

.meaning {
  margin-top: 16px;
  font-family: var(--font-display);
  font-size: 1.5rem;
}

.example {
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
