<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NPopconfirm, useMessage,
} from 'naive-ui'
import { listMyGroupBuyRequests, withdrawGroupBuyRequest } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  PENDING: { type: 'warning', label: '待審核' },
  APPROVED: { type: 'success', label: '已通過' },
  REJECTED: { type: 'error', label: '已拒絕' },
  WITHDRAWN: { type: 'default', label: '已撤回' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listMyGroupBuyRequests({ page, size: 10 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function withdraw(id) {
  acting.value = true
  try {
    await withdrawGroupBuyRequest(id)
    message.success('已撤回')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '撤回失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我發起的團購</h1>
    <p class="text-sm text-gray-500 mb-6">
      所有由你發起的團購申請,通過後即會自動建立公開團購活動,並由你擔任團主。
    </p>

    <n-spin :show="loading">
      <n-empty
        v-if="!loading && data.content.length === 0"
        description="尚未發起過團購"
      >
        <template #extra>
          <n-button @click="router.push({ name: 'products' })">逛商品發起團購</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="r in data.content"
          :key="r.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="r.productImageUrl" :src="r.productImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-bold text-farm-900 truncate">{{ r.productName }}</h3>
              <n-tag :type="statusMap[r.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[r.status]?.label || r.status }}
              </n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">小農 {{ r.farmerName }} · 申請 #{{ r.id }}</div>
            <div class="text-sm text-gray-700 mb-1">
              目標 <strong>{{ r.targetQuantity }}</strong> {{ r.productUnit }} ·
              團購價 <strong class="text-farm">NT$ {{ Number(r.groupPrice).toLocaleString() }}</strong>
              <span class="text-gray-400 line-through ml-1">NT$ {{ Number(r.productPrice).toLocaleString() }}</span>
            </div>
            <div class="text-xs text-gray-500">
              開團 {{ fmt(r.openDate) }} · 截止 {{ fmt(r.deadlineDate) }}
            </div>
            <div v-if="r.message" class="text-xs text-gray-500 mt-1">留言:{{ r.message }}</div>
            <div v-if="r.status === 'REJECTED' && r.rejectReason" class="text-xs text-red-500 mt-1">
              拒絕原因:{{ r.rejectReason }}
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              v-if="r.status === 'APPROVED' && r.groupBuyId"
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'group-buy-detail', params: { id: r.groupBuyId } })"
            >查看團購</n-button>
            <n-button
              v-else-if="r.status === 'APPROVED'"
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'group-buys' })"
            >前往團購頁</n-button>
            <n-popconfirm v-if="r.status === 'PENDING'" @positive-click="withdraw(r.id)">
              <template #trigger>
                <n-button size="small" type="error" tertiary :loading="acting">撤回</n-button>
              </template>
              確定撤回此申請?
            </n-popconfirm>
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
