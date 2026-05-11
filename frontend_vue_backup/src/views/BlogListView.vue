<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NIcon, NTag, NPagination, NSelect, NInput, useMessage,
} from 'naive-ui'
import {
  NewspaperOutline, ChatbubbleOutline, HeartOutline, EyeOutline, CreateOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { listBlogs, listBlogTypes } from '@/api/blogs'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 12 })
const types = ref([])
const filterType = ref(null)
const keyword = ref('')

const typeOptions = computed(() => [
  { label: '全部分類', value: null },
  ...types.value.map(t => ({ label: `${t.icon || ''} ${t.name}`, value: t.id })),
])

async function load(page = 0) {
  loading.value = true
  try {
    const params = { page, size: 12 }
    if (filterType.value) params.typeId = filterType.value
    if (keyword.value) params.keyword = keyword.value
    const { data: res } = await listBlogs(params)
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function loadTypes() {
  try {
    const { data } = await listBlogTypes()
    types.value = data
  } catch {}
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 10) : '' }

onMounted(async () => {
  await loadTypes()
  await load(0)
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <div class="flex items-center justify-between flex-wrap gap-3 mb-2">
      <div class="flex items-center gap-2">
        <n-icon :component="NewspaperOutline" size="28" color="#4a7c2a" />
        <h1 class="text-2xl font-bold text-farm-800">小農部落格</h1>
      </div>
      <n-button v-if="auth.isLoggedIn && !auth.isAdmin" type="primary" @click="router.push({ name: 'blog-new' })">
        <template #icon><n-icon :component="CreateOutline" /></template>
        寫文章
      </n-button>
    </div>
    <p class="text-sm text-gray-500 mb-6">
      小農與消費者一起分享產地故事、食譜與耕種知識。
    </p>

    <div class="flex flex-wrap gap-3 mb-5">
      <n-select
        v-model:value="filterType"
        :options="typeOptions"
        style="width: 180px"
        @update:value="load(0)"
      />
      <n-input
        v-model:value="keyword"
        placeholder="搜尋文章"
        clearable
        style="max-width: 280px"
        @keyup.enter="load(0)"
      />
      <n-button @click="load(0)">搜尋</n-button>
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有文章" />

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="b in data.content"
          :key="b.id"
          class="bg-white border border-farm-100 rounded-lg overflow-hidden hover:shadow-md hover:border-farm transition cursor-pointer flex flex-col"
          @click="router.push({ name: 'blog-detail', params: { id: b.id } })"
        >
          <div v-if="b.coverImageUrl" class="aspect-video bg-farm-50 overflow-hidden">
            <img :src="b.coverImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="p-4 flex-1 flex flex-col">
            <div class="mb-2">
              <n-tag size="small" :bordered="false" type="success">
                {{ b.blogTypeIcon }} {{ b.blogTypeName }}
              </n-tag>
            </div>
            <h3 class="font-bold text-farm-900 mb-2 line-clamp-2">{{ b.title }}</h3>
            <p class="text-sm text-gray-600 line-clamp-3 mb-3">{{ b.content }}...</p>
            <div class="text-xs text-gray-500 mt-auto flex items-center justify-between">
              <span>{{ b.authorName }} · {{ fmt(b.createdAt) }}</span>
              <span class="flex items-center gap-3">
                <span><n-icon :component="EyeOutline" /> {{ b.viewCount }}</span>
                <span><n-icon :component="HeartOutline" /> {{ b.likeCount }}</span>
                <span><n-icon :component="ChatbubbleOutline" /> {{ b.commentCount }}</span>
              </span>
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
