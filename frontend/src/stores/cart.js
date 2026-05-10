import { defineStore } from 'pinia'
import * as cartApi from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    totalQuantity: 0,
    totalAmount: 0,
    loading: false,
    loaded: false,
  }),

  getters: {
    /** 依小農分組（結帳頁顯示用） */
    groupedByFarmer: (state) => {
      const map = new Map()
      for (const it of state.items) {
        if (!map.has(it.farmerId)) {
          map.set(it.farmerId, { farmerId: it.farmerId, farmerName: it.farmerName, items: [], subtotal: 0 })
        }
        const g = map.get(it.farmerId)
        g.items.push(it)
        if (it.available) g.subtotal += Number(it.subtotal)
      }
      return Array.from(map.values())
    },
    isEmpty: (state) => state.items.length === 0,
    hasUnavailable: (state) => state.items.some((it) => !it.available),
  },

  actions: {
    apply(data) {
      this.items = data.items || []
      this.totalQuantity = data.totalQuantity || 0
      this.totalAmount = Number(data.totalAmount || 0)
      this.loaded = true
    },

    async fetch() {
      this.loading = true
      try {
        const { data } = await cartApi.getCart()
        this.apply(data)
      } finally {
        this.loading = false
      }
    },

    async add(productId, quantity = 1) {
      const { data } = await cartApi.addCartItem(productId, quantity)
      this.apply(data)
    },

    async update(productId, quantity) {
      const { data } = await cartApi.updateCartItem(productId, quantity)
      this.apply(data)
    },

    async remove(productId) {
      const { data } = await cartApi.removeCartItem(productId)
      this.apply(data)
    },

    async clear() {
      await cartApi.clearCart()
      this.items = []
      this.totalQuantity = 0
      this.totalAmount = 0
    },

    reset() {
      this.items = []
      this.totalQuantity = 0
      this.totalAmount = 0
      this.loaded = false
    },
  },
})
