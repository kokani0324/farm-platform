<script setup>
import { onMounted, ref, reactive } from 'vue'
import {
  NSpin, NEmpty, NButton, NTag, NPagination, NSelect, NModal, NInput, NPopconfirm,
  useMessage,
} from 'naive-ui'
import { listFarmerGroupBuyRequests, reviewGroupBuyRequest } from '@/api/groupBuy'

const message = useMessage()

const loading = ref(false)
const acting = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 10 })

const filterStatus = ref(null)
const statusOptions = [
  { label: '全部', value: null },
  { label: '待審核', value: 'PENDING' },
  { label: '已通過', value: 'APPROVED' },
  { label: '已拒絕', value: 'REJECTED' },
  { label: '已撤回', value: 'WITHDRAWN' },
]
const statusMap = {
  PENDING: { type: 'warning', label: '待審核' },
  APPROVED: { type: 'success', label: '已通過' },
  REJECTED: { type: 'error', label: '已拒絕' },
  WITHDRAWN: { type: 'default', label: '已撤回' },
}

const showRejectModal = ref(false)
const rejectForm = reactive({ id: null, reason: '' })

async function load(page = 0) {
  loading.value = true
  try {
    const params = { page, size: 10 }
    if (filterStatus.value) params.status = filterStatus.value
    const { data: res } = await listFarmerGroupBuyRequests(params)
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  acting.value = true
  try {
    await reviewGroupBuyRequest(id, { approved: true })
    message.success('已通過,團購活動已建立')
    await load(data.value.page)
  } catch (e) {
    message.error(e.response?.data?.message || '操作失敗')
  } finally {
    acting.value = false
  }
}

function openReject(id) {
  rejectForm.id = id
  rejectForm.reason = ''
  showRejectModal.value = true
}

async function submitReject() {
  if (!rejectForm.reason.trim()) {
    message.warning('請填寫拒絕原因')
    return
  }
  acting.value = true
  try {
    await reviewGroupBuyRequest(rejectForm.id, { approved: false, rejectReason: rejectForm.reason })
    message.success('已拒絕')
    showRejectModal.value = false
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
  <div class="max-w-5xl mx-auto px-6 py-8">
    <div class="flex items-center justify-between mb-2 flex-wrap gap-3">
      <h1 class="text-2xl font-bold text-farm-800">團購請求審核</h1>
      <n-select
        v-model:value="filterStatus"
        :options="statusOptions"
        style="width: 160px"
        @update:value="load(0)"
      />
    </div>
    <p class="text-sm text-gray-500 mb-6">
      消費者對你的商品發起的團購請求。通過後將建立公開團購活動,讓更多人加入。
    </p>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有團購申請" />

      <div v-else class="space-y-3">
        <div
          v-for="r in data.content"
          :key="r.id"
          class="bg-white border border-farm-100 rounded-lg p-4 flex gap-4"
        >
          <div class="w-24 h-24 bg-farm-50 rounded shrink-0 overflow-hidden">
            <img v-if="r.productImageUrl" :src="r.productImageUrl" class="w-full h-full object-cover" />
          </div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h3 class="font-bold text-farm-900 truncate">{{ r.productName }}</h3>
              <n-tag :type="statusMap[r.status]?.type" size="small" :bordered="false" round>
                {{ statusMap[r.status]?.label || r.status }}
              </n-tag>
            </div>
            <div class="text-xs text-gray-500 mb-1">
              發起人 {{ r.initiatorName }} · 申請 #{{ r.id }} · {{ fmt(r.requestedAt) }}
            </div>
            <div class="text-sm text-gray-700 mb-1">
              目標 <strong>{{ r.targetQuantity }}</strong> {{ r.productUnit }} ·
              團購價 <strong class="text-farm">NT$ {{ Number(r.groupPrice).toLocaleString() }}</strong>
              <span class="text-gray-400 line-through ml-1">NT$ {{ Number(r.productPrice).toLocaleString() }}</span>
            </div>
            <div class="text-xs text-gray-500">
              開團 {{ fmt(r.openDate) }} · 截止 {{ fmt(r.deadlineDate) }}
            </div>
            <div v-if="r.message" class="text-xs text-gray-600 mt-1">
              <span class="text-gray-400">留言:</span>{{ r.message }}
            </div>
            <div v-if="r.status === 'REJECTED' && r.rejectReason" class="text-xs text-red-500 mt-1">
              拒絕原因:{{ r.rejectReason }}
            </div>
          </div>
          <div v-if="r.status === 'PENDING'" class="flex flex-col gap-2 shrink-0">
            <n-popconfirm @positive-click="approve(r.id)">
              <template #trigger>
                <n-button size="small" type="primary" :loading="acting">通過</n-button>
              </template>
              通過後會自動建立團購活動,確定?
            </n-popconfirm>
            <n-button size="small" type="error" tertiary @click="openReject(r.id)">拒絕</n-button>
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

    <n-modal v-model:show="showRejectModal" preset="card" style="width: 480px" title="拒絕團購申請">
      <n-input
        v-model:value="rejectForm.reason"
        type="textarea"
        :autosize="{ minRows: 3, maxRows: 6 }"
        placeholder="請填寫拒絕原因(會通知申請者)"
        maxlength="500"
        show-count
      />
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showRejectModal = false">取消</n-button>
          <n-button type="error" :loading="acting" @click="submitReject">確認拒絕</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
