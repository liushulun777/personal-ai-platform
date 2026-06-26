import axios from 'axios'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// Request interceptor — inject token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('blog-token')
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('blog-token')
      // Don't redirect automatically on blog — just clear token
    }
    return Promise.reject(error)
  }
)

export default service
