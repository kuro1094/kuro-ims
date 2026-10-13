<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { getOverview, warehouseApi, vehicleApi, forkliftApi, containerApi, taskApi, deviceApi, buildingApi, patrolVehicleApi, guardApi } from '../api'
import Scene3D from '../components/Scene3D.vue'
import SidePanel from '../components/SidePanel.vue'
import DetailPanel from '../components/DetailPanel.vue'
import ContainerCalc from '../components/ContainerCalc.vue'
import { t, locale, toggleLocale } from '../i18n'

const router = useRouter()
const auth = useAuthStore()

const stats = ref({})
const warehouses = ref([])
const vehicles = ref([])
const forklifts = ref([])
const containers = ref([])
const tasks = ref([])
const devices = ref([])
const buildings = ref([])
const patrolVehicles = ref([])
const guards = ref([])

const selection = ref(null)
const showCalc = ref(false)
const keyword = ref('')

// 顶栏搜索：按名称/编号/车牌定位资产
function search() {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return
  const sources = [
    ['warehouse', warehouses.value, w => w.name || w.code],
    ['vehicle', vehicles.value, v => v.plateNo],
    ['forklift', forklifts.value, f => f.code || f.name],
    ['container', containers.value, c => c.code || c.name]
  ]
  for (const [type, list, getKey] of sources) {
    const hit = list.find(item => (getKey(item) || '').toLowerCase().includes(k))
    if (hit) { onSelect({ type, data: hit }); return }
  }
}

let timer = null

async function loadAll() {
  try {
    const [s, wh, ve, fo, co, ta, de, bd, pv, gd] = await Promise.all([
      getOverview(),
      warehouseApi.list(),
      vehicleApi.list(),
      forkliftApi.list(),
      containerApi.list(),
      taskApi.page({ page: 1, size: 50 }),
      deviceApi.list(),
      buildingApi.list(),
      patrolVehicleApi.list(),
      guardApi.list()
    ])
    stats.value = s.data
    warehouses.value = wh.data
    vehicles.value = ve.data
    forklifts.value = fo.data
    containers.value = co.data
    tasks.value = ta.data.records || []
    devices.value = de.data
    buildings.value = bd.data
    patrolVehicles.value = pv.data
    guards.value = gd.data
  } catch (e) {
    console.error('加载数据失败', e)
  }
}

onMounted(() => {
  loadAll()
  timer = setInterval(loadAll, 30000)
})

onBeforeUnmount(() => clearInterval(timer))

function onSelect(sel) {
  selection.value = sel
}

function logout() {
  auth.clear()
  router.push('/login')
}
</script>

<template>
  <div class="home">
    <!-- 3D 场景 -->
    <Scene3D
      :warehouses="warehouses"
      :buildings="buildings"
      :vehicles="vehicles"
      :forklifts="forklifts"
      :containers="containers"
      :patrol-vehicles="patrolVehicles"
      :guards="guards"
      @select="onSelect"
    />

    <!-- 顶栏 -->
    <header class="topbar glass-deep">
      <div class="brand">
        <span class="brand-dot"></span>
        <span class="brand-name">Kuro IMS</span>
        <span class="brand-sub">{{ t('home.brandSub') }}</span>
      </div>
      <div class="top-search">
        <input v-model="keyword" class="search-input" :placeholder="t('home.searchPh')" @keyup.enter="search" />
      </div>
      <div class="top-metrics">
        <span class="metric-chip">{{ t('home.doingTasks') }} <b>{{ stats.doingTaskCount ?? tasks.filter(x => x.status === 'DOING').length }}</b></span>
        <span class="metric-chip">{{ t('home.transitVehicles') }} <b>{{ vehicles.filter(v => v.status === 'TRANSIT').length }}</b></span>
        <span class="metric-chip warn">{{ t('home.alarms') }} <b>{{ stats.alarmDeviceCount ?? 0 }}</b></span>
      </div>
      <div class="top-actions">
        <button class="btn lang-btn" @click="toggleLocale()">{{ locale === 'zh' ? 'EN' : '中文' }}</button>
        <button class="btn" @click="router.push('/admin')">{{ t('home.admin') }}</button>
        <button class="btn primary" @click="showCalc = true">{{ t('home.containerCalc') }}</button>
        <div class="user-chip">
          <span class="user-avatar">{{ (auth.user?.nickname || auth.user?.username || 'U').slice(0, 1).toUpperCase() }}</span>
          <span class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
          <button class="btn" @click="logout">{{ t('home.logout') }}</button>
        </div>
      </div>
    </header>

    <!-- 左侧数据面板 -->
    <SidePanel :stats="stats" :tasks="tasks" :devices="devices" :vehicles="vehicles" @select="onSelect" />

    <!-- 操作提示 -->
    <div class="hint glass">
      <span>{{ t('home.hint') }}</span>
    </div>

    <!-- 右侧详情 -->
    <DetailPanel :selection="selection" @close="selection = null" />

    <!-- 货柜计算 -->
    <ContainerCalc :visible="showCalc" :containers="containers" @close="showCalc = false" />
  </div>
</template>

<style scoped>
.home { position: relative; width: 100%; height: 100%; overflow: hidden; }
.topbar {
  position: absolute; top: 12px; left: 16px; right: 16px; height: 48px;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; z-index: 30;
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 10px var(--accent);
}
.brand-name { font-size: var(--fs-md); font-weight: 700; letter-spacing: 1px; }
.brand-sub { font-size: var(--fs-xs); color: var(--text-secondary); letter-spacing: 2px; }
.top-search { flex: 1; display: flex; justify-content: center; padding: 0 16px; }
.search-input {
  width: 280px; height: 28px; padding: 0 12px;
  font-size: var(--fs-sm); color: var(--text-primary);
  background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 14px; outline: none;
  transition: border-color .2s, background .2s;
}
.search-input:focus { background: #ffffff; border-color: var(--accent); }
.search-input::placeholder { color: var(--text-muted); }
.top-metrics { display: flex; align-items: center; gap: 8px; }
.metric-chip {
  font-size: var(--fs-xs); color: var(--text-secondary);
  background: #f4f8fd; border: 1px solid var(--glass-border);
  border-radius: 999px; padding: 3px 10px; white-space: nowrap;
}
.metric-chip b { color: var(--accent); font-weight: 700; margin-left: 2px; }
.metric-chip.warn b { color: var(--danger); }
.top-actions { display: flex; align-items: center; gap: 10px; }
.user-chip { display: flex; align-items: center; gap: 8px; }
.user-avatar {
  width: 26px; height: 26px; border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), var(--violet));
  color: #ffffff; font-size: var(--fs-xs); font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.user-name { font-size: var(--fs-sm); color: var(--text-secondary); }
.hint {
  position: absolute; bottom: 14px; left: 50%; transform: translateX(-50%);
  padding: 6px 14px; font-size: var(--fs-xs); color: var(--text-muted);
  z-index: 15; white-space: nowrap;
}
</style>
