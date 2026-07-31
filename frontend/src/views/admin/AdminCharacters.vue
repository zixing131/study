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
const saving = ref(false)

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

function emptyErrors() {
  return {
    charText: '',
    pinyin: '',
    strokeOrder: '',
    words: '',
    sentence: '',
    sortOrder: '',
  }
}

function clearErrors() {
  Object.assign(errors, emptyErrors())
  message.value = ''
}

async function load() {
  list.value = await api.adminListCharacters()
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
    charText: item.charText,
    pinyin: item.pinyin,
    strokeOrder: item.strokeOrder,
    words: item.words,
    sentence: item.sentence,
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

function validateCharText(v) {
  const base = required(v, '汉字')
  if (base) return base
  const t = String(v).trim()
  if ([...t].length !== 1) return '请填写单个汉字'
  return null
}

function validate() {
  const result = collectErrors({
    charText: validateCharText(form.charText),
    pinyin: required(form.pinyin, '拼音'),
    strokeOrder: required(form.strokeOrder, '笔顺'),
    words: required(form.words, '组词'),
    sentence: required(form.sentence, '例句'),
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
    await api.adminSaveCharacter({
      charText: form.charText.trim(),
      pinyin: form.pinyin.trim(),
      strokeOrder: form.strokeOrder.trim(),
      words: form.words.trim(),
      sentence: form.sentence.trim(),
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
      <form class="form-grid" @submit.prevent="save">
        <div class="field" :class="{ 'is-invalid': errors.charText }">
          <label>汉字<span class="req">*</span></label>
          <input v-model="form.charText" maxlength="4" placeholder="如：一" @input="clearField('charText')" />
          <div class="hint">{{ errors.charText }}</div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.pinyin }">
          <label>拼音<span class="req">*</span></label>
          <input v-model="form.pinyin" placeholder="如：yī" @input="clearField('pinyin')" />
          <div class="hint">{{ errors.pinyin }}</div>
        </div>
        <div class="field span-2" :class="{ 'is-invalid': errors.strokeOrder }">
          <label>笔顺<span class="req">*</span></label>
          <input v-model="form.strokeOrder" placeholder="逗号分隔，如：横" @input="clearField('strokeOrder')" />
          <div class="hint">{{ errors.strokeOrder }}</div>
        </div>
        <div class="field span-2" :class="{ 'is-invalid': errors.words }">
          <label>组词<span class="req">*</span></label>
          <input v-model="form.words" placeholder="逗号分隔" @input="clearField('words')" />
          <div class="hint">{{ errors.words }}</div>
        </div>
        <div class="field span-2" :class="{ 'is-invalid': errors.sentence }">
          <label>例句<span class="req">*</span></label>
          <input v-model="form.sentence" placeholder="例句" @input="clearField('sentence')" />
          <div class="hint">{{ errors.sentence }}</div>
        </div>
        <div class="field" :class="{ 'is-invalid': errors.sortOrder }">
          <label>排序<span class="req">*</span></label>
          <input v-model.number="form.sortOrder" type="number" min="0" step="1" placeholder="0" @input="clearField('sortOrder')" />
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
h2 {
  font-family: var(--font-display);
  margin: 0;
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
