<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NCard, NForm, NFormItem, NInput, NInputNumber, NSelect, NSwitch, NButton, NSpace,
  useMessage,
} from 'naive-ui'
import {
  listCategories, getProduct, createProduct, updateProduct,
} from '@/api/products'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const isEdit = computed(() => !!route.params.id)
const productId = computed(() => route.params.id)

const formRef = ref(null)
const saving = ref(false)
const categories = ref([])

const model = ref({
  name: '',
  description: '',
  price: 100,
  unit: '斤',
  stock: 10,
  imageUrl: '',
  origin: '',
  shippingMethod: '黑貓宅配',
  groupBuyEnabled: false,
  categoryId: null,
})

const rules = {
  name: { required: true, message: '請輸入商品名稱', trigger: 'blur' },
  price: { required: true, type: 'number', message: '請輸入單價', trigger: ['blur', 'change'] },
  unit: { required: true, message: '請輸入計價單位', trigger: 'blur' },
  stock: { required: true, type: 'number', message: '請輸入庫存', trigger: ['blur', 'change'] },
  categoryId: { required: true, type: 'number', message: '請選擇分類', trigger: 'change' },
}

const shippingOptions = [
  { label: '黑貓宅配', value: '黑貓宅配' },
  { label: '全家店到店', value: '全家店到店' },
  { label: '7-11 交貨便', value: '7-11 交貨便' },
  { label: '農場自取', value: '農場自取' },
]

async function loadCategories() {
  const { data } = await listCategories()
  categories.value = data.map(c => ({ label: `${c.icon} ${c.name}`, value: c.id }))
}

async function loadProduct() {
  if (!isEdit.value) return
  const { data } = await getProduct(productId.value)
  model.value = {
    name: data.name,
    description: data.description || '',
    price: Number(data.price),
    unit: data.unit,
    stock: data.stock,
    imageUrl: data.imageUrl || '',
    origin: data.origin || '',
    shippingMethod: data.shippingMethod || '黑貓宅配',
    groupBuyEnabled: !!data.groupBuyEnabled,
    categoryId: data.categoryId,
  }
}

async function onSubmit() {
  try { await formRef.value?.validate() } catch { return }

  saving.value = true
  try {
    if (isEdit.value) {
      await updateProduct(productId.value, model.value)
      message.success('商品已更新')
    } else {
      await createProduct(model.value)
      message.success('商品已上架')
    }
    router.push({ name: 'farmer-products' })
  } catch (e) {
    const fe = e.response?.data?.errors
    message.error(fe ? Object.values(fe).join('；') : (e.response?.data?.message || '儲存失敗'))
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadCategories()
  await loadProduct()
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-6 py-8">
    <h1 class="text-2xl font-bold text-farm-800 mb-6">
      {{ isEdit ? '編輯商品' : '新增商品' }}
    </h1>

    <n-card :bordered="false" class="shadow-sm">
      <n-form ref="formRef" :model="model" :rules="rules" label-placement="top">
        <n-form-item label="商品名稱" path="name">
          <n-input v-model:value="model.name" placeholder="例：有機溫室小白菜" maxlength="100" show-count />
        </n-form-item>

        <n-form-item label="商品分類" path="categoryId">
          <n-select v-model:value="model.categoryId" :options="categories" placeholder="選擇分類" />
        </n-form-item>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <n-form-item label="單價 (NT$)" path="price">
            <n-input-number v-model:value="model.price" :min="1" :precision="2" class="w-full" />
          </n-form-item>
          <n-form-item label="計價單位" path="unit">
            <n-input v-model:value="model.unit" placeholder="斤、箱、公斤..." />
          </n-form-item>
          <n-form-item label="庫存" path="stock">
            <n-input-number v-model:value="model.stock" :min="0" class="w-full" />
          </n-form-item>
        </div>

        <n-form-item label="商品圖片網址">
          <n-input v-model:value="model.imageUrl" placeholder="貼上圖片網址（可從 Unsplash / 自家網站）" />
        </n-form-item>
        <div v-if="model.imageUrl" class="mb-4">
          <img :src="model.imageUrl" alt="預覽" class="max-h-48 rounded border" />
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <n-form-item label="產地">
            <n-input v-model:value="model.origin" placeholder="例：雲林麥寮" />
          </n-form-item>
          <n-form-item label="出貨方式">
            <n-select v-model:value="model.shippingMethod" :options="shippingOptions" />
          </n-form-item>
        </div>

        <n-form-item label="是否開放團購">
          <n-switch v-model:value="model.groupBuyEnabled">
            <template #checked>開放</template>
            <template #unchecked>關閉</template>
          </n-switch>
        </n-form-item>

        <n-form-item label="商品描述">
          <n-input
            v-model:value="model.description"
            type="textarea"
            :autosize="{ minRows: 4, maxRows: 8 }"
            placeholder="介紹產品特色、種植方式、口感..."
            maxlength="2000"
            show-count
          />
        </n-form-item>

        <n-space justify="end">
          <n-button @click="router.push({ name: 'farmer-products' })">取消</n-button>
          <n-button type="primary" :loading="saving" @click="onSubmit">
            {{ isEdit ? '儲存修改' : '送出上架' }}
          </n-button>
        </n-space>
      </n-form>
    </n-card>
  </div>
</template>
