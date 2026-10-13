<script setup>
// 2D 园区地图编辑器：点击放置建筑、拖拽移动、右侧编辑属性
import { onMounted, ref } from 'vue'
import { buildingApi } from '../api'
import { t } from '../i18n'

const emit = defineEmits(['change'])

const svgRef = ref(null)
const buildings = ref([])
const selected = ref(null)
const placing = ref(null)   // 待放置的模型 key
const dragging = ref(null)
const msg = ref('')

const MODELS = [
  { key: 'WAREHOUSE', w: 24, h: 8, d: 16 },
  { key: 'WORKSHOP', w: 28, h: 8, d: 16 },
  { key: 'OFFICE', w: 22, h: 11, d: 14 },
  { key: 'CANTEEN', w: 16, h: 6, d: 12 },
  { key: 'QC', w: 18, h: 7, d: 13 },
  { key: 'VISITOR', w: 20, h: 7, d: 12 }
]
const modelName = (k) => t('map.model.' + k)

async function load() {
  const res = await buildingApi.list()
  buildings.value = res.data
}
onMounted(load)

// 屏幕坐标 → 场景坐标（SVG viewBox 即场景坐标系）
function toScene(e) {
  const pt = svgRef.value.createSVGPoint()
  pt.x = e.clientX; pt.y = e.clientY
  return pt.matrixTransform(svgRef.value.getScreenCTM().inverse())
}

// 放置模式：点击空白处创建建筑
async function onMapClick(e) {
  if (!placing.value) return
  const p = toScene(e)
  const m = MODELS.find(x => x.key === placing.value)
  const data = {
    name: modelName(placing.value) + '-' + String(buildings.value.length + 1).padStart(2, '0'),
    code: '',
    model: placing.value,
    posX: Math.round(p.x * 2) / 2,
    posZ: Math.round(p.y * 2) / 2,
    sizeX: m.w, sizeY: m.h, sizeZ: m.d,
    manager: '',
    capacity: 0,
    used: 0,
    status: 'RUNNING',
    description: ''
  }
  await buildingApi.create(data)
  placing.value = null
  await load()
  emit('change')
  selected.value = buildings.value[buildings.value.length - 1]
}

function selectBuilding(b, e) {
  e.stopPropagation()
  selected.value = b
  dragging.value = b
}

function onMove(e) {
  if (!dragging.value) return
  const p = toScene(e)
  dragging.value.posX = Math.round(p.x * 2) / 2
  dragging.value.posZ = Math.round(p.y * 2) / 2
}

async function onUp() {
  if (!dragging.value) return
  const b = dragging.value
  dragging.value = null
  await buildingApi.update(b.id, b)
  emit('change')
}

async function save() {
  if (!selected.value) return
  await buildingApi.update(selected.value.id, selected.value)
  msg.value = t('map.saved')
  setTimeout(() => msg.value = '', 1500)
  emit('change')
}

async function remove() {
  if (!selected.value || !confirm(t('map.confirmDelete', { name: selected.value.name }))) return
  await buildingApi.remove(selected.value.id)
  selected.value = null
  await load()
  emit('change')
}
</script>

<template>
  <div class="map-editor">
    <!-- 模型选择 -->
    <div class="palette glass">
      <div class="section-title">{{ t('map.models') }}</div>
      <button v-for="m in MODELS" :key="m.key"
              :class="['pal-btn', { active: placing === m.key }]"
              @click="placing = placing === m.key ? null : m.key">
        <span class="pal-swatch" :data-model="m.key"></span>{{ modelName(m.key) }}
      </button>
      <div class="pal-hint">{{ placing ? t('map.hintPlacing') : t('map.hintIdle') }}</div>
    </div>

    <!-- 地图画布 -->
    <svg ref="svgRef" class="map-canvas glass" viewBox="-115 -92 230 184"
         @click="onMapClick" @pointermove="onMove" @pointerup="onUp" @pointerleave="onUp">
      <!-- 园区地面与环路 -->
      <rect x="-110" y="-88" width="220" height="176" rx="2" fill="#edf1f6" stroke="#dbe4ee"/>
      <rect x="-98" y="-78" width="196" height="156" fill="none" stroke="#c6cfd9" stroke-width="7"/>
      <rect x="-98" y="-78" width="196" height="156" fill="none" stroke="#fff" stroke-width="0.5" stroke-dasharray="2 4"/>
      <!-- 内部道路 -->
      <line x1="-98" y1="-34" x2="98" y2="-34" stroke="#c6cfd9" stroke-width="7"/>
      <line x1="-98" y1="26" x2="98" y2="26" stroke="#c6cfd9" stroke-width="7"/>
      <line x1="-42" y1="-78" x2="-42" y2="78" stroke="#c6cfd9" stroke-width="7"/>
      <line x1="-26" y1="26" x2="-26" y2="78" stroke="#c6cfd9" stroke-width="7"/>
      <!-- B区围合 + 仓库槽位 -->
      <rect x="31" y="-31" width="59" height="49" fill="none" stroke="#4caf50" stroke-width="0.5" stroke-dasharray="1.4 0.9"/>
      <rect v-for="s in [{x:44,y:-17},{x:74,y:-17},{x:44,y:4},{x:74,y:4}]" :key="s.y + '-' + s.x"
            :x="s.x - 10" :y="s.y - 6" width="20" height="12"
            fill="#dbe5f0" stroke="#4f86c6" stroke-width="0.4"/>
      <text x="44" y="-17" text-anchor="middle" dominant-baseline="middle" font-size="2.6" fill="#1e3a8a">A</text>
      <text x="74" y="-17" text-anchor="middle" dominant-baseline="middle" font-size="2.6" fill="#1e3a8a">B</text>
      <text x="44" y="4" text-anchor="middle" dominant-baseline="middle" font-size="2.6" fill="#1e3a8a">C</text>
      <text x="74" y="4" text-anchor="middle" dominant-baseline="middle" font-size="2.6" fill="#1e3a8a">D</text>
      <!-- 停车场 -->
      <rect x="10.5" y="31" width="73" height="34" fill="#dfe6ee" stroke="#b9c2cd" stroke-width="0.3"/>
      <text x="47" y="48" text-anchor="middle" font-size="3" fill="#64748b">{{ t('map.parkingLot') }}</text>
      <!-- 入口 -->
      <circle cx="-26" cy="78" r="1.6" fill="#dc2626"/>

      <!-- 建筑（可拖拽） -->
      <g v-for="b in buildings" :key="b.id"
         :transform="`translate(${b.posX},${b.posZ})`"
         :class="['bldg', { sel: selected?.id === b.id }]"
         @pointerdown="selectBuilding(b, $event)">
        <rect :x="-b.sizeX / 2" :y="-b.sizeZ / 2" :width="b.sizeX" :height="b.sizeZ"
              :fill="selected?.id === b.id ? '#dbeafe' : '#ffffff'"
              :stroke="selected?.id === b.id ? '#2563eb' : '#94a3b8'"
              :stroke-width="selected?.id === b.id ? 0.7 : 0.4" rx="0.6"/>
        <text text-anchor="middle" dominant-baseline="middle" font-size="2.8"
              :fill="selected?.id === b.id ? '#1e3a8a' : '#475569'">{{ b.name }}</text>
      </g>
    </svg>

    <!-- 属性编辑 -->
    <div class="props glass" v-if="selected">
      <div class="section-title">{{ t('map.props') }}</div>
      <label>{{ t('map.prop.name') }}<input v-model="selected.name" class="input" /></label>
      <label>{{ t('map.prop.model') }}
        <select v-model="selected.model" class="select">
          <option v-for="m in MODELS" :key="m.key" :value="m.key">{{ modelName(m.key) }}</option>
        </select>
      </label>
      <div class="row2">
        <label>X<input v-model.number="selected.posX" type="number" class="input" /></label>
        <label>Z<input v-model.number="selected.posZ" type="number" class="input" /></label>
      </div>
      <div class="row3">
        <label>{{ t('map.prop.width') }}<input v-model.number="selected.sizeX" type="number" class="input" /></label>
        <label>{{ t('map.prop.height') }}<input v-model.number="selected.sizeY" type="number" class="input" /></label>
        <label>{{ t('map.prop.depth') }}<input v-model.number="selected.sizeZ" type="number" class="input" /></label>
      </div>
      <label>{{ t('map.prop.roofColor') }}<input v-model="selected.color" class="input" :placeholder="t('map.prop.roofColorPh')" /></label>
      <div class="row2">
        <label>{{ t('map.prop.code') }}<input v-model="selected.code" class="input" /></label>
        <label>{{ t('map.prop.manager') }}<input v-model="selected.manager" class="input" /></label>
      </div>
      <div class="row2">
        <label>{{ t('map.prop.capacity') }}<input v-model.number="selected.capacity" type="number" class="input" /></label>
        <label>{{ t('map.prop.used') }}<input v-model.number="selected.used" type="number" class="input" /></label>
      </div>
      <label>{{ t('map.prop.status') }}
        <select v-model="selected.status" class="select">
          <option value="RUNNING">{{ t('status.RUNNING') }}</option>
          <option value="MAINTAIN">{{ t('status.MAINTAIN') }}</option>
          <option value="STOP">{{ t('status.STOP') }}</option>
        </select>
      </label>
      <label>{{ t('map.prop.description') }}<input v-model="selected.description" class="input" /></label>
      <label>{{ t('map.prop.remark') }}<input v-model="selected.remark" class="input" /></label>
      <div class="props-actions">
        <button class="btn primary" @click="save">{{ t('common.save') }}</button>
        <button class="btn danger" @click="remove">{{ t('common.delete') }}</button>
        <span class="save-msg">{{ msg }}</span>
      </div>
    </div>
    <div class="props glass empty-props" v-else>
      <div class="section-title">{{ t('map.props') }}</div>
      <p class="empty-tip">{{ t('map.emptyTip') }}</p>
    </div>
  </div>
</template>

<style scoped>
.map-editor { display: grid; grid-template-columns: 180px 1fr 240px; gap: var(--gap); height: 100%; min-height: 0; }
.palette { padding: 14px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; }
.pal-btn {
  display: flex; align-items: center; gap: 8px; height: 32px; padding: 0 10px;
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: var(--radius-sm);
  font-size: var(--fs-sm); color: var(--text-primary); cursor: pointer; transition: all .2s;
}
.pal-btn:hover { border-color: var(--accent); }
.pal-btn.active { border-color: var(--accent); background: #eff6ff; color: var(--accent); }
.pal-swatch { width: 12px; height: 12px; border-radius: 3px; background: #6b9bd1; }
.pal-swatch[data-model="OFFICE"] { background: #c9d6e6; }
.pal-swatch[data-model="CANTEEN"] { background: #fbbf24; }
.pal-swatch[data-model="QC"] { background: #5ad8a6; }
.pal-swatch[data-model="VISITOR"] { background: #a78bfa; }
.pal-swatch[data-model="WAREHOUSE"] { background: #4f86c6; }
.pal-hint { font-size: var(--fs-xs); color: var(--text-muted); line-height: 1.6; margin-top: 4px; }
.map-canvas { width: 100%; height: 100%; min-height: 0; touch-action: none; }
.bldg { cursor: move; }
.bldg rect { transition: fill .15s; }
.props { padding: 14px; display: flex; flex-direction: column; gap: 8px; overflow-y: auto; }
.props label { display: flex; flex-direction: column; gap: 4px; font-size: var(--fs-xs); color: var(--text-secondary); }
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.row3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px; }
.props-actions { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.save-msg { font-size: var(--fs-xs); color: var(--success); }
.empty-props { align-items: flex-start; }
.empty-tip { font-size: var(--fs-sm); color: var(--text-muted); }
</style>
