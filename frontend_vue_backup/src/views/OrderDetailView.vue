<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NTag, NButton, NIcon, NDivider, NPopconfirm, NDescriptions, NDescriptionsItem, useMessage,
} from 'naive-ui'
import { CardOutline, CloseCircleOutline, RocketOutline, LeafOutline, CheckmarkDoneOutline } from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { getOrder, payOrder, cancelOrder, shipOrder, confirmReceipt } from '@/api/orders'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const order = ref(null)
const loading = ref(true)
const acting = ref(false)

const statusMap = {
  PENDING_PAYMENT: { type: 'warning', label: '待付款' },
  PAID: { type: 'info', label: '已付款' },
  SHIPPED: { type: 'success', label: '已出貨' },
  COMPLETED: { type: 'success', label: '已完成' },
  CANCELLED: { type: 'default', label: '已取消' },
}

const paymentLabels = {
  CREDIT_CARD_SIM: '信用卡（模擬）',
  BANK_TRANSFER_SIM: 'ATM 轉帳（模擬）',
  CASH_ON_DELIVERY: '貨到付款',
}

const isMyOrder = computed(() =>
  order.value && auth.user?.userId === order.value.consumerId
)
const isMyFarmerOrder = computed(() =>
  order.value && auth.user?.userId === order.value.farmerId
)

async function load() {
  loading.value = true
  try {
    const { data } = await getOrder(route.params.id)
    order.value = data
  } catch (e) {
    message.error(e.response?.data?.message || '找不到此訂單')
    router.replace({ name: 'orders' })
  } finally {
    loading.value = false
  }
}

async function onPay() {
  acting.value = true
  try {
    const { data } = await payOrder(order.value.id)
    order.value = data
    message.success('付款成功！等待小農出貨')
  } catch (e) {
    message.error(e.response?.data?.message || '付款失敗')
  } finally {
    acting.value = false
  }
}

async function onCancel() {
  acting.value = true
  try {
    const { data } = await cancelOrder(order.value.id)
    order.value = data
    message.success('訂單已取消，庫存已還原')
  } catch (e) {
    message.error(e.response?.data?.message || '取消失敗')
  } finally {
    acting.value = false
  }
}

async function onShip() {
  acting.value = true
  try {
    const { data } = await shipOrder(order.value.id)
    order.value = data
    message.success('已標記為出貨')
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

async function onConfirmReceipt() {
  acting.value = true
  try {
    const { data } = await confirmReceipt(order.value.id)
    order.value = data
    message.success('已確認收貨,訂單完成!')
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

function formatDate(s) {
  if (!s) return '—'
  return s.replace('T', ' ').slice(0, 19)
}

onMounted(load)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8">
    <n-spin :show="loading">
      <template v-if="order">
        <div class="flex items-center justify-between mb-4 flex-wrap gap-2">
          <div>
            <div class="text-sm text-gray-500 mb-1">訂單編號</div>
            <h1 class="text-2xl font-bold text-farm-800 font-mono">{{ order.orderNo }}</h1>
          </div>
          <n-tag :type="statusMap[order.status]?.type" size="large" round :bordered="false">
            {{ statusMap[order.status]?.label || order.status }}
          </n-tag>
        </div>

        <!-- 操作按鈕 -->
        <div class="flex gap-2 mb-6 flex-wrap">
          <template v-if="isMyOrder && order.status === 'PENDING_PAYMENT'">
            <n-button type="primary" :loading="acting" @click="onPay">
              <template #icon><n-icon :component="CardOutline" /></template>
              {{ order.paymentMethod === 'CASH_ON_DELIVERY' ? '確認下單（貨到付款）' : '確認付款（模擬）' }}
            </n-button>
            <n-popconfirm @positive-click="onCancel">
              <template #trigger>
                <n-button type="error" secondary :loading="acting">
                  <template #icon><n-icon :component="CloseCircleOutline" /></template>
                  取消訂單
                </n-button>
              </template>
              取消後庫存會還原，確定？
            </n-popconfirm>
          </template>

          <template v-if="isMyFarmerOrder && order.status === 'PAID'">
            <n-button type="primary" :loading="acting" @click="onShip">
              <template #icon><n-icon :component="RocketOutline" /></template>
              標記為已出貨
            </n-button>
          </template>

          <template v-if="isMyOrder && order.status === 'SHIPPED'">
            <n-popconfirm @positive-click="onConfirmReceipt">
              <template #trigger>
                <n-button type="primary" :loading="acting">
                  <template #icon><n-icon :component="CheckmarkDoneOutline" /></template>
                  確認收貨
                </n-button>
              </template>
              已收到商品?確認後訂單將標記為「已完成」。
            </n-popconfirm>
          </template>
        </div>

        <!-- 商品列表 -->
        <div class="bg-white rounded-lg border border-farm-100 mb-6">
          <div class="px-5 py-3 border-b border-farm-50 font-bold text-farm-800">
            商品明細（{{ order.farmerName }}）
          </div>
          <div class="divide-y divide-farm-50">
            <div
              v-for="it in order.items"
              :key="it.productId || it.productName"
              class="p-4 flex items-center gap-4"
            >
              <div class="w-16 h-16 bg-farm-50 rounded overflow-hidden flex-shrink-0">
                <img v-if="it.imageUrl" :src="it.imageUrl" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center">
                  <n-icon :component="LeafOutline" size="28" color="#a8c96b" />
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="font-medium text-farm-900">{{ it.productName }}</div>
                <div class="text-sm text-gray-500">
                  NT$ {{ Number(it.unitPrice).toLocaleString() }} / {{ it.unit }} x {{ it.quantity }}
                </div>
              </div>
              <div class="text-right font-bold text-farm-800">
                NT$ {{ Number(it.subtotal).toLocaleString() }}
              </div>
            </div>
          </div>
          <div class="px-5 py-4 border-t border-farm-50 flex items-baseline justify-between bg-farm-50/30">
            <span class="text-base font-bold">合計</span>
            <span class="text-2xl font-bold text-farm">NT$ {{ Number(order.totalAmount).toLocaleString() }}</span>
          </div>
        </div>

        <!-- 詳細資訊 -->
        <div class="bg-white rounded-lg border border-farm-100 p-5">
          <h2 class="font-bold text-farm-800 mb-4">訂單資訊</h2>
          <n-descriptions :column="1" label-placement="left" bordered size="small">
            <n-descriptions-item label="收件人">{{ order.recipientName }}</n-descriptions-item>
            <n-descriptions-item label="電話">{{ order.recipientPhone }}</n-descriptions-item>
            <n-descriptions-item label="地址">{{ order.shippingAddress }}</n-descriptions-item>
            <n-descriptions-item label="備註">{{ order.note || '—' }}</n-descriptions-item>
            <n-descriptions-item label="付款方式">{{ paymentLabels[order.paymentMethod] || order.paymentMethod }}</n-descriptions-item>
            <n-descriptions-item label="下單時間">{{ formatDate(order.createdAt) }}</n-descriptions-item>
            <n-descriptions-item label="付款時間">{{ formatDate(order.paidAt) }}</n-descriptions-item>
          </n-descriptions>
        </div>
      </template>
    </n-spin>
  </div>
</template>
