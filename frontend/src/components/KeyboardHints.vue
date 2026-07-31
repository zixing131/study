<script setup>
defineProps({
  items: {
    type: Array,
    default: () => [],
  },
})
</script>

<template>
  <div v-if="items.length" class="kb-hints" aria-label="键盘快捷键提示">
    <span v-for="item in items" :key="item.keys + item.label" class="kb-item">
      <kbd v-for="k in item.keys.split('+')" :key="k">{{ k }}</kbd>
      <span class="kb-label">{{ item.label }}</span>
    </span>
  </div>
</template>

<style scoped>
.kb-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  justify-content: center;
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px dashed rgba(61, 44, 41, 0.12);
}

.kb-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.78rem;
  font-weight: 800;
  color: var(--ink-soft);
}

kbd {
  display: inline-grid;
  place-items: center;
  min-width: 1.5em;
  padding: 2px 7px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e7d8cc;
  box-shadow: 0 2px 0 #ecdccc;
  color: var(--ink);
  font-family: var(--font-body);
  font-size: 0.72rem;
  font-weight: 900;
  line-height: 1.3;
}

.kb-label {
  white-space: nowrap;
}
</style>
