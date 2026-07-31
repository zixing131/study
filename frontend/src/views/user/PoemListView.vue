<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../../api/http'
import { useHotkeys } from '../../composables/useHotkeys'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const poems = ref([])
const loading = ref(true)
const cursor = ref(0)

const current = computed(() => poems.value[cursor.value] || null)

onMounted(async () => {
  try {
    poems.value = await api.getPoems()
  } finally {
    loading.value = false
  }
})

function move(delta) {
  if (!poems.value.length) return
  cursor.value = (cursor.value + delta + poems.value.length) % poems.value.length
}

function openCurrent() {
  if (!current.value) return
  router.push(`/chinese/poem/${current.value.id}`)
}

useHotkeys(() => ({
  ArrowUp: () => move(-1),
  ArrowDown: () => move(1),
  ArrowLeft: () => move(-1),
  ArrowRight: () => move(1),
  Enter: openCurrent,
  ' ': openCurrent,
  Space: openCurrent,
  Escape: () => router.push('/'),
  Backspace: () => router.push('/'),
}))
</script>

<template>
  <div class="phone-shell">
    <div class="page">
      <div class="topbar">
        <button class="icon-btn" @click="router.push('/')">←</button>
        <h1>古诗童谣</h1>
        <span style="width:44px"></span>
      </div>

      <div v-if="loading" class="panel">加载中…</div>
      <div class="list">
        <button
          v-for="(poem, i) in poems"
          :key="poem.id"
          class="poem-card panel"
          :class="{ focused: cursor === i }"
          @click="router.push(`/chinese/poem/${poem.id}`)"
          @mouseenter="cursor = i"
        >
          <div class="title">{{ poem.title }}</div>
          <div class="meta">{{ poem.dynasty }} · {{ poem.author }}</div>
          <div class="preview">{{ (poem.lines || []).slice(0, 2).join('，') }}…</div>
        </button>
      </div>

      <KeyboardHints
        :items="[
          { keys: '↑/↓', label: '选择' },
          { keys: 'Enter', label: '打开' },
          { keys: 'Esc', label: '返回' },
        ]"
      />
    </div>
  </div>
</template>

<style scoped>
.list {
  display: grid;
  gap: 12px;
}

.poem-card {
  text-align: left;
  cursor: pointer;
  border: 2px solid #f1e2d6;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.poem-card:active {
  transform: scale(0.98);
}

.poem-card.focused {
  border-color: var(--sun);
  box-shadow: 0 0 0 3px rgba(255, 138, 92, 0.18);
}

.title {
  font-family: var(--font-display);
  font-size: 1.4rem;
}

.meta {
  margin-top: 4px;
  color: var(--ink-soft);
  font-weight: 700;
}

.preview {
  margin-top: 10px;
  color: var(--sun-deep);
  font-weight: 700;
}
</style>
