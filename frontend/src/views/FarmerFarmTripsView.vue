<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NPopconfirm, NIcon, useMessage,
} from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import { listFarmerFarmTrips, cancelFarmTrip } from '@/api/farmTrips'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  ACTIVE: { type: 'warning', label: '招募中' },
  FULL: { type: 'error', label: '已額滿' },
  CLOSED: { type: 'default', label: '報名截止' },
  CANCELLED: { type: 'default', label: '已取消' },
  COMPLETED: { type: 'success', label: '已舉辦' },
}
const typeLabel = (t) => t === 'FARM_EXPERIENCE' ? '農場體驗營' : t === 'FIELD_VISIT' ? '產地參訪' : t

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listFarmerFarmTrips({ page, size: 10 })
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
    await cancelFarmTrip(id)
    message.success('已取消活動,所有預約一併取消')
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
    <div class="flex items-center justify-between mb-2 flex-wrap gap-3">
      <h1 class="text-2xl font-bold text-farm-800">我的體驗活動</h1>
      <n-button type="primary" @click="router.push({ name: 'farmer-farm-trip-new' })">
        <template #icon><n-icon :component="AddOutline" /></template>
        新增活動
      </n-button>
    </div>
    <p class="text-sm text-gray-500 mb-6">管理你開設的農場體驗活動,可以新增、編輯或取消。</p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="還沒開設過體驗活動">
        <template #extra>
          <n-button type="primary" @click="router.push({ name: 'farmer-farm-trip-new' })">新增第一個活動</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="t in data.content"
          :key="t.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="t.imageUrl" :src="t.imageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <h3 class="font-bold text-farm-900 truncate">{{ t.title }}</h3>
              <n-tag :type="statusMap[t.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[t.status]?.label || t.status }}
              </n-tag>
              <n-tag size="small" :bordered="false" round>{{ typeLabel(t.tripType) }}</n-tag>
              <n-tag size="small" :bordered="false" type="success" round>{{ t.categoryName }}</n-tag>
            </div>
            <div class="text-sm text-gray-700 mb-1">
              NT$ {{ Number(t.price).toLocaleString() }} / 人 ·
              名額 {{ t.currentBookings }} / {{ t.capacity }}
              <span v-if="t.location"> · {{ t.location }}</span>
            </div>
            <div class="text-xs text-gray-500">
              活動 {{ fmt(t.tripStart) }} · 報名截止 {{ fmt(t.bookEnd) }}
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              :disabled="t.status === 'CANCELLED' || t.status === 'COMPLETED'"
              @click="router.push({ name: 'farmer-farm-trip-edit', params: { id: t.id } })"
            >編輯</n-button>
            <n-popconfirm
              v-if="t.status !== 'CANCELLED' && t.status !== 'COMPLETED'"
              @positive-click="cancel(t.id)"
            >
              <template #trigger>
                <n-button size="small" type="error" tertiary :loading="acting">取消</n-button>
              </template>
              取消活動會連同所有已確認的預約一併取消,確定?
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
