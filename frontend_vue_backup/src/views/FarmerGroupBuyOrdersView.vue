<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NModal, NDescriptions,
  NDescriptionsItem, NPopconfirm, useMessage,
} from 'naive-ui'
import {
  listFarmerGroupBuyOrders, getFarmerGroupBuyOrder, markParticipationShipped,
} from '@/api/groupBuy'

const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const showDetailModal = ref(false)
const detail = ref(null)
const detailLoading = ref(false)
const acting = ref(false)

const statusMap = {
  PENDING_PAYMENT: { type: 'warning', label: '待團主付款' },
  PAID: { type: 'info', label: '已付款待出貨' },
  SHIPPING: { type: 'info', label: '出貨中' },
  COMPLETED: { type: 'success', label: '已完成' },
  CANCELLED: { type: 'default', label: '已取消' },
}

const receiptMap = {
  NOT_SHIPPED: { type: 'default', label: '尚未出貨' },
  SHIPPED: { type: 'info', label: '已出貨' },
  RECEIVED: { type: 'success', label: '已收貨' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listFarmerGroupBuyOrders({ page, size: 10 })
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
    const { data: res } = await getFarmerGroupBuyOrder(o.groupBuyId)
    detail.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '取得明細失敗')
    showDetailModal.value = false
  } finally {
    detailLoading.value = false
  }
}

async function onShip(p) {
  acting.value = true
  try {
    await markParticipationShipped(detail.value.groupBuyId, p.id)
    message.success('已標記出貨')
    // 重抓明細與列表
    const { data: res } = await getFarmerGroupBuyOrder(detail.value.groupBuyId)
    detail.value = res
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '出貨失敗')
  } finally {
    acting.value = false
  }
}

const canShip = computed(() => detail.value && (detail.value.status === 'PAID' || detail.value.status === 'SHIPPING'))

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">團購出貨管理</h1>
    <p class="text-sm text-gray-500 mb-6">
      已成團的團購會以「整單」呈現,團主付款後可逐筆對團員出貨。
    </p>

    <n-spin :show="loading">
      <n-empty
        v-if="!loading && data.content.length === 0"
        description="尚無團購整單"
      />

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
              團主 {{ o.hostName }} · 成團於 {{ fmt(o.createdAt) }} · 團購 #{{ o.groupBuyId }}
            </div>
            <div class="text-sm text-gray-700">
              全團 <strong>{{ o.totalQuantity }}</strong> {{ o.productUnit }} ·
              總額 <strong class="text-farm">NT$ {{ Number(o.totalAmount).toLocaleString() }}</strong>
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button size="small" type="primary" @click="openDetail(o)">明細 / 出貨</n-button>
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

    <!-- 明細 + 出貨 modal -->
    <n-modal v-model:show="showDetailModal" preset="card" style="width: 760px">
      <template #header>
        <span class="text-farm-800">出貨明細 {{ detail?.orderNo }}</span>
      </template>
      <n-spin :show="detailLoading">
        <template v-if="detail">
          <n-descriptions :column="2" label-placement="left" bordered size="small" class="mb-4">
            <n-descriptions-item label="商品">{{ detail.productName }}</n-descriptions-item>
            <n-descriptions-item label="團主">{{ detail.hostName }}</n-descriptions-item>
            <n-descriptions-item label="總數量">{{ detail.totalQuantity }} {{ detail.productUnit }}</n-descriptions-item>
            <n-descriptions-item label="總金額">NT$ {{ Number(detail.totalAmount).toLocaleString() }}</n-descriptions-item>
            <n-descriptions-item label="狀態">
              <n-tag :type="statusMap[detail.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[detail.status]?.label || detail.status }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="付款時間">{{ fmt(detail.paidAt) }}</n-descriptions-item>
          </n-descriptions>

          <p v-if="!canShip" class="text-sm text-amber-700 mb-2">
            等待團主付款後才能出貨。
          </p>

          <h4 class="font-semibold text-farm-800 mb-2">逐筆出貨（共 {{ detail.participations.length }} 人）</h4>
          <div class="space-y-2 max-h-96 overflow-y-auto">
            <div
              v-for="p in detail.participations"
              :key="p.id"
              class="flex items-center gap-3 border border-farm-100 rounded p-2 text-sm"
            >
              <div class="flex-1 min-w-0">
                <div class="font-medium">
                  {{ p.userName }}
                  <n-tag v-if="p.isHost" type="warning" size="tiny" :bordered="false" round>團主</n-tag>
                  <n-tag :type="receiptMap[p.receiptStatus]?.type" size="tiny" :bordered="false" round class="ml-1">
                    {{ receiptMap[p.receiptStatus]?.label || p.receiptStatus }}
                  </n-tag>
                </div>
                <div class="text-xs text-gray-500 truncate">
                  {{ p.recipientName }} / {{ p.recipientPhone }} · {{ p.fullAddress }}
                </div>
                <div v-if="p.note" class="text-xs text-gray-400 truncate">備註：{{ p.note }}</div>
              </div>
              <div class="text-right text-xs text-gray-500">
                <div>{{ p.quantity }} {{ detail.productUnit }}</div>
                <div>NT$ {{ Number(p.subtotal).toLocaleString() }}</div>
              </div>
              <n-popconfirm
                v-if="canShip && p.receiptStatus === 'NOT_SHIPPED'"
                @positive-click="onShip(p)"
              >
                <template #trigger>
                  <n-button size="small" type="primary" :loading="acting">標記出貨</n-button>
                </template>
                確認對 {{ p.userName }} 出貨?
              </n-popconfirm>
              <n-button v-else size="small" disabled>—</n-button>
            </div>
          </div>
        </template>
      </n-spin>
    </n-modal>
  </div>
</template>
