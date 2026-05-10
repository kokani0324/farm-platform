import http from './http'

export const getCart = () => http.get('/api/cart')

export const addCartItem = (productId, quantity) =>
  http.post('/api/cart/items', { productId, quantity })

export const updateCartItem = (productId, quantity) =>
  http.put(`/api/cart/items/${productId}`, { quantity })

export const removeCartItem = (productId) =>
  http.delete(`/api/cart/items/${productId}`)

export const clearCart = () => http.delete('/api/cart')
