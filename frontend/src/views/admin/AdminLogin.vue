<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, setAdminToken } from '../../api/http'

const router = useRouter()
const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function login() {
  error.value = ''
  loading.value = true
  try {
    const data = await api.adminLogin(username.value.trim(), password.value)
    setAdminToken(data.token)
    router.push('/admin/characters')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="admin-shell login">
    <div class="box">
      <h1>思答帝 · 管理端</h1>
      <p>设置识字 / 古诗 / 英语题库</p>

      <form autocomplete="on" @submit.prevent="login">
        <label for="admin-username">账号</label>
        <input
          id="admin-username"
          v-model="username"
          name="username"
          type="text"
          autocomplete="username"
          autocapitalize="off"
          autocorrect="off"
          spellcheck="false"
          required
        />

        <label for="admin-password">密码</label>
        <input
          id="admin-password"
          v-model="password"
          name="password"
          type="password"
          autocomplete="current-password"
          required
        />

        <p v-if="error" class="error">{{ error }}</p>
        <button class="btn btn-sun" type="submit" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <button class="btn btn-ghost" type="button" @click="router.push('/')">返回用户端</button>
    </div>
  </div>
</template>

<style scoped>
.login {
  display: grid;
  place-items: center;
  min-height: 100vh;
}

.box {
  width: min(420px, 100%);
  background: white;
  border-radius: 24px;
  padding: 28px;
  box-shadow: var(--shadow);
  display: grid;
  gap: 10px;
}

form {
  display: grid;
  gap: 10px;
}

h1 {
  font-family: var(--font-display);
  font-size: 1.8rem;
  color: var(--sun-deep);
}

p {
  color: var(--ink-soft);
  font-weight: 700;
  margin-bottom: 8px;
}

label {
  font-weight: 800;
  font-size: 0.9rem;
}

.error {
  color: #e85d5d;
}

.btn {
  margin-top: 6px;
}
</style>
