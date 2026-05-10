<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { NButton, NIcon, NSpin } from 'naive-ui'
import { LeafOutline, BasketOutline, PeopleOutline } from '@vicons/ionicons5'
import { listProducts, listCategories } from '@/api/products'
import ProductCard from '@/components/ProductCard.vue'

const auth = useAuthStore()
const router = useRouter()

const featured = ref([])
const categories = ref([])
const loading = ref(true)

const features = [
  { icon: BasketOutline, title: '在地直送', desc: '從產地新鮮直送你家餐桌' },
  { icon: PeopleOutline, title: '團購省錢', desc: '揪團一起買，同享好價格' },
  { icon: LeafOutline, title: '產地日記', desc: '看見小農種植的每一個故事' },
]

onMounted(async () => {
  try {
    const [pRes, cRes] = await Promise.all([
      listProducts({ size: 8, sort: 'createdAt,desc' }),
      listCategories(),
    ])
    featured.value = pRes.data.content
    categories.value = cRes.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <!-- Hero -->
  <section class="bg-gradient-to-br from-farm-50 via-earth-50 to-farm-100 py-16">
    <div class="max-w-4xl mx-auto px-6 text-center">
      <h1 class="text-4xl md:text-5xl font-bold text-farm-900 mb-3">你儂我農</h1>
      <p class="text-lg text-farm-700 mb-1">我家門前有塊地，你想種什麼？</p>
      <p class="text-base text-gray-600 mb-7">連結台灣小農與你的餐桌，吃得新鮮，吃得安心。</p>

      <div class="flex justify-center gap-3">
        <n-button type="primary" size="large" @click="router.push({ name: 'products' })">
          開始逛商品
        </n-button>
        <n-button v-if="!auth.isLoggedIn" size="large" @click="router.push({ name: 'register' })">
          立即註冊
        </n-button>
        <span v-else class="self-center text-farm-700">哈囉 {{ auth.user?.name }} 👋</span>
      </div>
    </div>
  </section>

  <!-- 分類快速入口 -->
  <section class="max-w-5xl mx-auto px-6 py-12">
    <h2 class="text-2xl font-bold text-center mb-8 text-farm-800">逛逛分類</h2>
    <div class="grid grid-cols-4 md:grid-cols-8 gap-4">
      <button
        v-for="c in categories"
        :key="c.id"
        class="flex flex-col items-center gap-1 p-3 rounded-lg hover:bg-farm-50 transition-colors"
        @click="router.push({ name: 'products', query: { categoryId: c.id } })"
      >
        <span class="text-3xl">{{ c.icon }}</span>
        <span class="text-xs text-gray-700">{{ c.name }}</span>
      </button>
    </div>
  </section>

  <!-- 熱門商品 -->
  <section class="max-w-6xl mx-auto px-6 pb-12">
    <div class="flex items-end justify-between mb-6">
      <h2 class="text-2xl font-bold text-farm-800">最新上架</h2>
      <n-button text type="primary" @click="router.push({ name: 'products' })">查看全部 →</n-button>
    </div>
    <n-spin :show="loading">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <ProductCard v-for="p in featured" :key="p.id" :product="p" />
      </div>
    </n-spin>
  </section>

  <!-- 平台特色 -->
  <section class="bg-farm-50 py-12">
    <div class="max-w-5xl mx-auto px-6">
      <h2 class="text-2xl font-bold text-center mb-8 text-farm-800">平台特色</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="f in features" :key="f.title" class="bg-white rounded-lg p-6 text-center">
          <n-icon :component="f.icon" size="40" color="#4a7c2a" class="mb-2" />
          <h3 class="text-lg font-bold mb-1">{{ f.title }}</h3>
          <p class="text-sm text-gray-600">{{ f.desc }}</p>
        </div>
      </div>
    </div>
  </section>
</template>
