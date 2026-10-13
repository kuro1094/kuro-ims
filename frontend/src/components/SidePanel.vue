<script setup>
import { ref, computed } from 'vue'
import { t } from '../i18n'

defineProps({
  stats: { type: Object, default: () => ({}) },
  tasks: { type: Array, default: () => [] },
  devices: { type: Array, default: () => [] },
  vehicles: { type: Array, default: () => [] }
})
const emit = defineEmits(['select'])

const tab = ref('tasks')

const statCards = computed(() => [
  { key: 'warehouseCount', label: t('side.stat.warehouseCount'), unit: t('side.unit.warehouse') },
  { key: 'containerCount', label: t('side.stat.containerCount'), unit: t('side.unit.container') },
  { key: 'goodsCount', label: t('side.stat.goodsCount'), unit: t('side.unit.goods') },
  { key: 'vehicleCount', label: t('side.stat.vehicleCount'), unit: t('side.unit.vehicle') },
  { key: 'forkliftCount', label: t('side.stat.forkliftCount'), unit: t('side.unit.forklift') },
  { key: 'alarmDeviceCount', label: t('side.stat.alarmDeviceCount'), unit: t('side.unit.alarm'), danger: true }
])

function tagClass(s) {
  if (['RUNNING', 'DOING', 'TRANSIT'].includes(s)) return 'success'
  if (['ALARM', 'STOP'].includes(s)) return 'danger'
  if (['PENDING', 'IDLE', 'MAINTAIN', 'CHARGING'].includes(s)) return 'warning'
  return 'info'
}
</script>

<template>
  <div class="side-panel">
    <!-- 统计卡片 -->
    <div class="glass stats">
      <div class="section-title">{{ t('side.overview') }}</div>
      <div class="stat-grid">
        <div v-for="c in statCards" :key="c.key" class="stat-card">
          <div class="stat-value" :class="{ danger: c.danger && stats[c.key] > 0 }">
            {{ stats[c.key] ?? '-' }}<span class="stat-unit">{{ c.unit }}</span>
          </div>
          <div class="stat-label">{{ c.label }}</div>
        </div>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="glass lists">
      <div class="tab-bar">
        <button v-for="k in ['tasks','devices','vehicles']" :key="k"
                :class="['tab-btn', { active: tab === k }]" @click="tab = k">{{ t('side.tab.' + k) }}</button>
      </div>

      <div class="list-body">
        <template v-if="tab === 'tasks'">
          <div v-for="tk in tasks" :key="tk.id" class="list-item" @click="emit('select', { type: 'task', data: tk })">
            <div class="item-main">
              <span class="item-name">{{ tk.code }}</span>
              <span class="item-sub">{{ t('taskType.' + tk.type) }} · {{ tk.goodsName }} ×{{ tk.quantity }}</span>
            </div>
            <span class="tag" :class="tagClass(tk.status)">{{ t('taskStatus.' + tk.status) }}</span>
          </div>
          <div v-if="!tasks.length" class="empty">{{ t('side.empty.tasks') }}</div>
        </template>

        <template v-if="tab === 'devices'">
          <div v-for="d in devices" :key="d.id" class="list-item" @click="emit('select', { type: 'device', data: d })">
            <div class="item-main">
              <span class="item-name">{{ d.name }}</span>
              <span class="item-sub">{{ d.workshop }} · {{ d.temperature }}℃ · OEE {{ d.oee }}%</span>
            </div>
            <span class="tag" :class="tagClass(d.status)">{{ t('deviceStatus.' + d.status) }}</span>
          </div>
          <div v-if="!devices.length" class="empty">{{ t('side.empty.devices') }}</div>
        </template>

        <template v-if="tab === 'vehicles'">
          <div v-for="v in vehicles" :key="v.id" class="list-item" @click="emit('select', { type: 'vehicle', data: v })">
            <div class="item-main">
              <span class="item-name">{{ v.plateNo }}</span>
              <span class="item-sub">{{ v.origin && v.destination ? v.origin + ' → ' + v.destination : t('side.standby') }}</span>
            </div>
            <span class="tag" :class="tagClass(v.status)">{{ t('vehicleStatus.' + v.status) }}</span>
          </div>
          <div v-if="!vehicles.length" class="empty">{{ t('side.empty.vehicles') }}</div>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.side-panel {
  position: absolute;
  left: 16px;
  top: 76px;
  width: 260px;
  display: flex;
  flex-direction: column;
  gap: var(--gap);
  z-index: 15;
  max-height: calc(100% - 92px);
}
.stats { padding: 14px; }
.stat-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.stat-card { text-align: center; padding: 8px 4px; background: #f4f8fd; border-radius: var(--radius-sm); }
.stat-value { font-size: var(--fs-lg); font-weight: 700; color: var(--accent); }
.stat-value.danger { color: var(--danger); }
.stat-unit { font-size: var(--fs-xs); color: var(--text-muted); margin-left: 2px; }
.stat-label { font-size: var(--fs-xs); color: var(--text-secondary); margin-top: 2px; }
.lists { display: flex; flex-direction: column; min-height: 0; flex: 1; }
.tab-bar { display: flex; gap: 4px; padding: 10px 10px 0; }
.tab-btn {
  flex: 1; height: 26px; font-size: var(--fs-sm);
  background: transparent; border: none; color: var(--text-muted);
  border-bottom: 2px solid transparent; cursor: pointer; transition: all .2s;
}
.tab-btn.active { color: var(--text-primary); border-bottom-color: var(--accent); }
.list-body { padding: 10px; overflow-y: auto; display: flex; flex-direction: column; gap: 6px; }
.list-item {
  display: flex; align-items: center; justify-content: space-between; gap: 8px;
  padding: 8px 10px; border-radius: var(--radius-sm);
  background: #f8fafc; cursor: pointer; transition: background .2s;
}
.list-item:hover { background: #eef4fc; }
.item-main { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.item-name { font-size: var(--fs-sm); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.item-sub { font-size: var(--fs-xs); color: var(--text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty { text-align: center; color: var(--text-muted); font-size: var(--fs-sm); padding: 20px 0; }
</style>
