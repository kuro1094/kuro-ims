import axios from 'axios'
import { useAuthStore } from '../store/auth'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

let refreshing = null

request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    if (res.code === 401) {
      return handle401(response.config)
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  async (error) => {
    if (error.response?.status === 401) {
      return handle401(error.config)
    }
    return Promise.reject(error)
  }
)

async function handle401(config) {
  const auth = useAuthStore()
  if (!auth.refreshToken) {
    auth.clear()
    router.push('/login')
    return Promise.reject(new Error('登录已过期'))
  }
  if (!refreshing) {
    refreshing = auth
      .refresh()
      .catch(() => {
        auth.clear()
        router.push('/login')
      })
      .finally(() => {
        refreshing = null
      })
  }
  await refreshing
  if (!auth.accessToken) return Promise.reject(new Error('登录已过期'))
  config.headers.Authorization = `Bearer ${auth.accessToken}`
  return request(config)
}

export default request
