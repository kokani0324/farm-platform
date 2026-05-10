<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NIcon, NTag, NProgress, NPagination, useMessage,
} from 'naive-ui'
import { PeopleOutline, TimeOutline, LeafOutline, FlashOutline } from '@vicons/ionicons5'
import { listOpenGroupBuys } from '@/api/groupBuy'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 12 })

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listOpenGroupBuys({ page, size: 12 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

function timeRemaining(deadline) {
  const ms = new Date(deadline).getTime() - Date.now()
  if (ms <= 0) return '已截止'
  const hours = Math.floor(ms / 3600000)
  if (hours >= 24) return `剩 ${Math.floor(hours / 24)} 天`
  if (hours >= 1) return `剩 ${hours} 小時`
  return `剩 ${Math.floor(ms / 60000)} 分鐘`
}

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <div class="flex items-center gap-2 mb-2">
      <n-icon :component="FlashOutline" size="28" color="#c89b3c" />
      <h1 class="text-2xl font-bold text-farm-800">進行中的團購</h1>
    </div>
    <p class="text-sm text-gray-500 mb-6">
      集結眾人之力,讓你以更優惠的價格買到小農好物。集滿目標數量即出貨,未達標自動取消、不收費。
    </p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有進行中的團購">
        <template #extra>
          <n-button @click="router.push({ name: 'products' })">逛商品</n-button>
        </template>
      </n-empty>

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="gb in data.content"
          :key="gb.id"
          class="bg-white border border-farm-100 rounded-lg overflow-hidden hover:shadow-md hover:border-farm transition cursor-pointer flex flex-col"
          @click="router.push({ name: 'group-buy-detail', params: { id: gb.id } })"
        >
          <div class="aspect-video bg-farm-50 overflow-hidden relative">
            <img v-if="gb.productImageUrl" :src="gb.productImageUrl" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center">
              <n-icon :component="LeafOutline" size="40" color="#a8c96b" />
            </div>
            <div class="absolute top-2 right-2 bg-amber-500 text-white text-xs font-bold px-2 py-1 rounded">
              省 NT$ {{ Number(gb.saving).toLocaleString() }}/{{ gb.productUnit }}
            </div>
          </div>

          <div class="p-4 flex-1 flex flex-col">
            <h3 class="font-bold text-farm-900 mb-1 line-clamp-1">{{ gb.productName }}</h3>
            <div class="text-xs text-gray-500 mb-2">{{ gb.farmerName }} · 團主 {{ gb.hostName }}</div>

            <div class="flex items-baseline gap-2 mb-3">
              <span class="text-2xl font-bold text-farm">NT$ {{ Number(gb.groupPrice).toLocaleString() }}</span>
              <span class="text-xs text-gray-400 line-through">NT$ {{ Number(gb.productPrice).toLocaleString() }}</span>
              <span class="text-xs text-gray-500">/ {{ gb.productUnit }}</span>
            </div>

            <div class="mb-2">
              <n-progress
                type="line"
                :percentage="gb.percent"
                :height="8"
                :show-indicator="false"
                color="#c89b3c"
                rail-color="#f0e8d0"
              />
              <div class="flex justify-between text-xs text-gray-600 mt-1">
                <span>
                  <n-icon :component="PeopleOutline" /> {{ gb.currentQuantity }} / {{ gb.targetQuantity }}
                </span>
                <span :class="gb.percent >= 100 ? 'text-farm font-semibold' : ''">
                  {{ gb.percent }}%
                </span>
              </div>
            </div>

            <div class="flex items-center justify-between text-xs text-gray-500 mt-auto">
              <span><n-icon :component="TimeOutline" /> {{ timeRemaining(gb.deadlineDate) }}</span>
              <n-tag v-if="gb.joined" size="small" type="success" :bordered="false">已參加</n-tag>
            </div>
          </div>
        </div>
      </div>

      <div v-if="data.totalPages > 1" class="flex justify-center pt-6">
        <n-pagination
          :page="data.page + 1"
          :page-count="data.totalPages"
          @update:page="(p) => load(p - 1)"
        />
      </div>
    </n-spin>
  </div>
</template>
