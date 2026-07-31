<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/http'
import AdminModal from '../../components/AdminModal.vue'
import { asInt, collectErrors, isBlank, required, nonNegInt } from '../../utils/validate'

const list = ref([])
const editingId = ref(null)
const modalOpen = ref(false)
const form = reactive(emptyForm())
const errors = reactive(emptyErrors())
const message = ref('')
const saving = ref(false)

function emptyForm() {
  return {
    title: '',
    author: '',
    dynasty: '唐',
    linesText: '',
    sortOrder: 0,
  }
}

function emptyErrors() {
  return {
    title: '',
    author: '',
    dynasty: '',
    linesText: '',
    sortOrder: '',
  }
}

function clearErrors() {
  Object.assign(errors, emptyErrors())
  message.value = ''
}

async function load() {
  list.value = await api.adminListPoems()
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
    title: item.title,
    author: item.author,
    dynasty: item.dynasty,
    linesText: (item.lines || []).join('\n'),
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

function parseLines() {
  return form.linesText.split(/\n+/).map((s) => s.trim()).filter(Boolean)
}

function validateLines(text) {
  if (isBlank(text)) return '诗句不能为空'
  if (!parseLines().length) return '请至少填写一行诗句'
  return null
}

function validate() {
  const result = collectErrors({
    title: required(form.title, '标题'),
    author: required(form.author, '作者'),
    linesText: validateLines(form.linesText),
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
    await api.adminSavePoem({
      title: form.title.trim(),
      author: form.author.trim(),
      dynasty: (form.dynasty || '').trim() || '唐',
      lines: parseLines(),
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
      <form class="form-grid poem-form" @submit.prevent="save">
        <div class="field" :class="{ 'is-invalid': errors.title }">
          <label>标题<span class="req">*</span></label>
          <input v-model="form.title" placeholder="标题" @input="clearField('title')" />
          <div class="hint">{{ errors.title }}</div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.author }">
          <label>作者<span class="req">*</span></label>
          <input v-model="form.author" placeholder="作者" @input="clearField('author')" />
          <div class="hint">{{ errors.author }}</div>
        </div>
        <div class="field">
          <label>朝代</label>
          <input v-model="form.dynasty" placeholder="如：唐" />
          <div class="hint"></div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.sortOrder }">
          <label>排序<span class="req">*</span></label>
          <input v-model.number="form.sortOrder" type="number" min="0" step="1" @input="clearField('sortOrder')" />
          <div class="hint">{{ errors.sortOrder }}</div>
        </div>
        <div class="field span-2" :class="{ 'is-invalid': errors.linesText }">
          <label>诗句<span class="req">*</span></label>
          <textarea
            v-model="form.linesText"
            rows="5"
            placeholder="每行一句"
            @input="clearField('linesText')"
          ></textarea>
          <div class="hint">{{ errors.linesText }}</div>
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
h2 { font-family: var(--font-display); margin: 0; }
.msg { color: #e85d5d; font-weight: 800; margin-right: auto; }
.link { background: none; color: var(--sky-deep); font-weight: 800; cursor: pointer; margin-right: 8px; }
.link.danger { color: #e85d5d; }
.poem-form { grid-template-columns: repeat(2, minmax(0, 1fr)); }
textarea { resize: vertical; }
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
