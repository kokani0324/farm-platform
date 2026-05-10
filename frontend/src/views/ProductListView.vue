<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NInput, NIcon, NPagination, NSpin, NEmpty, NSelect,
} from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { listCategories, listProducts } from '@/api/products'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const router = useRouter()

const categories = ref([])
const products = ref([])
const loading = ref(false)

const page = ref(0)         // 0-based
const size = ref(12)
const totalPages = ref(0)
const totalElements = ref(0)

const keyword = ref(route.query.keyword || '')
const categoryId = ref(route.query.categoryId ? Number(route.query.categoryId) : null)
const sort = ref('createdAt,desc')

const sortOptions = [
  { label: '最新上架', value: 'createdAt,desc' },
  { label: '價格由低到高', value: 'price,asc' },
  { label: '價格由高到低', value: 'price,desc' },
]

async function loadCategories() {
  const { data } = await listCategories()
  categories.value = data
}

async function fetchProducts() {
  loading.value = true
  try {
    const { data } = await listProducts({
      page: page.value,
      size: size.value,
      sort: sort.value,
      categoryId: categoryId.value || undefined,
      keyword: keyword.value || undefined,
    })
    products.value = data.content
    totalPages.value = data.totalPages
    totalElements.value = data.totalElements
  } finally {
    loading.value = false
  }
}

function onSelectCategory(id) {
  categoryId.value = id
  page.value = 0
  syncQuery()
  fetchProducts()
}

function onSearch() {
  page.value = 0
  syncQuery()
  fetchProducts()
}

function onPageChange(p) {
  page.value = p - 1
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function syncQuery() {
  router.replace({
    query: {
      ...(keyword.value ? { keyword: keyword.value } : {}),
      ...(categoryId.value ? { categoryId: categoryId.value } : {}),
    },
  })
}

watch(() => route.query, () => {
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.categoryId ? Number(route.query.categoryId) : null
})

onMounted(async () => {
  await loadCategories()
  await fetchProducts()
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <!-- 搜尋列 -->
    <div class="flex flex-col md:flex-row gap-3 mb-6">
      <n-input
        v-model:value="keyword"
        placeholder="搜尋蔬果、產地、小農..."
        clearable
        size="large"
        class="flex-1"
        @keyup.enter="onSearch"
      >
        <template #prefix>
          <n-icon :component="SearchOutline" />
        </template>
      </n-input>
      <n-select
        v-model:value="sort"
        :options="sortOptions"
        size="large"
        class="md:w-48"
        @update:value="fetchProducts"
      />
    </div>

    <div class="flex gap-6">
      <!-- 分類側欄 -->
      <aside class="hidden md:block w-48 shrink-0">
        <h3 class="font-bold text-farm-800 mb-3">商品分類</h3>
        <ul class="space-y-1">
          <li>
            <button
              class="w-full text-left px-3 py-2 rounded transition-colors"
              :class="categoryId === null ? 'bg-farm text-white' : 'hover:bg-farm-50'"
              @click="onSelectCategory(null)"
            >全部商品</button>
          </li>
          <li v-for="c in categories" :key="c.id">
            <button
              class="w-full text-left px-3 py-2 rounded transition-colors"
              :class="categoryId === c.id ? 'bg-farm text-white' : 'hover:bg-farm-50'"
              @click="onSelectCategory(c.id)"
            >{{ c.icon }} {{ c.name }}</button>
          </li>
        </ul>
      </aside>

      <!-- 商品網格 -->
      <section class="flex-1 min-w-0">
        <div class="flex items-center justify-between mb-4">
          <p class="text-sm text-gray-500">共 {{ totalElements }} 筆商品</p>
        </div>

        <n-spin :show="loading">
          <div
            v-if="products.length === 0 && !loading"
            class="flex justify-center py-20"
          >
            <n-empty description="找不到符合條件的商品" />
          </div>

          <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <ProductCard v-for="p in products" :key="p.id" :product="p" />
          </div>

          <div v-if="totalPages > 1" class="flex justify-center mt-8">
            <n-pagination
              :page="page + 1"
              :page-count="totalPages"
              show-quick-jumper
              @update:page="onPageChange"
            />
          </div>
        </n-spin>
      </section>
    </div>
  </div>
</template>
