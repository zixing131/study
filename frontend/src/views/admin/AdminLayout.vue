<script setup>
import { useRoute, useRouter } from 'vue-router'
import { api, clearAdminToken } from '../../api/http'

const router = useRouter()
const route = useRoute()

const tabs = [
  { path: '/admin/characters', label: '识字题库' },
  { path: '/admin/poems', label: '古诗题库' },
  { path: '/admin/english', label: '英语题库' },
]

async function logout() {
  try { await api.adminLogout() } catch { /* ignore */ }
  clearAdminToken()
  router.push('/admin/login')
}
</script>

<template>
  <div class="admin-shell">
    <header class="head">
      <div>
        <h1>思答帝管理端</h1>
        <p>题库配置</p>
      </div>
      <div class="actions">
        <button class="btn btn-ghost" @click="router.push('/')">用户端</button>
        <button class="btn btn-pink" @click="logout">退出</button>
      </div>
    </header>

    <nav class="tabs">
      <router-link
        v-for="tab in tabs"
        :key="tab.path"
        :to="tab.path"
        class="tab"
        :class="{ active: route.path === tab.path }"
      >
        {{ tab.label }}
      </router-link>
    </nav>

    <main class="main panel">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

h1 {
  font-family: var(--font-display);
  font-size: 1.8rem;
  color: var(--sun-deep);
}

p {
  color: var(--ink-soft);
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.tab {
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  font-weight: 800;
  border: 2px solid transparent;
}

.tab.active {
  background: white;
  border-color: var(--sun);
  color: var(--sun-deep);
}

.main {
  background: white;
}
</style>
