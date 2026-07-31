<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/http'
import AdminModal from '../../components/AdminModal.vue'
import { asInt, collectErrors, required, nonNegInt } from '../../utils/validate'

const list = ref([])
const editingId = ref(null)
const modalOpen = ref(false)
const form = reactive(emptyForm())
const errors = reactive(emptyErrors())
const message = ref('')
const filter = ref('')
const saving = ref(false)

function emptyForm() {
  return {
    word: '',
    phonetic: '',
    meaning: '',
    category: 'word',
    example: '',
    emoji: '',
    sortOrder: 0,
  }
}

function emptyErrors() {
  return {
    word: '',
    phonetic: '',
    meaning: '',
    category: '',
    example: '',
    emoji: '',
    sortOrder: '',
  }
}

function clearErrors() {
  Object.assign(errors, emptyErrors())
  message.value = ''
}

async function load() {
  list.value = await api.adminListEnglish(filter.value || undefined)
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  clearErrors()
  modalOpen.value = true
}

function edit(item) {
  editingId.value = item.id
  Object.assign(form, {
    word: item.word,
    phonetic: item.phonetic,
    meaning: item.meaning,
    category: item.category,
    example: item.example,
    emoji: item.emoji,
    sortOrder: item.sortOrder,
  })
  clearErrors()
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  clearErrors()
  saving.value = false
}

function validateWord(v, category) {
  const base = required(v, category === 'letter' ? '字母' : '单词')
  if (base) return base
  const t = String(v).trim()
  if (category === 'letter') {
    if (!/^[A-Za-z]$/.test(t)) return '字母须为单个 A–Z'
  }
  return null
}

function validateCategory(v) {
  const base = required(v, '分类')
  if (base) return base
  if (v !== 'letter' && v !== 'word') return '分类无效'
  return null
}

function validate() {
  const result = collectErrors({
    word: validateWord(form.word, form.category),
    meaning: required(form.meaning, '中文释义'),
    category: validateCategory(form.category),
    sortOrder: nonNegInt(form.sortOrder, '排序'),
  })
  Object.assign(errors, emptyErrors(), result.errors)
  message.value = result.first
  return result.ok
}

function clearField(key) {
  errors[key] = ''
  if (message.value) message.value = ''
}

async function save() {
  if (!validate()) return
  saving.value = true
  try {
    const word = form.category === 'letter'
      ? form.word.trim().toUpperCase()
      : form.word.trim()
    await api.adminSaveEnglish({
      word,
      phonetic: (form.phonetic || '').trim(),
      meaning: form.meaning.trim(),
      category: form.category,
      example: (form.example || '').trim(),
      emoji: (form.emoji || '').trim(),
      sortOrder: asInt(form.sortOrder, 0),
    }, editingId.value)
    await load()
    closeModal()
  } catch (e) {
    message.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

async function remove(id) {
  if (!confirm('确定删除？')) return
  await api.adminDeleteEnglish(id)
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>题库列表（{{ list.length }}）</h2>
      <div class="actions">
        <select v-model="filter" @change="load">
          <option value="">全部</option>
          <option value="letter">仅字母</option>
          <option value="word">仅单词</option>
        </select>
        <button class="btn btn-sun" @click="openCreate">新增英语条目</button>
      </div>
    </div>

    <table>
      <thead>
        <tr><th>内容</th><th>音标</th><th>释义</th><th>分类</th><th>例句</th><th>排序</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.emoji }} {{ item.word }}</td>
          <td>{{ item.phonetic }}</td>
          <td>{{ item.meaning }}</td>
          <td>{{ item.category }}</td>
          <td>{{ item.example }}</td>
          <td>{{ item.sortOrder }}</td>
          <td>
            <button class="link" @click="edit(item)">编辑</button>
            <button class="link danger" @click="remove(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <AdminModal
      :open="modalOpen"
      :title="editingId ? '编辑英语条目' : '新增英语条目'"
      @close="closeModal"
    >
      <form class="form-grid" @submit.prevent="save">
        <div class="field" :class="{ 'is-invalid': errors.category }">
          <label>分类<span class="req">*</span></label>
          <select v-model="form.category" @change="clearField('category'); clearField('word')">
            <option value="letter">字母 letter</option>
            <option value="word">单词 word</option>
          </select>
          <div class="hint">{{ errors.category }}</div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.word }">
          <label>{{ form.category === 'letter' ? '字母' : '单词' }}<span class="req">*</span></label>
          <input
            v-model="form.word"
            :maxlength="form.category === 'letter' ? 1 : 64"
            :placeholder="form.category === 'letter' ? 'A' : 'apple'"
            @input="clearField('word')"
          />
          <div class="hint">{{ errors.word }}</div>
        </div>
        <div class="field">
          <label>音标</label>
          <input v-model="form.phonetic" placeholder="/ˈæpl/" />
          <div class="hint"></div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.meaning }">
          <label>中文释义<span class="req">*</span></label>
          <input v-model="form.meaning" placeholder="释义" @input="clearField('meaning')" />
          <div class="hint">{{ errors.meaning }}</div>
        </div>
        <div class="field span-2">
          <label>例句</label>
          <input v-model="form.example" placeholder="例句（可选）" />
          <div class="hint"></div>
        </div>
        <div class="field">
          <label>Emoji</label>
          <input v-model="form.emoji" placeholder="🍎" />
          <div class="hint"></div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.sortOrder }">
          <label>排序<span class="req">*</span></label>
          <input v-model.number="form.sortOrder" type="number" min="0" step="1" @input="clearField('sortOrder')" />
          <div class="hint">{{ errors.sortOrder }}</div>
        </div>
        <button class="sr-only" type="submit">保存</button>
      </form>
      <template #footer>
        <span v-if="message" class="msg">{{ message }}</span>
        <button type="button" class="btn btn-ghost" @click="closeModal">取消</button>
        <button type="button" class="btn btn-sun" :disabled="saving" @click="save">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </template>
    </AdminModal>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
h2 { font-family: var(--font-display); margin: 0; }
.msg { color: #e85d5d; font-weight: 800; margin-right: auto; }
.link { background: none; color: var(--sky-deep); font-weight: 800; cursor: pointer; margin-right: 8px; }
.link.danger { color: #e85d5d; }
.actions > select {
  max-width: 160px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(61, 44, 41, 0.05);
}
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}
</style>
