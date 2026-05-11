import http from './http'

/* ====== 公開瀏覽 ====== */
export const listFarmTrips = (params = {}) => http.get('/api/farm-trips', { params })
export const getFarmTrip = (id) => http.get(`/api/farm-trips/${id}`)
export const listFarmTripCategories = () => http.get('/api/farm-trips/categories')

/* ====== 消費者:預約 ====== */
export const bookFarmTrip = (id, payload) => http.post(`/api/farm-trips/${id}/bookings`, payload)
export const cancelFarmTripBooking = (bookingId) => http.post(`/api/farm-trips/bookings/${bookingId}/cancel`)
export const listMyFarmTripBookings = (params = {}) => http.get('/api/farm-trips/bookings/mine', { params })

/* ====== 小農:CRUD ====== */
export const listFarmerFarmTrips = (params = {}) => http.get('/api/farmer/farm-trips', { params })
export const createFarmTrip = (payload) => http.post('/api/farmer/farm-trips', payload)
export const updateFarmTrip = (id, payload) => http.put(`/api/farmer/farm-trips/${id}`, payload)
export const cancelFarmTrip = (id) => http.post(`/api/farmer/farm-trips/${id}/cancel`)
export const listFarmerFarmTripBookings = (params = {}) => http.get('/api/farmer/farm-trip-bookings', { params })
