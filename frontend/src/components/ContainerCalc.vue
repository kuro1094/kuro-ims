<script setup>
import { reactive, ref, watch } from 'vue'
import { containerApi } from '../api'
import { t } from '../i18n'

const props = defineProps({
  visible: Boolean,
  containers: { type: Array, default: () => [] }
})
const emit = defineEmits(['close'])

const TYPES = ['20GP', '40GP', '40HQ', '45HQ', 'CUSTOM']

const form = reactive({
  containerCode: 'CALC-001',
  containerName: '模拟货柜',
  type: '40HQ',
  length: null,
  width: null,
  height: null,
  maxWeight: 26500,
  goodsList: [
    { name: '伺服电机', qty: 120, length: 60, width: 40, height: 40, weight: 18.5 },
    { name: 'PLC控制器', qty: 80, length: 50, width: 40, height: 30, weight: 12 }
  ]
})

const result = ref(null)
const loading = ref(false)
const error = ref('')

watch(() => form.type, (tp) => {
  if (tp !== 'CUSTOM') form.maxWeight = ({ '20GP': 21700, '40GP': 26500, '40HQ': 26500, '45HQ': 27600 })[tp]
})

watch(() => props.visible, (v) => { if (v) result.value = null })

function addGoods() {
  form.goodsList.push({ name: '', qty: 1, length: 60, width: 40, height: 40, weight: 10 })
}
function removeGoods(i) {
  form.goodsList.splice(i, 1)
}

async function calculate() {
  error.value = ''
  loading.value = true
  try {
    const res = await containerApi.calculate(form)
    result.value = res.data
  } catch (e) {
    error.value = e?.response?.data?.message || e.message || t('calc.failed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-if="visible" class="modal-mask" @click.self="emit('close')">
    <div class="modal glass-deep fade-up">
      <div class="modal-header">
        <div>
          <div class="modal-sub">CONTAINER LOADING</div>
          <div class="modal-title">{{ t('calc.title') }}</div>
        </div>
        <button class="btn close-btn" @click="emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <!-- 柜型 -->
        <div class="block">
          <div class="section-title">{{ t('calc.params') }}</div>
          <div class="grid-4">
            <label>{{ t('calc.containerCode') }}<input v-model="form.containerCode" class="input" /></label>
            <label>{{ t('calc.containerName') }}<input v-model="form.containerName" class="input" /></label>
            <label>{{ t('calc.type') }}
              <select v-model="form.type" class="select">
                <option v-for="tp in TYPES" :key="tp" :value="tp">{{ tp }}</option>
              </select>
            </label>
            <label>{{ t('calc.maxWeight') }}<input v-model.number="form.maxWeight" type="number" class="input" :disabled="form.type !== 'CUSTOM'" /></label>
          </div>
          <div v-if="form.type === 'CUSTOM'" class="grid-3" style="margin-top:8px">
            <label>{{ t('calc.innerL') }}<input v-model.number="form.length" type="number" class="input" /></label>
            <label>{{ t('calc.innerW') }}<input v-model.number="form.width" type="number" class="input" /></label>
            <label>{{ t('calc.innerH') }}<input v-model.number="form.height" type="number" class="input" /></label>
          </div>
        </div>

        <!-- 货物清单 -->
        <div class="block">
          <div class="section-title" style="justify-content:space-between">
            <span>{{ t('calc.goodsList') }}</span>
            <button class="btn" @click="addGoods">{{ t('calc.addGoods') }}</button>
          </div>
          <div class="goods-header goods-row">
            <span>{{ t('calc.col.name') }}</span><span>{{ t('calc.col.qty') }}</span><span>{{ t('calc.col.length') }}</span><span>{{ t('calc.col.width') }}</span><span>{{ t('calc.col.height') }}</span><span>{{ t('calc.col.weight') }}</span><span></span>
          </div>
          <div v-for="(g, i) in form.goodsList" :key="i" class="goods-row">
            <input v-model="g.name" class="input" :placeholder="t('calc.goodsNamePh')" />
            <input v-model.number="g.qty" type="number" class="input" />
            <input v-model.number="g.length" type="number" class="input" />
            <input v-model.number="g.width" type="number" class="input" />
            <input v-model.number="g.height" type="number" class="input" />
            <input v-model.number="g.weight" type="number" class="input" />
            <button class="btn danger" @click="removeGoods(i)">✕</button>
          </div>
        </div>

        <div v-if="error" class="calc-error">{{ error }}</div>

        <button class="btn primary calc-btn" :disabled="loading" @click="calculate">
          {{ loading ? t('calc.calculating') : t('calc.start') }}
        </button>

        <!-- 结果 -->
        <div v-if="result" class="result fade-up">
          <div class="section-title">{{ t('calc.result') }}</div>
          <div class="result-summary">
            <div class="metric">
              <div class="metric-value" :style="{ color: 'var(--accent)' }">{{ result.volumeUtilization }}%</div>
              <div class="metric-label">{{ t('calc.volumeRate') }}</div>
              <div class="metric-sub">{{ result.usedVolumeM3 }} / {{ result.volumeM3 }} m³</div>
            </div>
            <div class="metric">
              <div class="metric-value" :style="{ color: result.weightUtilization > 90 ? 'var(--danger)' : 'var(--success)' }">{{ result.weightUtilization }}%</div>
              <div class="metric-label">{{ t('calc.weightRate') }}</div>
              <div class="metric-sub">{{ result.usedWeight }} / {{ result.maxWeight }} kg</div>
            </div>
            <div class="metric">
              <div class="metric-value" :style="{ color: result.feasible ? 'var(--success)' : 'var(--warning)' }">
                {{ result.feasible ? t('calc.feasibleYes') : t('calc.feasibleNo') }}
              </div>
              <div class="metric-label">{{ t('calc.feasibility') }}</div>
              <div class="metric-sub">{{ result.reason || t('calc.allFit') }}</div>
            </div>
          </div>

          <div class="result-bars">
            <div class="bar-row">
              <span class="bar-label">{{ t('calc.volume') }}</span>
              <div class="bar"><div class="bar-inner volume" :style="{ width: Math.min(100, result.volumeUtilization) + '%' }"></div></div>
              <span class="bar-value">{{ result.volumeUtilization }}%</span>
            </div>
            <div class="bar-row">
              <span class="bar-label">{{ t('calc.weight') }}</span>
              <div class="bar"><div class="bar-inner weight" :style="{ width: Math.min(100, result.weightUtilization) + '%' }"></div></div>
              <span class="bar-value">{{ result.weightUtilization }}%</span>
            </div>
          </div>

          <div v-if="result.placedList?.length" class="result-table">
            <div class="table-title">{{ t('calc.loaded') }}</div>
            <div v-for="(p, i) in result.placedList" :key="i" class="table-row">
              <span>{{ p.name }}</span><span>{{ t('calc.pcs', { n: p.qty }) }}</span><span>{{ p.volumeM3 }} m³</span><span>{{ p.weight }} kg</span>
            </div>
          </div>
          <div v-if="result.leftList?.length" class="result-table">
            <div class="table-title" style="color:var(--warning)">{{ t('calc.unloaded') }}</div>
            <div v-for="(l, i) in result.leftList" :key="i" class="table-row warn">
              <span>{{ l.name }}</span><span>{{ t('calc.left', { n: l.leftQty }) }}</span><span colspan="2">{{ l.reason }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-mask {
  position: fixed; inset: 0; z-index: 100;
  background: rgba(30, 58, 95, 0.35);
  display: flex; align-items: center; justify-content: center;
}
.modal {
  width: 680px; max-width: 94vw; max-height: 88vh;
  display: flex; flex-direction: column;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  padding: 16px 20px; border-bottom: 1px solid var(--glass-border);
}
.modal-sub { font-size: var(--fs-xs); color: var(--accent); letter-spacing: 2px; }
.modal-title { font-size: var(--fs-lg); font-weight: 600; margin-top: 2px; }
.close-btn { width: 26px; height: 26px; padding: 0; }
.modal-body { padding: 16px 20px; overflow-y: auto; display: flex; flex-direction: column; gap: 14px; }
.block { display: flex; flex-direction: column; gap: 8px; }
.grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
label { display: flex; flex-direction: column; gap: 4px; font-size: var(--fs-xs); color: var(--text-secondary); }
.goods-row { display: grid; grid-template-columns: 1.6fr 0.7fr 0.7fr 0.7fr 0.7fr 0.8fr 32px; gap: 6px; align-items: center; }
.goods-header { font-size: var(--fs-xs); color: var(--text-muted); }
.goods-header span { padding-left: 2px; }
.calc-error { font-size: var(--fs-sm); color: var(--danger); text-align: center; }
.calc-btn { height: 34px; width: 100%; }
.result { border-top: 1px solid var(--glass-border); padding-top: 12px; display: flex; flex-direction: column; gap: 12px; }
.result-summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; }
.metric { background: #f8fafc; border: 1px solid var(--glass-border); border-radius: var(--radius-sm); padding: 10px; text-align: center; }
.metric-value { font-size: var(--fs-xl); font-weight: 700; }
.metric-label { font-size: var(--fs-xs); color: var(--text-secondary); margin-top: 2px; }
.metric-sub { font-size: var(--fs-xs); color: var(--text-muted); margin-top: 4px; }
.result-bars { display: flex; flex-direction: column; gap: 6px; }
.bar-row { display: flex; align-items: center; gap: 8px; }
.bar-label { font-size: var(--fs-xs); color: var(--text-secondary); width: 28px; }
.bar { flex: 1; height: 8px; border-radius: 4px; background: #e2e8f0; overflow: hidden; }
.bar-inner { height: 100%; border-radius: 4px; transition: width .5s; }
.bar-inner.volume { background: linear-gradient(90deg, var(--accent), var(--accent-2)); }
.bar-inner.weight { background: linear-gradient(90deg, var(--success), var(--accent)); }
.bar-value { font-size: var(--fs-xs); color: var(--text-primary); width: 44px; text-align: right; }
.result-table { display: flex; flex-direction: column; gap: 4px; }
.table-title { font-size: var(--fs-xs); color: var(--text-secondary); margin-bottom: 2px; }
.table-row {
  display: grid; grid-template-columns: 1.6fr 1fr 1fr 1fr; gap: 6px;
  font-size: var(--fs-sm); padding: 6px 8px;
  background: #f8fafc; border-radius: 6px;
}
.table-row.warn { background: rgba(217,119,6,0.08); color: var(--warning); }
</style>
