import { defineStore } from 'pinia'
import * as authApi from '@/api/auth'
import http from '@/api/http'
import { useCartStore } from '@/stores/cart'

const TOKEN_KEY = 'nong_token'
const USER_KEY = 'nong_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    /** 當下啟用的身份(沿用舊欄位名,讓既有程式 auth.role 不需改) */
    role: (state) => state.user?.activeRole || state.user?.role || null,
    activeRole: (state) => state.user?.activeRole || state.user?.role || null,
    roles: (state) => state.user?.roles || [],
    isFarmer: (state) => (state.user?.activeRole || state.user?.role) === 'FARMER',
    isConsumer: (state) => (state.user?.activeRole || state.user?.role) === 'CONSUMER',
    isAdmin: (state) => (state.user?.activeRole || state.user?.role) === 'ADMIN',
    /** 此帳號是否同時擁有兩個以上身份(顯示切換按鈕用) */
    hasMultiRoles: (state) => (state.user?.roles?.length || 0) > 1,
    /** 是否擁有(不論當下是否啟用) */
    canBeFarmer: (state) => (state.user?.roles || []).includes('FARMER'),
    canBeConsumer: (state) => (state.user?.roles || []).includes('CONSUMER'),
  },

  actions: {
    async login(payload) {
      const { data } = await authApi.login(payload)
      this.persist(data)
      return data
    },

    async register(payload) {
      const { data } = await authApi.register(payload)
      this.persist(data)
      return data
    },

    /** 切換身份(會拿到新 token + 新 activeRole) */
    async switchRole(newRole) {
      const { data } = await http.post('/api/account/switch-role', { role: newRole })
      this.persist(data)
      // 切換身份後購物車狀態也要重抓
      try { useCartStore().reset() } catch (_) {}
      return data
    },

    persist(data) {
      this.token = data.token
      this.user = {
        userId: data.userId,
        email: data.email,
        name: data.name,
        // 後端為向後相容仍會回 role,我們以 activeRole 為主
        role: data.role,
        activeRole: data.activeRole || data.role,
        roles: data.roles || (data.role ? [data.role] : []),
      }
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },

    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      try { useCartStore().reset() } catch (_) { /* store 尚未掛載時忽略 */ }
    },
  },
})
