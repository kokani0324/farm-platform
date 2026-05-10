import http from './http'

/* ====== 公開瀏覽 ====== */
export const listOpenGroupBuys = (params = {}) => http.get('/api/group-buys', { params })
export const getGroupBuy = (id) => http.get(`/api/group-buys/${id}`)

/* ====== 消費者:發起 / 撤回 / 我發起的 ====== */
export const createGroupBuyRequest = (payload) =>
  http.post('/api/group-buys/requests', payload)
export const listMyGroupBuyRequests = (params = {}) =>
  http.get('/api/group-buys/requests/mine', { params })
export const withdrawGroupBuyRequest = (id) =>
  http.post(`/api/group-buys/requests/${id}/withdraw`)

/* ====== 消費者:加入 / 退出 / 我參加的 ====== */
export const joinGroupBuy = (id, payload) =>
  http.post(`/api/group-buys/${id}/join`, payload)
export const withdrawGroupBuy = (id) =>
  http.post(`/api/group-buys/${id}/withdraw`)
export const listMyParticipations = (params = {}) =>
  http.get('/api/group-buys/participations/mine', { params })

/* ====== 小農:審核 / 我的團購 ====== */
export const listFarmerGroupBuyRequests = (params = {}) =>
  http.get('/api/farmer/group-buy-requests', { params })
export const reviewGroupBuyRequest = (id, payload) =>
  http.post(`/api/farmer/group-buy-requests/${id}/review`, payload)
export const listFarmerGroupBuys = (params = {}) =>
  http.get('/api/farmer/group-buys', { params })
