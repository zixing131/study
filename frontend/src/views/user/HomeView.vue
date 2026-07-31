<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useHotkeys } from '../../composables/useHotkeys'
import KeyboardHints from '../../components/KeyboardHints.vue'

const router = useRouter()
const mascot = ref('🦁')
const mascots = ['🦁', '🐰', '🐻', '🦊', '🐼', '🐯']
const focus = ref(0)
let timer

onMounted(() => {
  let i = 0
  timer = setInterval(() => {
    i = (i + 1) % mascots.length
    mascot.value = mascots[i]
  }, 2400)
})

onUnmounted(() => clearInterval(timer))

function scrollToLearn() {
  document.getElementById('learn')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const subjects = [
  {
    title: '语文',
    color: 'sun',
    emoji: '📖',
    blurb: '识字朗读，古诗跟读',
    items: [
      { name: '识字乐园', desc: '大字朗读 · 笔顺组词', path: '/chinese/literacy', icon: '字' },
      { name: '古诗童谣', desc: '点句跟读 · 趣味测试', path: '/chinese/poem', icon: '诗' },
    ],
  },
  {
    title: '数学',
    color: 'sky',
    emoji: '🔢',
    blurb: '数数挑战，加减小练习',
    items: [
      { name: '快乐数数', desc: '1 到 100 · 领读挑战', path: '/math/counting', icon: '123' },
      { name: '加减运算', desc: '10 以内加减法', path: '/math/arithmetic', icon: '±' },
    ],
  },
  {
    title: '英语',
    color: 'mint',
    emoji: '🔤',
    blurb: '字母跟读，单词卡片',
    items: [
      { name: '字母宝宝', desc: 'A 到 Z · 有道发音', path: '/english/letters', icon: 'Aa' },
      { name: '单词卡片', desc: '生活小单词', path: '/english/words', icon: 'Hi' },
    ],
  },
]

const flatEntries = computed(() =>
  subjects.flatMap((s) => s.items.map((item) => ({ ...item, subject: s.title }))),
)

function entryIndex(path) {
  return flatEntries.value.findIndex((e) => e.path === path)
}

function moveFocus(delta) {
  const total = flatEntries.value.length
  if (!total) return
  focus.value = (focus.value + delta + total) % total
  scrollToLearn()
}

function openFocused() {
  const item = flatEntries.value[focus.value]
  if (item) router.push(item.path)
}

useHotkeys(() => {
  const handlers = {
    ArrowLeft: () => moveFocus(-1),
    ArrowRight: () => moveFocus(1),
    ArrowUp: () => moveFocus(-1),
    ArrowDown: () => moveFocus(1),
    Enter: openFocused,
    ' ': openFocused,
    Space: openFocused,
    Home: () => { focus.value = 0; scrollToLearn() },
    End: () => { focus.value = Math.max(0, flatEntries.value.length - 1); scrollToLearn() },
  }
  for (let i = 1; i <= 6; i++) {
    handlers[String(i)] = () => {
      focus.value = i - 1
      openFocused()
    }
    handlers[`Digit${i}`] = () => {
      focus.value = i - 1
      openFocused()
    }
  }
  return handlers
})
</script>

<template>
  <div class="home-shell">
    <div class="bg-orb orb-a" aria-hidden="true"></div>
    <div class="bg-orb orb-b" aria-hidden="true"></div>
    <div class="bg-orb orb-c" aria-hidden="true"></div>

    <header class="top">
      <div class="top-brand">思答帝</div>
      <button class="top-admin" type="button" @click="router.push('/admin/login')">老师入口</button>
    </header>

    <section class="hero">
      <div class="hero-copy">
        <h1 class="brand">思答帝</h1>
        <p class="tagline">和小朋友一起，快乐学语文、数学、英语</p>
        <div class="cta-row">
          <button class="btn btn-sun cta" type="button" @click="scrollToLearn">开始学习</button>
          <button class="btn btn-ghost cta-soft" type="button" @click="router.push('/chinese/literacy')">先去识字</button>
        </div>
      </div>

      <div class="hero-visual" aria-hidden="true">
        <div class="ring ring-1"></div>
        <div class="ring ring-2"></div>
        <div class="mascot floaty">{{ mascot }}</div>
        <span class="spark s1">✨</span>
        <span class="spark s2">🎈</span>
        <span class="spark s3">🌟</span>
      </div>
    </section>

    <section id="learn" class="learn">
      <div class="learn-head">
        <h2>选一个主题开始玩</h2>
        <p>点进去就能听、读、练；也可用方向键 / 数字键 1-6 快捷进入</p>
      </div>

      <div class="subject-grid">
        <article
          v-for="subject in subjects"
          :key="subject.title"
          class="subject"
          :class="subject.color"
        >
          <div class="subject-head">
            <span class="emoji">{{ subject.emoji }}</span>
            <div>
              <h3>{{ subject.title }}</h3>
              <p>{{ subject.blurb }}</p>
            </div>
          </div>
          <div class="entries">
            <button
              v-for="item in subject.items"
              :key="item.path"
              class="entry"
              :class="{ focused: focus === entryIndex(item.path) }"
              type="button"
              @click="router.push(item.path)"
              @mouseenter="focus = entryIndex(item.path)"
            >
              <span class="badge">{{ item.icon }}</span>
              <span class="meta">
                <span class="name">{{ item.name }}</span>
                <span class="desc">{{ item.desc }}</span>
              </span>
              <span class="go">去玩</span>
            </button>
          </div>
        </article>
      </div>

      <KeyboardHints
        :items="[
          { keys: '←/→/↑/↓', label: '选择模块' },
          { keys: 'Enter', label: '进入' },
          { keys: '1-6', label: '快捷进入' },
        ]"
      />
    </section>

    <footer class="foot">
      <span>思答帝 · 学龄前趣味学习</span>
    </footer>
  </div>
</template>

<style scoped>
.home-shell {
  position: relative;
  min-height: 100vh;
  min-height: 100dvh;
  width: 100%;
  overflow-x: hidden;
  padding: 0 clamp(16px, 4vw, 48px) 40px;
}

.bg-orb {
  position: fixed;
  border-radius: 50%;
  filter: blur(40px);
  pointer-events: none;
  z-index: 0;
  opacity: 0.55;
}

.orb-a {
  width: min(48vw, 420px);
  height: min(48vw, 420px);
  left: -8%;
  top: -6%;
  background: #ffe08a;
  animation: drift 12s ease-in-out infinite;
}

.orb-b {
  width: min(42vw, 380px);
  height: min(42vw, 380px);
  right: -6%;
  top: 18%;
  background: #8fd6f5;
  animation: drift 14s ease-in-out infinite reverse;
}

.orb-c {
  width: min(36vw, 320px);
  height: min(36vw, 320px);
  left: 35%;
  bottom: 4%;
  background: #9fe3bf;
  animation: drift 16s ease-in-out infinite;
}

@keyframes drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(18px, -22px) scale(1.06); }
}

.top,
.hero,
.learn,
.foot {
  position: relative;
  z-index: 1;
  width: min(1120px, 100%);
  margin: 0 auto;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 0 8px;
}

.top-brand {
  font-family: var(--font-display);
  font-size: 1.35rem;
  color: var(--sun-deep);
  letter-spacing: 0.04em;
}

.top-admin {
  background: rgba(255, 255, 255, 0.72);
  border: 2px solid rgba(61, 44, 41, 0.06);
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 800;
  color: var(--ink-soft);
  cursor: pointer;
  transition: transform 0.15s ease, background 0.15s ease;
}

.top-admin:hover {
  background: #fff;
  transform: translateY(-1px);
}

.hero {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  gap: clamp(24px, 5vw, 64px);
  min-height: min(72vh, 640px);
  padding: clamp(24px, 5vh, 56px) 0 clamp(32px, 6vh, 72px);
}

.brand {
  font-family: var(--font-display);
  font-size: clamp(3.4rem, 8vw, 6.2rem);
  line-height: 1.05;
  color: var(--sun-deep);
  text-shadow: 0 6px 0 rgba(255, 184, 140, 0.35);
  letter-spacing: 0.06em;
  animation: pop-in 0.55s ease;
}

.tagline {
  margin-top: 16px;
  max-width: 22em;
  color: var(--ink-soft);
  font-weight: 800;
  font-size: clamp(1.05rem, 2vw, 1.35rem);
  line-height: 1.55;
}

.cta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.cta,
.cta-soft {
  min-width: 140px;
  font-size: 1.05rem;
}

.cta-soft {
  background: rgba(255, 255, 255, 0.78);
}

.hero-visual {
  position: relative;
  display: grid;
  place-items: center;
  min-height: 280px;
}

.ring {
  position: absolute;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.65);
  box-shadow: inset 0 0 40px rgba(255, 255, 255, 0.35);
}

.ring-1 {
  width: min(72%, 300px);
  aspect-ratio: 1;
  background: linear-gradient(160deg, rgba(255, 229, 102, 0.45), rgba(94, 200, 242, 0.28));
  animation: pulse-ring 4s ease-in-out infinite;
}

.ring-2 {
  width: min(52%, 210px);
  aspect-ratio: 1;
  background: linear-gradient(200deg, rgba(126, 217, 168, 0.4), rgba(255, 143, 184, 0.25));
  animation: pulse-ring 4s ease-in-out infinite 0.6s;
}

@keyframes pulse-ring {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.04); }
}

.mascot {
  position: relative;
  z-index: 2;
  font-size: clamp(5.5rem, 12vw, 8rem);
  filter: drop-shadow(0 16px 18px rgba(61, 44, 41, 0.12));
  transition: transform 0.35s ease;
}

.spark {
  position: absolute;
  font-size: 1.6rem;
  animation: bounce-soft 2.4s ease-in-out infinite;
  z-index: 3;
}

.s1 { top: 12%; left: 14%; animation-delay: 0s; }
.s2 { top: 18%; right: 16%; animation-delay: 0.35s; }
.s3 { bottom: 16%; right: 22%; animation-delay: 0.7s; }

.learn {
  padding: 8px 0 24px;
}

.learn-head {
  margin-bottom: clamp(18px, 3vw, 28px);
}

.learn-head h2 {
  font-family: var(--font-display);
  font-size: clamp(1.7rem, 3.5vw, 2.3rem);
  color: var(--ink);
}

.learn-head p {
  margin-top: 8px;
  color: var(--ink-soft);
  font-weight: 700;
  font-size: 1rem;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: clamp(14px, 2vw, 22px);
}

.subject {
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(12px);
  border-radius: 28px;
  padding: 20px;
  box-shadow: var(--shadow);
  border: 3px solid transparent;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.subject:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 36px rgba(61, 44, 41, 0.14);
}

.subject.sun { border-color: rgba(255, 138, 92, 0.28); }
.subject.sky { border-color: rgba(94, 200, 242, 0.32); }
.subject.mint { border-color: rgba(126, 217, 168, 0.36); }

.subject-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.subject-head .emoji {
  width: 52px;
  height: 52px;
  display: grid;
  place-items: center;
  border-radius: 18px;
  font-size: 1.7rem;
  background: #fff7ea;
}

.subject.sky .subject-head .emoji { background: #eaf8ff; }
.subject.mint .subject-head .emoji { background: #ebfaf2; }

.subject-head h3 {
  font-family: var(--font-display);
  font-size: 1.55rem;
  line-height: 1.1;
}

.subject-head p {
  margin-top: 4px;
  color: var(--ink-soft);
  font-weight: 700;
  font-size: 0.92rem;
}

.entries {
  display: grid;
  gap: 10px;
}

.entry {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
  width: 100%;
  text-align: left;
  border-radius: 18px;
  padding: 12px 14px;
  background: linear-gradient(135deg, #fff, #fffaf4);
  border: 2px solid #f0e2d6;
  cursor: pointer;
  transition: transform 0.15s ease, border-color 0.15s ease, background 0.15s ease;
}

.entry:hover {
  border-color: rgba(255, 138, 92, 0.45);
  background: #fff;
  transform: translateX(2px);
}

.entry.focused {
  border-color: var(--sun);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 138, 92, 0.2);
  transform: translateX(2px);
}

.entry:active {
  transform: scale(0.98);
}

.badge {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 900;
  font-size: 0.95rem;
  color: #fff;
  background: linear-gradient(180deg, #ffb08a, var(--sun));
}

.subject.sky .badge {
  background: linear-gradient(180deg, #8ad8f7, var(--sky));
}

.subject.mint .badge {
  background: linear-gradient(180deg, #a8ecc8, var(--mint-deep));
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.name {
  font-weight: 900;
  font-size: 1.08rem;
}

.desc {
  color: var(--ink-soft);
  font-size: 0.86rem;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.go {
  color: var(--sun-deep);
  font-weight: 800;
  font-size: 0.92rem;
  white-space: nowrap;
}

.subject.sky .go { color: var(--sky-deep); }
.subject.mint .go { color: var(--mint-deep); }

.foot {
  margin-top: 28px;
  padding-top: 18px;
  border-top: 1px solid rgba(61, 44, 41, 0.06);
  text-align: center;
  color: var(--ink-soft);
  font-weight: 700;
  font-size: 0.9rem;
  opacity: 0.8;
}

/* PC：整页一屏装下，不滚动 */
@media (min-width: 901px) {
  .home-shell {
    height: 100vh;
    height: 100dvh;
    min-height: 0;
    max-height: 100dvh;
    overflow: hidden;
    padding: 0 clamp(20px, 3vw, 40px) 12px;
    display: flex;
    flex-direction: column;
  }

  .top {
    flex: 0 0 auto;
    padding: 10px 0 0;
  }

  .top-brand {
    font-size: 1.15rem;
  }

  .top-admin {
    padding: 6px 12px;
    font-size: 0.9rem;
  }

  .hero {
    flex: 0 0 auto;
    min-height: 0;
    max-height: 42vh;
    gap: clamp(16px, 2.5vw, 36px);
    padding: 12px 0 16px;
    align-content: center;
  }

  .brand {
    font-size: clamp(2.6rem, 4.4vw, 3.8rem);
    text-shadow: 0 4px 0 rgba(255, 184, 140, 0.35);
  }

  .tagline {
    margin-top: 10px;
    font-size: clamp(0.95rem, 1.2vw, 1.12rem);
  }

  .cta-row {
    margin-top: 16px;
    gap: 10px;
  }

  .cta,
  .cta-soft {
    min-width: 120px;
    font-size: 0.95rem;
    padding: 10px 18px;
    box-shadow: 0 5px 0 rgba(0, 0, 0, 0.08);
  }

  .hero-visual {
    min-height: 0;
    height: min(28vh, 230px);
  }

  .ring-1 {
    width: min(70%, 200px);
  }

  .ring-2 {
    width: min(48%, 140px);
  }

  .mascot {
    font-size: clamp(3.6rem, 6vw, 5rem);
  }

  .spark {
    font-size: 1.1rem;
  }

  .learn {
    flex: 1 1 auto;
    min-height: 0;
    padding: 8px 0 0;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
  }

  .learn-head {
    margin-bottom: 14px;
    flex: 0 0 auto;
  }

  .learn-head h2 {
    font-size: clamp(1.35rem, 2vw, 1.65rem);
  }

  .learn-head p {
    margin-top: 4px;
    font-size: 0.9rem;
  }

  .subject-grid {
    gap: 16px;
    flex: 0 0 auto;
    align-items: start;
  }

  .subject {
    border-radius: 24px;
    padding: 16px;
    height: auto;
    display: block;
  }

  .subject-head {
    gap: 10px;
    margin-bottom: 12px;
  }

  .subject-head .emoji {
    width: 44px;
    height: 44px;
    border-radius: 14px;
    font-size: 1.4rem;
  }

  .subject-head h3 {
    font-size: 1.35rem;
  }

  .subject-head p {
    font-size: 0.86rem;
  }

  .entries {
    gap: 10px;
    flex: none;
  }

  .entry {
    padding: 10px 12px;
    border-radius: 16px;
    gap: 12px;
    flex: none;
    min-height: 0;
    height: auto;
  }

  .badge {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    font-size: 0.88rem;
  }

  .name {
    font-size: 1rem;
  }

  .desc {
    font-size: 0.82rem;
  }

  .go {
    font-size: 0.86rem;
  }

  .foot {
    display: none;
  }
}

@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
    text-align: center;
    gap: 12px;
    padding-top: 12px;
  }

  .tagline {
    margin-left: auto;
    margin-right: auto;
  }

  .cta-row {
    justify-content: center;
  }

  .hero-visual {
    order: -1;
    min-height: 220px;
  }

  .subject-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .home-shell {
    padding: 0 14px 28px;
  }

  .brand {
    font-size: clamp(2.8rem, 16vw, 3.6rem);
  }

  .cta,
  .cta-soft {
    width: 100%;
  }

  .entry {
    grid-template-columns: auto 1fr;
  }

  .go {
    display: none;
  }

  .desc {
    white-space: normal;
  }
}

@media (hover: none) {
  .subject:hover {
    transform: none;
  }
}
</style>
