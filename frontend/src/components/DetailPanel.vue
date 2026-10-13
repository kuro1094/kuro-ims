<script setup>
import { computed } from 'vue'
import { t } from '../i18n'

const props = defineProps({
  selection: { type: Object, default: null }
})
const emit = defineEmits(['close'])

const title = computed(() => {
  if (!props.selection) return ''
  const d = props.selection.data
  return d.name || d.plateNo || d.code || d.warehouse || t('detail.title')
})

const typeName = computed(() => props.selection?.type ? t('typeName.' + props.selection.type) : '')

const fields = computed(() => {
  if (!props.selection) return []
  const { type, data: d } = props.selection
  if (type === 'warehouse') {
    return [
      [t('detail.field.code'), d.code], [t('detail.field.type'), t('warehouseType.' + d.type)], [t('detail.field.manager'), d.manager],
      [t('detail.field.capacity'), `${d.used} / ${d.capacity} ${t('detail.palletSlots')}`], [t('detail.field.utilization'), `${Math.round(d.used / d.capacity * 100)}%`],
      [t('detail.field.status'), statusText(d.status)], [t('detail.field.size'), `${d.sizeX}×${d.sizeZ}×${d.sizeY} m`],
      [t('detail.field.description'), d.description || '—']
    ]
  }
  if (type === 'vehicle') {
    return [
      [t('detail.field.plateNo'), d.plateNo], [t('detail.field.driver'), d.driver], [t('detail.field.phone'), d.phone || '—'],
      [t('detail.field.type'), t('vehicleType.' + d.type)], [t('detail.field.status'), statusText(d.status)],
      [t('detail.field.route'), d.origin && d.destination ? `${d.origin} → ${d.destination}` : '—'],
      [t('detail.field.progress'), d.status === 'TRANSIT' ? `${d.progress}%` : '—'],
      [t('detail.field.cargo'), d.cargoDesc || '—'], [t('detail.field.eta'), d.eta || '—']
    ]
  }
  if (type === 'forklift') {
    return [
      [t('detail.field.code'), d.code], [t('detail.field.name'), d.name], [t('detail.field.operator'), d.driver || t('detail.auto')],
      [t('detail.field.status'), statusText(d.status)], [t('detail.field.battery'), `${d.battery}%`],
      [t('detail.field.currentTask'), d.currentTask || '—']
    ]
  }
  if (type === 'container') {
    return [
      [t('detail.field.code'), d.code], [t('detail.field.name'), d.name], [t('detail.field.containerType'), d.type],
      [t('detail.field.innerSize'), `${d.length}×${d.width}×${d.height} cm`],
      [t('detail.field.volume'), `${(d.length * d.width * d.height / 1e6).toFixed(1)} m³`],
      [t('detail.field.maxWeight'), `${d.maxWeight} kg`], [t('detail.field.status'), statusText(d.status)],
      [t('detail.field.location'), d.location || '—']
    ]
  }
  if (type === 'pallet') {
    return [[t('detail.field.belongWarehouse'), d.warehouse], [t('detail.field.layer'), t('detail.layerN', { n: d.layer })], [t('detail.field.status'), t('detail.storing')]]
  }
  if (type === 'patrolVehicle') {
    return [
      [t('detail.field.code'), d.code], [t('detail.field.name'), d.name || '—'],
      [t('detail.field.driver'), d.driver || '—'], [t('detail.field.phone'), d.phone || '—'],
      [t('detail.field.status'), statusText(d.status)], [t('detail.field.battery'), d.battery != null ? `${d.battery}%` : '—'],
      [t('detail.field.route'), d.route || '—'], [t('detail.field.description'), d.description || '—']
    ]
  }
  if (type === 'guard') {
    return [
      [t('detail.field.code'), d.code], [t('detail.field.name'), d.name || '—'],
      [t('detail.field.phone'), d.phone || '—'], [t('detail.field.status'), statusText(d.status)],
      [t('detail.field.area'), d.area || '—'], [t('detail.field.locatorNo'), d.locatorNo || '—'],
      [t('detail.field.description'), d.description || '—']
    ]
  }
  return []
})

const progress = computed(() => {
  const { type, data: d } = props.selection || {}
  if (type === 'warehouse') return Math.round(d.used / d.capacity * 100)
  if (type === 'vehicle' && d.status === 'TRANSIT') return d.progress
  if (type === 'forklift') return d.battery
  if (type === 'patrolVehicle') return d.battery ?? null
  if (type === 'container') return Math.min(100, Math.round((d.usedVolume || 0) / (d.length * d.width * d.height / 1e6) * 100))
  return null
})

function statusText(s) {
  return t('status.' + s)
}
</script>

<template>
  <div v-if="selection" class="detail-panel glass-deep fade-up">
    <div class="panel-header">
      <div>
        <div class="panel-type">{{ typeName }}</div>
        <div class="panel-title">{{ title }}</div>
      </div>
      <button class="close" @click="emit('close')">✕</button>
    </div>

    <div v-if="progress !== null" class="progress-wrap">
      <div class="progress-bar">
        <div class="progress-inner" :style="{ width: progress + '%' }"></div>
      </div>
      <span class="progress-text">{{ progress }}%</span>
    </div>

    <div class="field-list">
      <div v-for="[label, value] in fields" :key="label" class="field">
        <span class="field-label">{{ label }}</span>
        <span class="field-value">{{ value }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-panel {
  position: absolute;
  right: 16px;
  top: 76px;
  width: 280px;
  padding: 16px;
  z-index: 20;
}
.panel-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.panel-type { font-size: var(--fs-xs); color: var(--accent); letter-spacing: 1px; }
.panel-title { font-size: var(--fs-lg); font-weight: 600; margin-top: 2px; }
.close {
  width: 22px; height: 22px; border: none; border-radius: 6px;
  background: #f1f5f9; color: var(--text-secondary);
  cursor: pointer; font-size: 11px;
}
.close:hover { background: #e2e8f0; color: var(--text-primary); }
.progress-wrap { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.progress-bar { flex: 1; height: 6px; border-radius: 3px; background: #e2e8f0; overflow: hidden; }
.progress-inner { height: 100%; border-radius: 3px; background: linear-gradient(90deg, var(--accent), var(--accent-2)); transition: width .4s; }
.progress-text { font-size: var(--fs-xs); color: var(--accent); min-width: 32px; text-align: right; }
.field-list { display: flex; flex-direction: column; gap: 8px; }
.field { display: flex; justify-content: space-between; gap: 12px; font-size: var(--fs-sm); }
.field-label { color: var(--text-muted); flex-shrink: 0; }
.field-value { color: var(--text-primary); text-align: right; word-break: break-all; }
</style>
