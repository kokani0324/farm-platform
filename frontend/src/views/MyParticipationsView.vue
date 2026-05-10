<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, useMessage,
} from 'naive-ui'
import { listMyParticipations } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  JOINED: { type: 'success', label: '參與中' },
  WITHDRAWN: { type: 'default', label: '已退出' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listMyParticipations({ page, size: 10 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我參加的團購</h1>
    <p class="text-sm text-gray-500 mb-6">
      所有你參加過的團購記錄;成團後會自動建立訂單,可在「我的訂單」追蹤出貨狀態。
    </p>

    <n-spin :show="loading">
      <n-empty
        v-if="!loading && data.content.length === 0"
        description="尚未參加過團購"
      >
        <template #extra>
          <n-button @click="router.push({ name: 'group-buys' })">看看正在進行的團購</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="p in data.content"
          :key="p.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="p.productImageUrl" :src="p.productImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-bold text-farm-900 truncate">{{ p.productName }}</h3>
              <n-tag :type="statusMap[p.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[p.status]?.label || p.status }}
              </n-tag>
              <n-tag v-if="p.isHost" type="warning" size="small" :bordered="false" round>團主</n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">
              小農 {{ p.farmerName }} · 加入於 {{ fmt(p.joinedAt) }} · 團購 #{{ p.groupBuyId }}
            </div>
            <div class="text-sm text-gray-700">
              購買 <strong>{{ p.quantity }}</strong> {{ p.productUnit }} ·
              小計 <strong class="text-farm">NT$ {{ Number(p.subtotal).toLocaleString() }}</strong>
            </div>
            <div class="text-xs text-gray-500 mt-1">
              收件 {{ p.recipientName }} / {{ p.recipientPhone }} · {{ p.shippingAddress }}
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'group-buy-detail', params: { id: p.groupBuyId } })"
            >查看團購</n-button>
            <n-button
              v-if="p.orderId"
              size="small"
              tertiary
              @click="router.push({ name: 'order-detail', params: { id: p.orderId } })"
            >查看訂單</n-button>
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
