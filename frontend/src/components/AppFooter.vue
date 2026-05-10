<script setup>
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { LeafOutline, StorefrontOutline, ChevronForwardOutline } from '@vicons/ionicons5'

const router = useRouter()
const year = new Date().getFullYear()

function goBecomeFarmer() {
  router.push({ name: 'become-farmer' })
}
</script>

<template>
  <footer class="bg-farm-900 text-farm-100 mt-16">
    <!-- 招募小農區塊 -->
    <div class="border-b border-farm-700">
      <div class="max-w-6xl mx-auto px-6 py-10">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <button
            class="text-left bg-farm-800/60 hover:bg-farm-700/70 border border-farm-700 rounded-lg p-5 transition group"
            @click="goBecomeFarmer"
          >
            <div class="flex items-center gap-3 mb-2">
              <n-icon :component="StorefrontOutline" size="28" color="#c89b3c" />
              <span class="font-bold text-lg">成為小農</span>
              <n-icon :component="ChevronForwardOutline" class="ml-auto opacity-60 group-hover:translate-x-1 transition" />
            </div>
            <p class="text-sm text-farm-200">
              招募有理念的農夫、漁夫加入,把您的好物直送餐桌。
            </p>
          </button>

          <div class="bg-farm-800/40 border border-farm-700 rounded-lg p-5">
            <div class="flex items-center gap-3 mb-2">
              <n-icon :component="LeafOutline" size="28" color="#a8c96b" />
              <span class="font-bold text-lg">關於我們</span>
            </div>
            <p class="text-sm text-farm-200">
              連結小農與餐桌的友善平台,讓每一份食材都有來源故事。
            </p>
          </div>

          <div class="bg-farm-800/40 border border-farm-700 rounded-lg p-5">
            <div class="font-bold text-lg mb-2">聯絡資訊</div>
            <p class="text-sm text-farm-200">
              CFA104 第二組 · 我家門前有塊地<br>
              service@nong.example
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 版權 -->
    <div class="max-w-6xl mx-auto px-6 py-6 flex flex-col md:flex-row justify-between items-center gap-3">
      <div>
        <p class="font-bold">你儂我農</p>
        <p class="text-xs text-farm-300 mt-1">連結小農與餐桌的友善平台</p>
      </div>
      <p class="text-xs text-farm-300">© {{ year }} CFA104 第二組 · 我家門前有塊地</p>
    </div>
  </footer>
</template>
