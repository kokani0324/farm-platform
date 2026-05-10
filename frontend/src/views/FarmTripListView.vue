<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NButton, NIcon, NTag, NPagination, NSelect, useMessage,
} from 'naive-ui'
import { CompassOutline, LocationOutline, TimeOutline, PeopleOutline, LeafOutline } from '@vicons/ionicons5'
import { listFarmTrips, listFarmTripCategories } from '@/api/farmTrips'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const data = ref({ content: [], totalElements: 0, totalPages: 0, page: 0, size: 12 })
const categories = ref([])

const filterCat = ref(null)
const filterType = ref(null)
const typeOptions = [
  { label: '全部類型', value: null },
  { label: '農場體驗營', value: 'FARM_EXPERIENCE' },
  { label: '產地參訪', value: 'FIELD_VISIT' },
]
const catOptions = computed(() => [
  { label: '全部類別', value: null },
  ...categories.value.map(c => ({ label: `${c.icon || ''} ${c.name}`, value: c.id })),
])

const statusMap = {
  ACTIVE: { type: 'warning', label: '招募中' },
  FULL: { type: 'error', label: '已額滿' },
  CLOSED: { type: 'default', label: '報名截止' },
  CANCELLED: { type: 'default', label: '已取消' },
  COMPLETED: { type: 'default', label: '已舉辦' },
}

async function load(page = 0) {
  loading.value = true
  try {
    const params = { page, size: 12 }
    if (filterCat.value) params.categoryId = filterCat.value
    if (filterType.value) params.tripType = filterType.value
    const { data: res } = await listFarmTrips(params)
    data.value = res
  } catch (e) {
    message.error(e.response?.data?.message || '載入失敗')
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const { data: res } = await listFarmTripCategories()
    categories.value = res
  } catch {}
}

function fmtDate(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(async () => {
  await loadCategories()
  await load(0)
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-6 py-8">
    <div class="flex items-center gap-2 mb-2">
      <n-icon :component="CompassOutline" size="28" color="#4a7c2a" />
      <h1 class="text-2xl font-bold text-farm-800">農場體驗活動</h1>
    </div>
    <p class="text-sm text-gray-500 mb-6">
      走進田裡親手體驗農作,跟著小農認識土地。報名後即確認名額,活動前可取消。
    </p>

    <div class="flex flex-wrap gap-3 mb-5">
      <n-select
        v-model:value="filterCat"
        :options="catOptions"
        placeholder="全部類別"
        style="width: 180px"
        @update:value="load(0)"
      />
      <n-select
        v-model:value="filterType"
        :options="typeOptions"
        placeholder="活動類型"
        style="width: 160px"
        @update:value="load(0)"
      />
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && data.content.length === 0" description="目前沒有體驗活動" />

      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="t in data.content"
          :key="t.id"
          class="bg-white border border-farm-100 rounded-lg overflow-hidden hover:shadow-md hover:border-farm transition cursor-pointer flex flex-col"
          @click="router.push({ name: 'farm-trip-detail', params: { id: t.id } })"
        >
          <div class="aspect-video bg-farm-50 overflow-hidden relative">
            <img v-if="t.imageUrl" :src="t.imageUrl" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center">
              <n-icon :component="LeafOutline" size="40" color="#a8c96b" />
            </div>
            <div class="absolute top-2 left-2">
              <n-tag size="small" :bordered="false" type="success">{{ t.categoryName }}</n-tag>
            </div>
            <div class="absolute top-2 right-2">
              <n-tag size="small" :bordered="false" :type="statusMap[t.status]?.type">
                {{ statusMap[t.status]?.label || t.status }}
              </n-tag>
            </div>
          </div>
          <div class="p-4 flex-1 flex flex-col">
            <h3 class="font-bold text-farm-900 mb-1 line-clamp-1">{{ t.title }}</h3>
            <div class="text-xs text-gray-500 mb-2">{{ t.farmerName }}</div>
            <div class="text-sm text-gray-600 space-y-1 mb-3">
              <div v-if="t.location"><n-icon :component="LocationOutline" /> {{ t.location }}</div>
              <div><n-icon :component="TimeOutline" /> {{ fmtDate(t.tripStart) }}</div>
              <div><n-icon :component="PeopleOutline" /> 名額 {{ t.remaining }} / {{ t.capacity }}</div>
            </div>
            <div class="flex items-baseline justify-between mt-auto">
              <span class="text-xl font-bold text-farm">NT$ {{ Number(t.price).toLocaleString() }}</span>
              <span class="text-xs text-gray-500">/ 人</span>
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
