import http from './http'

export const checkout = (payload) => http.post('/api/orders/checkout', payload)

export const listMyOrders = (params = {}) => http.get('/api/orders', { params })

export const listFarmerOrders = (params = {}) => http.get('/api/orders/farmer', { params })

export const getOrder = (id) => http.get(`/api/orders/${id}`)

export const payOrder = (id) => http.post(`/api/orders/${id}/pay`)

export const cancelOrder = (id) => http.post(`/api/orders/${id}/cancel`)

export const shipOrder = (id) => http.post(`/api/orders/${id}/ship`)

export const confirmReceipt = (id) => http.post(`/api/orders/${id}/confirm`)
