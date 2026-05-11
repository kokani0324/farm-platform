<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NSelect, useMessage,
} from 'naive-ui'
import { listFarmerFarmTripBookings } from '@/api/farmTrips'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const filterStatus = ref(null)
const statusOptions = [
  { label: '全部', value: null },
  { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '已確認', value: 'CONFIRMED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已完成', value: 'COMPLETED' },
]
const statusMap = {
  PENDING_PAYMENT: { type: 'warning', label: '待付款' },
  CONFIRMED: { type: 'success', label: '已確認' },
  CANCELLED: { type: 'default', label: '已取消' },
  COMPLETED: { type: 'info', label: '已完成' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const params = { page, size: 10 }
    if (filterStatus.value) params.status = filterStatus.value
    const { data: res } = await listFarmerFarmTripBookings(params)
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
    <div class="flex items-center justify-between mb-2 flex-wrap gap-3">
      <h1 class="text-2xl font-bold text-farm-800">體驗預約管理</h1>
      <n-select
        v-model:value="filterStatus"
        :options="statusOptions"
        style="width: 160px"
        @update:value="load(0)"
      />
    </div>
    <p class="text-sm text-gray-500 mb-6">查看你開設的活動有哪些消費者報名了。</p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有預約" />

      <div v-else class="space-y-3">
        <div
          v-for="b in data.content"
          :key="b.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="b.farmTripImageUrl" :src="b.farmTripImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-bold text-farm-900 truncate">{{ b.farmTripTitle }}</h3>
              <n-tag :type="statusMap[b.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[b.status]?.label || b.status }}
              </n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">
              訂單 {{ b.bookingNo }} · 預約於 {{ fmt(b.bookedAt) }}
            </div>
            <div class="text-sm text-gray-700 mb-1">
              {{ b.numPeople }} 人 · 小計 <strong class="text-farm">NT$ {{ Number(b.totalAmount).toLocaleString() }}</strong>
            </div>
            <div class="text-xs text-gray-500">
              聯絡人 {{ b.contactName }} / {{ b.contactPhone }} · 活動 {{ fmt(b.farmTripStart) }}
            </div>
            <div v-if="b.note" class="text-xs text-gray-500 mt-1">備註:{{ b.note }}</div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'farm-trip-detail', params: { id: b.farmTripId } })"
            >活動詳情</n-button>
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
