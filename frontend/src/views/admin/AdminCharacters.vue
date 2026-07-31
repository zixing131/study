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
    charText: '',
    pinyin: '',
    strokeOrder: '',
    words: '',
    sentence: '',
    sortOrder: 0,
  }
}

async function load() {
  list.value = await api.adminListCharacters()
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
    charText: item.charText,
    pinyin: item.pinyin,
    strokeOrder: item.strokeOrder,
    words: item.words,
    sentence: item.sentence,
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
    await api.adminSaveCharacter({ ...form }, editingId.value)
    message.value = '保存成功'
    await load()
    closeModal()
  } catch (e) {
    message.value = e.message
  }
}

async function remove(id) {
  if (!confirm('确定删除？')) return
  await api.adminDeleteCharacter(id)
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>题库列表（{{ list.length }}）</h2>
      <button class="btn btn-sun" @click="openCreate">新增汉字</button>
    </div>

    <table>
      <thead>
        <tr>
          <th>字</th><th>拼音</th><th>笔顺</th><th>组词</th><th>句子</th><th>排序</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list" :key="item.id">
          <td class="big">{{ item.charText }}</td>
          <td>{{ item.pinyin }}</td>
          <td>{{ item.strokeOrder }}</td>
          <td>{{ item.words }}</td>
          <td>{{ item.sentence }}</td>
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
      :title="editingId ? '编辑汉字' : '新增汉字'"
      @close="closeModal"
    >
      <div class="form">
        <input v-model="form.charText" placeholder="汉字" />
        <input v-model="form.pinyin" placeholder="拼音，如 yī" />
        <input v-model="form.strokeOrder" placeholder="笔顺，逗号分隔" />
        <input v-model="form.words" placeholder="组词，逗号分隔" />
        <input v-model="form.sentence" placeholder="例句" />
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
h2 {
  font-family: var(--font-display);
  margin: 0;
}
.form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}
.msg { color: #e85d5d; font-weight: 800; margin-right: auto; }
.big { font-size: 1.4rem; font-weight: 900; }
.link {
  background: none;
  color: var(--sky-deep);
  font-weight: 800;
  cursor: pointer;
  margin-right: 8px;
}
.link.danger { color: #e85d5d; }
</style>
