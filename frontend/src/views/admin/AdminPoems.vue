<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/http'
import AdminModal from '../../components/AdminModal.vue'

const list = ref([])
const editingId = ref(null)
const modalOpen = ref(false)
const form = reactive(emptyForm())
const message = ref('')

function emptyForm() {
  return {
    title: '',
    author: '',
    dynasty: '唐',
    linesText: '',
    sortOrder: 0,
  }
}

async function load() {
  list.value = await api.adminListPoems()
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
    title: item.title,
    author: item.author,
    dynasty: item.dynasty,
    linesText: (item.lines || []).join('\n'),
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
  const lines = form.linesText.split(/\n+/).map((s) => s.trim()).filter(Boolean)
  try {
    await api.adminSavePoem({
      title: form.title,
      author: form.author,
      dynasty: form.dynasty,
      lines,
      sortOrder: form.sortOrder,
    }, editingId.value)
    message.value = '保存成功'
    await load()
    closeModal()
  } catch (e) {
    message.value = e.message
  }
}

async function remove(id) {
  if (!confirm('确定删除？')) return
  await api.adminDeletePoem(id)
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>题库列表（{{ list.length }}）</h2>
      <button class="btn btn-sun" @click="openCreate">新增古诗</button>
    </div>

    <table>
      <thead>
        <tr><th>标题</th><th>作者</th><th>诗句</th><th>排序</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td>{{ item.title }}</td>
          <td>{{ item.dynasty }} · {{ item.author }}</td>
          <td>{{ (item.lines || []).join(' / ') }}</td>
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
      :title="editingId ? '编辑古诗' : '新增古诗'"
      @close="closeModal"
    >
      <div class="form">
        <input v-model="form.title" placeholder="标题" />
        <input v-model="form.author" placeholder="作者" />
        <input v-model="form.dynasty" placeholder="朝代" />
        <input v-model.number="form.sortOrder" type="number" placeholder="排序" />
        <textarea v-model="form.linesText" rows="5" placeholder="诗句，每行一句"></textarea>
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
h2 { font-family: var(--font-display); margin: 0; }
.form { display: grid; gap: 10px; grid-template-columns: repeat(2, 1fr); }
textarea { grid-column: 1 / -1; resize: vertical; }
.msg { color: #e85d5d; font-weight: 800; margin-right: auto; }
.link { background: none; color: var(--sky-deep); font-weight: 800; cursor: pointer; margin-right: 8px; }
.link.danger { color: #e85d5d; }
</style>
