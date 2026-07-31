<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/http'
import AdminModal from '../../components/AdminModal.vue'

const list = ref([])
const editingId = ref(null)
const modalOpen = ref(false)
const form = reactive(emptyForm())
const message = ref('')
const filter = ref('')

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

async function load() {
  list.value = await api.adminListEnglish(filter.value || undefined)
}

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  message.value = ''
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
  message.value = ''
  modalOpen.value = true
}

function closeModal() {
  modalOpen.value = false
  editingId.value = null
  Object.assign(form, emptyForm())
  message.value = ''
}

async function save() {
  try {
    await api.adminSaveEnglish({ ...form }, editingId.value)
    message.value = '保存成功'
    await load()
    closeModal()
  } catch (e) {
    message.value = e.message
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
      <div class="form">
        <input v-model="form.word" placeholder="单词/字母" />
        <input v-model="form.phonetic" placeholder="音标" />
        <input v-model="form.meaning" placeholder="中文释义" />
        <select v-model="form.category">
          <option value="letter">字母 letter</option>
          <option value="word">单词 word</option>
        </select>
        <input v-model="form.example" placeholder="例句" />
        <input v-model="form.emoji" placeholder="emoji" />
        <input v-model.number="form.sortOrder" type="number" placeholder="排序" />
      </div>
      <template #footer>
        <span v-if="message" class="msg">{{ message }}</span>
        <button class="btn btn-ghost" @click="closeModal">取消</button>
        <button class="btn btn-sun" @click="save">保存</button>
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
.form { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 10px; }
.msg { color: #e85d5d; font-weight: 800; margin-right: auto; }
.link { background: none; color: var(--sky-deep); font-weight: 800; cursor: pointer; margin-right: 8px; }
.link.danger { color: #e85d5d; }
select {
  max-width: 160px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(61, 44, 41, 0.05);
}
</style>
