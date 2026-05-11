<script setup>
import { onMounted, ref } from 'vue'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NInput, NPopconfirm, useMessage,
} from 'naive-ui'
import { listAdminUsers, enableAdminUser, disableAdminUser } from '@/api/admin'

const message = useMessage()
const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 15 })
const keyword = ref('')

const roleLabel = {
  CONSUMER: '消費者',
  FARMER: '小農',
  ADMIN: '管理員',
}

async function load(page = 0) {
  loading.value = true
  try {
    const { data: res } = await listAdminUsers({ page, size: 15, keyword: keyword.value || '' })
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function toggleEnabled(u) {
  acting.value = true
  try {
    if (u.enabled) {
      await disableAdminUser(u.id)
      message.success('已停權')
    } else {
      await enableAdminUser(u.id)
      message.success('已啟用')
    }
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(() => load(0))
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-2">會員管理</h1>
    <p class="text-sm text-gray-500 mb-6">管理所有平台會員,可啟用或停權。停權後該帳號無法登入。</p>

    <div class="mb-4 flex gap-3">
      <n-input
        v-model:value="keyword"
        placeholder="搜尋 Email / 姓名"
        clearable
        style="max-width: 320px"
        @keyup.enter="load(0)"
      />
      <n-button @click="load(0)">搜尋</n-button>
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="找不到符合的會員" />

      <div v-else class="bg-white border border-farm-100 rounded-lg overflow-hidden">
        <table class="w-full text-sm">
          <thead class="bg-farm-50 text-farm-800">
            <tr>
              <th class="text-left px-4 py-2">#</th>
              <th class="text-left px-4 py-2">Email</th>
              <th class="text-left px-4 py-2">姓名</th>
              <th class="text-left px-4 py-2">身份</th>
              <th class="text-left px-4 py-2">狀態</th>
              <th class="text-left px-4 py-2">建立</th>
              <th class="text-left px-4 py-2">動作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="u in data.content"
              :key="u.id"
              class="border-t border-farm-50 hover:bg-farm-50/40"
            >
              <td class="px-4 py-2">{{ u.id }}</td>
              <td class="px-4 py-2">{{ u.email }}</td>
              <td class="px-4 py-2">{{ u.name }}</td>
              <td class="px-4 py-2">
                <n-tag
                  v-for="r in u.roles"
                  :key="r"
                  size="small"
                  :bordered="false"
                  :type="r === 'ADMIN' ? 'error' : r === 'FARMER' ? 'warning' : 'success'"
                  class="mr-1"
                >{{ roleLabel[r] || r }}</n-tag>
              </td>
              <td class="px-4 py-2">
                <n-tag :type="u.enabled ? 'success' : 'default'" size="small" :bordered="false" round>
                  {{ u.enabled ? '正常' : '停權' }}
                </n-tag>
              </td>
              <td class="px-4 py-2 text-gray-500">{{ fmt(u.createdAt) }}</td>
              <td class="px-4 py-2">
                <n-popconfirm
                  v-if="u.primaryRole !== 'ADMIN'"
                  @positive-click="toggleEnabled(u)"
                >
                  <template #trigger>
                    <n-button
                      size="tiny"
                      :type="u.enabled ? 'error' : 'primary'"
                      tertiary
                      :loading="acting"
                    >
                      {{ u.enabled ? '停權' : '啟用' }}
                    </n-button>
                  </template>
                  確定{{ u.enabled ? '停權' : '啟用' }} {{ u.email }} ?
                </n-popconfirm>
                <span v-else class="text-xs text-gray-400">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.totalPages > 1" class="flex justify-center pt-4">
        <n-pagination
          :page="data.page + 1"
          :page-count="data.totalPages"
          @update:page="(p) => load(p - 1)"
        />
      </div>
    </n-spin>
  </div>
</template>
