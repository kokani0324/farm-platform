<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NButton, NTag, NIcon, NDivider, NInput, NPagination, NPopconfirm,
  NModal, useMessage,
} from 'naive-ui'
import {
  ChevronBackOutline, HeartOutline, EyeOutline, ChatbubbleOutline,
  CreateOutline, TrashOutline, FlagOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import {
  getBlog, listBlogComments, likeBlog, addBlogComment, deleteBlogComment,
  reportBlog, reportBlogComment, deleteBlog,
} from '@/api/blogs'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const blog = ref(null)
const loading = ref(true)
const comments = ref({ content: [], totalPages: 0, page: 0 })
const commentInput = ref('')
const acting = ref(false)

const showReportModal = ref(false)
const reportForm = reactive({ targetType: 'BLOG', targetId: null, reason: '' })

const isAuthor = computed(() => blog.value && auth.user?.userId === blog.value.authorId)

async function load() {
  loading.value = true
  try {
    const { data } = await getBlog(route.params.id)
    blog.value = data
    await loadComments(0)
  } catch (e) {
    message.error(e.response?.data?.message || '找不到文章')
    router.replace({ name: 'blogs' })
  } finally {
    loading.value = false
  }
}

async function loadComments(page = 0) {
  try {
    const { data } = await listBlogComments(route.params.id, { page, size: 20 })
    comments.value = data
  } catch {}
}

async function like() {
  acting.value = true
  try {
    const { data } = await likeBlog(blog.value.id)
    blog.value.likeCount = data.likeCount
    message.success('已按讚')
  } catch (e) {
    message.error('按讚失敗')
  } finally {
    acting.value = false
  }
}

async function submitComment() {
  if (!auth.isLoggedIn) {
    message.warning('請先登入')
    return
  }
  if (!commentInput.value.trim()) return
  acting.value = true
  try {
    await addBlogComment(blog.value.id, { content: commentInput.value })
    message.success('已留言')
    commentInput.value = ''
    blog.value.commentCount += 1
    await loadComments(0)
  } catch (e) {
    message.error(e.response?.data?.message || '留言失敗')
  } finally {
    acting.value = false
  }
}

async function delComment(id) {
  acting.value = true
  try {
    await deleteBlogComment(id)
    blog.value.commentCount = Math.max(0, blog.value.commentCount - 1)
    await loadComments(comments.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '刪除失敗')
  } finally {
    acting.value = false
  }
}

async function delBlog() {
  acting.value = true
  try {
    await deleteBlog(blog.value.id)
    message.success('已刪除')
    router.replace({ name: 'my-blogs' })
  } catch (e) {
    message.error(e.response?.data?.message || '刪除失敗')
  } finally {
    acting.value = false
  }
}

function openReport(targetType, targetId) {
  if (!auth.isLoggedIn) {
    message.warning('請先登入')
    return
  }
  reportForm.targetType = targetType
  reportForm.targetId = targetId
  reportForm.reason = ''
  showReportModal.value = true
}

async function submitReport() {
  if (!reportForm.reason.trim()) {
    message.warning('請填寫檢舉事由')
    return
  }
  acting.value = true
  try {
    if (reportForm.targetType === 'BLOG') {
      await reportBlog(reportForm.targetId, { reason: reportForm.reason })
    } else {
      await reportBlogComment(reportForm.targetId, { reason: reportForm.reason })
    }
    message.success('已提交檢舉,等待管理員處理')
    showReportModal.value = false
  } catch (e) {
    message.error(e.response?.data?.message || '檢舉失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '' }

onMounted(load)
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-8">
    <n-spin :show="loading">
      <template v-if="blog">
        <n-button text @click="router.push({ name: 'blogs' })" class="mb-4">
          <template #icon><n-icon :component="ChevronBackOutline" /></template>
          回部落格
        </n-button>

        <article class="bg-white border border-farm-100 rounded-lg p-6 mb-6">
          <div class="flex items-center gap-2 mb-3 flex-wrap">
            <n-tag size="small" :bordered="false" type="success">
              {{ blog.blogTypeIcon }} {{ blog.blogTypeName }}
            </n-tag>
            <span class="text-sm text-gray-500">{{ blog.authorName }} · {{ fmt(blog.createdAt) }}</span>
          </div>
          <h1 class="text-3xl font-bold text-farm-900 mb-4">{{ blog.title }}</h1>

          <img v-if="blog.coverImageUrl" :src="blog.coverImageUrl" class="w-full max-h-96 object-cover rounded mb-5" />

          <div class="prose max-w-none text-gray-800 leading-relaxed whitespace-pre-line">{{ blog.content }}</div>

          <div class="flex items-center gap-4 mt-6 pt-4 border-t border-farm-50 text-sm text-gray-600">
            <span><n-icon :component="EyeOutline" /> {{ blog.viewCount }} 閱讀</span>
            <span><n-icon :component="ChatbubbleOutline" /> {{ blog.commentCount }} 留言</span>
          </div>

          <div class="flex gap-2 mt-4 flex-wrap">
            <n-button @click="like" :loading="acting" type="primary" secondary>
              <template #icon><n-icon :component="HeartOutline" /></template>
              讚 {{ blog.likeCount }}
            </n-button>
            <template v-if="isAuthor">
              <n-button @click="router.push({ name: 'blog-edit', params: { id: blog.id } })" tertiary>
                <template #icon><n-icon :component="CreateOutline" /></template>
                編輯
              </n-button>
              <n-popconfirm @positive-click="delBlog">
                <template #trigger>
                  <n-button type="error" tertiary :loading="acting">
                    <template #icon><n-icon :component="TrashOutline" /></template>
                    刪除
                  </n-button>
                </template>
                確定刪除文章?
              </n-popconfirm>
            </template>
            <n-button v-else-if="auth.isLoggedIn" tertiary @click="openReport('BLOG', blog.id)">
              <template #icon><n-icon :component="FlagOutline" /></template>
              檢舉
            </n-button>
          </div>
        </article>

        <section class="bg-white border border-farm-100 rounded-lg p-6">
          <h2 class="font-bold text-farm-800 mb-4">留言 ({{ blog.commentCount }})</h2>

          <div v-if="auth.isLoggedIn" class="mb-5">
            <n-input
              v-model:value="commentInput"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 5 }"
              maxlength="200"
              show-count
              placeholder="留下你的想法..."
            />
            <div class="flex justify-end mt-2">
              <n-button type="primary" :loading="acting" :disabled="!commentInput.trim()" @click="submitComment">
                送出
              </n-button>
            </div>
          </div>
          <div v-else class="mb-5 text-sm text-gray-500">
            <n-button text @click="router.push({ name: 'login', query: { redirect: route.fullPath } })">
              登入
            </n-button>
            後可留言
          </div>

          <div v-if="comments.content.length === 0" class="text-sm text-gray-400">還沒有留言,搶頭香吧!</div>
          <div v-else class="space-y-3">
            <div
              v-for="c in comments.content"
              :key="c.id"
              class="border-t border-farm-50 pt-3"
            >
              <div class="flex items-start justify-between mb-1">
                <div>
                  <span class="font-semibold text-farm-800">{{ c.authorName }}</span>
                  <span class="text-xs text-gray-400 ml-2">{{ fmt(c.createdAt) }}</span>
                </div>
                <div class="flex gap-1">
                  <n-popconfirm v-if="auth.user?.userId === c.authorId" @positive-click="delComment(c.id)">
                    <template #trigger>
                      <n-button size="tiny" tertiary type="error">刪除</n-button>
                    </template>
                    刪除留言?
                  </n-popconfirm>
                  <n-button
                    v-else-if="auth.isLoggedIn"
                    size="tiny"
                    tertiary
                    @click="openReport('COMMENT', c.id)"
                  >檢舉</n-button>
                </div>
              </div>
              <p class="text-sm text-gray-700 whitespace-pre-line">{{ c.content }}</p>
            </div>

            <div v-if="comments.totalPages > 1" class="flex justify-center pt-3">
              <n-pagination
                :page="comments.page + 1"
                :page-count="comments.totalPages"
                @update:page="(p) => loadComments(p - 1)"
              />
            </div>
          </div>
        </section>
      </template>
    </n-spin>

    <n-modal v-model:show="showReportModal" preset="card" style="width: 480px" title="檢舉">
      <p class="text-sm text-gray-500 mb-3">
        請填寫檢舉事由,管理員會盡快處理。
      </p>
      <n-input
        v-model:value="reportForm.reason"
        type="textarea"
        :autosize="{ minRows: 3, maxRows: 5 }"
        maxlength="100"
        show-count
        placeholder="例如:廣告、不實內容、違反社群規範..."
      />
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showReportModal = false">取消</n-button>
          <n-button type="error" :loading="acting" @click="submitReport">送出檢舉</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
