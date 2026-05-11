<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NCard, NIcon, NStatistic, useMessage } from 'naive-ui'
import {
  PeopleOutline, LeafOutline, BasketOutline, ReceiptOutline,
  FlashOutline, CompassOutline, NewspaperOutline, AlertCircleOutline,
} from '@vicons/ionicons5'
import { getAdminStats } from '@/api/admin'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const stats = ref({})

async function load() {
  loading.value = true
  try {
    const { data } = await getAdminStats()
    stats.value = data
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

const cards = [
  { title: '總會員', key: 'totalUsers', icon: PeopleOutline, color: '#4a7c2a', go: 'admin-users' },
  { title: '消費者', key: 'totalConsumers', icon: PeopleOutline, color: '#7eb846', go: 'admin-users' },
  { title: '小農', key: 'totalFarmers', icon: LeafOutline, color: '#c89b3c', go: 'admin-users' },
  { title: '停權帳號', key: 'disabledUsers', icon: AlertCircleOutline, color: '#e88080', go: 'admin-users' },
  { title: '總商品', key: 'totalProducts', icon: BasketOutline, color: '#4a7c2a', go: 'admin-products' },
  { title: '上架中商品', key: 'activeProducts', icon: BasketOutline, color: '#7eb846', go: 'admin-products' },
  { title: '訂單總數', key: 'totalOrders', icon: ReceiptOutline, color: '#c89b3c' },
  { title: '進行中團購', key: 'openGroupBuys', icon: FlashOutline, color: '#e8a020' },
  { title: '體驗活動', key: 'totalFarmTrips', icon: CompassOutline, color: '#4a7c2a' },
  { title: '部落格文章', key: 'totalBlogs', icon: NewspaperOutline, color: '#7eb846' },
  { title: '待處理檢舉', key: 'pendingBlogReports', icon: AlertCircleOutline, color: '#e88080', go: 'admin-blog-reports' },
]

onMounted(load)
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">後台總覽</h1>
    <p class="text-sm text-gray-500 mb-6">平台關鍵數字一覽。點選卡片可進入對應管理頁。</p>

    <n-spin :show="loading">
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <n-card
          v-for="c in cards"
          :key="c.key"
          :class="[c.go ? 'cursor-pointer hover:shadow-md transition' : '', 'border border-farm-100']"
          @click="c.go && router.push({ name: c.go })"
        >
          <div class="flex items-center gap-3">
            <n-icon :component="c.icon" size="32" :color="c.color" />
            <n-statistic :label="c.title" :value="stats[c.key] ?? 0" />
          </div>
        </n-card>
      </div>
    </n-spin>
  </div>
</template>
