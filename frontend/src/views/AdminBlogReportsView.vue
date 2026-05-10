<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NTabs, NTabPane, NSelect,
  NPopconfirm, useMessage,
} from 'naive-ui'
import {
  listBlogReports, handleBlogReport,
  listBlogCommentReports, handleBlogCommentReport,
} from '@/api/admin'

const router = useRouter()
const message = useMessage()

const tab = ref('blog')
const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 15 })
const filterStatus = ref('PENDING')

const statusOptions = [
  { label: '待處理', value: 'PENDING' },
  { label: '已處理:顯示', value: 'KEEP' },
  { label: '已處理:隱藏', value: 'HIDDEN' },
  { label: '全部', value: null },
]
const statusMap = {
  PENDING: { type: 'warning', label: '待處理' },
  KEEP: { type: 'success', label: '已處理(保留)' },
  HIDDEN: { type: 'default', label: '已處理(隱藏)' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const params = { page, size: 15 }
    if (filterStatus.value) params.status = filterStatus.value
    const fn = tab.value === 'blog' ? listBlogReports : listBlogCommentReports
    const { data: res } = await fn(params)
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function handle(id, action) {
  acting.value = true
  try {
    const fn = tab.value === 'blog' ? handleBlogReport : handleBlogCommentReport
    await fn(id, { action })
    message.success(action === 'HIDDEN' ? '已隱藏目標內容' : '已標記為保留')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

function onTabChange() {
  data.value = { content: [], totalElements: 0, totalPages: 0, page: 0, size: 15 }
  load(0)
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">檢舉處理</h1>
    <p class="text-sm text-gray-500 mb-4">處理會員對文章與留言的檢舉,可保留或隱藏目標內容。</p>

    <div class="flex items-center gap-3 mb-4 flex-wrap">
      <n-tabs v-model:value="tab" type="line" @update:value="onTabChange">
        <n-tab-pane name="blog" tab="文章檢舉" />
        <n-tab-pane name="comment" tab="留言檢舉" />
      </n-tabs>
      <n-select
        v-model:value="filterStatus"
        :options="statusOptions"
        style="width: 160px"
        @update:value="load(0)"
      />
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有檢舉" />

      <div v-else class="space-y-3">
        <div
          v-for="r in data.content"
          :key="r.id"
          class="bg-white border border-farm-100 rounded-lg p-4"
        >
          <div class="flex items-start justify-between mb-2 flex-wrap gap-2">
            <div class="flex items-center gap-2">
              <n-tag :type="statusMap[r.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[r.status]?.label || r.status }}
              </n-tag>
              <n-tag size="small" :bordered="false">
                {{ r.targetType === 'BLOG' ? '文章' : '留言' }} #{{ r.targetId }}
              </n-tag>
              <span class="text-xs text-gray-500">
                檢舉人 {{ r.reporterName }} · {{ fmt(r.createdAt) }}
              </span>
            </div>
            <div class="flex gap-2" v-if="r.status === 'PENDING'">
              <n-popconfirm @positive-click="handle(r.id, 'KEEP')">
                <template #trigger>
                  <n-button size="small" type="primary" tertiary :loading="acting">保留</n-button>
                </template>
                判定無違規,保留內容並結案?
              </n-popconfirm>
              <n-popconfirm @positive-click="handle(r.id, 'HIDDEN')">
                <template #trigger>
                  <n-button size="small" type="error" :loading="acting">隱藏內容</n-button>
                </template>
                確定隱藏目標內容?(會從公開頁消失)
              </n-popconfirm>
            </div>
          </div>

          <div class="bg-farm-50/50 rounded p-3 mb-2">
            <div class="text-xs text-gray-500 mb-1">檢舉目標</div>
            <div class="text-sm text-gray-800 line-clamp-3">{{ r.targetTitleOrContent }}</div>
            <n-button
              v-if="r.blogId"
              size="tiny"
              text
              type="primary"
              @click="router.push({ name: 'blog-detail', params: { id: r.blogId } })"
              class="mt-1"
            >前往該文章</n-button>
          </div>

          <div class="text-sm text-gray-700">
            <span class="text-gray-500">檢舉事由:</span>{{ r.reason }}
          </div>

          <div v-if="r.status !== 'PENDING'" class="text-xs text-gray-500 mt-1">
            處理人 {{ r.handlerName || '—' }} · {{ fmt(r.handledAt) }}
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
