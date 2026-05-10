<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NIcon, NPopconfirm, useMessage,
} from 'naive-ui'
import { CreateOutline, AddOutline } from '@vicons/ionicons5'
import { listMyBlogs, deleteBlog } from '@/api/blogs'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const statusMap = {
  PUBLISHED: { type: 'success', label: '已發布' },
  HIDDEN: { type: 'error', label: '被隱藏' },
  DELETED: { type: 'default', label: '已刪除' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listMyBlogs({ page, size: 10 })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function del(id) {
  acting.value = true
  try {
    await deleteBlog(id)
    message.success('已刪除')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '刪除失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <div class="flex items-center justify-between flex-wrap gap-3 mb-2">
      <h1 class="text-2xl font-bold text-farm-800">我的文章</h1>
      <n-button type="primary" @click="router.push({ name: 'blog-new' })">
        <template #icon><n-icon :component="AddOutline" /></template>
        寫文章
      </n-button>
    </div>
    <p class="text-sm text-gray-500 mb-6">管理你發布過的部落格文章。</p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="還沒寫過文章">
        <template #extra>
          <n-button type="primary" @click="router.push({ name: 'blog-new' })">寫第一篇</n-button>
        </template>
      </n-empty>

      <div v-else class="space-y-3">
        <div
          v-for="b in data.content"
          :key="b.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="b.coverImageUrl" :src="b.coverImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1 flex-wrap">
              <h3 class="font-bold text-farm-900 truncate">{{ b.title }}</h3>
              <n-tag :type="statusMap[b.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[b.status]?.label || b.status }}
              </n-tag>
              <n-tag size="small" :bordered="false" type="success">
                {{ b.blogTypeIcon }} {{ b.blogTypeName }}
              </n-tag>
            </div>
            <p class="text-sm text-gray-500 line-clamp-2">{{ b.content }}</p>
            <div class="text-xs text-gray-500 mt-2">
              {{ fmt(b.createdAt) }} · {{ b.viewCount }} 閱讀 · {{ b.likeCount }} 讚 · {{ b.commentCount }} 留言
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0">
            <n-button
              size="small"
              type="primary"
              secondary
              @click="router.push({ name: 'blog-detail', params: { id: b.id } })"
            >檢視</n-button>
            <n-button
              v-if="b.status === 'PUBLISHED'"
              size="small"
              tertiary
              @click="router.push({ name: 'blog-edit', params: { id: b.id } })"
            >
              <template #icon><n-icon :component="CreateOutline" /></template>
              編輯
            </n-button>
            <n-popconfirm
              v-if="b.status !== 'DELETED'"
              @positive-click="del(b.id)"
            >
              <template #trigger>
                <n-button size="small" type="error" tertiary :loading="acting">刪除</n-button>
              </template>
              確定刪除文章?
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
