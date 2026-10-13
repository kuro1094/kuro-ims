<script setup>
// 后台管理：地图编辑 / 仓库 / 车辆 / 叉车 / 人员 / 巡逻车 / 保安
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { vehicleApi, forkliftApi, workerApi, warehouseApi, patrolVehicleApi, guardApi } from '../api'
import MapEditor from '../components/MapEditor.vue'
import { t, locale, toggleLocale } from '../i18n'

const router = useRouter()
const auth = useAuthStore()

const tab = ref('map')
const tabs = computed(() => [
  { key: 'map', name: t('admin.menu.map') },
  { key: 'warehouse', name: t('admin.menu.warehouse') },
  { key: 'vehicle', name: t('admin.menu.vehicle') },
  { key: 'forklift', name: t('admin.menu.forklift') },
  { key: 'worker', name: t('admin.menu.worker') },
  { key: 'patrol', name: t('admin.menu.patrol') },
  { key: 'guard', name: t('admin.menu.guard') }
])

// ---------- 表格配置 ----------
const opt = (dictKey, vals) => vals.map(v => [v, t(dictKey + v)])

const CONF = computed(() => ({
  warehouse: {
    api: warehouseApi,
    title: t('admin.title.warehouse'),
    columns: [
      ['code', t('admin.field.code')], ['name', t('admin.field.name')], ['type', t('admin.field.type')],
      ['manager', t('admin.field.manager')], ['capacity', t('admin.field.capacity')], ['used', t('admin.field.used')],
      ['status', t('admin.field.status')]
    ],
    fields: [
      { key: 'code', label: t('admin.field.code') },
      { key: 'name', label: t('admin.field.name') },
      { key: 'type', label: t('admin.field.type'), type: 'select', options: opt('warehouseType.', ['NORMAL', 'COLD', 'HAZARD']) },
      { key: 'manager', label: t('admin.field.manager') },
      { key: 'capacity', label: t('admin.field.capacity'), type: 'number' },
      { key: 'used', label: t('admin.field.used'), type: 'number' },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('status.', ['RUNNING', 'MAINTAIN', 'STOP']) },
      { key: 'sizeX', label: t('admin.field.sizeX'), type: 'number' },
      { key: 'sizeZ', label: t('admin.field.sizeZ'), type: 'number' },
      { key: 'sizeY', label: t('admin.field.sizeY'), type: 'number' },
      { key: 'description', label: t('admin.field.description') }
    ],
    blank: { code: '', name: '', type: 'NORMAL', manager: '', capacity: 1000, used: 0, status: 'RUNNING', sizeX: 16, sizeZ: 12, sizeY: 7, description: '' }
  },
  vehicle: {
    api: vehicleApi,
    title: t('admin.title.vehicle'),
    columns: [
      ['plateNo', t('admin.field.plateNo')], ['driver', t('admin.field.driver')], ['phone', t('admin.field.phone')],
      ['type', t('admin.field.type')], ['status', t('admin.field.status')], ['line', t('admin.field.line')], ['cargoDesc', t('admin.field.cargoDesc')]
    ],
    fields: [
      { key: 'plateNo', label: t('admin.field.plateNo') },
      { key: 'driver', label: t('admin.field.driver') },
      { key: 'phone', label: t('admin.field.phone') },
      { key: 'type', label: t('admin.field.type'), type: 'select', options: opt('vehicleType.', ['TRUCK', 'VAN', 'CONTAINER']) },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('vehicleStatus.', ['IDLE', 'LOADING', 'TRANSIT', 'UNLOADING']) },
      { key: 'origin', label: t('admin.field.origin') },
      { key: 'destination', label: t('admin.field.destination') },
      { key: 'progress', label: t('admin.field.progress'), type: 'number' },
      { key: 'cargoDesc', label: t('admin.field.cargoDescLong') }
    ],
    blank: { plateNo: '', driver: '', phone: '', type: 'TRUCK', status: 'IDLE', origin: '', destination: '', progress: 0, cargoDesc: '' }
  },
  forklift: {
    api: forkliftApi,
    title: t('admin.title.forklift'),
    columns: [
      ['code', t('admin.field.code')], ['name', t('admin.field.name')], ['driver', t('admin.field.operator')],
      ['status', t('admin.field.status')], ['battery', t('admin.field.battery')], ['currentTask', t('admin.field.currentTask')]
    ],
    fields: [
      { key: 'code', label: t('admin.field.code') },
      { key: 'name', label: t('admin.field.name') },
      { key: 'driver', label: t('admin.field.operator') },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('status.', ['IDLE', 'WORKING', 'CHARGING', 'MAINTAIN']) },
      { key: 'battery', label: t('admin.field.battery'), type: 'number' },
      { key: 'currentTask', label: t('admin.field.currentTask') }
    ],
    blank: { code: '', name: '', driver: '', status: 'IDLE', battery: 100, currentTask: '' }
  },
  worker: {
    api: workerApi,
    title: t('admin.title.worker'),
    columns: [
      ['code', t('admin.field.workerCode')], ['name', t('admin.field.workerName')], ['phone', t('admin.field.phone')], ['role', t('admin.field.role')],
      ['department', t('admin.field.department')], ['area', t('admin.field.area')], ['status', t('admin.field.status')]
    ],
    fields: [
      { key: 'code', label: t('admin.field.workerCode') },
      { key: 'name', label: t('admin.field.workerName') },
      { key: 'phone', label: t('admin.field.phone') },
      { key: 'role', label: t('admin.field.role') },
      { key: 'department', label: t('admin.field.department') },
      { key: 'area', label: t('admin.field.area') },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('workerStatus.', ['ON', 'OFF', 'LEAVE']) },
      { key: 'remark', label: t('admin.field.remark') }
    ],
    blank: { code: '', name: '', phone: '', role: '', department: '', area: '', status: 'ON', remark: '' }
  },
  patrol: {
    api: patrolVehicleApi,
    title: t('admin.title.patrol'),
    columns: [
      ['code', t('admin.field.code')], ['name', t('admin.field.name')], ['driver', t('admin.field.driver')],
      ['phone', t('admin.field.phone')], ['status', t('admin.field.status')], ['battery', t('admin.field.battery')],
      ['route', t('admin.field.route')]
    ],
    fields: [
      { key: 'code', label: t('admin.field.code') },
      { key: 'name', label: t('admin.field.name') },
      { key: 'driver', label: t('admin.field.driver') },
      { key: 'phone', label: t('admin.field.phone') },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('status.', ['PATROL', 'IDLE', 'CHARGING', 'MAINTAIN']) },
      { key: 'battery', label: t('admin.field.battery'), type: 'number' },
      { key: 'route', label: t('admin.field.route') },
      { key: 'description', label: t('admin.field.description') }
    ],
    blank: { code: '', name: '', driver: '', phone: '', status: 'PATROL', battery: 100, route: '园区外环', description: '' }
  },
  guard: {
    api: guardApi,
    title: t('admin.title.guard'),
    columns: [
      ['code', t('admin.field.code')], ['name', t('admin.field.name')], ['phone', t('admin.field.phone')],
      ['status', t('admin.field.status')], ['area', t('admin.field.area')], ['locatorNo', t('admin.field.locatorNo')]
    ],
    fields: [
      { key: 'code', label: t('admin.field.code') },
      { key: 'name', label: t('admin.field.name') },
      { key: 'phone', label: t('admin.field.phone') },
      { key: 'status', label: t('admin.field.status'), type: 'select', options: opt('status.', ['ON_PATROL', 'IDLE']) },
      { key: 'area', label: t('admin.field.area') },
      { key: 'locatorNo', label: t('admin.field.locatorNo') },
      { key: 'description', label: t('admin.field.description') }
    ],
    blank: { code: '', name: '', phone: '', status: 'ON_PATROL', area: '', locatorNo: '', description: '' }
  }
}))

const statusText = computed(() => ({
  ...Object.fromEntries(['IDLE', 'LOADING', 'TRANSIT', 'UNLOADING', 'WORKING', 'CHARGING', 'MAINTAIN', 'RUNNING', 'STOP', 'PATROL', 'ON_PATROL'].map(k => [k, t('status.' + k)])),
  ...Object.fromEntries(['ON', 'OFF', 'LEAVE'].map(k => [k, t('workerStatus.' + k)])),
  ...Object.fromEntries(['TRUCK', 'VAN', 'CONTAINER'].map(k => [k, t('vehicleType.' + k)])),
  ...Object.fromEntries(['NORMAL', 'COLD', 'HAZARD'].map(k => [k, t('warehouseType.' + k)]))
}))
const fmt = (key, val, row) => {
  if (key === 'line') return row.origin && row.destination ? `${row.origin} → ${row.destination}` : '—'
  if (key === 'battery') return val + '%'
  return statusText.value[val] || val || '—'
}

// ---------- 数据与编辑弹窗 ----------
const rows = ref({ warehouse: [], vehicle: [], forklift: [], worker: [], patrol: [], guard: [] })
const dialog = reactive({ visible: false, editing: null, form: {} })
const error = ref('')

async function loadTab(tb) {
  if (tb === 'map') return
  const res = await CONF.value[tb].api.list()
  rows.value[tb] = res.data
}

onMounted(() => { loadTab('warehouse'); loadTab('vehicle'); loadTab('forklift'); loadTab('worker'); loadTab('patrol'); loadTab('guard') })

function switchTab(tb) { tab.value = tb; loadTab(tb) }

function openCreate(tb) {
  dialog.visible = true
  dialog.editing = null
  dialog.form = { ...CONF.value[tb].blank }
  error.value = ''
}
function openEdit(row) {
  dialog.visible = true
  dialog.editing = row
  dialog.form = { ...row }
  error.value = ''
}
async function saveDialog() {
  const conf = CONF.value[tab.value]
  try {
    if (dialog.editing) await conf.api.update(dialog.editing.id, dialog.form)
    else await conf.api.create(dialog.form)
    dialog.visible = false
    loadTab(tab.value)
  } catch (e) {
    error.value = e?.response?.data?.message || t('admin.saveFailed')
  }
}
async function removeRow(row) {
  if (!confirm(t('admin.confirmDelete'))) return
  await CONF.value[tab.value].api.remove(row.id)
  loadTab(tab.value)
}
</script>

<template>
  <div class="admin">
    <!-- 顶栏 -->
    <header class="topbar glass-deep">
      <div class="brand">
        <span class="brand-dot"></span>
        <span class="brand-name">Kuro IMS</span>
        <span class="brand-sub">{{ t('admin.sub') }}</span>
      </div>
      <div class="top-actions">
        <button class="btn" @click="toggleLocale()">{{ locale === 'zh' ? 'EN' : '中文' }}</button>
        <span class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
        <button class="btn" @click="router.push('/')">{{ t('admin.back') }}</button>
      </div>
    </header>

    <div class="body">
      <!-- 侧边菜单 -->
      <nav class="menu glass">
        <button v-for="t in tabs" :key="t.key"
                :class="['menu-btn', { active: tab === t.key }]" @click="switchTab(t.key)">
          {{ t.name }}
        </button>
      </nav>

      <!-- 地图编辑 -->
      <main v-if="tab === 'map'" class="content">
        <MapEditor />
      </main>

      <!-- 数据表格页 -->
      <main v-else class="content">
        <div class="panel glass">
          <div class="panel-head">
            <div class="section-title" style="margin:0">{{ t('admin.list', { title: CONF[tab].title }) }}</div>
            <button class="btn primary" @click="openCreate(tab)">+ {{ t('admin.add') }}{{ CONF[tab].title }}</button>
          </div>
          <table class="table">
            <thead>
              <tr>
                <th v-for="c in CONF[tab].columns" :key="c[0]">{{ c[1] }}</th>
                <th style="width:110px">{{ t('admin.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in rows[tab]" :key="row.id">
                <td v-for="c in CONF[tab].columns" :key="c[0]">{{ fmt(c[0], row[c[0]], row) }}</td>
                <td>
                  <button class="btn" @click="openEdit(row)">{{ t('admin.edit') }}</button>
                  <button class="btn danger" @click="removeRow(row)">{{ t('admin.del') }}</button>
                </td>
              </tr>
              <tr v-if="!rows[tab].length"><td :colspan="CONF[tab].columns.length + 1" class="empty">{{ t('admin.noData') }}</td></tr>
            </tbody>
          </table>
        </div>
      </main>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="dialog.visible" class="modal-mask" @click.self="dialog.visible = false">
      <div class="modal glass-deep fade-up">
        <div class="modal-head">
          <div class="modal-title">{{ dialog.editing ? t('admin.edit') : t('admin.add') }}{{ CONF[tab].title }}</div>
          <button class="btn" @click="dialog.visible = false">✕</button>
        </div>
        <div class="modal-body">
          <label v-for="f in CONF[tab].fields" :key="f.key">
            {{ f.label }}
            <select v-if="f.type === 'select'" v-model="dialog.form[f.key]" class="select">
              <option v-for="o in f.options" :key="o[0]" :value="o[0]">{{ o[1] }}</option>
            </select>
            <input v-else v-model="dialog.form[f.key]" :type="f.type === 'number' ? 'number' : 'text'" class="input" />
          </label>
          <div v-if="error" class="err">{{ error }}</div>
        </div>
        <div class="modal-foot">
          <button class="btn" @click="dialog.visible = false">{{ t('admin.cancel') }}</button>
          <button class="btn primary" @click="saveDialog">{{ t('admin.save') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin { height: 100%; display: flex; flex-direction: column; padding: 12px 16px; gap: var(--gap); }
.topbar {
  height: 48px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: space-between; padding: 0 16px;
}
.brand { display: flex; align-items: center; gap: 10px; }
.brand-dot { width: 8px; height: 8px; border-radius: 50%; background: var(--accent); }
.brand-name { font-size: var(--fs-md); font-weight: 700; letter-spacing: 1px; }
.brand-sub { font-size: var(--fs-xs); color: var(--text-secondary); letter-spacing: 2px; }
.top-actions { display: flex; align-items: center; gap: 10px; }
.user-name { font-size: var(--fs-sm); color: var(--text-secondary); }
.body { flex: 1; display: flex; gap: var(--gap); min-height: 0; }
.menu { width: 140px; flex-shrink: 0; padding: 10px; display: flex; flex-direction: column; gap: 6px; }
.menu-btn {
  height: 34px; border: none; border-radius: var(--radius-sm); cursor: pointer;
  background: transparent; font-size: var(--fs-base); color: var(--text-secondary);
  text-align: left; padding: 0 12px; transition: all .2s;
}
.menu-btn:hover { background: #f1f5f9; }
.menu-btn.active { background: #eff6ff; color: var(--accent); font-weight: 600; }
.content { flex: 1; min-width: 0; min-height: 0; display: flex; }
.panel { flex: 1; padding: 16px; overflow: auto; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.table { width: 100%; border-collapse: collapse; font-size: var(--fs-sm); }
.table th {
  text-align: left; padding: 8px 10px; font-size: var(--fs-xs); font-weight: 600;
  color: var(--text-muted); border-bottom: 1px solid #e2e8f0; white-space: nowrap;
}
.table td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; color: var(--text-primary); white-space: nowrap; }
.table td .btn { height: 24px; padding: 0 8px; margin-right: 6px; }
.empty { text-align: center; color: var(--text-muted); padding: 32px 0 !important; }
.modal-mask {
  position: fixed; inset: 0; z-index: 100; background: rgba(30, 58, 95, 0.35);
  display: flex; align-items: center; justify-content: center;
}
.modal { width: 420px; max-width: 92vw; }
.modal-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 18px; border-bottom: 1px solid var(--glass-border);
}
.modal-title { font-size: var(--fs-lg); font-weight: 600; }
.modal-body { padding: 16px 18px; display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.modal-body label { display: flex; flex-direction: column; gap: 4px; font-size: var(--fs-xs); color: var(--text-secondary); }
.modal-foot { padding: 12px 18px; display: flex; justify-content: flex-end; gap: 8px; border-top: 1px solid var(--glass-border); }
.err { grid-column: 1 / -1; font-size: var(--fs-sm); color: var(--danger); text-align: center; }
</style>
