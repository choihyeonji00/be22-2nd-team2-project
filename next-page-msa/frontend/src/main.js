import { createApp } from 'vue'
import { createPinia } from 'pinia'
import axios from 'axios'

import App from './App.vue'
import router from './router'

import './assets/css/main.css'

const app = createApp(App)

// Axios Global Defaults
axios.defaults.baseURL = '/api' // Proxy via Vite
axios.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

// Global Error Handler for 401 - Auto refresh token
let isRefreshing = false
let refreshSubscribers = []

const subscribeTokenRefresh = (callback) => {
    refreshSubscribers.push(callback)
}

const onTokenRefreshed = (newToken) => {
    refreshSubscribers.forEach(callback => callback(newToken))
    refreshSubscribers = []
}

axios.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // /auth/refresh 요청 자체에서 401이 발생하면 로그아웃
            if (originalRequest.url === '/auth/refresh') {
                localStorage.removeItem('accessToken')
                localStorage.removeItem('refreshToken')
                localStorage.removeItem('autoLoginEnabled')
                window.location.href = '/'
                return Promise.reject(error)
            }

            const refreshToken = localStorage.getItem('refreshToken')

            // refreshToken이 없으면 로그아웃
            if (!refreshToken) {
                localStorage.removeItem('accessToken')
                window.location.href = '/'
                return Promise.reject(error)
            }

            // 이미 갱신 중이면 대기
            if (isRefreshing) {
                return new Promise(resolve => {
                    subscribeTokenRefresh(newToken => {
                        originalRequest.headers.Authorization = `Bearer ${newToken}`
                        resolve(axios(originalRequest))
                    })
                })
            }

            originalRequest._retry = true
            isRefreshing = true

            try {
                const response = await axios.post('/auth/refresh', { refreshToken })
                const { accessToken: newAccess, refreshToken: newRefresh } = response.data.data

                localStorage.setItem('accessToken', newAccess)
                localStorage.setItem('refreshToken', newRefresh)

                axios.defaults.headers.common['Authorization'] = `Bearer ${newAccess}`
                originalRequest.headers.Authorization = `Bearer ${newAccess}`

                onTokenRefreshed(newAccess)
                isRefreshing = false

                return axios(originalRequest)
            } catch (refreshError) {
                isRefreshing = false
                localStorage.removeItem('accessToken')
                localStorage.removeItem('refreshToken')
                localStorage.removeItem('autoLoginEnabled')
                window.location.href = '/'
                return Promise.reject(refreshError)
            }
        }
        return Promise.reject(error)
    }
)

app.use(createPinia())
app.use(router)

app.mount('#app')
