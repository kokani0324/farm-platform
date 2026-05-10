<script setup>
import { ref, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton, NDataTable, NTag, NPopconfirm, NIcon, useMessage, useDialog, NSpace,
} from 'naive-ui'
import { AddOutline, CreateOutline, TrashOutline, ToggleOutline } from '@vicons/ionicons5'
import {
  listMyProducts, deleteProduct, toggleProductStatus,
} from '@/api/products'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const products = ref([])
const loading = ref(false)

async function fetch() {
  loading.value = true
  try {
    const { data } = await listMyProducts()
    products.value = data
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

function statusTag(status) {
  const map = {
    ACTIVE:   { type: 'success', label: '上架中' },
    INACTIVE: { type: 'default', label: '已下架' },
    SOLD_OUT: { type: 'warning', label: '已售完' },
  }
  return map[status] || { type: 'default', label: status }
}

const columns = [
  {
    title: '圖片', key: 'imageUrl', width: 80,
    render: (row) => row.imageUrl
      ? h('img', { src: row.imageUrl, class: 'w-14 h-14 object-cover rounded' })
      : h('div', { class: 'w-14 h-14 bg-farm-50 rounded' }),
  },
  { title: '商品名稱', key: 'name', minWidth: 180 },
  { title: '分類', key: 'categoryName', width: 100 },
  {
    title: '單價', key: 'price', width: 110,
    render: (row) => `NT$ ${Number(row.price).toLocaleString()} / ${row.unit}`,
  },
  {
    title: '庫存', key: 'stock', width: 80,
    render: (row) => h('span', { class: row.stock === 0 ? 'text-red-500 font-bold' : '' }, row.stock),
  },
  {
    title: '團購', key: 'groupBuyEnabled', width: 70,
    render: (row) => row.groupBuyEnabled ? h(NTag, { size: 'small', type: 'warning', round: true, bordered: false }, () => '可團購') : '—',
  },
  {
    title: '狀態', key: 'status', width: 90,
    render: (row) => {
      const s = statusTag(row.status)
      return h(NTag, { size: 'small', type: s.type, round: true, bordered: false }, () => s.label)
    },
  },
  {
    title: '操作', key: 'actions', width: 220, fixed: 'right',
    render: (row) => h(NSpace, { size: 'small' }, () => [
      h(NButton, {
        size: 'small', secondary: true,
        onClick: () => router.push({ name: 'farmer-product-edit', params: { id: row.id } }),
      }, { default: () => '編輯', icon: () => h(NIcon, { component: CreateOutline }) }),
      h(NButton, {
        size: 'small', secondary: true,
        disabled: row.status === 'SOLD_OUT',
        onClick: () => onToggle(row),
      }, { default: () => row.status === 'ACTIVE' ? '下架' : '上架', icon: () => h(NIcon, { component: ToggleOutline }) }),
      h(NPopconfirm, {
        onPositiveClick: () => onDelete(row),
      }, {
        trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, {
          default: () => '刪除', icon: () => h(NIcon, { component: TrashOutline }),
        }),
        default: () => `確定刪除「${row.name}」？`,
      }),
    ]),
  },
]

async function onToggle(row) {
  try {
    await toggleProductStatus(row.id)
    message.success('狀態已切換')
    fetch()
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  }
}

async function onDelete(row) {
  try {
    await deleteProduct(row.id)
    message.success('已刪除')
    fetch()
  } catch (e) {
    message.error(e.response?.data?.message || '刪除失敗')
  }
}

onMounted(fetch)
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-farm-800">我的商品</h1>
      <n-button type="primary" @click="router.push({ name: 'farmer-product-new' })">
        <template #icon><n-icon :component="AddOutline" /></template>
        新增商品
      </n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="products"
      :loading="loading"
      :bordered="false"
      :scroll-x="900"
      striped
    />
  </div>
</template>
