import http from './http'

/* ========== 公開 ========== */

/** 取得分類列表 */
export const listCategories = () => http.get('/api/public/categories')

/**
 * 商品列表（分頁 + 過濾）
 * @param {{page?:number, size?:number, sort?:string, categoryId?:number, keyword?:string}} params
 */
export const listProducts = (params = {}) =>
  http.get('/api/public/products', { params })

/** 商品詳情 */
export const getProduct = (id) => http.get(`/api/public/products/${id}`)

/* ========== 小農 ========== */

export const listMyProducts = () => http.get('/api/farmer/products')
export const createProduct = (payload) => http.post('/api/farmer/products', payload)
export const updateProduct = (id, payload) => http.put(`/api/farmer/products/${id}`, payload)
export const deleteProduct = (id) => http.delete(`/api/farmer/products/${id}`)
export const toggleProductStatus = (id) => http.patch(`/api/farmer/products/${id}/toggle-status`)
