<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NTabs, NTabPane, NEmpty, NSpin, NTag, NButton, NPagination, NIcon, useMessage,
} from 'naive-ui'
import { ReceiptOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { listMyOrders, listFarmerOrders } from '@/api/orders'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const props = defineProps({
  // 'consumer' | 'farmer'
  scope: { type: String, default: 'consumer' },
})

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

async function load(page = 0) {
  loading.value = true
  try {
    const api = props.scope === 'farmer' ? listFarmerOrders : listMyOrders
    const { data: res } = await api({ page, size: 10, sort: 'createdAt,desc' })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

const statusMap = {
  PENDING_PAYMENT: { type: 'warning', label: '待付款' },
  PAID: { type: 'info', label: '已付款' },
  SHIPPED: { type: 'success', label: '已出貨' },
  COMPLETED: { type: 'success', label: '已完成' },
  CANCELLED: { type: 'default', label: '已取消' },
}

const paymentLabels = {
  CREDIT_CARD_SIM: '信用卡',
  BANK_TRANSFER_SIM: 'ATM 轉帳',
  CASH_ON_DELIVERY: '貨到付款',
}

function formatDate(s) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}

const title = computed(() => props.scope === 'farmer' ? '小農訂單' : '我的訂單')

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <div class="flex items-center gap-2 mb-6">
      <n-icon :component="ReceiptOutline" size="28" color="#4a7c2a" />
      <h1 class="text-2xl font-bold text-farm-800">{{ title }}</h1>
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有訂單" />

      <div v-else class="space-y-3">
        <div
          v-for="o in data.content"
          :key="o.id"
          class="bg-white border border-farm-100 rounded-lg p-5 hover:border-farm transition cursor-pointer"
          @click="router.push({ name: scope === 'farmer' ? 'farmer-order-detail' : 'order-detail', params: { id: o.id } })"
        >
          <div class="flex items-start justify-between mb-3 flex-wrap gap-2">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-mono text-sm text-gray-500">{{ o.orderNo }}</span>
                <n-tag size="small" :type="statusMap[o.status]?.type" :bordered="false">
                  {{ statusMap[o.status]?.label || o.status }}
                </n-tag>
              </div>
              <div class="text-xs text-gray-400">{{ formatDate(o.createdAt) }}</div>
            </div>
            <div class="text-right">
              <div class="text-xs text-gray-500">
                {{ scope === 'farmer' ? '消費者' : '小農' }}：{{ scope === 'farmer' ? o.consumerName : o.farmerName }}
              </div>
              <div class="text-xs text-gray-500">{{ paymentLabels[o.paymentMethod] }}</div>
            </div>
          </div>

          <div class="flex items-center gap-3 mb-3 overflow-x-auto">
            <div
              v-for="it in o.items"
              :key="it.productId || it.productName"
              class="flex-shrink-0 w-14 h-14 bg-farm-50 rounded overflow-hidden"
              :title="`${it.productName} x ${it.quantity}`"
            >
              <img v-if="it.imageUrl" :src="it.imageUrl" class="w-full h-full object-cover" />
            </div>
            <div class="text-sm text-gray-600 ml-2 flex-shrink-0">共 {{ o.items.length }} 項商品</div>
          </div>

          <div class="flex items-center justify-between">
            <div class="text-lg">
              合計 <span class="text-farm font-bold text-xl">NT$ {{ Number(o.totalAmount).toLocaleString() }}</span>
            </div>
            <n-button text type="primary">
              查看詳情
              <template #icon><n-icon :component="ChevronForwardOutline" /></template>
            </n-button>
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
