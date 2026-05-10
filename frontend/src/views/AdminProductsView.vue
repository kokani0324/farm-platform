<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NInput, NPopconfirm, useMessage,
} from 'naive-ui'
import { listAdminProducts, takeDownProduct, restoreProduct } from '@/api/admin'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 15 })
const keyword = ref('')

const statusMap = {
  ACTIVE: { type: 'success', label: '上架中' },
  INACTIVE: { type: 'default', label: '已下架' },
  SOLD_OUT: { type: 'warning', label: '已售完' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listAdminProducts({ page, size: 15, keyword: keyword.value || '' })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function takeDown(id) {
  acting.value = true
  try {
    await takeDownProduct(id)
    message.success('已下架')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

async function restore(id) {
  acting.value = true
  try {
    await restoreProduct(id)
    message.success('已重新上架')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">商品管理</h1>
    <p class="text-sm text-gray-500 mb-6">查看全平台所有商品,可下架違規商品或復原。</p>

    <div class="mb-4 flex gap-3">
      <n-input
        v-model:value="keyword"
        placeholder="搜尋商品名 / 小農名"
        clearable
        style="max-width: 320px"
        @keyup.enter="load(0)"
      />
      <n-button @click="load(0)">搜尋</n-button>
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="找不到商品" />

      <div v-else class="space-y-3">
        <div
          v-for="p in data.content"
          :key="p.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-20 h-20 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="p.imageUrl" :src="p.imageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <h3 class="font-bold text-farm-900 truncate">{{ p.name }}</h3>
              <n-tag :type="statusMap[p.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[p.status]?.label || p.status }}
              </n-tag>
              <n-tag size="small" :bordered="false" type="success">{{ p.categoryName }}</n-tag>
              <n-tag v-if="p.groupBuyEnabled" size="small" :bordered="false" type="warning">可團購</n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">#{{ p.id }} · 小農 {{ p.farmerName }}</div>
            <div class="text-sm text-gray-700">
              NT$ {{ Number(p.price).toLocaleString() }} / {{ p.unit }} · 庫存 {{ p.stock }}
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'product-detail', params: { id: p.id } })"
            >檢視</n-button>
            <n-popconfirm v-if="p.status !== 'INACTIVE'" @positive-click="takeDown(p.id)">
              <template #trigger>
                <n-button size="small" type="error" tertiary :loading="acting">下架</n-button>
              </template>
              強制下架此商品?
            </n-popconfirm>
            <n-button
              v-else
              size="small"
              type="primary"
              tertiary
              :loading="acting"
              @click="restore(p.id)"
            >重新上架</n-button>
          </div>
        </div>

        <div v-if="data.totalPages > 1" class="flex justify-center pt-4">
          <n-pagination
            :page="data.page + 1"
            :page-count="data.totalPages"
            @update:page="(p) => load(p - 1)"
          />
        </div>
      </div>
    </n-spin>
  </div>
</template>
