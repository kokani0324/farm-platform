<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NButton, NForm, NFormItem, NInput, NInputNumber, NSelect,
  NDatePicker, NCard, useMessage,
} from 'naive-ui'
import { listFarmTripCategories, createFarmTrip, updateFarmTrip, getFarmTrip } from '@/api/farmTrips'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const categories = ref([])

const form = reactive({
  categoryId: null,
  tripType: 'FARM_EXPERIENCE',
  title: '',
  intro: '',
  imageUrl: '',
  location: '',
  price: 500,
  capacity: 10,
  tripStart: Date.now() + 14 * 86400000,
  tripEnd: Date.now() + 14 * 86400000 + 8 * 3600000,
  bookStart: Date.now(),
  bookEnd: Date.now() + 12 * 86400000,
})

const typeOptions = [
  { label: '農場體驗營', value: 'FARM_EXPERIENCE' },
  { label: '產地參訪', value: 'FIELD_VISIT' },
]
const catOptions = computed(() =>
  categories.value.map(c => ({ label: `${c.icon || ''} ${c.name}`, value: c.id }))
)

const rules = {
  categoryId: { required: true, type: 'number', message: '請選類別', trigger: 'change' },
  tripType: { required: true, message: '請選活動類型', trigger: 'change' },
  title: { required: true, message: '請填活動標題', trigger: 'blur' },
  price: { required: true, type: 'number', message: '請填費用', trigger: 'blur' },
  capacity: { required: true, type: 'number', min: 1, message: '名額至少 1', trigger: 'blur' },
  tripStart: { required: true, type: 'number', message: '請選活動開始時間', trigger: 'change' },
  tripEnd: { required: true, type: 'number', message: '請選活動結束時間', trigger: 'change' },
  bookStart: { required: true, type: 'number', message: '請選報名開始時間', trigger: 'change' },
  bookEnd: { required: true, type: 'number', message: '請選報名截止時間', trigger: 'change' },
}

async function loadCategories() {
  try {
    const { data } = await listFarmTripCategories()
    categories.value = data
  } catch (e) {
    message.error('載入類別失敗')
  }
}

async function loadTrip() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const { data } = await getFarmTrip(route.params.id)
    Object.assign(form, {
      categoryId: data.categoryId,
      tripType: data.tripType,
      title: data.title,
      intro: data.intro || '',
      imageUrl: data.imageUrl || '',
      location: data.location || '',
      price: Number(data.price),
      capacity: data.capacity,
      tripStart: new Date(data.tripStart).getTime(),
      tripEnd: new Date(data.tripEnd).getTime(),
      bookStart: new Date(data.bookStart).getTime(),
      bookEnd: new Date(data.bookEnd).getTime(),
    })
  } catch (e) {
    message.error('載入活動失敗')
    router.replace({ name: 'farmer-farm-trips' })
  } finally {
    loading.value = false
  }
}

async function submit() {
  try { await formRef.value?.validate() } catch { return }
  submitting.value = true
  try {
    const payload = {
      ...form,
      tripStart: new Date(form.tripStart).toISOString().slice(0, 19),
      tripEnd: new Date(form.tripEnd).toISOString().slice(0, 19),
      bookStart: new Date(form.bookStart).toISOString().slice(0, 19),
      bookEnd: new Date(form.bookEnd).toISOString().slice(0, 19),
    }
    if (isEdit.value) {
      await updateFarmTrip(route.params.id, payload)
      message.success('已更新')
    } else {
      await createFarmTrip(payload)
      message.success('已建立')
    }
    router.push({ name: 'farmer-farm-trips' })
  } catch (e) {
    message.error(e.response?.data?.message || '儲存失敗')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadTrip()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-6">
      {{ isEdit ? '編輯體驗活動' : '新增體驗活動' }}
    </h1>

    <n-spin :show="loading">
      <n-card>
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4">
            <n-form-item label="類別" path="categoryId">
              <n-select v-model:value="form.categoryId" :options="catOptions" placeholder="選擇類別" />
            </n-form-item>
            <n-form-item label="類型" path="tripType">
              <n-select v-model:value="form.tripType" :options="typeOptions" />
            </n-form-item>
          </div>

          <n-form-item label="活動標題" path="title">
            <n-input v-model:value="form.title" maxlength="30" show-count />
          </n-form-item>

          <n-form-item label="活動介紹">
            <n-input
              v-model:value="form.intro"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 6 }"
              maxlength="500"
              show-count
              placeholder="活動內容、行程、注意事項..."
            />
          </n-form-item>

          <n-form-item label="圖片網址">
            <n-input v-model:value="form.imageUrl" placeholder="https://..." />
          </n-form-item>

          <n-form-item label="活動地點">
            <n-input v-model:value="form.location" maxlength="100" placeholder="縣市鄉鎮地址" />
          </n-form-item>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4">
            <n-form-item label="每人費用 (NT$)" path="price">
              <n-input-number v-model:value="form.price" :min="0" :precision="0" class="w-full" />
            </n-form-item>
            <n-form-item label="名額上限" path="capacity">
              <n-input-number v-model:value="form.capacity" :min="1" :max="999" class="w-full" />
            </n-form-item>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4">
            <n-form-item label="活動開始時間" path="tripStart">
              <n-date-picker v-model:value="form.tripStart" type="datetime" class="w-full" clearable />
            </n-form-item>
            <n-form-item label="活動結束時間" path="tripEnd">
              <n-date-picker v-model:value="form.tripEnd" type="datetime" class="w-full" clearable />
            </n-form-item>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-4">
            <n-form-item label="報名開始時間" path="bookStart">
              <n-date-picker v-model:value="form.bookStart" type="datetime" class="w-full" clearable />
            </n-form-item>
            <n-form-item label="報名截止時間" path="bookEnd">
              <n-date-picker v-model:value="form.bookEnd" type="datetime" class="w-full" clearable />
            </n-form-item>
          </div>

          <div class="flex justify-end gap-2 pt-4">
            <n-button @click="router.push({ name: 'farmer-farm-trips' })">取消</n-button>
            <n-button type="primary" :loading="submitting" @click="submit">
              {{ isEdit ? '儲存修改' : '建立活動' }}
            </n-button>
          </div>
        </n-form>
      </n-card>
    </n-spin>
  </div>
</template>
