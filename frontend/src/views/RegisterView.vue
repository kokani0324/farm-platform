<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  NCard, NForm, NFormItem, NInput, NButton, NIcon, useMessage,
} from 'naive-ui'
import { LeafOutline } from '@vicons/ionicons5'

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
    { required: true, message: '請輸入姓名', trigger: 'blur' },
  ],
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
      role: 'CONSUMER',
    })
    message.success(`註冊成功,歡迎加入你儂我農,${auth.user?.name}!`)
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
</script>

<template>
  <n-card class="w-full max-w-md shadow-lg" :bordered="false">
    <div class="text-center mb-6">
      <n-icon :component="LeafOutline" size="48" color="#4a7c2a" />
      <h1 class="text-2xl font-bold mt-2 text-farm-800">註冊會員</h1>
      <p class="text-sm text-gray-500 mt-1">加入你儂我農,連結產地與餐桌</p>
    </div>

    <n-form ref="formRef" :model="model" :rules="rules" label-placement="top">
      <n-form-item label="姓名" path="name">
        <n-input v-model:value="model.name" placeholder="您的姓名" />
      </n-form-item>

      <n-form-item label="Email" path="email">
        <n-input v-model:value="model.email" placeholder="example@mail.com" />
      </n-form-item>

      <n-form-item label="手機(選填)" path="phone">
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

      <n-button type="primary" block :loading="loading" size="large" @click="onSubmit">
        建立帳號
      </n-button>
    </n-form>

    <div class="text-center text-sm text-gray-500 mt-5 space-y-2">
      <div>
        已經有帳號?
        <RouterLink :to="{ name: 'login' }" class="text-farm font-medium hover:underline">
          前往登入
        </RouterLink>
      </div>
      <div class="pt-2 border-t border-gray-100">
        想成為小農賣自家好物?
        <RouterLink :to="{ name: 'become-farmer' }" class="text-farm-700 font-medium hover:underline">
          前往小農申請頁
        </RouterLink>
      </div>
    </div>
  </n-card>
</template>
