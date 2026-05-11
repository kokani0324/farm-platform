<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  NCard, NForm, NFormItem, NInput, NButton, NIcon, NAlert, useMessage,
} from 'naive-ui'
import {
  StorefrontOutline, LeafOutline, CashOutline, PeopleOutline, SparklesOutline,
  ArrowDownOutline, MailOutline,
} from '@vicons/ionicons5'

const router = useRouter()
const auth = useAuthStore()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)
const model = ref({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  phone: '',
})

const rules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: 'Email 格式錯誤', trigger: ['blur', 'input'] },
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, max: 50, message: '密碼長度需 6-50 字元', trigger: ['blur', 'input'] },
  ],
  confirmPassword: [
    { required: true, message: '請再次輸入密碼', trigger: 'blur' },
    {
      validator: (_, value) => value === model.value.password,
      message: '兩次密碼不一致',
      trigger: ['blur', 'input'],
    },
  ],
  name: [
    { required: true, message: '請輸入姓名 / 農場名', trigger: 'blur' },
  ],
}

function scrollToForm() {
  document.getElementById('farmer-form')?.scrollIntoView({ behavior: 'smooth' })
}

async function onSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await auth.register({
      email: model.value.email,
      password: model.value.password,
      name: model.value.name,
      phone: model.value.phone || null,
      role: 'FARMER',
    })
    message.warning('小農帳號已建立,待管理員審核啟用後即可上架商品')
    router.push('/')
  } catch (err) {
    const msg = err.response?.data?.message || '註冊失敗,請稍後再試'
    const fieldErrors = err.response?.data?.errors
    if (fieldErrors) {
      message.error(Object.values(fieldErrors).join(';'))
    } else {
      message.error(msg)
    }
  } finally {
    loading.value = false
  }
}

const benefits = [
  {
    icon: CashOutline,
    title: '直接觸及消費者',
    desc: '跳過層層中盤,把您的好物直接送到關心食材來源的消費者手上,守住合理價格。',
  },
  {
    icon: SparklesOutline,
    title: '說出你的故事',
    desc: '每一筆商品都有專屬的產地、栽種方式介紹,讓消費者認識您的用心,建立品牌信任。',
  },
  {
    icon: PeopleOutline,
    title: '社群與支持',
    desc: '我們有審核制度與小農社群,平台會協助行銷曝光,你不是一個人在賣東西。',
  },
]

const steps = [
  { no: '01', title: '填寫申請表', desc: '送出 Email、姓名、農場名稱即可,3 分鐘搞定。' },
  { no: '02', title: '管理員審核', desc: '我們會聯繫您確認生產背景,審核期 1-3 個工作天。' },
  { no: '03', title: '開始上架', desc: '審核通過後即可登入小農後台,新增商品、處理訂單。' },
]
</script>

<template>
  <div class="bg-gradient-to-b from-farm-50 to-white">
    <!-- Hero -->
    <section class="max-w-5xl mx-auto px-6 pt-16 pb-12 text-center">
      <div class="inline-flex items-center gap-2 px-4 py-1 bg-farm-100 text-farm-800 rounded-full text-sm font-medium mb-6">
        <n-icon :component="StorefrontOutline" />
        小農招募中
      </div>
      <h1 class="text-4xl md:text-5xl font-bold text-farm-900 leading-tight mb-4">
        把您的好物<br class="md:hidden">,送上每一張餐桌
      </h1>
      <p class="text-lg text-gray-600 mb-8 max-w-2xl mx-auto">
        加入「你儂我農」,讓真心耕種的食材,被真心吃飯的人吃到。<br>
        我們協助您直接面對消費者,說出產地的故事。
      </p>
      <div class="flex flex-wrap items-center justify-center gap-3">
        <n-button type="primary" size="large" @click="scrollToForm">
          <template #icon><n-icon :component="LeafOutline" /></template>
          立即申請成為小農
        </n-button>
        <n-button size="large" @click="scrollToForm">
          <template #icon><n-icon :component="ArrowDownOutline" /></template>
          先看看怎麼加入
        </n-button>
      </div>
    </section>

    <!-- 三大特色 -->
    <section class="max-w-5xl mx-auto px-6 py-12">
      <h2 class="text-2xl font-bold text-farm-800 text-center mb-10">為什麼選擇你儂我農</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div
          v-for="b in benefits"
          :key="b.title"
          class="bg-white border border-farm-100 rounded-xl p-6 shadow-sm hover:shadow-md transition"
        >
          <div class="w-12 h-12 rounded-full bg-farm-100 flex items-center justify-center mb-4">
            <n-icon :component="b.icon" size="26" color="#4a7c2a" />
          </div>
          <h3 class="font-bold text-lg text-farm-900 mb-2">{{ b.title }}</h3>
          <p class="text-sm text-gray-600 leading-relaxed">{{ b.desc }}</p>
        </div>
      </div>
    </section>

    <!-- 加入流程 -->
    <section class="bg-farm-50/60 border-y border-farm-100 py-12">
      <div class="max-w-5xl mx-auto px-6">
        <h2 class="text-2xl font-bold text-farm-800 text-center mb-10">三步驟加入</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="s in steps"
            :key="s.no"
            class="bg-white rounded-xl p-6 border border-farm-100"
          >
            <div class="text-4xl font-bold text-farm/20 mb-2">{{ s.no }}</div>
            <h3 class="font-bold text-lg text-farm-900 mb-2">{{ s.title }}</h3>
            <p class="text-sm text-gray-600 leading-relaxed">{{ s.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 註冊表單 -->
    <section id="farmer-form" class="max-w-2xl mx-auto px-6 py-16">
      <div class="text-center mb-8">
        <h2 class="text-3xl font-bold text-farm-800 mb-2">小農申請表</h2>
        <p class="text-sm text-gray-500">送出後我們會在 1-3 個工作天內聯繫您</p>
      </div>

      <n-card :bordered="false" class="shadow-lg">
        <n-alert type="info" :show-icon="false" class="mb-5 text-sm">
          送出申請後,系統會同時為您建立「消費者」身份(可在右上角下拉切換),
          這樣您也能瀏覽其他小農的商品,不需另外註冊。
        </n-alert>

        <n-form ref="formRef" :model="model" :rules="rules" label-placement="top">
          <n-form-item label="姓名 / 農場名稱" path="name">
            <n-input v-model:value="model.name" placeholder="例:阿美姐田園" />
          </n-form-item>

          <n-form-item label="Email" path="email">
            <n-input v-model:value="model.email" placeholder="example@mail.com">
              <template #prefix><n-icon :component="MailOutline" /></template>
            </n-input>
          </n-form-item>

          <n-form-item label="聯絡手機(選填)" path="phone">
            <n-input v-model:value="model.phone" placeholder="0912345678" />
          </n-form-item>

          <n-form-item label="密碼" path="password">
            <n-input
              v-model:value="model.password"
              type="password"
              show-password-on="click"
              placeholder="6-50 字元"
            />
          </n-form-item>

          <n-form-item label="確認密碼" path="confirmPassword">
            <n-input
              v-model:value="model.confirmPassword"
              type="password"
              show-password-on="click"
              placeholder="再次輸入密碼"
            />
          </n-form-item>

          <n-button type="primary" block size="large" :loading="loading" @click="onSubmit">
            <template #icon><n-icon :component="StorefrontOutline" /></template>
            送出申請
          </n-button>
        </n-form>

        <div class="text-center text-sm text-gray-500 mt-5">
          已經有小農帳號?
          <RouterLink :to="{ name: 'login' }" class="text-farm font-medium hover:underline">
            前往登入
          </RouterLink>
        </div>
      </n-card>
    </section>
  </div>
</template>
