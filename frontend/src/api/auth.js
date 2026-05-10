import http from './http'

/**
 * 註冊
 * @param {{email:string, password:string, name:string, phone?:string, role:'CONSUMER'|'FARMER'}} payload
 */
export const register = (payload) => http.post('/api/auth/register', payload)

/**
 * 登入
 * @param {{email:string, password:string}} payload
 */
export const login = (payload) => http.post('/api/auth/login', payload)
