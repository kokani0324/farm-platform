<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NPopconfirm, useMessage,
} from 'naive-ui'
import { listMyFarmTripBookings, cancelFarmTripBooking } from '@/api/farmTrips'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  PENDING_PAYMENT: { type: 'warning', label: '待付款' },
  CONFIRMED: { type: 'success', label: '已確認' },
  CANCELLED: { type: 'default', label: '已取消' },
  COMPLETED: { type: 'info', label: '已完成' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listMyFarmTripBookings({ page, size: 10 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function cancel(id) {
  acting.value = true
  try {
    await cancelFarmTripBooking(id)
    message.success('已取消')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '取消失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我的體驗預約</h1>
    <p class="text-sm text-gray-500 mb-6">所有你預約過的農場體驗活動,活動開始前可取消。</p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="尚未預約過體驗活動">
        <template #extra>
          <n-button @click="router.push({ name: 'farm-trips' })">看看有什麼體驗活動</n-button>
        </template>
      </n-empty>

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
              訂單 {{ b.bookingNo }} · 主辦 {{ b.farmerName }}
            </div>
            <div class="text-sm text-gray-700 mb-1">
              {{ b.numPeople }} 人 · 小計 <strong class="text-farm">NT$ {{ Number(b.totalAmount).toLocaleString() }}</strong>
            </div>
            <div class="text-xs text-gray-500">
              活動 {{ fmt(b.farmTripStart) }} · {{ b.farmTripLocation || '地點未填' }}
            </div>
            <div v-if="b.note" class="text-xs text-gray-500 mt-1">備註:{{ b.note }}</div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'farm-trip-detail', params: { id: b.farmTripId } })"
            >查看活動</n-button>
            <n-popconfirm
              v-if="b.status === 'CONFIRMED' || b.status === 'PENDING_PAYMENT'"
              @positive-click="cancel(b.id)"
            >
              <template #trigger>
                <n-button size="small" type="error" tertiary :loading="acting">取消預約</n-button>
              </template>
              確定取消預約?
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
