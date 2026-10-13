<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { login, register } from '../api'
import { t, locale, toggleLocale } from '../i18n'

const router = useRouter()
const auth = useAuthStore()

const mode = ref('login')
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = t('login.errFill')
    return
  }
  if (mode.value === 'register' && form.password !== form.confirmPassword) {
    error.value = t('login.errPwdMismatch')
    return
  }
  loading.value = true
  try {
    const res = mode.value === 'login'
      ? await login({ username: form.username, password: form.password })
      : await register(form)
    auth.setAuth(res.data)
    router.push('/')
  } catch (e) {
    error.value = e?.response?.data?.message || e.message || t('login.errFailed')
  } finally {
    loading.value = false
  }
}

function toggleMode() {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  error.value = ''
}
</script>

<template>
  <div class="login-page">
    <div class="login-bg"></div>
    <div class="login-card glass-deep fade-up">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none">
            <path d="M3 21V8l7-5 7 5v13" stroke="currentColor" stroke-width="1.5"/>
            <path d="M9 21v-6h6v6" stroke="currentColor" stroke-width="1.5"/>
            <path d="M17 12l4-3v12h-4" stroke="currentColor" stroke-width="1.5"/>
          </svg>
        </div>
        <h1>Kuro IMS</h1>
        <p>{{ t('login.sub') }}</p>
      </div>

      <button class="btn lang-switch" @click="toggleLocale()">{{ locale === 'zh' ? 'EN' : '中文' }}</button>

      <div class="tabs">
        <button :class="['tab', { active: mode === 'login' }]" @click="mode = 'login'">{{ t('login.tabLogin') }}</button>
        <button :class="['tab', { active: mode === 'register' }]" @click="mode = 'register'">{{ t('login.tabRegister') }}</button>
      </div>

      <form @submit.prevent="submit" class="form">
        <input v-model="form.username" class="input" :placeholder="t('login.username')" autocomplete="username" />
        <input v-model="form.password" type="password" class="input" :placeholder="t('login.password')" autocomplete="current-password" />
        <template v-if="mode === 'register'">
          <input v-model="form.confirmPassword" type="password" class="input" :placeholder="t('login.confirmPassword')" />
          <input v-model="form.nickname" class="input" :placeholder="t('login.nickname')" />
        </template>

        <div v-if="error" class="error">{{ error }}</div>

        <button type="submit" class="btn primary submit" :disabled="loading">
          {{ loading ? t('login.loading') : (mode === 'login' ? t('login.submitLogin') : t('login.submitRegister')) }}
        </button>
      </form>

      <div class="hint">
        <span v-if="mode === 'login'">{{ t('login.demo') }}</span>
        <span v-else>{{ t('login.hasAccount') }}<a href="javascript:void(0)" @click="toggleMode">{{ t('login.backToLogin') }}</a></span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}
.login-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(600px 400px at 30% 20%, rgba(59, 130, 246, 0.08), transparent 60%),
    radial-gradient(800px 500px at 70% 80%, rgba(124, 58, 237, 0.05), transparent 60%);
  pointer-events: none;
}
.login-card {
  width: 360px;
  padding: 32px 28px;
  position: relative;
  z-index: 1;
}
.logo { text-align: center; margin-bottom: 24px; }
.logo-icon {
  width: 56px; height: 56px; margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  color: var(--accent);
  background: rgba(37, 99, 235, 0.08);
  border-radius: 16px;
  border: 1px solid rgba(37, 99, 235, 0.18);
}
.logo h1 { font-size: var(--fs-xl); font-weight: 600; letter-spacing: 2px; }
.logo p { font-size: var(--fs-sm); color: var(--text-secondary); margin-top: 4px; }
.lang-switch { position: absolute; top: 14px; right: 14px; }
.tabs { display: flex; gap: 8px; margin-bottom: 16px; }
.tab {
  flex: 1; height: 30px; font-size: var(--fs-sm);
  background: transparent; border: none; color: var(--text-secondary);
  border-bottom: 2px solid transparent; cursor: pointer; transition: all .2s;
}
.tab.active { color: var(--text-primary); border-bottom-color: var(--accent); }
.form { display: flex; flex-direction: column; gap: 12px; }
.submit { height: 34px; margin-top: 4px; }
.error { font-size: var(--fs-sm); color: var(--danger); text-align: center; }
.hint { margin-top: 16px; text-align: center; font-size: var(--fs-xs); color: var(--text-muted); }
.hint a { color: var(--accent); text-decoration: none; }
</style>
