<script setup>
import { useRouter } from 'vue-router'
import { NButton } from 'naive-ui'
const router = useRouter()
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-farm-50 px-4">
    <h1 class="text-7xl font-bold text-farm mb-4">404</h1>
    <p class="text-lg text-gray-600 mb-6">這片田地還沒長出東西呢～</p>
    <n-button type="primary" @click="router.push('/')">回首頁</n-button>
  </div>
</template>
