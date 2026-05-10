<script setup>
import { computed, h, onMounted, watch } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { NButton, NDropdown, NIcon, NBadge, NTag, useMessage } from 'naive-ui'
import {
  LeafOutline, PersonCircleOutline, LogOutOutline,
  StorefrontOutline, BasketOutline, CartOutline, ReceiptOutline,
  SwapHorizontalOutline, FlashOutline, PeopleOutline, CheckmarkCircleOutline,
  CompassOutline, CalendarOutline, NewspaperOutline, SettingsOutline,
  CreateOutline, AlertCircleOutline,
} from '@vicons/ionicons5'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const cart = useCartStore()
const message = useMessage()

const userOptions = computed(() => {
  const items = [
    { label: '會員中心', key: 'profile', icon: () => h(NIcon, null, { default: () => h(PersonCircleOutline) }) },
  ]
  // 消費者前台才有「我的訂單」(消費者買的訂單)
  if (auth.isConsumer) {
    items.push({ label: '我的訂單', key: 'orders', icon: () => h(NIcon, null, { default: () => h(ReceiptOutline) }) })
    items.push({ label: '我發起的團購', key: 'my-group-buy-requests', icon: () => h(NIcon, null, { default: () => h(FlashOutline) }) })
    items.push({ label: '我參加的團購', key: 'my-group-buys', icon: () => h(NIcon, null, { default: () => h(PeopleOutline) }) })
    items.push({ label: '我的體驗預約', key: 'my-farm-trip-bookings', icon: () => h(NIcon, null, { default: () => h(CalendarOutline) }) })
  }
  if (auth.isFarmer) {
    items.push({ label: '我的商品', key: 'farmer-products', icon: () => h(NIcon, null, { default: () => h(StorefrontOutline) }) })
    items.push({ label: '小農訂單', key: 'farmer-orders', icon: () => h(NIcon, null, { default: () => h(ReceiptOutline) }) })
    items.push({ label: '團購審核', key: 'farmer-group-buy-requests', icon: () => h(NIcon, null, { default: () => h(CheckmarkCircleOutline) }) })
    items.push({ label: '我的團購活動', key: 'farmer-group-buys', icon: () => h(NIcon, null, { default: () => h(FlashOutline) }) })
    items.push({ label: '我的體驗活動', key: 'farmer-farm-trips', icon: () => h(NIcon, null, { default: () => h(CompassOutline) }) })
    items.push({ label: '體驗預約管理', key: 'farmer-farm-trip-bookings', icon: () => h(NIcon, null, { default: () => h(CalendarOutline) }) })
  }
  // 所有登入會員都能寫部落格
  if (auth.isLoggedIn && !auth.isAdmin) {
    items.push({ label: '我的文章', key: 'my-blogs', icon: () => h(NIcon, null, { default: () => h(CreateOutline) }) })
  }
  // 後台
  if (auth.isAdmin) {
    items.push({ type: 'divider', key: 'd-admin' })
    items.push({ label: '後台總覽', key: 'admin-dashboard', icon: () => h(NIcon, null, { default: () => h(SettingsOutline) }) })
    items.push({ label: '會員管理', key: 'admin-users', icon: () => h(NIcon, null, { default: () => h(PeopleOutline) }) })
    items.push({ label: '商品管理', key: 'admin-products', icon: () => h(NIcon, null, { default: () => h(BasketOutline) }) })
    items.push({ label: '檢舉處理', key: 'admin-blog-reports', icon: () => h(NIcon, null, { default: () => h(AlertCircleOutline) }) })
  }
  // 多身份帳號才顯示切換按鈕
  if (auth.hasMultiRoles) {
    items.push({ type: 'divider', key: 'd-switch' })
    if (auth.canBeConsumer && !auth.isConsumer) {
      items.push({ label: '切換為消費者', key: 'switch-consumer', icon: () => h(NIcon, null, { default: () => h(SwapHorizontalOutline) }) })
    }
    if (auth.canBeFarmer && !auth.isFarmer) {
      items.push({ label: '切換為小農', key: 'switch-farmer', icon: () => h(NIcon, null, { default: () => h(SwapHorizontalOutline) }) })
    }
  }
  items.push({ type: 'divider', key: 'd1' })
  items.push({ label: '登出', key: 'logout', icon: () => h(NIcon, null, { default: () => h(LogOutOutline) }) })
  return items
})

async function handleSelect(key) {
  if (key === 'logout') {
    auth.logout()
    message.success('已登出')
    router.push({ name: 'home' })
  } else if (key === 'profile') {
    router.push({ name: 'profile' })
  } else if (key === 'orders') {
    router.push({ name: 'orders' })
  } else if (key === 'farmer-products') {
    router.push({ name: 'farmer-products' })
  } else if (key === 'farmer-orders') {
    router.push({ name: 'farmer-orders' })
  } else if (key === 'my-group-buy-requests') {
    router.push({ name: 'my-group-buy-requests' })
  } else if (key === 'my-group-buys') {
    router.push({ name: 'my-group-buys' })
  } else if (key === 'my-farm-trip-bookings') {
    router.push({ name: 'my-farm-trip-bookings' })
  } else if (key === 'farmer-group-buy-requests') {
    router.push({ name: 'farmer-group-buy-requests' })
  } else if (key === 'farmer-group-buys') {
    router.push({ name: 'farmer-group-buys' })
  } else if (key === 'farmer-farm-trips') {
    router.push({ name: 'farmer-farm-trips' })
  } else if (key === 'farmer-farm-trip-bookings') {
    router.push({ name: 'farmer-farm-trip-bookings' })
  } else if (key === 'my-blogs') {
    router.push({ name: 'my-blogs' })
  } else if (key === 'admin-dashboard') {
    router.push({ name: 'admin-dashboard' })
  } else if (key === 'admin-users') {
    router.push({ name: 'admin-users' })
  } else if (key === 'admin-products') {
    router.push({ name: 'admin-products' })
  } else if (key === 'admin-blog-reports') {
    router.push({ name: 'admin-blog-reports' })
  } else if (key === 'switch-consumer' || key === 'switch-farmer') {
    const target = key === 'switch-consumer' ? 'CONSUMER' : 'FARMER'
    try {
      await auth.switchRole(target)
      message.success(`已切換為${target === 'FARMER' ? '小農' : '消費者'}前台`)
      router.push({ name: target === 'FARMER' ? 'farmer-products' : 'home' })
    } catch (e) {
      message.error(e.response?.data?.message || '切換失敗')
    }
  }
}

// 登入後自動載入購物車；登入狀態切換時也跟著更新
async function refreshCartIfLoggedIn() {
  if (auth.isLoggedIn && !auth.isFarmer && !auth.isAdmin) {
    try { await cart.fetch() } catch (_) {}
  }
}
onMounted(refreshCartIfLoggedIn)
watch(() => auth.isLoggedIn, refreshCartIfLoggedIn)
watch(() => auth.activeRole, refreshCartIfLoggedIn)

const roleLabel = computed(() => {
  switch (auth.role) {
    case 'FARMER': return '小農'
    case 'CONSUMER': return '消費者'
    case 'ADMIN': return '管理員'
    default: return ''
  }
})

const isActive = (name) => route.name === name
</script>

<template>
  <header class="bg-white border-b border-farm-100 shadow-sm sticky top-0 z-40">
    <div class="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
      <RouterLink :to="{ name: 'home' }" class="flex items-center gap-2 text-farm font-bold text-xl">
        <n-icon size="28" :component="LeafOutline" />
        <span>你儂我農</span>
      </RouterLink>

      <nav class="hidden md:flex items-center gap-1 ml-8 flex-1">
        <RouterLink
          :to="{ name: 'home' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('home') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >首頁</RouterLink>
        <RouterLink
          :to="{ name: 'products' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('products') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >
          <n-icon :component="BasketOutline" class="align-middle" /> 逛商品
        </RouterLink>
        <RouterLink
          :to="{ name: 'group-buys' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('group-buys') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >
          <n-icon :component="FlashOutline" class="align-middle" /> 團購
        </RouterLink>
        <RouterLink
          :to="{ name: 'farm-trips' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('farm-trips') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >
          <n-icon :component="CompassOutline" class="align-middle" /> 體驗活動
        </RouterLink>
        <RouterLink
          :to="{ name: 'blogs' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('blogs') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >
          <n-icon :component="NewspaperOutline" class="align-middle" /> 部落格
        </RouterLink>
        <RouterLink
          v-if="auth.isFarmer"
          :to="{ name: 'farmer-products' }"
          class="px-3 py-2 text-sm rounded transition-colors"
          :class="isActive('farmer-products') ? 'text-farm font-semibold' : 'text-gray-600 hover:text-farm'"
        >
          <n-icon :component="StorefrontOutline" class="align-middle" /> 我的商品
        </RouterLink>
      </nav>

      <div class="flex items-center gap-3">
        <template v-if="auth.isLoggedIn">
          <!-- 購物車（小農/管理員不顯示，因為他們不會購物） -->
          <n-badge
            v-if="!auth.isFarmer && !auth.isAdmin"
            :value="cart.totalQuantity"
            :max="99"
            :show="cart.totalQuantity > 0"
            color="#c89b3c"
          >
            <n-button quaternary circle @click="router.push({ name: 'cart' })">
              <template #icon>
                <n-icon :component="CartOutline" size="22" />
              </template>
            </n-button>
          </n-badge>

          <span class="text-sm text-gray-500 hidden md:inline">
            {{ roleLabel }}・{{ auth.user?.name }}
          </span>
          <n-dropdown :options="userOptions" trigger="hover" @select="handleSelect">
            <n-button quaternary circle>
              <template #icon>
                <n-icon :component="PersonCircleOutline" size="22" />
              </template>
            </n-button>
          </n-dropdown>
        </template>
        <template v-else>
          <n-button quaternary @click="router.push({ name: 'login' })">登入</n-button>
          <n-button type="primary" @click="router.push({ name: 'register' })">註冊</n-button>
        </template>
      </div>
    </div>
  </header>
</template>
