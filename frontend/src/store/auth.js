import { defineStore } from 'pinia'
import { refreshToken } from '../api'

const STORAGE_KEY = 'kuro-ims-auth'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw
      ? JSON.parse(raw)
      : {
          accessToken: '',
          refreshToken: '',
          expiresIn: 0,
          user: null
        }
  },
  getters: {
    isLogin: (state) => !!state.accessToken
  },
  actions: {
    setAuth(data) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.expiresIn = data.expiresIn
      this.user = data.user
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        expiresIn: this.expiresIn,
        user: this.user
      }))
    },
    clear() {
      this.accessToken = ''
      this.refreshToken = ''
      this.expiresIn = 0
      this.user = null
      localStorage.removeItem(STORAGE_KEY)
    },
    async refresh() {
      if (!this.refreshToken) throw new Error('no refresh token')
      const res = await refreshToken({ refreshToken: this.refreshToken })
      this.setAuth(res.data)
    }
  }
})
