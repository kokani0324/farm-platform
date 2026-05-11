<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  NCard, NForm, NFormItem, NInput, NButton, NIcon, NModal, NSpace, useMessage,
} from 'naive-ui'
import { LeafOutline, PersonOutline, StorefrontOutline } from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const message = useMessage()

const formRef = ref(null)
const loading = ref(false)
const model = ref({ email: '', password: '' })

const showRolePicker = ref(false)
const switching = ref(false)

const rules = {
  email: [
    { required: true, message: '請輸入 Email', trigger: 'blur' },
    { type: 'email', message: 'Email 格式錯誤', trigger: ['blur', 'input'] },
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
  ],
}

function goAfterLogin() {
  const redirect = route.query.redirect
  if (redirect) {
    router.push(redirect)
  } else if (auth.isFarmer) {
    router.push({ name: 'farmer-products' })
  } else {
    router.push({ name: 'home' })
  }
}

async function onSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await auth.login({ email: model.value.email, password: model.value.password })

    // 多身份帳號 → 跳出選擇器讓使用者挑要進入的前台
    if (auth.hasMultiRoles) {
      showRolePicker.value = true
    } else {
      message.success(`歡迎回來,${auth.user?.name}!`)
      goAfterLogin()
    }
  } catch (err) {
    const msg = err.response?.data?.message || '登入失敗,請檢查帳號或密碼'
    message.error(msg)
  } finally {
    loading.value = false
  }
}

async function pickRole(role) {
  switching.value = true
  try {
    if (role !== auth.activeRole) {
      await auth.switchRole(role)
    }
    showRolePicker.value = false
    message.success(`歡迎回來,${auth.user?.name}!`)
    goAfterLogin()
  } catch (err) {
    message.error(err.response?.data?.message || '切換身份失敗')
  } finally {
    switching.value = false
  }
}
</script>

<template>
  <n-card class="w-full max-w-md shadow-lg" :bordered="false">
    <div class="text-center mb-6">
      <n-icon :component="LeafOutline" size="48" color="#4a7c2a" />
      <h1 class="text-2xl font-bold mt-2 text-farm-800">會員登入</h1>
      <p class="text-sm text-gray-500 mt-1">歡迎回到你儂我農</p>
    </div>

    <n-form ref="formRef" :model="model" :rules="rules" label-placement="top" size="large">
      <n-form-item label="Email" path="email">
        <n-input v-model:value="model.email" placeholder="example@mail.com" @keyup.enter="onSubmit" />
      </n-form-item>
      <n-form-item label="密碼" path="password">
        <n-input
          v-model:value="model.password"
          type="password"
          show-password-on="click"
          placeholder="請輸入密碼"
          @keyup.enter="onSubmit"
        />
      </n-form-item>

      <n-button type="primary" block :loading="loading" size="large" @click="onSubmit">
        登入
      </n-button>
    </n-form>

    <div class="text-center text-sm text-gray-500 mt-5">
      還沒有帳號?
      <RouterLink :to="{ name: 'register' }" class="text-farm font-medium hover:underline">
        立即註冊
      </RouterLink>
    </div>
  </n-card>

  <!-- 多身份選擇器 -->
  <n-modal v-model:show="showRolePicker" :mask-closable="false" :close-on-esc="false" preset="card" style="width: 380px">
    <template #header>
      <span class="text-farm-800">選擇要進入的前台</span>
    </template>

    <p class="text-sm text-gray-500 mb-4">
      您的帳號同時擁有消費者與小農身份,請選擇此次登入要進入哪一個前台。之後也可在右上角下拉切換。
    </p>

    <n-space vertical size="medium">
      <n-button
        v-if="auth.canBeConsumer"
        size="large"
        block
        :loading="switching && auth.activeRole !== 'CONSUMER'"
        @click="pickRole('CONSUMER')"
      >
        <template #icon><n-icon :component="PersonOutline" /></template>
        以消費者身份進入
      </n-button>
      <n-button
        v-if="auth.canBeFarmer"
        size="large"
        type="primary"
        block
        :loading="switching && auth.activeRole !== 'FARMER'"
        @click="pickRole('FARMER')"
      >
        <template #icon><n-icon :component="StorefrontOutline" /></template>
        以小農身份進入
      </n-button>
    </n-space>
  </n-modal>
</template>
