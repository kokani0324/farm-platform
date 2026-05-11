<script setup>
import { onMounted, ref, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NButton, NTag, NIcon, NProgress, NDivider, NDescriptions, NDescriptionsItem,
  NModal, NCard, NForm, NFormItem, NInput, NInputNumber, NPopconfirm,
  NRadioGroup, NRadio, NSpace, useMessage,
} from 'naive-ui'
import {
  PeopleOutline, TimeOutline, LeafOutline, FlashOutline, PersonOutline,
  StorefrontOutline, ChevronBackOutline,
} from '@vicons/ionicons5'
import { useAuthStore } from '@/stores/auth'
import { getGroupBuy, joinGroupBuy, withdrawGroupBuy } from '@/api/groupBuy'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const gb = ref(null)
const loading = ref(true)
const acting = ref(false)

const showJoinModal = ref(false)
const formRef = ref(null)
const form = reactive({
  quantity: 1,
  recipientName: auth.user?.name || '',
  recipientPhone: '',
  shippingZipcode: '',
  shippingCity: '',
  shippingDist: '',
  shippingDetail: '',
  paymentMethod: 'CREDIT_CARD_SIM',
  note: '',
})

const rules = {
  quantity: { required: true, type: 'number', min: 1, message: '至少 1', trigger: 'blur' },
  recipientName: { required: true, message: '請填收件人', trigger: 'blur' },
  recipientPhone: {
    required: true,
    validator: (_r, v) => /^[0-9\-+\s()]{8,20}$/.test(v) || new Error('請填有效電話'),
    trigger: 'blur',
  },
  shippingZipcode: {
    required: true,
    validator: (_r, v) => /^\d{3,6}$/.test(v) || new Error('郵遞區號 3~6 位數'),
    trigger: 'blur',
  },
  shippingCity: { required: true, message: '請填縣市', trigger: 'blur' },
  shippingDist: { required: true, message: '請填鄉鎮區', trigger: 'blur' },
  shippingDetail: { required: true, message: '請填詳細地址', trigger: 'blur' },
}

const statusMap = {
  OPEN: { type: 'warning', label: '招募中' },
  SUCCESS: { type: 'success', label: '已成團' },
  FAILED: { type: 'default', label: '未達標' },
  CANCELLED: { type: 'default', label: '已取消' },
}

const subtotal = computed(() => gb.value ? Number(gb.value.groupPrice) * form.quantity : 0)
const isHost = computed(() => gb.value && auth.user?.userId === gb.value.hostId)
const canJoin = computed(() => gb.value && gb.value.status === 'OPEN' && new Date(gb.value.deadlineDate) > new Date())

async function load() {
  loading.value = true
  try {
    const { data } = await getGroupBuy(route.params.id)
    gb.value = data
  } catch (e) {
    message.error(e.response?.data?.message || '找不到此團購')
    router.replace({ name: 'group-buys' })
  } finally {
    loading.value = false
  }
}

function openJoinModal() {
  if (!auth.isLoggedIn) {
    message.warning('請先登入')
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  if (auth.isFarmer || auth.isAdmin) {
    message.warning('請切換為消費者身份才能加入團購')
    return
  }
  showJoinModal.value = true
}

async function submitJoin() {
  try { await formRef.value?.validate() } catch { return }
  acting.value = true
  try {
    await joinGroupBuy(gb.value.id, { ...form })
    message.success('已加入團購!')
    showJoinModal.value = false
    await load()
  } catch (e) {
    message.error(e.response?.data?.message || '加入失敗')
  } finally {
    acting.value = false
  }
}

async function onWithdraw() {
  acting.value = true
  try {
    await withdrawGroupBuy(gb.value.id)
    message.success('已退出團購')
    await load()
  } catch (e) {
    message.error(e.response?.data?.message || '退出失敗')
  } finally {
    acting.value = false
  }
}

function timeRemaining(deadline) {
  const ms = new Date(deadline).getTime() - Date.now()
  if (ms <= 0) return '已截止'
  const hours = Math.floor(ms / 3600000)
  if (hours >= 24) return `${Math.floor(hours / 24)} 天 ${hours % 24} 小時`
  if (hours >= 1) return `${hours} 小時 ${Math.floor((ms % 3600000) / 60000)} 分鐘`
  return `${Math.floor(ms / 60000)} 分鐘`
}

function fmt(s) { return s ? s.replace('T', ' ').slice(0, 16) : '—' }

onMounted(load)
</script>

<template>
  <div class="max-w-4xl mx-auto px-6 py-8">
    <n-spin :show="loading">
      <template v-if="gb">
        <n-button text @click="router.push({ name: 'group-buys' })" class="mb-4">
          <template #icon><n-icon :component="ChevronBackOutline" /></template>
          回團購列表
        </n-button>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <div class="aspect-square bg-farm-50 rounded-lg overflow-hidden">
            <img v-if="gb.productImageUrl" :src="gb.productImageUrl" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center">
              <n-icon :component="LeafOutline" size="80" color="#a8c96b" />
            </div>
          </div>

          <div>
            <div class="flex items-center gap-2 mb-2">
              <n-tag :type="statusMap[gb.status]?.type" :bordered="false" size="small" round>
                {{ statusMap[gb.status]?.label || gb.status }}
              </n-tag>
              <n-tag size="small" :bordered="false" type="warning" round>團購活動</n-tag>
            </div>
            <h1 class="text-2xl font-bold text-farm-900 mb-2">{{ gb.productName }}</h1>
            <div class="text-sm text-gray-600 mb-4 space-y-1">
              <div><n-icon :component="StorefrontOutline" /> 小農 {{ gb.farmerName }}</div>
              <div><n-icon :component="PersonOutline" /> 團主 {{ gb.hostName }}</div>
            </div>

            <div class="flex items-baseline gap-3 mb-4 bg-amber-50 border border-amber-200 px-4 py-3 rounded">
              <span class="text-3xl font-bold text-farm">NT$ {{ Number(gb.groupPrice).toLocaleString() }}</span>
              <span class="text-base text-gray-400 line-through">NT$ {{ Number(gb.productPrice).toLocaleString() }}</span>
              <span class="text-sm text-gray-500">/ {{ gb.productUnit }}</span>
              <span class="ml-auto text-amber-700 text-sm font-semibold">
                省 NT$ {{ Number(gb.saving).toLocaleString() }}
              </span>
            </div>

            <div class="mb-4">
              <div class="flex justify-between text-sm mb-1">
                <span class="text-gray-700">
                  <n-icon :component="PeopleOutline" /> 已集 <strong>{{ gb.currentQuantity }}</strong> / {{ gb.targetQuantity }}
                </span>
                <span :class="gb.percent >= 100 ? 'text-farm font-bold' : 'text-gray-700'">
                  {{ gb.percent }}%
                </span>
              </div>
              <n-progress type="line" :percentage="gb.percent" :height="12" :show-indicator="false"
                color="#c89b3c" rail-color="#f0e8d0" />
              <div class="text-xs text-gray-500 mt-1">
                還差 <strong>{{ gb.remainingQuantity }}</strong> {{ gb.productUnit }}達標
              </div>
            </div>

            <div class="text-sm text-gray-600 mb-4">
              <n-icon :component="TimeOutline" /> 截止倒數:<strong class="text-farm-800">{{ timeRemaining(gb.deadlineDate) }}</strong>
            </div>

            <div class="flex gap-2">
              <template v-if="canJoin">
                <n-button v-if="!gb.joined" type="primary" size="large" @click="openJoinModal" class="flex-1">
                  <template #icon><n-icon :component="FlashOutline" /></template>
                  加入這團
                </n-button>
                <template v-else>
                  <n-button disabled size="large" class="flex-1">
                    已參加 (買 {{ gb.myQuantity }})
                  </n-button>
                  <n-popconfirm @positive-click="onWithdraw">
                    <template #trigger>
                      <n-button type="error" secondary size="large" :loading="acting">退出</n-button>
                    </template>
                    確定退出此團購?截止前可重新加入。
                  </n-popconfirm>
                </template>
              </template>
              <n-button v-else disabled size="large" class="flex-1">
                {{ statusMap[gb.status]?.label || gb.status }}
              </n-button>
            </div>
          </div>
        </div>

        <n-divider />

        <div class="bg-white border border-farm-100 rounded-lg p-5">
          <h2 class="font-bold text-farm-800 mb-3">團購資訊</h2>
          <n-descriptions :column="1" label-placement="left" bordered size="small">
            <n-descriptions-item label="團購編號">#{{ gb.id }}</n-descriptions-item>
            <n-descriptions-item label="開團時間">{{ fmt(gb.openDate) }}</n-descriptions-item>
            <n-descriptions-item label="截止時間">{{ fmt(gb.deadlineDate) }}</n-descriptions-item>
            <n-descriptions-item label="達標規則">集滿 {{ gb.targetQuantity }} {{ gb.productUnit }} 即出貨,未達標自動取消</n-descriptions-item>
          </n-descriptions>
        </div>
      </template>
    </n-spin>

    <!-- 加入 Modal -->
    <n-modal v-model:show="showJoinModal" preset="card" style="width: 500px">
      <template #header>
        <span class="text-farm-800">加入團購 - {{ gb?.productName }}</span>
      </template>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
        <n-form-item label="購買數量" path="quantity">
          <n-input-number v-model:value="form.quantity" :min="1" :max="gb?.remainingQuantity || 999" />
          <span class="text-sm text-gray-500 ml-3">
            小計 <strong class="text-farm">NT$ {{ Number(subtotal).toLocaleString() }}</strong>
          </span>
        </n-form-item>
        <n-form-item label="收件人姓名" path="recipientName">
          <n-input v-model:value="form.recipientName" maxlength="50" />
        </n-form-item>
        <n-form-item label="收件人電話" path="recipientPhone">
          <n-input v-model:value="form.recipientPhone" placeholder="0912-345-678" />
        </n-form-item>
        <div class="grid grid-cols-3 gap-2">
          <n-form-item label="郵遞區號" path="shippingZipcode">
            <n-input v-model:value="form.shippingZipcode" placeholder="320" maxlength="6" />
          </n-form-item>
          <n-form-item label="縣市" path="shippingCity">
            <n-input v-model:value="form.shippingCity" placeholder="桃園市" maxlength="20" />
          </n-form-item>
          <n-form-item label="鄉鎮區" path="shippingDist">
            <n-input v-model:value="form.shippingDist" placeholder="楊梅區" maxlength="20" />
          </n-form-item>
        </div>
        <n-form-item label="詳細地址" path="shippingDetail">
          <n-input v-model:value="form.shippingDetail" placeholder="街/路/巷弄/號" maxlength="200" />
        </n-form-item>
        <n-form-item label="付款方式" path="paymentMethod">
          <n-radio-group v-model:value="form.paymentMethod">
            <n-space vertical>
              <n-radio value="CREDIT_CARD_SIM">信用卡（模擬，立即扣款）</n-radio>
              <n-radio value="BANK_TRANSFER_SIM">ATM 轉帳（模擬）</n-radio>
              <n-radio value="CASH_ON_DELIVERY">貨到付款</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="備註(選填)">
          <n-input v-model:value="form.note" type="textarea" :autosize="{ minRows: 2, maxRows: 3 }" maxlength="500" />
        </n-form-item>
        <div class="bg-amber-50 border border-amber-200 rounded p-3 text-sm text-amber-800 mb-2">
          加入即視為完成付款（demo 模擬，不會實際扣款）。<br>
          團購未達標時將自動退款，退出也會自動退款。
        </div>
      </n-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <n-button @click="showJoinModal = false">取消</n-button>
          <n-button type="primary" :loading="acting" @click="submitJoin">確認加入並付款</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>
