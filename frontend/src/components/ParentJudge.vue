<script setup>
import { useHotkeys } from '../composables/useHotkeys'
import KeyboardHints from './KeyboardHints.vue'

const emit = defineEmits(['correct', 'wrong', 'cancel'])

useHotkeys(() => ({
  Enter: () => emit('correct'),
  y: () => emit('correct'),
  Y: () => emit('correct'),
  KeyY: () => emit('correct'),
  n: () => emit('wrong'),
  N: () => emit('wrong'),
  KeyN: () => emit('wrong'),
  Escape: () => emit('cancel'),
}))
</script>

<template>
  <div class="judge panel">
    <div class="title">家长来判断一下吧～</div>
    <div class="hint">宝宝念对了吗？</div>
    <div class="actions">
      <button class="btn btn-mint" @click="$emit('correct')">✅ 正确</button>
      <button class="btn btn-pink" @click="$emit('wrong')">❌ 不对</button>
    </div>
    <button class="btn btn-ghost cancel" @click="$emit('cancel')">先不测了</button>
    <KeyboardHints
      :items="[
        { keys: 'Enter/Y', label: '正确' },
        { keys: 'N', label: '不对' },
        { keys: 'Esc', label: '取消' },
      ]"
    />
  </div>
</template>

<style scoped>
.judge {
  margin-top: 16px;
  text-align: center;
  animation: pop-in 0.35s ease;
}

.title {
  font-family: var(--font-display);
  font-size: 1.35rem;
}

.hint {
  margin: 8px 0 16px;
  color: var(--ink-soft);
  font-weight: 700;
}

.actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.cancel {
  width: 100%;
  margin-top: 12px;
}
</style>
