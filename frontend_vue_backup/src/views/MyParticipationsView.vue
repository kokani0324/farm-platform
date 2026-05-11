<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NPopconfirm, useMessage,
} from 'naive-ui'
import { listMyParticipations, markMyReceipt } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  JOINED: { type: 'success', label: '參與中' },
  WITHDRAWN: { type: 'default', label: '已退出' },
}

const receiptMap = {
  NOT_SHIPPED: { type: 'default', label: '尚未出貨' },
  SHIPPED: { type: 'info', label: '已出貨' },
  RECEIVED: { type: 'success', label: '已收貨' },
}

const paymentMap = {
  PENDING: { type: 'warning', label: '待付款' },
  PAID: { type: 'success', label: '已付款' },
  REFUNDED: { type: 'default', label: '已退款' },
}

const payMethodLabel = (m) => ({
  CREDIT_CARD_SIM: '信用卡',
  BANK_TRANSFER_SIM: 'ATM 轉帳',
  CASH_ON_DELIVERY: '貨到付款',
}[m] || m || '—')

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

async function onConfirmReceipt(p) {
  acting.value = true
  try {
    await markMyReceipt(p.groupBuyId)
    message.success('已確認收貨,謝謝!')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '確認失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">我參加的團購</h1>
    <p class="text-sm text-gray-500 mb-6">
      所有你參加過的團購記錄;成團後團主名下會建立一張整單,各團員自行確認收貨。
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
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <h3 class="font-bold text-farm-900 truncate">{{ p.productName }}</h3>
              <n-tag :type="statusMap[p.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[p.status]?.label || p.status }}
              </n-tag>
              <n-tag v-if="p.isHost" type="warning" size="small" :bordered="false" round>團主</n-tag>
              <n-tag
                v-if="p.paymentStatus"
                :type="paymentMap[p.paymentStatus]?.type"
                size="small"
                :bordered="false"
                round
              >
                {{ paymentMap[p.paymentStatus]?.label || p.paymentStatus }}
              </n-tag>
              <n-tag
                v-if="p.status === 'JOINED' && p.receiptStatus"
                :type="receiptMap[p.receiptStatus]?.type"
                size="small"
                :bordered="false"
                round
              >
                {{ receiptMap[p.receiptStatus]?.label || p.receiptStatus }}
              </n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">
              小農 {{ p.farmerName }} · 加入於 {{ fmt(p.joinedAt) }} · 團購 #{{ p.groupBuyId }}
              <span v-if="p.groupBuyOrderNo"> · 整單 {{ p.groupBuyOrderNo }}</span>
            </div>
            <div class="text-sm text-gray-700">
              購買 <strong>{{ p.quantity }}</strong> {{ p.productUnit }} ·
              小計 <strong class="text-farm">NT$ {{ Number(p.subtotal).toLocaleString() }}</strong>
            </div>
            <div class="text-xs text-gray-500 mt-1">
              收件 {{ p.recipientName }} / {{ p.recipientPhone }} · {{ p.fullAddress }}
            </div>
            <div class="text-xs text-gray-500 mt-1">
              付款 {{ payMethodLabel(p.paymentMethod) }}
              <span v-if="p.paidAt"> · 付款於 {{ fmt(p.paidAt) }}</span>
              <span v-if="p.refundedAt"> · 退款於 {{ fmt(p.refundedAt) }}</span>
            </div>
            <div v-if="p.shippedAt || p.receiptDatetime" class="text-xs text-gray-500 mt-1">
              <span v-if="p.shippedAt">出貨 {{ fmt(p.shippedAt) }}</span>
              <span v-if="p.receiptDatetime"> · 收貨 {{ fmt(p.receiptDatetime) }}</span>
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
              v-if="p.isHost && p.groupBuyOrderId"
              size="small"
              tertiary
              @click="router.push({ name: 'my-group-buy-orders' })"
            >我的整單</n-button>
            <n-popconfirm
              v-if="p.status === 'JOINED' && p.receiptStatus === 'SHIPPED'"
              @positive-click="onConfirmReceipt(p)"
            >
              <template #trigger>
                <n-button size="small" type="success" :loading="acting">確認收貨</n-button>
              </template>
              確認已收到貨品?確認後無法復原。
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
