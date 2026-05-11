<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NEmpty, NButton, NIcon, NInputNumber, NPopconfirm, NSpin, NDivider, NTag, useMessage,
} from 'naive-ui'
import { TrashOutline, BagHandleOutline, LeafOutline, CartOutline } from '@vicons/ionicons5'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const message = useMessage()
const cart = useCartStore()
const updating = ref({})

onMounted(() => cart.fetch())

async function onUpdate(productId, qty) {
  updating.value[productId] = true
  try {
    await cart.update(productId, qty)
  } catch (e) {
    message.error(e.response?.data?.message || '更新失敗')
    cart.fetch() // 還原為實際狀態
  } finally {
    updating.value[productId] = false
  }
}

async function onRemove(productId) {
  try {
    await cart.remove(productId)
    message.success('已移除')
  } catch (e) {
    message.error(e.response?.data?.message || '移除失敗')
  }
}

async function onClear() {
  try {
    await cart.clear()
    message.success('購物車已清空')
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  }
}

function goCheckout() {
  if (cart.hasUnavailable) {
    message.warning('請先移除已下架/缺貨的商品')
    return
  }
  if (cart.totalQuantity === 0) {
    message.warning('購物車是空的')
    return
  }
  router.push({ name: 'checkout' })
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <div class="flex items-center gap-2 mb-6">
      <n-icon :component="CartOutline" size="28" color="#4a7c2a" />
      <h1 class="text-2xl font-bold text-farm-800">我的購物車</h1>
    </div>

    <n-spin :show="cart.loading">
      <n-empty
        v-if="cart.loaded && cart.isEmpty"
        description="購物車空空的，去逛逛吧"
      >
        <template #extra>
          <n-button type="primary" @click="router.push({ name: 'products' })">
            <template #icon><n-icon :component="LeafOutline" /></template>
            前往選購
          </n-button>
        </template>
      </n-empty>

      <template v-else>
        <div class="bg-white rounded-lg border border-farm-100 divide-y divide-farm-50">
          <div
            v-for="it in cart.items"
            :key="it.productId"
            class="p-4 flex gap-4 items-center"
            :class="{ 'opacity-60 bg-gray-50': !it.available }"
          >
            <!-- 圖片 -->
            <div class="w-20 h-20 flex-shrink-0 bg-farm-50 rounded overflow-hidden">
              <img v-if="it.imageUrl" :src="it.imageUrl" :alt="it.name" class="w-full h-full object-cover" />
              <div v-else class="w-full h-full flex items-center justify-center">
                <n-icon :component="LeafOutline" size="32" color="#a8c96b" />
              </div>
            </div>

            <!-- 資訊 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <button
                  class="text-base font-medium text-farm-900 hover:text-farm truncate text-left"
                  @click="router.push({ name: 'product-detail', params: { id: it.productId } })"
                >
                  {{ it.name }}
                </button>
                <n-tag v-if="!it.available" size="small" type="error" :bordered="false">已下架/缺貨</n-tag>
              </div>
              <div class="text-xs text-gray-500 mb-1">{{ it.farmerName }}</div>
              <div class="text-farm font-semibold">
                NT$ {{ Number(it.price).toLocaleString() }}
                <span class="text-xs text-gray-400 font-normal">/ {{ it.unit }}</span>
              </div>
            </div>

            <!-- 數量 -->
            <div class="flex-shrink-0">
              <n-input-number
                :value="it.quantity"
                :min="1"
                :max="it.stock"
                :disabled="!it.available || updating[it.productId]"
                size="small"
                style="width: 110px"
                @update:value="(v) => v && onUpdate(it.productId, v)"
              />
              <div class="text-xs text-gray-400 mt-1 text-center">庫存 {{ it.stock }}</div>
            </div>

            <!-- 小計 -->
            <div class="w-28 text-right">
              <div class="text-base font-bold text-farm-800">
                NT$ {{ Number(it.subtotal).toLocaleString() }}
              </div>
            </div>

            <!-- 刪除 -->
            <n-popconfirm @positive-click="onRemove(it.productId)">
              <template #trigger>
                <n-button quaternary circle type="error">
                  <template #icon><n-icon :component="TrashOutline" /></template>
                </n-button>
              </template>
              確定移除「{{ it.name }}」？
            </n-popconfirm>
          </div>
        </div>

        <!-- 結帳列 -->
        <div class="mt-6 bg-white rounded-lg border border-farm-100 p-5 flex items-center justify-between flex-wrap gap-3">
          <div>
            <n-popconfirm @positive-click="onClear">
              <template #trigger>
                <n-button quaternary type="error" size="small">清空購物車</n-button>
              </template>
              確定清空購物車？
            </n-popconfirm>
          </div>
          <div class="flex items-center gap-6">
            <div class="text-sm text-gray-600">
              共 <span class="text-farm-800 font-bold">{{ cart.totalQuantity }}</span> 件
            </div>
            <div class="text-lg">
              合計：<span class="text-2xl font-bold text-farm">NT$ {{ Number(cart.totalAmount).toLocaleString() }}</span>
            </div>
            <n-button type="primary" size="large" @click="goCheckout" :disabled="cart.totalQuantity === 0">
              <template #icon><n-icon :component="BagHandleOutline" /></template>
              前往結帳
            </n-button>
          </div>
        </div>
      </template>
    </n-spin>
  </div>
</template>
