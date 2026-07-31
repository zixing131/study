<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../../api/http'

const list = ref([])
const editingId = ref(null)
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

function reset() {
  editingId.value = null
  Object.assign(form, emptyForm())
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
}

async function save() {
  try {
    await api.adminSaveEnglish({ ...form }, editingId.value)
    message.value = '保存成功'
    reset()
    await load()
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
    <h2>{{ editingId ? '编辑英语条目' : '新增英语条目' }}</h2>
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
    <div class="ops">
      <button class="btn btn-sun" @click="save">保存</button>
      <button class="btn btn-ghost" @click="reset">清空</button>
      <select v-model="filter" @change="load" style="max-width:160px">
        <option value="">全部</option>
        <option value="letter">仅字母</option>
        <option value="word">仅单词</option>
      </select>
      <span v-if="message" class="msg">{{ message }}</span>
    </div>

    <h2>题库列表（{{ list.length }}）</h2>
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
  </div>
</template>

<style scoped>
h2 { font-family: var(--font-display); margin-bottom: 12px; }
.form { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 10px; }
.ops { display: flex; gap: 10px; align-items: center; margin: 12px 0 20px; flex-wrap: wrap; }
.msg { color: var(--mint-deep); font-weight: 800; }
.link { background: none; color: var(--sky-deep); font-weight: 800; cursor: pointer; margin-right: 8px; }
.link.danger { color: #e85d5d; }
</style>
