import http from './http'

export const getAdminStats = () => http.get('/api/admin/stats')

/* Users */
export const listAdminUsers = (params = {}) => http.get('/api/admin/users', { params })
export const enableAdminUser = (id) => http.post(`/api/admin/users/${id}/enable`)
export const disableAdminUser = (id) => http.post(`/api/admin/users/${id}/disable`)

/* Products */
export const listAdminProducts = (params = {}) => http.get('/api/admin/products', { params })
export const takeDownProduct = (id) => http.post(`/api/admin/products/${id}/take-down`)
export const restoreProduct = (id) => http.post(`/api/admin/products/${id}/restore`)

/* Blog reports - phase 6 後段填 */
export const listBlogReports = (params = {}) => http.get('/api/admin/blog-reports', { params })
export const handleBlogReport = (id, payload) => http.post(`/api/admin/blog-reports/${id}/handle`, payload)
export const listBlogCommentReports = (params = {}) => http.get('/api/admin/blog-comment-reports', { params })
export const handleBlogCommentReport = (id, payload) => http.post(`/api/admin/blog-comment-reports/${id}/handle`, payload)
