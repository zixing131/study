<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { speak } from '../../utils/speech'
import { useHotkeys } from '../../composables/useHotkeys'
import SpeakerButton from '../../components/SpeakerButton.vue'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const list = ref([])
const index = ref(0)
const loading = ref(true)
const error = ref('')

const current = computed(() => list.value[index.value] || null)
const words = computed(() => (current.value?.words || '').split(/[,，、]/).filter(Boolean))
const strokes = computed(() => (current.value?.strokeOrder || '').split(/[,，、]/).filter(Boolean))

onMounted(async () => {
  try {
    list.value = await api.getCharacters()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function next() {
  if (!list.value.length) return
  index.value = (index.value + 1) % list.value.length
}

function prev() {
  if (!list.value.length) return
  index.value = (index.value - 1 + list.value.length) % list.value.length
}

async function play() {
  if (!current.value) return
  await speak(current.value.charText)
}

useHotkeys(() => ({
  ' ': play,
  Space: play,
  ArrowLeft: prev,
  ArrowRight: next,
  ArrowUp: prev,
  ArrowDown: next,
  Escape: () => router.push('/'),
  Backspace: () => router.push('/'),
}))
</script>

<template>
  <div class="phone-shell">
    <div class="page">
      <div class="topbar">
        <button class="icon-btn" @click="router.push('/')">←</button>
        <h1>识字乐园</h1>
        <div class="progress">{{ list.length ? index + 1 : 0 }}/{{ list.length }}</div>
      </div>

      <div v-if="loading" class="panel tip">正在准备汉字宝宝…</div>
      <div v-else-if="error" class="panel tip">{{ error }}</div>
      <template v-else-if="current">
        <div class="stage panel">
          <div class="char-row">
            <div class="char floaty" @click="play">{{ current.charText }}</div>
            <SpeakerButton :text="current.charText" />
          </div>
          <div class="pinyin">{{ current.pinyin }}</div>
        </div>

        <div class="info panel">
          <div class="block">
            <div class="label">✏️ 笔顺</div>
            <div class="chips">
              <span v-for="(s, i) in strokes" :key="i" class="chip">{{ i + 1 }}.{{ s }}</span>
            </div>
          </div>
          <div class="block">
            <div class="label">🧩 组词</div>
            <div class="chips">
              <span v-for="w in words" :key="w" class="chip soft">{{ w }}</span>
            </div>
          </div>
          <div class="block">
            <div class="label">💬 句子</div>
            <p class="sentence">{{ current.sentence }}</p>
          </div>
        </div>

        <div class="nav">
          <button class="btn btn-ghost" @click="prev">← 上一题</button>
          <button class="btn btn-sun" @click="next">下一题 →</button>
        </div>

        <KeyboardHints
          :items="[
            { keys: '空格', label: '发音' },
            { keys: '←/→', label: '切换' },
            { keys: 'Esc', label: '返回' },
          ]"
        />
      </template>
    </div>
  </div>
</template>

<style scoped>
.progress {
  min-width: 44px;
  text-align: right;
  font-weight: 800;
  color: var(--ink-soft);
}

.tip {
  text-align: center;
  font-weight: 700;
}

.stage {
  text-align: center;
  padding: 28px 18px 22px;
  margin-bottom: 14px;
  background: linear-gradient(180deg, #fff, #fff4ea);
}

.char-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
}

.char {
  font-family: 'Songti SC', 'SimSun', serif;
  font-size: clamp(6rem, 28vw, 8rem);
  line-height: 1;
  color: var(--ink);
  text-shadow: 4px 6px 0 rgba(255, 184, 140, 0.35);
  cursor: pointer;
}

.pinyin {
  margin-top: 10px;
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--sky-deep);
  letter-spacing: 0.08em;
}

.info {
  display: grid;
  gap: 14px;
}

.label {
  font-family: var(--font-display);
  margin-bottom: 8px;
  font-size: 1.15rem;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  background: #fff1d8;
  border-radius: 999px;
  padding: 6px 12px;
  font-weight: 800;
}

.chip.soft {
  background: #e8f8ff;
}

.sentence {
  font-weight: 700;
  line-height: 1.6;
  color: var(--ink-soft);
  font-size: 1.05rem;
}

.nav {
  margin-top: auto;
  padding-top: 18px;
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 12px;
}
</style>
