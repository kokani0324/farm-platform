<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NTag, NButton, NIcon, NInputNumber, NDivider, NBreadcrumb, NBreadcrumbItem,
  NModal, NForm, NFormItem, NInput, NDatePicker, useMessage,
} from 'naive-ui'
import { LeafOutline, LocationOutline, CarOutline, PeopleOutline, FlashOutline } from '@vicons/ionicons5'
import { getProduct } from '@/api/products'
import { createGroupBuyRequest } from '@/api/groupBuy'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const message = useMessage()

const product = ref(null)
const loading = ref(true)
const qty = ref(1)
const adding = ref(false)

// 發起團購
const showGbModal = ref(false)
const gbFormRef = ref(null)
const gbSubmitting = ref(false)
const gbForm = reactive({
  targetQuantity: 10,
  groupPrice: null,
  openDate: Date.now(),
  deadlineDate: Date.now() + 7 * 86400000,
  message: '',
})
const gbRules = computed(() => ({
  targetQuantity: { required: true, type: 'number', min: 2, message: '至少 2', trigger: 'blur' },
  groupPrice: {
    required: true,
    validator(_r, v) {
      if (v == null || v <= 0) return new Error('請填團購價')
      if (product.value && Number(v) >= Number(product.value.price)) {
        return new Error(`團購價必須低於原價 NT$ ${product.value.price}`)
      }
      return true
    },
    trigger: ['blur', 'input'],
  },
  openDate: { required: true, type: 'number', message: '請選開團時間', trigger: 'change' },
  deadlineDate: {
    required: true,
    validator(_r, v) {
      if (!v) return new Error('請選截止時間')
      if (v <= gbForm.openDate) return new Error('截止時間必須晚於開團時間')
      return true
    },
    trigger: 'change',
  },
}))

function openGbModal() {
  if (!auth.isLoggedIn) {
    message.warning('請先登入')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (auth.isFarmer || auth.isAdmin) {
    message.warning('請以消費者身份發起團購')
    return
  }
  // 預設團購價為原價 8 折
  gbForm.groupPrice = Math.round(Number(product.value.price) * 0.8)
  showGbModal.value = true
}

async function submitGb() {
  try { await gbFormRef.value?.validate() } catch { return }
  gbSubmitting.value = true
  try {
    await createGroupBuyRequest({
      productId: product.value.id,
      targetQuantity: gbForm.targetQuantity,
      groupPrice: gbForm.groupPrice,
      openDate: new Date(gbForm.openDate).toISOString().slice(0, 19),
      deadlineDate: new Date(gbForm.deadlineDate).toISOString().slice(0, 19),
      message: gbForm.message,
    })
    message.success('發起成功!等待小農審核')
    showGbModal.value = false
    router.push({ name: 'my-group-buy-requests' })
  } catch (e) {
    message.error(e.response?.data?.message || '發起失敗')
  } finally {
    gbSubmitting.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await getProduct(route.params.id)
    product.value = data
  } catch (e) {
    message.error('找不到此商品')
    router.push({ name: 'products' })
  } finally {
    loading.value = false
  }
}

const soldOut = computed(() => !product.value || product.value.stock === 0 || product.value.status !== 'ACTIVE')
const totalPrice = computed(() => product.value ? Number(product.value.price) * qty.value : 0)

async function addToCart() {
  if (!auth.isLoggedIn) {
    message.warning('請先登入會員')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (auth.isFarmer || auth.isAdmin) {
    message.warning('小農 / 管理員帳號不能購物，請改用消費者帳號')
    return
  }
  adding.value = true
  try {
    await cart.add(product.value.id, qty.value)
    message.success(`已加入購物車（${product.value.name} x ${qty.value}）`)
  } catch (e) {
    message.error(e.response?.data?.message || '加入失敗')
  } finally {
    adding.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <n-spin :show="loading">
      <template v-if="product">
        <n-breadcrumb class="mb-4">
          <n-breadcrumb-item @click="router.push({ name: 'home' })">首頁</n-breadcrumb-item>
          <n-breadcrumb-item @click="router.push({ name: 'products' })">商品</n-breadcrumb-item>
          <n-breadcrumb-item>{{ product.name }}</n-breadcrumb-item>
        </n-breadcrumb>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div class="aspect-square bg-farm-50 rounded-lg overflow-hidden">
            <img
              v-if="product.imageUrl"
              :src="product.imageUrl"
              :alt="product.name"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center">
              <n-icon :component="LeafOutline" size="80" color="#a8c96b" />
            </div>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2">
              <n-tag size="small" round :bordered="false" type="success">{{ product.categoryName }}</n-tag>
              <n-tag v-if="product.groupBuyEnabled" size="small" round :bordered="false" type="warning">可團購</n-tag>
            </div>
            <h1 class="text-3xl font-bold text-farm-900 mb-3">{{ product.name }}</h1>

            <div class="text-3xl font-bold text-farm mb-1">
              NT$ {{ Number(product.price).toLocaleString() }}
              <span class="text-base text-gray-500 font-normal">/ {{ product.unit }}</span>
            </div>
            <div class="text-sm text-gray-500 mb-6">
              庫存 {{ product.stock }} {{ product.unit }}
            </div>

            <div class="space-y-3 mb-6 text-sm text-gray-700">
              <div class="flex items-center gap-2">
                <n-icon :component="PeopleOutline" />
                <span>來自 <strong>{{ product.farmerName }}</strong></span>
              </div>
              <div v-if="product.origin" class="flex items-center gap-2">
                <n-icon :component="LocationOutline" />
                <span>產地 {{ product.origin }}</span>
              </div>
              <div v-if="product.shippingMethod" class="flex items-center gap-2">
                <n-icon :component="CarOutline" />
                <span>{{ product.shippingMethod }}</span>
              </div>
            </div>

            <n-divider />

            <div v-if="product.description" class="mb-6">
              <h3 class="font-bold text-farm-800 mb-2">商品介紹</h3>
              <p class="text-sm text-gray-700 whitespace-pre-line leading-relaxed">{{ product.description }}</p>
            </div>

            <div class="flex items-center gap-3 mb-3">
              <span class="text-sm">數量</span>
              <n-input-number v-model:value="qty" :min="1" :max="product.stock" :disabled="soldOut" />
            </div>
            <div class="text-lg mb-4">
              小計：<strong class="text-farm">NT$ {{ totalPrice.toLocaleString() }}</strong>
            </div>

            <div class="flex gap-3">
              <n-button type="primary" size="large" :disabled="soldOut" :loading="adding" @click="addToCart" class="flex-1">
                {{ soldOut ? '已售完' : '加入購物車' }}
              </n-button>
              <n-button
                v-if="product.groupBuyEnabled && !soldOut"
                size="large"
                color="#c89b3c"
                @click="openGbModal"
              >
                <template #icon><n-icon :component="FlashOutline" /></template>
                發起團購
              </n-button>
            </div>
          </div>
        </div>
      </template>
    </n-spin>

    <!-- 發起團購 Modal -->
    <n-modal v-model:show="showGbModal" preset="card" style="width: 540px">
      <template #header>
        <span class="text-farm-800">發起團購 - {{ product?.name }}</span>
      </template>
      <p class="text-sm text-gray-500 mb-3">
        填寫條件後送出,將由小農審核;通過後會自動產生公開團購活動,你會成為團主。
      </p>
      <n-form ref="gbFormRef" :model="gbForm" :rules="gbRules" label-placement="top">
        <n-form-item label="目標達標數量" path="targetQuantity">
          <n-input-number v-model:value="gbForm.targetQuantity" :min="2" :max="9999" />
          <span class="text-xs text-gray-500 ml-3">{{ product?.unit }}</span>
        </n-form-item>
        <n-form-item label="團購價(每單位)" path="groupPrice">
          <n-input-number v-model:value="gbForm.groupPrice" :min="1" :max="product ? Number(product.price) - 1 : 99999" />
          <span class="text-xs text-gray-500 ml-3">原價 NT$ {{ Number(product?.price || 0).toLocaleString() }} / {{ product?.unit }}</span>
        </n-form-item>
        <n-form-item label="開團時間" path="openDate">
          <n-date-picker v-model:value="gbForm.openDate" type="datetime" clearable class="w-full" />
        </n-form-item>
        <n-form-item label="截止時間" path="deadlineDate">
          <n-date-picker v-model:value="gbForm.deadlineDate" type="datetime" clearable class="w-full" />
        </n-form-item>
        <n-form-item label="想對小農說(選填)">
          <n-input v-model:value="gbForm.message" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" maxlength="500" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showGbModal = false">取消</n-button>
          <n-button type="primary" :loading="gbSubmitting" @click="submitGb">送出申請</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
