<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NButton, NCard, NForm, NFormItem, NInput, NSelect, useMessage,
} from 'naive-ui'
import { listBlogTypes, createBlog, updateBlog, getBlog } from '@/api/blogs'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const types = ref([])

const form = reactive({
  blogTypeId: null,
  title: '',
  content: '',
  coverImageUrl: '',
})

const typeOptions = computed(() =>
  types.value.map(t => ({ label: `${t.icon || ''} ${t.name}`, value: t.id }))
)

const rules = {
  blogTypeId: { required: true, type: 'number', message: '請選分類', trigger: 'change' },
  title: { required: true, message: '請填標題', trigger: 'blur' },
  content: { required: true, message: '請填內容', trigger: 'blur' },
}

async function loadTypes() {
  const { data } = await listBlogTypes()
  types.value = data
}

async function loadBlog() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const { data } = await getBlog(route.params.id)
    Object.assign(form, {
      blogTypeId: data.blogTypeId,
      title: data.title,
      content: data.content,
      coverImageUrl: data.coverImageUrl || '',
    })
  } catch {
    message.error('載入文章失敗')
    router.replace({ name: 'my-blogs' })
  } finally {
    loading.value = false
  }
}

async function submit() {
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateBlog(route.params.id, form)
      message.success('已更新')
      router.push({ name: 'blog-detail', params: { id: route.params.id } })
    } else {
      const { data } = await createBlog(form)
      message.success('已發布')
      router.push({ name: 'blog-detail', params: { id: data.id } })
    }
  } catch (e) {
    message.error(e.response?.data?.message || '儲存失敗')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadTypes()
  await loadBlog()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-6">{{ isEdit ? '編輯文章' : '寫文章' }}</h1>

    <n-spin :show="loading">
      <n-card>
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <n-form-item label="分類" path="blogTypeId">
            <n-select v-model:value="form.blogTypeId" :options="typeOptions" placeholder="選擇分類" />
          </n-form-item>

          <n-form-item label="標題" path="title">
            <n-input v-model:value="form.title" maxlength="60" show-count />
          </n-form-item>

          <n-form-item label="封面圖網址">
            <n-input v-model:value="form.coverImageUrl" placeholder="https://..." />
          </n-form-item>

          <n-form-item label="內容" path="content">
            <n-input
              v-model:value="form.content"
              type="textarea"
              :autosize="{ minRows: 10, maxRows: 30 }"
              maxlength="50000"
              show-count
              placeholder="寫下你想分享的內容..."
            />
          </n-form-item>

          <div class="flex justify-end gap-2 pt-4">
            <n-button @click="router.back()">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="submit">
              {{ isEdit ? '儲存修改' : '發布' }}
            </n-button>
          </div>
        </n-form>
      </n-card>
    </n-spin>
  </div>
</template>
