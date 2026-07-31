<script setup>
import { onMounted, ref } from 'vue'
import { playFailSound, playSuccessSound } from '../utils/speech'

const props = defineProps({
  type: { type: String, default: 'success' }, // success | fail
})
const emit = defineEmits(['done'])
const petals = ref([])

onMounted(() => {
  if (props.type === 'success') {
    playSuccessSound()
    petals.value = Array.from({ length: 18 }, (_, i) => ({
      id: i,
      left: Math.random() * 90 + 5,
      delay: Math.random() * 0.4,
      emoji: ['🌸', '🌺', '🌼', '⭐', '💖'][i % 5],
    }))
  } else {
    playFailSound()
  }
  setTimeout(() => emit('done'), 1800)
})
</script>

<template>
  <div class="overlay" :class="type">
    <div v-if="type === 'success'" class="burst">
      <div class="big">🌸 太棒啦！</div>
      <div class="sub">你真聪明！</div>
      <span
        v-for="p in petals"
        :key="p.id"
        class="petal"
        :style="{ left: p.left + '%', animationDelay: p.delay + 's' }"
      >{{ p.emoji }}</span>
    </div>
    <div v-else class="burst cry">
      <div class="face">😢</div>
      <div class="big">再试一次吧</div>
      <div class="sub">加油加油！</div>
    </div>
  </div>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 99;
  display: grid;
  place-items: center;
  background: rgba(255, 248, 240, 0.55);
  backdrop-filter: blur(4px);
  animation: pop-in 0.35s ease;
}

.burst {
  position: relative;
  width: min(86vw, 320px);
  text-align: center;
  background: white;
  border-radius: 28px;
  padding: 28px 20px 24px;
  box-shadow: 0 18px 40px rgba(61, 44, 41, 0.16);
  overflow: hidden;
}

.big {
  font-family: var(--font-display);
  font-size: 2rem;
  color: var(--sun-deep);
}

.sub {
  margin-top: 8px;
  color: var(--ink-soft);
  font-weight: 700;
}

.cry .big {
  color: var(--sky-deep);
}

.face {
  font-size: 4rem;
  animation: shake-cry 0.5s ease;
  margin-bottom: 6px;
}

.petal {
  position: absolute;
  top: -10px;
  font-size: 1.4rem;
  animation: confetti-fall 1.6s ease-in forwards;
  pointer-events: none;
}
</style>
