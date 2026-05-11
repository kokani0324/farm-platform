import http from './http'

/* ===== 公開 ===== */
export const listBlogTypes = () => http.get('/api/blogs/types')
export const listBlogs = (params = {}) => http.get('/api/blogs', { params })
export const getBlog = (id) => http.get(`/api/blogs/${id}`)
export const listBlogComments = (id, params = {}) => http.get(`/api/blogs/${id}/comments`, { params })

/* ===== 寫作 ===== */
export const createBlog = (payload) => http.post('/api/blogs', payload)
export const updateBlog = (id, payload) => http.put(`/api/blogs/${id}`, payload)
export const deleteBlog = (id) => http.delete(`/api/blogs/${id}`)
export const listMyBlogs = (params = {}) => http.get('/api/blogs/mine', { params })

/* ===== 互動 ===== */
export const likeBlog = (id) => http.post(`/api/blogs/${id}/like`)
export const addBlogComment = (id, payload) => http.post(`/api/blogs/${id}/comments`, payload)
export const deleteBlogComment = (commentId) => http.delete(`/api/blogs/comments/${commentId}`)
export const reportBlog = (id, payload) => http.post(`/api/blogs/${id}/report`, payload)
export const reportBlogComment = (commentId, payload) =>
  http.post(`/api/blogs/comments/${commentId}/report`, payload)
