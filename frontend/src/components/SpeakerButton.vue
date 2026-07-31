<script setup>
import { ref } from 'vue'
import { speak } from '../utils/speech'

const props = defineProps({
  text: { type: String, required: true },
  lang: { type: String, default: 'zh-CN' },
  size: { type: String, default: 'lg' },
})

const bouncing = ref(false)

async function onClick(e) {
  e?.stopPropagation?.()
  bouncing.value = true
  await speak(props.text, { lang: props.lang })
  setTimeout(() => { bouncing.value = false }, 400)
}
</script>

<template>
  <button
    class="speaker"
    :class="[size, { bounce: bouncing }]"
    type="button"
    aria-label="朗读"
    @click="onClick"
  >
    🔊
  </button>
</template>

<style scoped>
.speaker {
  border-radius: 50%;
  background: linear-gradient(180deg, #fff3b0, #ffe566);
  box-shadow: 0 8px 0 #f0c93d, 0 12px 24px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: transform 0.15s ease;
}

.speaker.lg {
  width: 72px;
  height: 72px;
  font-size: 2rem;
}

.speaker.md {
  width: 48px;
  height: 48px;
  font-size: 1.35rem;
}

.speaker:active {
  transform: translateY(4px);
  box-shadow: 0 3px 0 #f0c93d;
}

.speaker.bounce {
  animation: wiggle 0.4s ease;
}
</style>
