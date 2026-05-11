<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { NCard, NTag, NIcon } from 'naive-ui'
import { LeafOutline, LocationOutline } from '@vicons/ionicons5'

const props = defineProps({
  product: { type: Object, required: true },
})

const router = useRouter()

const soldOut = computed(() => props.product.stock === 0 || props.product.status === 'SOLD_OUT')
const formattedPrice = computed(() => `NT$ ${Number(props.product.price).toLocaleString()}`)

function go() {
  router.push({ name: 'product-detail', params: { id: props.product.id } })
}
</script>

<template>
  <n-card
    hoverable
    class="cursor-pointer transition-transform hover:-translate-y-1 overflow-hidden"
    content-style="padding: 0;"
    @click="go"
  >
    <div class="aspect-[4/3] bg-farm-50 relative overflow-hidden">
      <img
        v-if="product.imageUrl"
        :src="product.imageUrl"
        :alt="product.name"
        class="w-full h-full object-cover"
        loading="lazy"
      />
      <div v-else class="w-full h-full flex items-center justify-center">
        <n-icon :component="LeafOutline" size="48" color="#a8c96b" />
      </div>
      <span
        v-if="product.groupBuyEnabled"
        class="absolute top-2 left-2 bg-earth text-white text-xs px-2 py-0.5 rounded"
      >團購</span>
      <div
        v-if="soldOut"
        class="absolute inset-0 bg-black/50 flex items-center justify-center text-white font-bold text-lg"
      >已售完</div>
    </div>
    <div class="p-4">
      <h3 class="font-bold text-base mb-1 line-clamp-1">{{ product.name }}</h3>
      <div class="flex items-center text-xs text-gray-500 gap-1 mb-2">
        <n-icon :component="LocationOutline" size="14" />
        <span>{{ product.origin || product.farmerName }}</span>
      </div>
      <div class="flex items-end justify-between">
        <div>
          <span class="text-farm font-bold text-lg">{{ formattedPrice }}</span>
          <span class="text-xs text-gray-500 ml-1">/{{ product.unit }}</span>
        </div>
        <n-tag size="small" round :bordered="false" type="success">{{ product.categoryName }}</n-tag>
      </div>
    </div>
  </n-card>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
