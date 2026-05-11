<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NModal,
  NDescriptions, NDescriptionsItem, useMessage,
} from 'naive-ui'
import { listMyHostedOrders, getGroupBuyOrder } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const showDetailModal = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

const statusMap = {
  PAID: { type: 'info', label: '已付款待出貨' },
  SHIPPING: { type: 'info', label: '出貨中' },
  COMPLETED: { type: 'success', label: '已完成' },
  CANCELLED: { type: 'default', label: '已取消' },
  PENDING_PAYMENT: { type: 'warning', label: '待付款' }, // 保留容錯
}

const receiptMap = {
  NOT_SHIPPED: { type: 'default', label: '尚未出貨' },
  SHIPPED: { type: 'info', label: '已出貨' },
  RECEIVED: { type: 'success', label: '已收貨' },
}

const payMethodLabel = (m) => ({
  CREDIT_CARD_SIM: '信用卡',
  BANK_TRANSFER_SIM: 'ATM 轉帳',
  CASH_ON_DELIVERY: '貨到付款',
}[m] || m || '—')

const paidCount = computed(() =>
  detail.value?.participations?.filter(p => p.paymentStatus === 'PAID').length || 0
)
const refundedCount = computed(() =>
  detail.value?.participations?.filter(p => p.paymentStatus === 'REFUNDED').length || 0
)

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listMyHostedOrders({ page, size: 10 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function openDetail(o) {
  detail.value = null
  showDetailModal.value = true
  detailLoading.value = true
  try {
    const { data: res } = await getGroupBuyOrder(o.groupBuyId)
    detail.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '取得明細失敗')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

const detailParticipations = computed(() => detail.value?.participations || [])

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我發起的團購整單</h1>
    <p class="text-sm text-gray-500 mb-6">
      你發起並成團的團購會在這裡列為一張「整單」。團員加入時各自付款,你可在這裡看誰已付款、誰已收貨。
    </p>

    <n-spin :show="loading">
      <n-empty
        v-if="!loading && data.content.length === 0"
        description="尚無已成團的整單(等截止後才會結算)"
      >
        <template #extra>
          <n-button @click="router.push({ name: 'group-buys' })">看看其它團購</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="o in data.content"
          :key="o.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="o.productImageUrl" :src="o.productImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <h3 class="font-bold text-farm-900 truncate">{{ o.productName }}</h3>
              <n-tag :type="statusMap[o.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[o.status]?.label || o.status }}
              </n-tag>
              <span class="text-xs text-gray-500">{{ o.orderNo }}</span>
            </div>
            <div class="text-xs text-gray-500 mb-1">
              小農 {{ o.farmerName }} · 成團於 {{ fmt(o.createdAt) }} · 團購 #{{ o.groupBuyId }}
            </div>
            <div class="text-sm text-gray-700">
              全團共 <strong>{{ o.totalQuantity }}</strong> {{ o.productUnit }} ·
              總額 <strong class="text-farm">NT$ {{ Number(o.totalAmount).toLocaleString() }}</strong>
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button size="small" type="primary" @click="openDetail(o)">明細</n-button>
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

    <!-- 明細 modal -->
    <n-modal v-model:show="showDetailModal" preset="card" style="width: 760px">
      <template #header>
        <span class="text-farm-800">整單明細 {{ detail?.orderNo }}</span>
      </template>
      <n-spin :show="detailLoading">
        <template v-if="detail">
          <n-descriptions :column="2" label-placement="left" bordered size="small" class="mb-4">
            <n-descriptions-item label="商品">{{ detail.productName }}</n-descriptions-item>
            <n-descriptions-item label="小農">{{ detail.farmerName }}</n-descriptions-item>
            <n-descriptions-item label="團購單價">NT$ {{ Number(detail.groupPrice).toLocaleString() }}</n-descriptions-item>
            <n-descriptions-item label="總數量">{{ detail.totalQuantity }} {{ detail.productUnit }}</n-descriptions-item>
            <n-descriptions-item label="總金額">NT$ {{ Number(detail.totalAmount).toLocaleString() }}</n-descriptions-item>
            <n-descriptions-item label="狀態">
              <n-tag :type="statusMap[detail.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[detail.status]?.label || detail.status }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="已付款 / 退款">
              {{ paidCount }} 筆已付款,{{ refundedCount }} 筆退款
            </n-descriptions-item>
          </n-descriptions>

          <h4 class="font-semibold text-farm-800 mb-2">參與成員（{{ detailParticipations.length }} 人）</h4>
          <div class="space-y-2 max-h-80 overflow-y-auto">
            <div
              v-for="p in detailParticipations"
              :key="p.id"
              class="flex items-center gap-3 border border-farm-100 rounded p-2 text-sm"
            >
              <div class="flex-1 min-w-0">
                <div class="font-medium">
                  {{ p.userName }}
                  <n-tag v-if="p.isHost" type="warning" size="tiny" :bordered="false" round>團主</n-tag>
                </div>
                <div class="text-xs text-gray-500 truncate">{{ p.recipientName }} / {{ p.recipientPhone }} · {{ p.fullAddress }}</div>
                <div class="text-xs text-gray-400">付款方式 {{ payMethodLabel(p.paymentMethod) }}</div>
              </div>
              <div class="text-right">
                <div>{{ p.quantity }} {{ detail.productUnit }} · NT$ {{ Number(p.subtotal).toLocaleString() }}</div>
                <div class="flex flex-col gap-1 mt-1 items-end">
                  <n-tag :type="p.paymentStatus === 'PAID' ? 'success' : (p.paymentStatus === 'REFUNDED' ? 'default' : 'warning')" size="tiny" :bordered="false" round>
                    {{ p.paymentStatus === 'PAID' ? '已付款' : (p.paymentStatus === 'REFUNDED' ? '已退款' : '待付款') }}
                  </n-tag>
                  <n-tag :type="receiptMap[p.receiptStatus]?.type" size="tiny" :bordered="false" round>
                    {{ receiptMap[p.receiptStatus]?.label || p.receiptStatus }}
                  </n-tag>
                </div>
              </div>
            </div>
          </div>
        </template>
      </n-spin>
    </n-modal>
  </div>
</template>
