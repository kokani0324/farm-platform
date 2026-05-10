import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'home',
        component: () => import('@/views/HomeView.vue'),
      },
      {
        path: 'products',
        name: 'products',
        component: () => import('@/views/ProductListView.vue'),
      },
      {
        path: 'products/:id',
        name: 'product-detail',
        component: () => import('@/views/ProductDetailView.vue'),
      },
      // 小農招募頁(專屬)
      {
        path: 'become-farmer',
        name: 'become-farmer',
        component: () => import('@/views/BecomeFarmerView.vue'),
      },
      // ===== 小農專區 =====
      {
        path: 'farmer/products',
        name: 'farmer-products',
        component: () => import('@/views/FarmerProductsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/products/new',
        name: 'farmer-product-new',
        component: () => import('@/views/FarmerProductFormView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/products/:id/edit',
        name: 'farmer-product-edit',
        component: () => import('@/views/FarmerProductFormView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/HomeView.vue'),  // Phase 5 補
        meta: { requiresAuth: true },
      },
      // ===== 團購 =====
      {
        path: 'group-buys',
        name: 'group-buys',
        component: () => import('@/views/GroupBuyListView.vue'),
      },
      {
        path: 'group-buys/:id',
        name: 'group-buy-detail',
        component: () => import('@/views/GroupBuyDetailView.vue'),
      },
      {
        path: 'my/group-buy-requests',
        name: 'my-group-buy-requests',
        component: () => import('@/views/MyGroupBuyRequestsView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'my/group-buys',
        name: 'my-group-buys',
        component: () => import('@/views/MyParticipationsView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'farmer/group-buy-requests',
        name: 'farmer-group-buy-requests',
        component: () => import('@/views/FarmerGroupBuyRequestsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/group-buys',
        name: 'farmer-group-buys',
        component: () => import('@/views/FarmerGroupBuysView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      // ===== 體驗活動 =====
      {
        path: 'farm-trips',
        name: 'farm-trips',
        component: () => import('@/views/FarmTripListView.vue'),
      },
      {
        path: 'farm-trips/:id',
        name: 'farm-trip-detail',
        component: () => import('@/views/FarmTripDetailView.vue'),
      },
      {
        path: 'my/farm-trip-bookings',
        name: 'my-farm-trip-bookings',
        component: () => import('@/views/MyFarmTripBookingsView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'farmer/farm-trips',
        name: 'farmer-farm-trips',
        component: () => import('@/views/FarmerFarmTripsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/farm-trips/new',
        name: 'farmer-farm-trip-new',
        component: () => import('@/views/FarmerFarmTripFormView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/farm-trips/:id/edit',
        name: 'farmer-farm-trip-edit',
        component: () => import('@/views/FarmerFarmTripFormView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/farm-trip-bookings',
        name: 'farmer-farm-trip-bookings',
        component: () => import('@/views/FarmerFarmTripBookingsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      // ===== 部落格 =====
      {
        path: 'blogs',
        name: 'blogs',
        component: () => import('@/views/BlogListView.vue'),
      },
      {
        path: 'blogs/:id',
        name: 'blog-detail',
        component: () => import('@/views/BlogDetailView.vue'),
      },
      {
        path: 'blogs/new',
        name: 'blog-new',
        component: () => import('@/views/BlogFormView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'blogs/:id/edit',
        name: 'blog-edit',
        component: () => import('@/views/BlogFormView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'my/blogs',
        name: 'my-blogs',
        component: () => import('@/views/MyBlogsView.vue'),
        meta: { requiresAuth: true },
      },
      // ===== 後台 =====
      {
        path: 'admin',
        name: 'admin-dashboard',
        component: () => import('@/views/AdminDashboardView.vue'),
        meta: { requiresAuth: true, requiresRole: 'ADMIN' },
      },
      {
        path: 'admin/users',
        name: 'admin-users',
        component: () => import('@/views/AdminUsersView.vue'),
        meta: { requiresAuth: true, requiresRole: 'ADMIN' },
      },
      {
        path: 'admin/products',
        name: 'admin-products',
        component: () => import('@/views/AdminProductsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'ADMIN' },
      },
      {
        path: 'admin/blog-reports',
        name: 'admin-blog-reports',
        component: () => import('@/views/AdminBlogReportsView.vue'),
        meta: { requiresAuth: true, requiresRole: 'ADMIN' },
      },
      // ===== 購物車 / 結帳 / 訂單 =====
      {
        path: 'cart',
        name: 'cart',
        component: () => import('@/views/CartView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'checkout',
        name: 'checkout',
        component: () => import('@/views/CheckoutView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrderListView.vue'),
        props: { scope: 'consumer' },
        meta: { requiresAuth: true },
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: () => import('@/views/OrderDetailView.vue'),
        meta: { requiresAuth: true },
      },
      {
        path: 'farmer/orders',
        name: 'farmer-orders',
        component: () => import('@/views/OrderListView.vue'),
        props: { scope: 'farmer' },
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
      {
        path: 'farmer/orders/:id',
        name: 'farmer-order-detail',
        component: () => import('@/views/OrderDetailView.vue'),
        meta: { requiresAuth: true, requiresRole: 'FARMER' },
      },
    ],
  },
  {
    path: '/auth',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      {
        path: '/login',
        name: 'login',
        component: () => import('@/views/LoginView.vue'),
      },
      {
        path: '/register',
        name: 'register',
        component: () => import('@/views/RegisterView.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresRole && auth.role !== to.meta.requiresRole) {
    return { name: 'home' }
  }

  if ((to.name === 'login' || to.name === 'register') && auth.isLoggedIn) {
    return { name: 'home' }
  }
})

export default router
