<script setup>
import { onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
})

const emit = defineEmits(['close'])

function onKey(e) {
  if (e.key === 'Escape' && props.open) emit('close')
}

watch(
  () => props.open,
  (v) => {
    document.body.style.overflow = v ? 'hidden' : ''
  },
)

onMounted(() => window.addEventListener('keydown', onKey))
onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
  document.body.style.overflow = ''
})
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="mask" @click.self="emit('close')">
      <div class="dialog" role="dialog" aria-modal="true">
        <header class="head">
          <h3>{{ title }}</h3>
          <button type="button" class="x" aria-label="关闭" @click="emit('close')">×</button>
        </header>
        <div class="body">
          <slot />
        </div>
        <footer v-if="$slots.footer" class="foot">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.mask {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(61, 44, 41, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fadeIn 0.16s ease;
}

.dialog {
  width: min(640px, 100%);
  max-height: min(86vh, 820px);
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 24px 48px rgba(61, 44, 41, 0.22);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: pop 0.18s ease;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(61, 44, 41, 0.08);
}

h3 {
  font-family: var(--font-display);
  font-size: 1.25rem;
  color: var(--sun-deep);
}

.x {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(61, 44, 41, 0.06);
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  color: var(--ink-soft);
}

.x:hover {
  background: rgba(61, 44, 41, 0.12);
}

.body {
  padding: 18px;
  overflow: auto;
}

.foot {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  padding: 14px 18px 18px;
  border-top: 1px solid rgba(61, 44, 41, 0.08);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes pop {
  from { opacity: 0; transform: translateY(8px) scale(0.98); }
  to { opacity: 1; transform: none; }
}
</style>
