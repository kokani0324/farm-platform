<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NForm, NFormItem, NInput, NRadioGroup, NRadio, NButton, NIcon, NSpin, NEmpty, NDivider, useMessage,
} from 'naive-ui'
import { LeafOutline, BagHandleOutline } from '@vicons/ionicons5'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { checkout } from '@/api/orders'

const router = useRouter()
const message = useMessage()
const cart = useCartStore()
const auth = useAuthStore()

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  recipientName: auth.user?.name || '',
  recipientPhone: '',
  shippingAddress: '',
  note: '',
  paymentMethod: 'CREDIT_CARD_SIM',
})

const rules = {
  recipientName: { required: true, message: '請填收件人姓名', trigger: 'blur' },
  recipientPhone: {
    required: true,
    validator: (_r, v) => /^[0-9\-+\s()]{8,20}$/.test(v) || new Error('請填有效電話'),
    trigger: 'blur',
  },
  shippingAddress: { required: true, message: '請填收件地址', trigger: 'blur' },
  paymentMethod: { required: true, message: '請選擇付款方式', trigger: 'change' },
}

const paymentLabels = {
  CREDIT_CARD_SIM: '信用卡（模擬）',
  BANK_TRANSFER_SIM: 'ATM 轉帳（模擬）',
  CASH_ON_DELIVERY: '貨到付款',
}

onMounted(async () => {
  if (!cart.loaded) await cart.fetch()
  if (cart.isEmpty) {
    message.warning('購物車是空的')
    router.replace({ name: 'cart' })
  } else if (cart.hasUnavailable) {
    message.warning('請先處理購物車中已下架的商品')
    router.replace({ name: 'cart' })
  }
})

async function submit() {
  try {
    await formRef.value?.validate()
  } catch (_) {
    return
  }
  submitting.value = true
  try {
    const { data: orders } = await checkout({ ...form })
    message.success(`下單成功，共 ${orders.length} 張訂單`)
    await cart.fetch()
    if (orders.length === 1) {
      router.replace({ name: 'order-detail', params: { id: orders[0].id } })
    } else {
      router.replace({ name: 'orders' })
    }
  } catch (e) {
    message.error(e.response?.data?.message || '結帳失敗')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-6">結帳</h1>

    <n-spin :show="cart.loading">
      <div v-if="!cart.isEmpty" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- 左：表單 -->
        <div class="lg:col-span-2 bg-white rounded-lg border border-farm-100 p-6">
          <h2 class="text-lg font-bold text-farm-800 mb-4">收件資訊</h2>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
            <n-form-item label="收件人姓名" path="recipientName">
              <n-input v-model:value="form.recipientName" maxlength="50" />
            </n-form-item>
            <n-form-item label="收件人電話" path="recipientPhone">
              <n-input v-model:value="form.recipientPhone" placeholder="0912-345-678" maxlength="20" />
            </n-form-item>
            <n-form-item label="收件地址" path="shippingAddress">
              <n-input v-model:value="form.shippingAddress" placeholder="請填完整地址" maxlength="200" />
            </n-form-item>
            <n-form-item label="備註（給小農的話）">
              <n-input
                v-model:value="form.note"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
                maxlength="500"
                show-count
                placeholder="非必填"
              />
            </n-form-item>

            <n-divider />

            <h2 class="text-lg font-bold text-farm-800 mb-3">付款方式</h2>
            <n-form-item path="paymentMethod" :show-label="false">
              <n-radio-group v-model:value="form.paymentMethod">
                <div class="flex flex-col gap-2">
                  <n-radio value="CREDIT_CARD_SIM">信用卡（模擬，按確認付款即視為付款成功）</n-radio>
                  <n-radio value="BANK_TRANSFER_SIM">ATM 轉帳（模擬）</n-radio>
                  <n-radio value="CASH_ON_DELIVERY">貨到付款</n-radio>
                </div>
              </n-radio-group>
            </n-form-item>
          </n-form>
        </div>

        <!-- 右：訂單摘要 -->
        <div class="bg-white rounded-lg border border-farm-100 p-6 h-fit lg:sticky lg:top-20">
          <h2 class="text-lg font-bold text-farm-800 mb-4">訂單摘要</h2>

          <div v-if="cart.groupedByFarmer.length > 1" class="text-xs text-gray-500 mb-3 bg-amber-50 border border-amber-200 px-3 py-2 rounded">
            購物車包含 {{ cart.groupedByFarmer.length }} 位小農的商品，將自動拆成 {{ cart.groupedByFarmer.length }} 張訂單。
          </div>

          <div
            v-for="g in cart.groupedByFarmer"
            :key="g.farmerId"
            class="mb-4 last:mb-0"
          >
            <div class="text-sm font-semibold text-farm-700 mb-2 pb-1 border-b border-farm-50">
              {{ g.farmerName }}
            </div>
            <div
              v-for="it in g.items"
              :key="it.productId"
              class="flex items-center gap-2 mb-2 text-sm"
            >
              <div class="w-10 h-10 bg-farm-50 rounded overflow-hidden flex-shrink-0">
                <img v-if="it.imageUrl" :src="it.imageUrl" class="w-full h-full object-cover" />
                <div v-else class="w-full h-full flex items-center justify-center">
                  <n-icon :component="LeafOutline" color="#a8c96b" />
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <div class="truncate">{{ it.name }}</div>
                <div class="text-xs text-gray-500">x {{ it.quantity }}</div>
              </div>
              <div class="text-sm font-medium text-farm-800">
                NT$ {{ Number(it.subtotal).toLocaleString() }}
              </div>
            </div>
            <div class="text-right text-xs text-gray-500 pt-1">
              小計 NT$ {{ Number(g.subtotal).toLocaleString() }}
            </div>
          </div>

          <n-divider />

          <div class="flex items-baseline justify-between mb-1">
            <span class="text-sm text-gray-600">商品數量</span>
            <span class="text-sm">{{ cart.totalQuantity }} 件</span>
          </div>
          <div class="flex items-baseline justify-between mb-4">
            <span class="text-sm text-gray-600">付款方式</span>
            <span class="text-sm">{{ paymentLabels[form.paymentMethod] }}</span>
          </div>

          <div class="flex items-baseline justify-between mb-4">
            <span class="text-base font-bold">應付金額</span>
            <span class="text-2xl font-bold text-farm">NT$ {{ Number(cart.totalAmount).toLocaleString() }}</span>
          </div>

          <n-button type="primary" size="large" block :loading="submitting" @click="submit">
            <template #icon><n-icon :component="BagHandleOutline" /></template>
            確認下單
          </n-button>
          <div class="text-xs text-gray-400 text-center mt-2">
            送出後將從庫存扣除商品數量
          </div>
        </div>
      </div>

      <n-empty v-else description="購物車是空的" />
    </n-spin>
  </div>
</template>
