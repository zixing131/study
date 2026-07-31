<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api/http'
import { speak, stopSpeak } from '../../utils/speech'
import { useHotkeys } from '../../composables/useHotkeys'
import FeedbackOverlay from '../../components/FeedbackOverlay.vue'
import ParentJudge from '../../components/ParentJudge.vue'
import KeyboardHints from '../../components/KeyboardHints.vue'

const route = useRoute()
const router = useRouter()
const poem = ref(null)
const activeLine = ref(-1)
const cursor = ref(0)
const testing = ref(false)
const missingIndex = ref(-1)
const showJudge = ref(false)
const feedback = ref('')

const lines = computed(() => poem.value?.lines || [])

const hints = computed(() => {
  if (showJudge.value || feedback.value) return []
  return [
    { keys: '空格/Enter', label: '朗读当前句' },
    { keys: '↑/↓', label: '选句' },
    { keys: 'T', label: testing.value ? '退出测试' : '测试' },
    { keys: 'Esc', label: '返回列表' },
  ]
})

onMounted(async () => {
  poem.value = await api.getPoem(route.params.id)
})

onUnmounted(() => stopSpeak())

async function readLine(i) {
  if (i < 0 || i >= lines.value.length) return
  cursor.value = i
  if (testing.value && i === missingIndex.value) {
    showJudge.value = true
    return
  }
  activeLine.value = i
  await speak(lines.value[i])
  activeLine.value = -1
}

function moveCursor(delta) {
  if (!lines.value.length || showJudge.value) return
  cursor.value = (cursor.value + delta + lines.value.length) % lines.value.length
}

function startTest() {
  stopSpeak()
  testing.value = true
  showJudge.value = false
  feedback.value = ''
  missingIndex.value = Math.floor(Math.random() * lines.value.length)
  cursor.value = missingIndex.value
}

function endTest() {
  testing.value = false
  missingIndex.value = -1
  showJudge.value = false
}

function toggleTest() {
  if (testing.value) endTest()
  else startTest()
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
  if (testing.value) {
    missingIndex.value = Math.floor(Math.random() * lines.value.length)
    cursor.value = missingIndex.value
  }
}

useHotkeys(() => {
  if (showJudge.value || feedback.value) {
    return {}
  }
  return {
    ' ': () => readLine(cursor.value),
    Space: () => readLine(cursor.value),
    Enter: () => readLine(cursor.value),
    ArrowUp: () => moveCursor(-1),
    ArrowDown: () => moveCursor(1),
    ArrowLeft: () => moveCursor(-1),
    ArrowRight: () => moveCursor(1),
    t: toggleTest,
    T: toggleTest,
    KeyT: toggleTest,
    Escape: () => router.push('/chinese/poem'),
    Backspace: () => router.push('/chinese/poem'),
  }
})
</script>

<template>
  <div class="phone-shell">
    <div class="page">
      <div class="topbar">
        <button class="icon-btn" @click="router.push('/chinese/poem')">←</button>
        <h1>{{ poem?.title || '古诗' }}</h1>
        <button class="test-btn" @click="toggleTest">
          {{ testing ? '退出测试' : '测试' }}
        </button>
      </div>

      <div v-if="poem" class="panel poem">
        <div class="meta">{{ poem.dynasty }} · {{ poem.author }}</div>
        <button
          v-for="(line, i) in lines"
          :key="i"
          class="line"
          :class="{
            active: activeLine === i,
            missing: testing && missingIndex === i,
            focused: cursor === i,
          }"
          @click="readLine(i)"
        >
          <template v-if="testing && missingIndex === i">？？？？</template>
          <template v-else>{{ line }}</template>
        </button>
        <p class="hint">{{ testing ? '空格读缺句，由宝宝念出来～' : '↑↓ 选句，空格朗读' }}</p>
      </div>

      <KeyboardHints :items="hints" />

      <ParentJudge
        v-if="showJudge"
        @correct="onCorrect"
        @wrong="onWrong"
        @cancel="endTest"
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
.test-btn {
  background: linear-gradient(180deg, #ffb4d0, var(--pink));
  color: white;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 0 rgba(0, 0, 0, 0.08);
}

.poem {
  text-align: center;
  padding: 22px 16px;
}

.meta {
  color: var(--ink-soft);
  font-weight: 700;
  margin-bottom: 16px;
}

.line {
  display: block;
  width: 100%;
  background: transparent;
  font-family: var(--font-display);
  font-size: 1.55rem;
  padding: 12px 8px;
  border-radius: 16px;
  cursor: pointer;
  margin-bottom: 6px;
  transition: background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.line.active {
  background: #fff1d8;
  transform: scale(1.03);
}

.line.focused {
  box-shadow: inset 0 0 0 2px rgba(255, 138, 92, 0.45);
}

.line.missing {
  background: #e8f8ff;
  color: var(--sky-deep);
  border: 2px dashed var(--sky);
  animation: bounce-soft 1.4s ease-in-out infinite;
}

.hint {
  margin-top: 14px;
  color: var(--ink-soft);
  font-weight: 700;
}
</style>
