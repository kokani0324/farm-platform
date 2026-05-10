<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NButton, NTag, NIcon, NDivider, NDescriptions, NDescriptionsItem,
  NModal, NForm, NFormItem, NInput, NInputNumber, useMessage,
} from 'naive-ui'
import {
  CompassOutline, LocationOutline, TimeOutline, PeopleOutline,
  LeafOutline, PersonOutline, ChevronBackOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { getFarmTrip, bookFarmTrip } from '@/api/farmTrips'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const trip = ref(null)
const loading = ref(true)
const acting = ref(false)

const showBookModal = ref(false)
const formRef = ref(null)
const form = reactive({
  numPeople: 1,
  contactName: auth.user?.name || '',
  contactPhone: '',
  note: '',
})
const rules = {
  numPeople: { required: true, type: 'number', min: 1, message: '至少 1 人', trigger: 'blur' },
  contactName: { required: true, message: '請填聯絡人', trigger: 'blur' },
  contactPhone: {
    required: true,
    validator: (_r, v) => /^[0-9\-+\s()]{8,20}$/.test(v) || new Error('請填有效電話'),
    trigger: 'blur',
  },
}

const statusMap = {
  ACTIVE: { type: 'warning', label: '招募中' },
  FULL: { type: 'error', label: '已額滿' },
  CLOSED: { type: 'default', label: '報名截止' },
  CANCELLED: { type: 'default', label: '已取消' },
  COMPLETED: { type: 'default', label: '已舉辦' },
}
const typeLabel = (t) => t === 'FARM_EXPERIENCE' ? '農場體驗營' : t === 'FIELD_VISIT' ? '產地參訪' : t

const subtotal = computed(() => trip.value ? Number(trip.value.price) * form.numPeople : 0)
const canBook = computed(() => {
  if (!trip.value) return false
  const now = Date.now()
  return trip.value.status === 'ACTIVE'
    && now >= new Date(trip.value.bookStart).getTime()
    && now <= new Date(trip.value.bookEnd).getTime()
    && trip.value.remaining > 0
})

async function load() {
  loading.value = true
  try {
    const { data } = await getFarmTrip(route.params.id)
    trip.value = data
  } catch (e) {
    message.error(e.response?.data?.message || '找不到此活動')
    router.replace({ name: 'farm-trips' })
  } finally {
    loading.value = false
  }
}

function openBookModal() {
  if (!auth.isLoggedIn) {
    message.warning('請先登入')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (auth.isFarmer || auth.isAdmin) {
    message.warning('請以消費者身份報名')
    return
  }
  showBookModal.value = true
}

async function submitBook() {
  try { await formRef.value?.validate() } catch { return }
  acting.value = true
  try {
    await bookFarmTrip(trip.value.id, { ...form })
    message.success('預約成功!')
    showBookModal.value = false
    router.push({ name: 'my-farm-trip-bookings' })
  } catch (e) {
    message.error(e.response?.data?.message || '預約失敗')
  } finally {
    acting.value = false
  }
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(load)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8">
    <n-spin :show="loading">
      <template v-if="trip">
        <n-button text @click="router.push({ name: 'farm-trips' })" class="mb-4">
          <template #icon><n-icon :component="ChevronBackOutline" /></template>
          回活動列表
        </n-button>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div class="aspect-square bg-farm-50 rounded-lg overflow-hidden">
            <img v-if="trip.imageUrl" :src="trip.imageUrl" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center">
              <n-icon :component="LeafOutline" size="80" color="#a8c96b" />
            </div>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2">
              <n-tag :type="statusMap[trip.status]?.type" :bordered="false" size="small" round>
                {{ statusMap[trip.status]?.label || trip.status }}
              </n-tag>
              <n-tag size="small" :bordered="false" type="success" round>{{ trip.categoryName }}</n-tag>
              <n-tag size="small" :bordered="false" round>{{ typeLabel(trip.tripType) }}</n-tag>
            </div>
            <h1 class="text-2xl font-bold text-farm-900 mb-3">{{ trip.title }}</h1>
            <div class="text-sm text-gray-600 mb-4 space-y-1">
              <div><n-icon :component="PersonOutline" /> 主辦小農 {{ trip.farmerName }}</div>
              <div v-if="trip.location"><n-icon :component="LocationOutline" /> {{ trip.location }}</div>
              <div><n-icon :component="TimeOutline" /> 活動 {{ fmt(trip.tripStart) }} ~ {{ fmt(trip.tripEnd) }}</div>
              <div><n-icon :component="PeopleOutline" /> 剩餘名額 <strong>{{ trip.remaining }}</strong> / {{ trip.capacity }}</div>
            </div>

            <div class="flex items-baseline gap-2 mb-4 bg-farm-50 border border-farm-100 px-4 py-3 rounded">
              <span class="text-3xl font-bold text-farm">NT$ {{ Number(trip.price).toLocaleString() }}</span>
              <span class="text-sm text-gray-500">/ 人</span>
            </div>

            <div class="flex gap-2">
              <n-button
                :disabled="!canBook"
                type="primary"
                size="large"
                @click="openBookModal"
                class="flex-1"
              >
                {{ canBook ? '立即報名' : (statusMap[trip.status]?.label || '無法報名') }}
              </n-button>
            </div>
          </div>
        </div>

        <n-divider />

        <div v-if="trip.intro" class="bg-white border border-farm-100 rounded-lg p-5 mb-4">
          <h2 class="font-bold text-farm-800 mb-3">活動介紹</h2>
          <p class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ trip.intro }}</p>
        </div>

        <div class="bg-white border border-farm-100 rounded-lg p-5">
          <h2 class="font-bold text-farm-800 mb-3">活動資訊</h2>
          <n-descriptions :column="1" label-placement="left" bordered size="small">
            <n-descriptions-item label="活動編號">#{{ trip.id }}</n-descriptions-item>
            <n-descriptions-item label="類型">{{ typeLabel(trip.tripType) }}</n-descriptions-item>
            <n-descriptions-item label="活動時間">{{ fmt(trip.tripStart) }} ~ {{ fmt(trip.tripEnd) }}</n-descriptions-item>
            <n-descriptions-item label="報名期間">{{ fmt(trip.bookStart) }} ~ {{ fmt(trip.bookEnd) }}</n-descriptions-item>
            <n-descriptions-item label="名額">{{ trip.capacity }} 人</n-descriptions-item>
            <n-descriptions-item v-if="trip.location" label="地點">{{ trip.location }}</n-descriptions-item>
          </n-descriptions>
        </div>
      </template>
    </n-spin>

    <n-modal v-model:show="showBookModal" preset="card" style="width: 500px">
      <template #header>
        <span class="text-farm-800">報名 - {{ trip?.title }}</span>
      </template>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="報名人數" path="numPeople">
          <n-input-number v-model:value="form.numPeople" :min="1" :max="trip?.remaining || 1" />
          <span class="text-sm text-gray-500 ml-3">
            小計 <strong class="text-farm">NT$ {{ Number(subtotal).toLocaleString() }}</strong>
          </span>
        </n-form-item>
        <n-form-item label="聯絡人姓名" path="contactName">
          <n-input v-model:value="form.contactName" maxlength="50" />
        </n-form-item>
        <n-form-item label="聯絡人電話" path="contactPhone">
          <n-input v-model:value="form.contactPhone" placeholder="0912-345-678" />
        </n-form-item>
        <n-form-item label="備註(過敏/特殊需求等)">
          <n-input v-model:value="form.note" type="textarea" :autosize="{ minRows: 2, maxRows: 3 }" maxlength="500" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showBookModal = false">取消</n-button>
          <n-button type="primary" :loading="acting" @click="submitBook">確認報名</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
