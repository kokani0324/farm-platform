<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NProgress, useMessage,
} from 'naive-ui'
import { listFarmerGroupBuys } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  OPEN: { type: 'warning', label: '招募中' },
  SUCCESS: { type: 'success', label: '已成團' },
  FAILED: { type: 'default', label: '未達標' },
  CANCELLED: { type: 'default', label: '已取消' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listFarmerGroupBuys({ page, size: 10 })
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
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我的團購活動</h1>
    <p class="text-sm text-gray-500 mb-6">
      所有由你通過的團購申請所建立的活動,可即時追蹤達標進度與狀態。
    </p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有進行中的團購">
        <template #extra>
          <n-button @click="router.push({ name: 'farmer-group-buy-requests' })">查看待審核申請</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="gb in data.content"
          :key="gb.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="gb.productImageUrl" :src="gb.productImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-bold text-farm-900 truncate">{{ gb.productName }}</h3>
              <n-tag :type="statusMap[gb.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[gb.status]?.label || gb.status }}
              </n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-2">
              團購 #{{ gb.id }} · 團主 {{ gb.hostName }}
            </div>
            <div class="text-sm text-gray-700 mb-2">
              團購價 <strong class="text-farm">NT$ {{ Number(gb.groupPrice).toLocaleString() }}</strong>
              <span class="text-gray-400 line-through ml-1">NT$ {{ Number(gb.productPrice).toLocaleString() }}</span>
              · 每單位省 NT$ {{ Number(gb.saving).toLocaleString() }}
            </div>
            <div class="mb-1">
              <n-progress
                type="line"
                :percentage="gb.percent"
                :height="10"
                :show-indicator="false"
                color="#c89b3c"
                rail-color="#f0e8d0"
              />
              <div class="flex justify-between text-xs text-gray-600 mt-1">
                <span>{{ gb.currentQuantity }} / {{ gb.targetQuantity }} {{ gb.productUnit }}</span>
                <span>{{ gb.percent }}%</span>
              </div>
            </div>
            <div class="text-xs text-gray-500">
              開團 {{ fmt(gb.openDate) }} · 截止 {{ fmt(gb.deadlineDate) }}
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'group-buy-detail', params: { id: gb.id } })"
            >查看詳情</n-button>
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
