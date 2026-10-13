<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { t, locale } from '../i18n'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { RoundedBoxGeometry } from 'three/examples/jsm/geometries/RoundedBoxGeometry.js'

const props = defineProps({
  warehouses: { type: Array, default: () => [] },
  buildings: { type: Array, default: () => [] },
  vehicles: { type: Array, default: () => [] },
  forklifts: { type: Array, default: () => [] },
  containers: { type: Array, default: () => [] },
  patrolVehicles: { type: Array, default: () => [] },
  guards: { type: Array, default: () => [] }
})
const emit = defineEmits(['select'])

const containerRef = ref(null)
const tooltip = ref({ visible: false, x: 0, y: 0, text: '' })

let renderer, scene, camera, controls, raycaster, mouse, lastTime
let animationId = null
let hovered = null
const interactables = []
const movers = [] // { group, speed, path[], t, axis }
let decoGroups = [] // 动态建筑（可来自后台编辑）

// 地图缩放系数：所有业务坐标统一放大，扩大园区空间
const MAP_SCALE = 2.1
// 园区环路（矩形环线）中心线半径
const RING_X = 98
const RING_Z = 78
const RING_PERIM = 2 * (RING_X * 2 + RING_Z * 2)

// 仓库固定槽位（B 区 2×2 厂房集群，门洞朝外侧马路）
const WH_SLOTS = [
  { x: 44, z: -17 },  // WH-A 原材料仓（北排西，门朝北侧马路）
  { x: 74, z: -17 },  // WH-B 成品仓（北排东，门朝北侧马路）
  { x: 44, z: 4 },    // WH-C 冷链仓（南排西，门朝南侧马路）
  { x: 74, z: 4 }     // WH-D 周转仓（南排东，门朝南侧马路）
]

// 装饰性建筑（对应参考图布局，仅展示不交互）
const DECO_BUILDINGS = [
  { name: '行政办公楼', en: 'Administration', x: -68, z: -55, w: 26, d: 15, h: 12, flat: true },
  { name: 'A1生产车间', en: 'Production Workshop', x: -10, z: -55, w: 34, d: 18, h: 9 },
  { name: 'A2装配车间', en: 'Assembly Workshop', x: 52, z: -55, w: 34, d: 18, h: 9 },
  { name: '质检中心', en: 'Quality Inspection', x: -68, z: -8, w: 22, d: 14, h: 7.5 },
  { name: '员工餐厅', en: 'Staff Canteen', x: -21, z: -8, w: 20, d: 13, h: 6.5 },
  { name: '访客中心', en: 'Visitor Center', x: -68, z: 47, w: 24, d: 14, h: 7.5 }
]

const STATUS_COLOR = {
  RUNNING: 0x34d399, MAINTAIN: 0xfbbf24, STOP: 0xf87171,
  IDLE: 0x94a3b8, LOADING: 0x38bdf8, TRANSIT: 0x3b82f6,
  UNLOADING: 0xa78bfa, WORKING: 0x34d399, CHARGING: 0xfbbf24,
  EMPTY: 0x94a3b8, FULL: 0x34d399, SHIPPED: 0xa78bfa
}

onMounted(() => { initScene() })
onBeforeUnmount(() => {
  cancelAnimationFrame(animationId)
  renderer?.dispose()
  window.removeEventListener('resize', onResize)
})

watch(() => [props.warehouses, props.vehicles, props.forklifts, props.containers, props.buildings, props.patrolVehicles, props.guards], rebuild, { deep: true })

function initScene() {
  const el = containerRef.value
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf0f5fb)
  scene.fog = new THREE.Fog(0xe9f1f9, 230, 600)

  camera = new THREE.PerspectiveCamera(42, el.clientWidth / el.clientHeight, 1, 800)
  // 等距斜 45° 鸟瞰视角
  camera.position.set(100, 100, 100)
  camera.lookAt(0, 0, 0)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(el.clientWidth, el.clientHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.shadowMap.enabled = true
  // 柔和软阴影（配合 shadow.radius 模糊边缘）
  renderer.shadowMap.type = THREE.PCFShadowMap
  el.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.06
  controls.maxPolarAngle = Math.PI / 2.15
  controls.minDistance = 15
  controls.maxDistance = 340
  controls.target.set(0, 0, 0)

  // 灯光（柔和漫射天光，无强烈高光）
  scene.add(new THREE.AmbientLight(0xffffff, 1.0))
  const hemi = new THREE.HemisphereLight(0xf0f6ff, 0xd8e2d4, 0.65)
  scene.add(hemi)
  const sun = new THREE.DirectionalLight(0xfff8ee, 1.1)
  sun.position.set(50, 80, 40)
  sun.castShadow = true
  sun.shadow.mapSize.set(2048, 2048)
  sun.shadow.camera.left = -160; sun.shadow.camera.right = 160
  sun.shadow.camera.top = 160; sun.shadow.camera.bottom = -160
  sun.shadow.radius = 10
  sun.shadow.blurSamples = 16
  sun.shadow.bias = -0.0003
  scene.add(sun)

  buildGround()
  buildRoads()
  buildLandscape()
  buildParkingLot()
  buildLoadingZone()

  raycaster = new THREE.Raycaster()
  mouse = new THREE.Vector2()
  lastTime = performance.now()

  el.addEventListener('pointermove', onPointerMove)
  el.addEventListener('click', onClick)
  window.addEventListener('resize', onResize)

  rebuild()
  animate()
}

function buildGround() {
  const ground = new THREE.Mesh(
    new THREE.PlaneGeometry(580, 580),
    new THREE.MeshStandardMaterial({ color: 0xf3f6fa, roughness: 1 })
  )
  ground.rotation.x = -Math.PI / 2
  ground.receiveShadow = true
  scene.add(ground)

  // 园区核心硬化地面（浅灰水泥）
  const core = new THREE.Mesh(
    new THREE.PlaneGeometry(220, 176),
    new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.95 })
  )
  core.rotation.x = -Math.PI / 2
  core.position.y = 0.015
  core.receiveShadow = true
  scene.add(core)

  // 外围淡蓝边界线
  const W = 220 / 2, D = 176 / 2
  const borderPts = [
    new THREE.Vector3(-W, 0.03, -D), new THREE.Vector3(W, 0.03, -D),
    new THREE.Vector3(W, 0.03, D), new THREE.Vector3(-W, 0.03, D),
    new THREE.Vector3(-W, 0.03, -D)
  ]
  scene.add(new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(borderPts),
    new THREE.LineBasicMaterial({ color: 0x7aa5d8, transparent: true, opacity: 0.6 })
  ))
}

function buildRoads() {
  const roadMat = new THREE.MeshStandardMaterial({ color: 0xc7d2ea, roughness: 0.95 })
  const RW = 7 // 路宽

  // 园区环路（矩形环线，对应参考图外圈道路）
  const segs = [
    { w: RING_X * 2 + RW, h: RW, x: 0, z: -RING_Z }, // 顶边
    { w: RING_X * 2 + RW, h: RW, x: 0, z: RING_Z },  // 底边
    { w: RW, h: RING_Z * 2 + RW, x: -RING_X, z: 0 }, // 左边
    { w: RW, h: RING_Z * 2 + RW, x: RING_X, z: 0 }   // 右边
  ]
  // 内部道路：两条横向 + 一条纵向 + 入口延伸段
  segs.push(
    { w: RING_X * 2, h: RW, x: 0, z: -34 },          // 上横路（顶部建筑区与中部之间）
    { w: RING_X * 2, h: RW, x: 0, z: 26 },           // 下横路（中部与底部之间）
    { w: RW, h: 2 * RING_Z, x: -42, z: 0 },          // 纵向路（左侧建筑列之间）
    { w: RW, h: RING_Z - 26 + RW, x: -26, z: (RING_Z + 26) / 2 } // 入口延伸段（底部大门至下横路）
  )
  segs.forEach(s => {
    const road = new THREE.Mesh(new THREE.PlaneGeometry(s.w, s.h), roadMat)
    road.rotation.x = -Math.PI / 2
    road.position.set(s.x, 0.03, s.z)
    road.receiveShadow = true
    scene.add(road)
  })

  // 车道白色虚线（环路顶/底边 + 内部横向路）
  const dashMat = new THREE.MeshBasicMaterial({ color: 0xffffff })
  const dashRows = [-RING_Z, RING_Z, -34, 26]
  dashRows.forEach(z => {
    for (let x = -RING_X + 4; x <= RING_X - 4; x += 6) {
      const dash = new THREE.Mesh(new THREE.PlaneGeometry(2, 0.22), dashMat)
      dash.rotation.x = -Math.PI / 2
      dash.position.set(x, 0.045, z)
      scene.add(dash)
    }
  })
  // 环路左/右边虚线
  ;[-RING_X, RING_X].forEach(x => {
    for (let z = -RING_Z + 4; z <= RING_Z - 4; z += 6) {
      const dash = new THREE.Mesh(new THREE.PlaneGeometry(0.22, 2), dashMat)
      dash.rotation.x = -Math.PI / 2
      dash.position.set(x, 0.045, z)
      scene.add(dash)
    }
  })
}

// 环路上的点：沿矩形环线取坐标与朝向（t 为沿环线的距离）
function ringPoint(t) {
  const W = RING_X * 2, H = RING_Z * 2
  const segs = [
    { len: W, fx: RING_X, fz: -RING_Z, dx: -1, dz: 0 },   // 顶边：向左
    { len: H, fx: -RING_X, fz: -RING_Z, dx: 0, dz: 1 },   // 左边：向下
    { len: W, fx: -RING_X, fz: RING_Z, dx: 1, dz: 0 },    // 底边：向右
    { len: H, fx: RING_X, fz: RING_Z, dx: 0, dz: -1 }     // 右边：向上
  ]
  let rem = ((t % RING_PERIM) + RING_PERIM) % RING_PERIM
  for (const s of segs) {
    if (rem <= s.len) {
      return {
        x: s.fx + s.dx * rem,
        z: s.fz + s.dz * rem,
        rot: Math.atan2(s.dx, s.dz)
      }
    }
    rem -= s.len
  }
  return { x: RING_X, z: -RING_Z, rot: 0 }
}

// 园区景观：树木、路灯、绿篱、围栏、入口大门、环线指示、消防通道
function buildLandscape() {
  const trunkMat = new THREE.MeshStandardMaterial({ color: 0xa88b6a, roughness: 1 })
  const leafMat = new THREE.MeshStandardMaterial({ color: 0x93c9a1, roughness: 0.9 })
  const lampMat = new THREE.MeshStandardMaterial({ color: 0x9aa7b5 })
  const lampGlow = new THREE.MeshBasicMaterial({ color: 0xfff3d6 })

  const rng = mulberry(42)
  // 树木散布在园区外围
  for (let i = 0; i < 46; i++) {
    const angle = rng() * Math.PI * 2
    const r = 125 + rng() * 70
    const x = Math.cos(angle) * r
    const z = Math.sin(angle) * r * 0.82
    const tree = new THREE.Group()
    const h = 2.2 + rng() * 2.2
    const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.22, 0.3, h * 0.4, 8), trunkMat)
    trunk.position.y = h * 0.2
    tree.add(trunk)
    // 简约棒棒糖树冠（小巧圆球，参考图样式）
    const crown = new THREE.Mesh(new THREE.SphereGeometry(0.85 + rng() * 0.45, 10, 8), leafMat)
    crown.position.y = h * 0.4 + 0.85
    crown.castShadow = true
    tree.add(crown)
    const crown2 = new THREE.Mesh(new THREE.SphereGeometry(0.45 + rng() * 0.25, 8, 6), leafMat)
    crown2.position.set(0.35 - rng() * 0.7, h * 0.4 + 1.4, 0.3 - rng() * 0.6)
    tree.add(crown2)
    tree.position.set(x, 0, z)
    tree.rotation.y = rng() * Math.PI
    scene.add(tree)
  }

  // 环路内侧绿篱（圆形灌木，对应参考图沿路绿化带）
  const bushMat = new THREE.MeshStandardMaterial({ color: 0x9ccfa4, roughness: 0.9 })
  const hedgeLine = (x1, z1, x2, z2, step) => {
    const dx = x2 - x1, dz = z2 - z1
    const len = Math.hypot(dx, dz)
    for (let d = 0; d <= len; d += step) {
      const x = x1 + dx * d / len, z = z1 + dz * d / len
      if (Math.abs(z - RING_Z) < 6 && x > -38 && x < -14) continue // 跳过入口
      const bush = new THREE.Mesh(new THREE.SphereGeometry(0.75, 8, 6), bushMat)
      bush.scale.y = 0.75
      bush.position.set(x, 0.5, z)
      bush.castShadow = true
      scene.add(bush)
    }
  }
  hedgeLine(-91, -71, 91, -71, 9)
  hedgeLine(-91, 71, 91, 71, 9)
  hedgeLine(-90, -65, -90, 65, 9)
  hedgeLine(90, -65, 90, 65, 9)

  // 路灯沿内部横向路
  for (let x = -78; x <= 78; x += 39) {
    ;[-39.6, -28.4, 20.4, 31.6].forEach(z => {
      if (Math.abs(x + 42) < 6 && (z === 20.4 || z === 31.6 || z === -28.4 || z === -39.6)) return
      const lamp = new THREE.Group()
      const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.08, 0.12, 5, 8), lampMat)
      pole.position.y = 2.5
      lamp.add(pole)
      const head = new THREE.Mesh(new THREE.SphereGeometry(0.22, 10, 10), lampGlow)
      head.position.y = 5
      lamp.add(head)
      lamp.position.set(x, 0, z)
      scene.add(lamp)
    })
  }

  // 外围围栏（淡蓝线条）
  const fenceMat = new THREE.LineBasicMaterial({ color: 0x6b9bd1, transparent: true, opacity: 0.45 })
  const W = 220 / 2, D = 176 / 2
  const pts = [
    new THREE.Vector3(-W, 0.6, -D), new THREE.Vector3(W, 0.6, -D),
    new THREE.Vector3(W, 0.6, D), new THREE.Vector3(-W, 0.6, D),
    new THREE.Vector3(-W, 0.6, -D)
  ]
  scene.add(new THREE.Line(new THREE.BufferGeometry().setFromPoints(pts), fenceMat))

  // 环线行车方向（蓝色虚线，对应参考图外圈蓝色箭头线）
  const loopPts = [
    new THREE.Vector3(-105, 0.05, -84.5), new THREE.Vector3(105, 0.05, -84.5),
    new THREE.Vector3(105, 0.05, 84.5), new THREE.Vector3(-105, 0.05, 84.5),
    new THREE.Vector3(-105, 0.05, -84.5)
  ]
  const loopLine = new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(loopPts),
    new THREE.LineDashedMaterial({ color: 0x3b82f6, dashSize: 2.2, gapSize: 1.6 })
  )
  loopLine.computeLineDistances()
  scene.add(loopLine)

  // 消防通道（右侧红色虚线，对应参考图 Fire Lane）
  const firePts = [new THREE.Vector3(94, 0.05, -68), new THREE.Vector3(94, 0.05, 68)]
  const fireLine = new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(firePts),
    new THREE.LineDashedMaterial({ color: 0xdc2626, dashSize: 1.6, gapSize: 1.1 })
  )
  fireLine.computeLineDistances()
  scene.add(fireLine)
  const fireLabel = locale.value === 'en' ? makeLabel(t('scene.fireLaneEn'), t('scene.fireLane')) : makeLabel(t('scene.fireLane'), t('scene.fireLaneEn'))
  fireLabel.scale.set(5, 1.6, 1)
  fireLabel.position.set(94, 3.5, -64)
  scene.add(fireLabel)

  // B 区仓库装卸场围合（绿色虚线，对应参考图绿色围合区域）
  const yardPts = [
    new THREE.Vector3(31, 0.05, -31), new THREE.Vector3(90, 0.05, -31),
    new THREE.Vector3(90, 0.05, 18), new THREE.Vector3(31, 0.05, 18),
    new THREE.Vector3(31, 0.05, -31)
  ]
  const yardLine = new THREE.Line(
    new THREE.BufferGeometry().setFromPoints(yardPts),
    new THREE.LineDashedMaterial({ color: 0x4caf50, dashSize: 1.4, gapSize: 0.9 })
  )
  yardLine.computeLineDistances()
  scene.add(yardLine)

  // 入口大门（底部，对应参考图红色定位针位置）
  const gate = new THREE.Group()
  const pillarMat = new THREE.MeshStandardMaterial({ color: 0xc3d2e4, roughness: 0.6 })
  ;[-4.6, 4.6].forEach(dx => {
    const pillar = new THREE.Mesh(new THREE.BoxGeometry(0.8, 5.4, 0.8), pillarMat)
    pillar.position.set(dx, 2.7, 0)
    pillar.castShadow = true
    gate.add(pillar)
  })
  const beam = new THREE.Mesh(new THREE.BoxGeometry(10, 0.9, 0.6), pillarMat)
  beam.position.set(0, 5.2, 0)
  gate.add(beam)
  const sign = new THREE.Mesh(new THREE.BoxGeometry(7.2, 0.5, 0.2),
    new THREE.MeshBasicMaterial({ color: 0x3b82f6, transparent: true, opacity: 0.75 }))
  sign.position.set(0, 5.2, 0.1)
  gate.add(sign)
  gate.position.set(-26, 0, RING_Z)
  scene.add(gate)

  // 入口红色定位针（对应参考图）
  const pin = new THREE.Group()
  const pinMat = new THREE.MeshStandardMaterial({ color: 0xdc2626, roughness: 0.35 })
  const pinHead = new THREE.Mesh(new THREE.SphereGeometry(0.9, 14, 12), pinMat)
  pinHead.position.y = 4.6
  pin.add(pinHead)
  const pinTip = new THREE.Mesh(new THREE.ConeGeometry(0.7, 2.4, 12), pinMat)
  pinTip.rotation.x = Math.PI
  pinTip.position.y = 2.6
  pin.add(pinTip)
  const pinDot = new THREE.Mesh(new THREE.SphereGeometry(0.32, 10, 8),
    new THREE.MeshBasicMaterial({ color: 0xffffff }))
  pinDot.position.y = 4.7
  pin.add(pinDot)
  pin.position.set(-35, 0, RING_Z + 3)
  scene.add(pin)
}

// B 区装卸作业区黄色标线（仓库门前黄色粗斜纹带 + 作业区边线）
function buildLoadingZone() {
  const yMat = new THREE.MeshBasicMaterial({ color: 0xf2c94c })
  // 北排门前（朝 z=-26 马路）与南排门前（朝 z=20 马路）黄色斜纹装卸区
  const bands = [{ z: -26.5, len: 1.2 }, { z: 13, len: 2.6 }]
  bands.forEach(b => {
    for (let x = 34; x <= 84; x += 2.4) {
      const stripe = new THREE.Mesh(new THREE.PlaneGeometry(0.42, b.len), yMat)
      stripe.rotation.x = -Math.PI / 2
      stripe.rotation.z = Math.PI / 4
      stripe.position.set(x, 0.05, b.z)
      scene.add(stripe)
    }
  })
  // 装卸作业区黄色边线（B 区围合内侧，北边缘让开门前斜纹带）
  const edges = [
    { w: 57, h: 0.35, x: 60.5, z: 17 },
    { w: 0.35, h: 49, x: 32, z: -6.5 },
    { w: 0.35, h: 49, x: 89, z: -6.5 }
  ]
  edges.forEach(e => {
    const line = new THREE.Mesh(new THREE.PlaneGeometry(e.w, e.h), yMat)
    line.rotation.x = -Math.PI / 2
    line.position.set(e.x, 0.05, e.z)
    scene.add(line)
  })
}

// 停车场（底部右侧，对应参考图 Parking Lot）
function buildParkingLot() {
  const PX = 47, PZ = 48 // 中心
  const PW = 73, PD = 34
  const lot = new THREE.Mesh(
    new THREE.PlaneGeometry(PW, PD),
    new THREE.MeshStandardMaterial({ color: 0xdfe7f2, roughness: 0.95 })
  )
  lot.rotation.x = -Math.PI / 2
  lot.position.set(PX, 0.035, PZ)
  lot.receiveShadow = true
  scene.add(lot)

  // 车位白线（两排，对面布置）
  const lineMat = new THREE.MeshBasicMaterial({ color: 0xf8fafc })
  const rows = [{ z: PZ - 10.4, face: 1 }, { z: PZ + 10.4, face: -1 }]
  rows.forEach(r => {
    for (let i = 0; i <= 12; i++) {
      const x = PX - 31.2 + i * 5.2
      const line = new THREE.Mesh(new THREE.PlaneGeometry(0.14, 8.3), lineMat)
      line.rotation.x = -Math.PI / 2
      line.position.set(x, 0.05, r.z)
      scene.add(line)
    }
    // 车位底线
    const base = new THREE.Mesh(new THREE.PlaneGeometry(62.4, 0.14), lineMat)
    base.rotation.x = -Math.PI / 2
    base.position.set(PX, 0.05, r.z + r.face * 4.15)
    scene.add(base)
  })

  const label = locale.value === 'en' ? makeLabel(t('scene.parkingLotEn'), t('scene.parkingLot')) : makeLabel(t('scene.parkingLot'), t('scene.parkingLotEn'))
  label.scale.set(5, 1.6, 1)
  label.position.set(PX, 4, PZ)
  scene.add(label)
}

// 装饰性建筑：低多边形壳体 + 标签（支持后台下发的动态建筑数据）
const MODEL_EN = { OFFICE: 'Office', WORKSHOP: 'Workshop', WAREHOUSE: 'Warehouse', CANTEEN: 'Canteen', QC: 'Quality Center', VISITOR: 'Visitor Center' }

function buildDecoBuilding(b) {
  const g = new THREE.Group()
  // 兼容硬编码常量与后端字段两种结构
  const bx = b.posX ?? b.x, bz = b.posZ ?? b.z
  const w = b.sizeX ?? b.w, d = b.sizeZ ?? b.d, h = b.sizeY ?? b.h
  const roofColor = b.color ? new THREE.Color(b.color) : new THREE.Color(0x4f86c6)

  const wall = new THREE.Mesh(
    new THREE.BoxGeometry(w, h, d),
    new THREE.MeshStandardMaterial({ color: 0xf5f8fc, roughness: 0.7, metalness: 0.1 })
  )
  wall.position.y = h / 2
  wall.castShadow = true
  g.add(wall)

  // 建筑外轮廓蓝色包边
  const edgeLines = new THREE.LineSegments(
    new THREE.EdgesGeometry(wall.geometry),
    new THREE.LineBasicMaterial({ color: 0x4f86c6, transparent: true, opacity: 0.85 })
  )
  edgeLines.position.copy(wall.position)
  g.add(edgeLines)

  // 统一平屋顶：浅灰白屋面板 + 蓝色檐口包边 + 顶部设备间
  const slab = new THREE.Mesh(
    new THREE.BoxGeometry(w + 0.3, 0.28, d + 0.3),
    new THREE.MeshStandardMaterial({ color: 0xe9eef5, roughness: 0.85, metalness: 0.05 })
  )
  slab.position.y = h + 0.14
  slab.castShadow = true
  g.add(slab)
  ;[
    { w: w + 0.44, d: 0.2, x: 0, z: d / 2 + 0.1 },
    { w: w + 0.44, d: 0.2, x: 0, z: -d / 2 - 0.1 },
    { w: 0.2, d: d, x: w / 2 + 0.1, z: 0 },
    { w: 0.2, d: d, x: -w / 2 - 0.1, z: 0 }
  ].forEach(p => {
    const parapet = new THREE.Mesh(new THREE.BoxGeometry(p.w, 0.5, p.d),
      new THREE.MeshStandardMaterial({ color: roofColor, roughness: 0.55, metalness: 0.1 }))
    parapet.position.set(p.x, h + 0.25, p.z)
    g.add(parapet)
  })
  const box = new THREE.Mesh(
    new THREE.BoxGeometry(Math.min(5, w * 0.25), 1.2, Math.min(4, d * 0.35)),
    new THREE.MeshStandardMaterial({ color: 0xdde5ef, roughness: 0.6, metalness: 0.2 })
  )
  box.position.set(-w / 4, h + 0.88, 0)
  box.castShadow = true
  g.add(box)

  // 主入口门（朝最近道路一侧，白色门 + 蓝色门框）
  const door = new THREE.Mesh(
    new THREE.BoxGeometry(2.6, 2.4, 0.16),
    new THREE.MeshStandardMaterial({ color: 0xeef3f9, roughness: 0.5, metalness: 0.2 })
  )
  door.position.set(0, 1.2, d / 2 * (bz > -20 ? -1 : 1))
  g.add(door)

  const enName = b.en || MODEL_EN[b.model] || ''
  const label = locale.value === 'en' && enName ? makeLabel(enName, b.name) : makeLabel(b.name, enName)
  label.position.set(0, h + 3.6, 0)
  g.add(label)

  g.position.set(bx, 0, bz)
  return g
}

// 渲染动态建筑列表（优先使用后台数据，无数据时回退默认布局）
function renderDecoBuildings() {
  decoGroups.forEach(g => scene.remove(g))
  decoGroups = []
  const list = props.buildings.length ? props.buildings : DECO_BUILDINGS
  list.forEach(b => {
    const g = buildDecoBuilding(b)
    decoGroups.push(g)
    scene.add(g)
  })
}

// 可复现的伪随机数
function mulberry(seed) {
  return function () {
    seed |= 0; seed = seed + 0x6D2B79F5 | 0
    let t = Math.imul(seed ^ seed >>> 15, 1 | seed)
    t = t + Math.imul(t ^ t >>> 7, 61 | t) ^ t
    return ((t ^ t >>> 14) >>> 0) / 4294967296
  }
}

function makeLabel(text, sub) {
  const canvas = document.createElement('canvas')
  canvas.width = 256; canvas.height = 80
  const ctx = canvas.getContext('2d')
  ctx.fillStyle = 'rgba(255,255,255,0.92)'
  roundRect(ctx, 8, 8, 240, sub ? 64 : 44, 10)
  ctx.fill()
  ctx.strokeStyle = 'rgba(59,130,246,0.55)'
  ctx.lineWidth = 2
  roundRect(ctx, 8, 8, 240, sub ? 64 : 44, 10)
  ctx.stroke()
  ctx.fillStyle = '#1e3a8a'
  ctx.font = '600 22px "PingFang SC", sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText(text, 128, 38)
  if (sub) {
    ctx.fillStyle = '#64748b'
    ctx.font = '16px "PingFang SC", sans-serif'
    ctx.fillText(sub, 128, 62)
  }
  const tex = new THREE.CanvasTexture(canvas)
  const sprite = new THREE.Sprite(new THREE.SpriteMaterial({ map: tex, transparent: true, depthWrite: false }))
  sprite.scale.set(7, 2.2, 1)
  return sprite
}

function roundRect(ctx, x, y, w, h, r) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

function buildWarehouse(wh) {
  const group = new THREE.Group()
  const W = wh.sizeX, D = wh.sizeZ, H = wh.sizeY

  // 钢结构厂房配色
  const wallColor = 0xf5f8fc        // 白色墙面
  const roofColor = 0x4f86c6        // 蓝色彩钢屋顶
  const trimBlue = 0x3b7bd4         // 粗蓝色包边
  const RECESS = 0.7                // 装卸门洞内凹深度（局部 +Z 面）

  const wallMat = new THREE.MeshStandardMaterial({ color: wallColor, roughness: 0.7, metalness: 0.1 })
  const trimMat = new THREE.MeshStandardMaterial({ color: trimBlue, roughness: 0.5, metalness: 0.15 })

  // 主体墙体（圆角壳体，门洞侧内退 RECESS，由墙垛补平形成真实凹槽）
  const body = new THREE.Mesh(
    new RoundedBoxGeometry(W, H, D - RECESS, 2, 0.15),
    wallMat
  )
  body.position.set(0, H / 2, -RECESS / 2)
  body.castShadow = true
  body.userData = { type: 'warehouse', data: wh, root: group }
  group.add(body)

  // 粗蓝色包边：四角立柱 + 顶部檐带 + 底部基座（兼作月台边缘）
  const cornerGeo = new THREE.BoxGeometry(0.5, H + 0.06, 0.5)
  ;[[-W / 2, -D / 2], [W / 2, -D / 2], [-W / 2, D / 2], [W / 2, D / 2]].forEach(([x, z]) => {
    const c = new THREE.Mesh(cornerGeo, trimMat)
    c.position.set(x, H / 2, z)
    c.castShadow = true
    group.add(c)
  })
  const band = new THREE.Mesh(new THREE.BoxGeometry(W + 0.3, 0.36, D + 0.3), trimMat)
  band.position.set(0, H - 0.14, 0)
  group.add(band)
  const plinth = new THREE.Mesh(new THREE.BoxGeometry(W + 0.25, 0.4, D + 0.25), trimMat)
  plinth.position.set(0, 0.2, 0)
  group.add(plinth)

  // 墙面竖向波纹彩钢板纹理（背立面 + 两侧山墙面）
  const ribPts = []
  for (let x = -W / 2 + 1.1; x < W / 2 - 0.5; x += 1.1) {
    ribPts.push(new THREE.Vector3(x, 0.45, -D / 2 - 0.02), new THREE.Vector3(x, H - 0.4, -D / 2 - 0.02))
  }
  for (let z = -D / 2 + 1.1; z < D / 2 - RECESS; z += 1.1) {
    ribPts.push(new THREE.Vector3(W / 2 + 0.02, 0.45, z), new THREE.Vector3(W / 2 + 0.02, H - 0.4, z))
    ribPts.push(new THREE.Vector3(-W / 2 - 0.02, 0.45, z), new THREE.Vector3(-W / 2 - 0.02, H - 0.4, z))
  }
  group.add(new THREE.LineSegments(
    new THREE.BufferGeometry().setFromPoints(ribPts),
    new THREE.LineBasicMaterial({ color: 0xc9d8ea, transparent: true, opacity: 0.6 })
  ))

  // 多跨连体蓝色斜坡彩钢屋顶（沿 X 方向多跨拼接）
  const spans = Math.max(2, Math.round(W / 7))
  const sw = W / spans
  const roofH = Math.min(1.6, sw * 0.3)
  const roofMat = new THREE.MeshStandardMaterial({ color: roofColor, roughness: 0.55, metalness: 0.15 })
  const ridgeMat = new THREE.MeshBasicMaterial({ color: 0xdce7f5 })
  const seamPts = []
  for (let i = 0; i < spans; i++) {
    const cx = -W / 2 + sw * (i + 0.5)
    const shape = new THREE.Shape()
    shape.moveTo(-sw / 2 - 0.12, 0)
    shape.lineTo(0, roofH)
    shape.lineTo(sw / 2 + 0.12, 0)
    shape.closePath()
    const geo = new THREE.ExtrudeGeometry(shape, { depth: D + 0.5, bevelEnabled: false })
    geo.translate(0, 0, -(D + 0.5) / 2)
    const roof = new THREE.Mesh(geo, roofMat)
    roof.position.set(cx, H, 0)
    roof.castShadow = true
    group.add(roof)
    // 屋脊亮线
    const ridgeLine = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.08, D + 0.5), ridgeMat)
    ridgeLine.position.set(cx, H + roofH + 0.02, 0)
    group.add(ridgeLine)
    // 彩钢板横向分割线（跨坡面）
    for (let z = -D / 2 + 2.2; z < D / 2 - 0.6; z += 2.2) {
      seamPts.push(
        new THREE.Vector3(cx - sw / 2 - 0.12, H + 0.03, z), new THREE.Vector3(cx, H + roofH + 0.03, z),
        new THREE.Vector3(cx, H + roofH + 0.03, z), new THREE.Vector3(cx + sw / 2 + 0.12, H + 0.03, z)
      )
    }
  }
  group.add(new THREE.LineSegments(
    new THREE.BufferGeometry().setFromPoints(seamPts),
    new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.35 })
  ))
  // 蓝色天沟（两长侧檐口）
  ;[-1, 1].forEach(s => {
    const gutter = new THREE.Mesh(new THREE.BoxGeometry(W + 0.5, 0.16, 0.16), trimMat)
    gutter.position.set(0, H - 0.02, s * (D / 2 + 0.26))
    group.add(gutter)
  })

  // 屋顶通风器（中间跨屋脊上）
  const ventMat = new THREE.MeshStandardMaterial({ color: 0xdde5ef, roughness: 0.55, metalness: 0.25 })
  const midCx = -W / 2 + sw * (Math.floor(spans / 2) + 0.5)
  ;[-D / 5, D / 5].forEach(z => {
    const vent = new THREE.Mesh(new THREE.CylinderGeometry(0.4, 0.48, 0.7, 10), ventMat)
    vent.position.set(midCx, H + roofH + 0.32, z)
    group.add(vent)
    const cap = new THREE.Mesh(new THREE.SphereGeometry(0.46, 10, 8, 0, Math.PI * 2, 0, Math.PI / 2), ventMat)
    cap.position.set(midCx, H + roofH + 0.67, z)
    group.add(cap)
  })

  // 屋脊状态灯带（中间跨）
  const ridge = new THREE.Mesh(
    new THREE.BoxGeometry(0.12, 0.1, D * 0.6),
    new THREE.MeshBasicMaterial({ color: STATUS_COLOR[wh.status] || 0x34d399 })
  )
  ridge.position.set(midCx, H + roofH + 0.09, 0)
  group.add(ridge)

  // 内凹装卸门洞（局部 +Z 面，凹槽内摆放托盘纸箱）
  const doorCount = Math.max(2, Math.floor(W / 5))
  const doorXs = []
  for (let i = 0; i < doorCount; i++) doorXs.push(-W / 2 + 2.2 + i * ((W - 4) / Math.max(1, doorCount - 1)))
  const doorHalf = 1.15, doorH = 2.7
  // 门间墙垛（白色，补平凹槽开口之间的墙面）
  let cur = -W / 2
  const addPier = (cx, w) => {
    const pier = new THREE.Mesh(new THREE.BoxGeometry(w, H, RECESS), wallMat)
    pier.position.set(cx, H / 2, D / 2 - RECESS / 2)
    pier.castShadow = true
    group.add(pier)
  }
  doorXs.forEach(x => {
    if (x - doorHalf - cur > 0.05) addPier((cur + x - doorHalf) / 2, x - doorHalf - cur)
    cur = x + doorHalf
  })
  if (W / 2 - cur > 0.05) addPier((cur + W / 2) / 2, W / 2 - cur)
  // 门洞上方门头墙体
  const header = new THREE.Mesh(new THREE.BoxGeometry(W, H - doorH, RECESS), wallMat)
  header.position.set(0, doorH + (H - doorH) / 2, D / 2 - RECESS / 2)
  header.castShadow = true
  group.add(header)

  const darkMat = new THREE.MeshStandardMaterial({ color: 0x3b4a61, roughness: 0.9 })
  const bumperMat = new THREE.MeshBasicMaterial({ color: 0xf2c94c })
  const palletMat = new THREE.MeshStandardMaterial({ color: 0xb08968, roughness: 0.8 })
  const goodsMat = new THREE.MeshStandardMaterial({ color: 0x5B8FF9, roughness: 0.6 })
  const goodsMat2 = new THREE.MeshStandardMaterial({ color: 0xdbe5f2, roughness: 0.7 })
  doorXs.forEach((x, i) => {
    // 凹槽背板（深色洞口）
    const opening = new THREE.Mesh(new THREE.BoxGeometry(doorHalf * 2, doorH, 0.08), darkMat)
    opening.position.set(x, doorH / 2 + 0.4, D / 2 - RECESS + 0.06)
    group.add(opening)
    // 蓝色门框（横梁 + 立柱）
    const lintel = new THREE.Mesh(new THREE.BoxGeometry(doorHalf * 2 + 0.36, 0.2, 0.24), trimMat)
    lintel.position.set(x, doorH + 0.5, D / 2 + 0.02)
    group.add(lintel)
    ;[-doorHalf - 0.09, doorHalf + 0.09].forEach(dx => {
      const post = new THREE.Mesh(new THREE.BoxGeometry(0.2, doorH + 0.4, 0.24), trimMat)
      post.position.set(x + dx, (doorH + 0.4) / 2 + 0.4, D / 2 + 0.02)
      group.add(post)
    })
    // 黄色防撞柱
    ;[-doorHalf + 0.28, doorHalf - 0.28].forEach(dx => {
      const bumper = new THREE.Mesh(new THREE.BoxGeometry(0.22, 0.5, 0.22), bumperMat)
      bumper.position.set(x + dx, 0.65, D / 2 - 0.12)
      group.add(bumper)
    })
    // 凹槽内托盘纸箱货物（隔门摆放）
    if (i % 2 === 0) {
      const pallet = new THREE.Mesh(new THREE.BoxGeometry(1.15, 0.14, 0.95), palletMat)
      pallet.position.set(x - 0.2, 0.47, D / 2 - RECESS / 2 - 0.05)
      group.add(pallet)
      for (let j = 0; j < 2; j++) {
        const box = new THREE.Mesh(new THREE.BoxGeometry(0.85, 0.55, 0.7), j % 2 ? goodsMat : goodsMat2)
        box.position.set(x - 0.2, 0.82 + j * 0.58, D / 2 - RECESS / 2 - 0.05)
        box.castShadow = true
        group.add(box)
      }
    }
  })

  // 状态灯柱
  const lampPole = new THREE.Mesh(
    new THREE.CylinderGeometry(0.07, 0.07, 1.6, 8),
    new THREE.MeshStandardMaterial({ color: 0x94a3b8 })
  )
  lampPole.position.set(W / 2 - 1, H + 0.8, D / 2 - 1)
  group.add(lampPole)
  const lamp = new THREE.Mesh(
    new THREE.SphereGeometry(0.32, 16, 16),
    new THREE.MeshBasicMaterial({ color: STATUS_COLOR[wh.status] || 0x34d399 })
  )
  lamp.position.set(W / 2 - 1, H + 1.75, D / 2 - 1)
  group.add(lamp)

  // 库外托盘堆（单排，门洞前月台边缘，不占道）
  const palletCount = Math.min(3 + Math.round((wh.used / wh.capacity) * 5), 4)
  for (let i = 0; i < palletCount; i++) {
    const px = -W / 2 + 2 + (i % 4) * 2.4
    const pz = D / 2 + 1.5
    const pallet = new THREE.Mesh(new THREE.BoxGeometry(1.6, 0.18, 1.6), palletMat)
    pallet.position.set(px, 0.09, pz)
    pallet.castShadow = true
    group.add(pallet)
    const stackH = 1 + (i % 3)
    for (let j = 0; j < stackH; j++) {
      const box = new THREE.Mesh(
        new THREE.BoxGeometry(1.28, 0.7, 1.28),
        (i + j) % 2 ? goodsMat : goodsMat2
      )
      box.position.set(px, 0.55 + j * 0.75, pz)
      box.rotation.y = (i * 0.3 + j * 0.12) % 0.5 - 0.25
      box.castShadow = true
      box.userData = { type: 'pallet', data: { warehouse: wh.name, layer: j + 1 }, root: pallet }
      group.add(box)
    }
  }

  // 标签
  const label = makeLabel(wh.name, `${wh.used}/${wh.capacity} ${t('scene.palletSlots')}`)
  label.position.set(0, H + roofH + 2.4, 0)
  group.add(label)

  group.position.set(wh.locationX * MAP_SCALE, 0, wh.locationZ * MAP_SCALE)
  // 门洞面（局部 +Z）朝外侧马路：北排朝北（z=-26 路），南排朝南（z=20 路）
  group.rotation.y = wh.locationZ < 0 ? Math.PI : 0
  group.userData = { type: 'warehouse', data: wh }
  scene.add(group)
  interactables.push(body)
  return group
}

function buildVehicle(v, index, stallIndex = 0) {
  const group = new THREE.Group()
  const isContainer = v.type === 'CONTAINER'

  // 车辆配色：白色主调（参考图白色卡车）
  const cabColor = 0xf8fafc
  const paintMat = new THREE.MeshStandardMaterial({ color: cabColor, roughness: 0.45, metalness: 0.15 })
  const darkMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.8 })
  const steelMat = new THREE.MeshStandardMaterial({ color: 0xcbd5e1, metalness: 0.85, roughness: 0.25 })
  const glassMat = new THREE.MeshPhysicalMaterial({ color: 0x9cc8ee, roughness: 0.05, metalness: 0.4, transparent: true, opacity: 0.9 })

  // 大梁
  const frame = new THREE.Mesh(new THREE.BoxGeometry(1.9, 0.3, 7.6), darkMat)
  frame.position.set(0, 0.62, -0.2)
  group.add(frame)

  // 驾驶室（圆润低多边形造型）
  const cab = new THREE.Mesh(new RoundedBoxGeometry(2.3, 2.3, 2.2, 2, 0.14), paintMat)
  cab.position.set(0, 1.95, 2.55)
  cab.castShadow = true
  group.add(cab)

  // 前脸进气格栅
  const grille = new THREE.Mesh(new THREE.BoxGeometry(1.7, 0.9, 0.08), darkMat)
  grille.position.set(0, 1.35, 3.67)
  group.add(grille)
  // 格栅横条
  for (let i = 0; i < 4; i++) {
    const bar = new THREE.Mesh(new THREE.BoxGeometry(1.5, 0.06, 0.1), steelMat)
    bar.position.set(0, 1.05 + i * 0.22, 3.68)
    group.add(bar)
  }

  // 挡风玻璃（内嵌深色）
  const windshield = new THREE.Mesh(new THREE.BoxGeometry(2.0, 0.85, 0.06), glassMat)
  windshield.position.set(0, 2.45, 3.66)
  group.add(windshield)
  // 侧窗
  ;[-1.16, 1.16].forEach(x => {
    const side = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.7, 1.5), glassMat)
    side.position.set(x, 2.45, 2.6)
    group.add(side)
  })
  // 窗框分隔柱
  const pillar = new THREE.Mesh(new THREE.BoxGeometry(0.1, 0.85, 0.08), paintMat)
  pillar.position.set(0, 2.45, 3.67)
  group.add(pillar)

  // 车顶导流罩
  const deflector = new THREE.Mesh(new THREE.BoxGeometry(2.2, 0.9, 1.1), paintMat)
  deflector.position.set(0, 3.45, 2.1)
  deflector.rotation.x = 0.35
  deflector.castShadow = true
  group.add(deflector)

  // 后视镜
  ;[-1.35, 1.35].forEach(x => {
    const arm = new THREE.Mesh(new THREE.BoxGeometry(0.35, 0.05, 0.05), darkMat)
    arm.position.set(x - Math.sign(x) * 0.15, 2.7, 3.5)
    group.add(arm)
    const mirror = new THREE.Mesh(new THREE.BoxGeometry(0.06, 0.4, 0.25), darkMat)
    mirror.position.set(x, 2.55, 3.5)
    group.add(mirror)
  })

  // 保险杠 + 车灯组
  const bumper = new THREE.Mesh(new THREE.BoxGeometry(2.35, 0.4, 0.3), steelMat)
  bumper.position.set(0, 0.85, 3.68)
  group.add(bumper)
  ;[-0.85, 0.85].forEach(x => {
    // 大灯
    const head = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.25, 0.1),
      new THREE.MeshBasicMaterial({ color: 0xfffbe6 }))
    head.position.set(x, 1.3, 3.69)
    group.add(head)
    // 雾灯
    const fog = new THREE.Mesh(new THREE.BoxGeometry(0.25, 0.12, 0.08),
      new THREE.MeshBasicMaterial({ color: 0xffe9a8 }))
    fog.position.set(x, 0.95, 3.85)
    group.add(fog)
  })

  // 登车踏板（两级）
  ;[-1.28, 1.28].forEach(x => {
    const step1 = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.08, 0.9), steelMat)
    step1.position.set(x, 0.5, 2.4)
    group.add(step1)
    const step2 = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.08, 0.9), steelMat)
    step2.position.set(x, 1.0, 2.4)
    group.add(step2)
  })

  // 油箱 + 排气管
  const tank = new THREE.Mesh(new THREE.CylinderGeometry(0.35, 0.35, 1.4, 14), steelMat)
  tank.rotation.x = Math.PI / 2
  tank.position.set(-1.15, 0.85, 0.9)
  group.add(tank)
  const exhaust = new THREE.Mesh(new THREE.CylinderGeometry(0.09, 0.09, 2.4, 10), steelMat)
  exhaust.position.set(1.15, 2.4, 1.5)
  group.add(exhaust)

  // 鞍座（第五轮）
  const saddle = new THREE.Mesh(new THREE.CylinderGeometry(0.55, 0.65, 0.2, 16), darkMat)
  saddle.position.set(0, 0.95, -0.3)
  group.add(saddle)

  // 挂车 / 货柜
  const cargoColor = 0xf1f5f9
  const cargoLen = isContainer ? 5.6 : 4.6
  const trailerMat = new THREE.MeshStandardMaterial({ color: cargoColor, roughness: 0.6, metalness: 0.1 })
  const cargo = new THREE.Mesh(new RoundedBoxGeometry(2.4, 2.4, cargoLen, 2, 0.12), trailerMat)
  cargo.position.set(0, 2.0, -1.5)
  cargo.castShadow = true
  group.add(cargo)
  // 箱顶角线（蓝色描边）
  const trim = new THREE.Mesh(new THREE.BoxGeometry(2.46, 0.1, cargoLen + 0.04),
    new THREE.MeshStandardMaterial({ color: 0x6b9bd1, metalness: 0.4, roughness: 0.35 }))
  trim.position.set(0, 3.22, -1.5)
  group.add(trim)
  // 箱侧竖梁
  const postMat = new THREE.MeshStandardMaterial({ color: 0x94a3b8, metalness: 0.5, roughness: 0.4 })
  ;[-1.15, 1.15].forEach(x => {
    ;[-3.1, -1.5, 0.1].forEach(z => {
      const post = new THREE.Mesh(new THREE.BoxGeometry(0.08, 2.4, 0.12), postMat)
      post.position.set(x, 2.0, z)
      group.add(post)
    })
  })
  // 挂车支腿
  ;[-0.9, 0.9].forEach(x => {
    const leg = new THREE.Mesh(new THREE.BoxGeometry(0.12, 1.1, 0.12), darkMat)
    leg.position.set(x, 0.55, -0.1)
    group.add(leg)
  })

  // 车轮：前转向轮 + 驱动轮 + 挂车轮，共 10 轮位
  const wheelMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.9 })
  const hubMat = new THREE.MeshStandardMaterial({ color: 0xe2e8f0, metalness: 0.8, roughness: 0.25 })
  const wheelGeo = new THREE.CylinderGeometry(0.48, 0.48, 0.36, 20)
  const hubGeo = new THREE.CylinderGeometry(0.2, 0.2, 0.38, 12)
  const wheelZ = [2.55, 0.35, -0.75, -2.5, -3.4]
  ;[-1.08, 1.08].forEach(x => {
    wheelZ.forEach(z => {
      const wheel = new THREE.Mesh(wheelGeo, wheelMat)
      wheel.rotation.z = Math.PI / 2
      wheel.position.set(x, 0.48, z)
      wheel.castShadow = true
      group.add(wheel)
      const hub = new THREE.Mesh(hubGeo, hubMat)
      hub.rotation.z = Math.PI / 2
      hub.position.set(x, 0.48, z)
      group.add(hub)
    })
  })
  // 后轮挡泥板
  const fender = new THREE.Mesh(new THREE.BoxGeometry(2.5, 0.08, 1.6), darkMat)
  fender.position.set(0, 1.05, -0.2)
  group.add(fender)

  // 状态灯（车顶）
  const lamp = new THREE.Mesh(
    new THREE.SphereGeometry(0.18, 12, 12),
    new THREE.MeshBasicMaterial({ color: STATUS_COLOR[v.status] || 0x94a3b8 })
  )
  lamp.position.set(0, 3.95, 2.0)
  group.add(lamp)

  const label = makeLabel(v.plateNo, v.status === 'TRANSIT' ? t('scene.inTransit', { n: v.progress }) : statusText(v.status))
  label.scale.set(5, 1.6, 1)
  label.position.set(0, 4.8, 0)
  group.add(label)

  cab.userData = { type: 'vehicle', data: v, root: group }
  cargo.userData = { type: 'vehicle', data: v, root: group }
  interactables.push(cab, cargo)

  if (v.status === 'TRANSIT') {
    // 在途车辆沿园区环路行驶
    const t = (v.progress / 100) * RING_PERIM + index * 40
    const p = ringPoint(t)
    group.position.set(p.x, 0, p.z)
    group.rotation.y = p.rot
    movers.push({ group, kind: 'truck', t, speed: 7 + index })
  } else {
    // 其余车辆停放在停车场车位（两排，随停车场位置同步）
    const col = stallIndex % 12, row = Math.floor(stallIndex / 12) % 2
    group.position.set(47 - 31.2 + col * 5.2, 0, 48 + (row === 0 ? -10.4 : 10.4))
    group.rotation.y = row === 0 ? Math.PI : 0
  }
  scene.add(group)
}

function buildForklift(f, index) {
  const group = new THREE.Group()

  // 参考图蓝色叉车
  const bodyMat = new THREE.MeshStandardMaterial({ color: 0x3b82f6, roughness: 0.5, metalness: 0.1 })
  const body = new THREE.Mesh(new RoundedBoxGeometry(1.5, 0.9, 2.2, 2, 0.14), bodyMat)
  body.position.y = 0.65
  body.castShadow = true
  group.add(body)

  // 尾部配重
  const counter = new THREE.Mesh(
    new RoundedBoxGeometry(1.5, 0.7, 0.5, 2, 0.12),
    new THREE.MeshStandardMaterial({ color: 0x2563eb, roughness: 0.5 })
  )
  counter.position.set(0, 0.75, -1.3)
  group.add(counter)

  // 驾驶室护顶架（四柱 + 顶棚）
  const guardMat = new THREE.MeshStandardMaterial({ color: 0x475569, roughness: 0.5, metalness: 0.5 })
  ;[[-0.62, 0.1], [0.62, 0.1], [-0.62, -0.9], [0.62, -0.9]].forEach(([x, z]) => {
    const post = new THREE.Mesh(new THREE.BoxGeometry(0.08, 1.3, 0.08), guardMat)
    post.position.set(x, 1.75, z)
    group.add(post)
  })
  const roof = new THREE.Mesh(new THREE.BoxGeometry(1.4, 0.08, 1.15), guardMat)
  roof.position.set(0, 2.42, -0.4)
  group.add(roof)

  // 座椅
  const seat = new THREE.Mesh(
    new THREE.BoxGeometry(0.55, 0.5, 0.5),
    new THREE.MeshStandardMaterial({ color: 0x64748b, roughness: 0.8 })
  )
  seat.position.set(0, 1.35, -0.55)
  group.add(seat)

  // 二级门架 + 货叉
  const mastMat = new THREE.MeshStandardMaterial({ color: 0x64748b, metalness: 0.6, roughness: 0.35 })
  ;[-0.5, 0.5].forEach(x => {
    const outer = new THREE.Mesh(new THREE.BoxGeometry(0.14, 2.4, 0.14), mastMat)
    outer.position.set(x, 1.5, 1.05)
    group.add(outer)
    const inner = new THREE.Mesh(new THREE.BoxGeometry(0.1, 2.6, 0.1), mastMat)
    inner.position.set(x, 1.6, 0.92)
    group.add(inner)
  })
  // 货叉架
  const carriage = new THREE.Mesh(new THREE.BoxGeometry(1.15, 0.7, 0.08), mastMat)
  carriage.position.set(0, 0.85, 0.95)
  group.add(carriage)
  // 两根货叉
  ;[-0.38, 0.38].forEach(x => {
    const fork = new THREE.Mesh(new THREE.BoxGeometry(0.14, 0.06, 0.95), mastMat)
    fork.position.set(x, 0.42, 1.45)
    group.add(fork)
  })

  // 轮子
  const wheelMat = new THREE.MeshStandardMaterial({ color: 0x334155 })
  ;[[-0.7, 0.7], [0.7, 0.7], [-0.7, -0.7], [0.7, -0.7]].forEach(([x, z]) => {
    const wheel = new THREE.Mesh(new THREE.CylinderGeometry(0.3, 0.3, 0.24, 12), wheelMat)
    wheel.rotation.z = Math.PI / 2
    wheel.position.set(x, 0.3, z)
    group.add(wheel)
  })

  // 作业中载货（叉尖上）
  if (f.status === 'WORKING') {
    const load = new THREE.Mesh(
      new THREE.BoxGeometry(1.05, 0.75, 1.05),
      new THREE.MeshStandardMaterial({ color: 0x5ad8a6, roughness: 0.6 })
    )
    load.position.set(0, 0.84, 1.45)
    load.castShadow = true
    group.add(load)
  }

  const lamp = new THREE.Mesh(
    new THREE.SphereGeometry(0.16, 12, 12),
    new THREE.MeshBasicMaterial({ color: STATUS_COLOR[f.status] || 0x94a3b8 })
  )
  lamp.position.set(0, 2.56, -0.4)
  group.add(lamp)

  const label = makeLabel(f.code, statusText(f.status))
  label.scale.set(4, 1.3, 1)
  label.position.set(0, 3.2, 0)
  group.add(label)

  body.userData = { type: 'forklift', data: f, root: group }
  roof.userData = { type: 'forklift', data: f, root: group }
  interactables.push(body, roof)

  // 叉车布置在南排仓库门前三货区（仓库与马路之间，不压马路）
  const baseX = 42 + (index % 4) * 10.5
  const baseZ = 17
  group.position.set(baseX, 0, baseZ)
  scene.add(group)

  if (f.status === 'WORKING') {
    movers.push({ group, kind: 'forklift', cx: baseX, cz: baseZ, radius: 1.8 + index * 0.35, speed: 0.25 + index * 0.06, t: Math.random() * Math.PI * 2 })
  }
}

function buildContainer(c, index) {
  const group = new THREE.Group()
  const len = c.type.startsWith('20') ? 4 : c.type.startsWith('45') ? 9 : 7.6
  const color = { '20GP': 0x5ad8a6, '40GP': 0x5b8ff9, '40HQ': 0xf6bd16, '45HQ': 0xa78bfa }[c.type] || 0x5b8ff9

  const box = new THREE.Mesh(
    new THREE.BoxGeometry(len, 2.6, 2.4),
    new THREE.MeshStandardMaterial({ color, roughness: 0.65, metalness: 0.05 })
  )
  box.position.y = 1.3
  box.castShadow = true
  box.userData = { type: 'container', data: c, root: group }
  group.add(box)

  // 瓦楞侧板（竖向凹槽线条）
  const ribMat = new THREE.LineBasicMaterial({ color: 0x000000, transparent: true, opacity: 0.22 })
  const ribPts = []
  const ribCount = Math.floor(len / 0.6)
  for (let i = 1; i < ribCount; i++) {
    const x = -len / 2 + i * 0.6
    ribPts.push(new THREE.Vector3(x, 0.15, 1.21), new THREE.Vector3(x, 2.45, 1.21))
    ribPts.push(new THREE.Vector3(x, 0.15, -1.21), new THREE.Vector3(x, 2.45, -1.21))
  }
  const ribs = new THREE.LineSegments(new THREE.BufferGeometry().setFromPoints(ribPts), ribMat)
  group.add(ribs)

  // 角件（八 corner castings）
  const cornerMat = new THREE.MeshStandardMaterial({ color: 0x64748b, metalness: 0.6, roughness: 0.4 })
  ;[[-len/2, -1.2], [len/2, -1.2], [-len/2, 1.2], [len/2, 1.2]].forEach(([x, z]) => {
    ;[0.15, 2.45].forEach(y => {
      const corner = new THREE.Mesh(new THREE.BoxGeometry(0.28, 0.3, 0.28), cornerMat)
      corner.position.set(x, y, z)
      group.add(corner)
    })
  })

  const edges = new THREE.LineSegments(
    new THREE.EdgesGeometry(box.geometry),
    new THREE.LineBasicMaterial({ color: 0xffffff, transparent: true, opacity: 0.25 })
  )
  edges.position.copy(box.position)
  group.add(edges)

  const label = makeLabel(c.code, c.type)
  label.scale.set(4, 1.3, 1)
  label.position.set(0, 3.4, 0)
  group.add(label)

  // 集装箱堆场：停车场东侧空地，两列纵向码放（避开马路与消防通道）
  group.position.set(87.5 + (index % 2) * 3.2, 0, 36 + Math.floor(index / 2) * 13)
  group.rotation.y = Math.PI / 2
  interactables.push(box)
  scene.add(group)
}

function statusText(s) {
  return t('status.' + s) !== 'status.' + s ? t('status.' + s) : s
}

let lastDataKey = '' // 数据指纹：无变化时跳过重建，避免周期性闪烁

// 语言切换时强制重建标签
watch(locale, () => { lastDataKey = ''; rebuild() })

// ============ 园区巡逻资产 ============
const blinkers = [] // { mat, phase } 警灯/定位器呼吸灯（随 rebuild 清空重建）

// 默认数据（后台无数据时的回退）
const DEFAULT_PATROLS = [
  { code: 'PV-01', status: 'PATROL' },
  { code: 'PV-02', status: 'PATROL' }
]
const DEFAULT_GUARDS = [
  { code: 'SEC-01', status: 'ON_PATROL' },
  { code: 'SEC-02', status: 'ON_PATROL' },
  { code: 'SEC-03', status: 'ON_PATROL' },
  { code: 'SEC-04', status: 'ON_PATROL' }
]

// 巡逻车：白色圆润车身 + 蓝腰线 + 红蓝爆闪灯，沿园区环路巡逻
function buildPatrolCar(pv, i) {
  const group = new THREE.Group()
  const white = new THREE.MeshStandardMaterial({ color: 0xf8fafc, roughness: 0.4, metalness: 0.1 })
  const blue = new THREE.MeshStandardMaterial({ color: 0x3b7bd4, roughness: 0.5 })

  const body = new THREE.Mesh(new RoundedBoxGeometry(1.9, 0.75, 4.2, 2, 0.22), white)
  body.position.y = 0.75
  body.castShadow = true
  group.add(body)
  const stripe = new THREE.Mesh(new THREE.BoxGeometry(1.94, 0.16, 4.24), blue)
  stripe.position.y = 0.82
  group.add(stripe)
  const cabin = new THREE.Mesh(new RoundedBoxGeometry(1.6, 0.65, 2.0, 2, 0.18), white)
  cabin.position.set(0, 1.4, -0.2)
  cabin.castShadow = true
  group.add(cabin)
  const win = new THREE.Mesh(new RoundedBoxGeometry(1.64, 0.36, 1.7, 2, 0.12),
    new THREE.MeshStandardMaterial({ color: 0x9fc3e8, roughness: 0.15, metalness: 0.3 }))
  win.position.set(0, 1.44, -0.2)
  group.add(win)

  // 红蓝爆闪警灯
  const bar = new THREE.Mesh(new THREE.BoxGeometry(1.0, 0.1, 0.4),
    new THREE.MeshStandardMaterial({ color: 0x334155 }))
  bar.position.set(0, 1.78, -0.2)
  group.add(bar)
  const redMat = new THREE.MeshBasicMaterial({ color: 0xef4444, transparent: true })
  const blueMat = new THREE.MeshBasicMaterial({ color: 0x3b82f6, transparent: true })
  const lr = new THREE.Mesh(new THREE.SphereGeometry(0.11, 8, 8), redMat)
  lr.position.set(-0.32, 1.9, -0.2)
  const lb = new THREE.Mesh(new THREE.SphereGeometry(0.11, 8, 8), blueMat)
  lb.position.set(0.32, 1.9, -0.2)
  group.add(lr, lb)
  blinkers.push({ mat: redMat, phase: i * 0.7 }, { mat: blueMat, phase: i * 0.7 + Math.PI })

  // 车轮
  const wheelGeo = new THREE.CylinderGeometry(0.34, 0.34, 0.28, 12)
  const wheelMat = new THREE.MeshStandardMaterial({ color: 0x334155, roughness: 0.9 })
  ;[[-0.95, 1.35], [0.95, 1.35], [-0.95, -1.35], [0.95, -1.35]].forEach(([x, z]) => {
    const w = new THREE.Mesh(wheelGeo, wheelMat)
    w.rotation.z = Math.PI / 2
    w.position.set(x, 0.34, z)
    group.add(w)
  })

  const label = makeLabel(pv.code || `PV-0${i + 1}`, t('scene.patrolling'))
  label.scale.set(4, 1.3, 1)
  label.position.set(0, 2.9, 0)
  group.add(label)

  group.userData = { type: 'patrol' }
  // 可点击：弹出巡逻车详情
  body.userData = { type: 'patrolVehicle', data: pv, root: group }
  cabin.userData = body.userData
  interactables.push(body, cabin)
  scene.add(group)
  if (pv.status === 'PATROL') {
    movers.push({ group, kind: 'truck', t: (i * RING_PERIM) / 2 + 30, speed: 5.5 })
  } else {
    // 非巡逻状态停放在入口大门旁
    group.position.set(-34, 0, RING_Z - 10 - i * 6)
    group.rotation.y = Math.PI / 2
  }
}

// 巡检保安步行环线（避开仓库月台、停车场与集装箱堆场）
const GUARD_LOOPS = [
  // 园区主路环线（上横路 → 右环路 → 下横路 → 左纵路）
  [{ x: -42, z: -34 }, { x: 94, z: -34 }, { x: 94, z: 26 }, { x: -42, z: 26 }],
  // 停车场环线
  [{ x: 6, z: 28 }, { x: 80, z: 28 }, { x: 80, z: 68 }, { x: 6, z: 68 }]
]

// 巡检保安：蓝色工装小人，肩部携带定位器（呼吸光点）
function buildGuard(gd, i) {
  const group = new THREE.Group()
  const uniform = new THREE.MeshStandardMaterial({ color: 0x3b7bd4, roughness: 0.7 })

  const body = new THREE.Mesh(new THREE.CapsuleGeometry(0.24, 0.62, 4, 10), uniform)
  body.position.y = 0.75
  body.castShadow = true
  group.add(body)
  const head = new THREE.Mesh(new THREE.SphereGeometry(0.17, 10, 8),
    new THREE.MeshStandardMaterial({ color: 0xf2c9a0, roughness: 0.7 }))
  head.position.y = 1.32
  group.add(head)
  const cap = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.19, 0.12, 10),
    new THREE.MeshStandardMaterial({ color: 0x1e3a5f, roughness: 0.6 }))
  cap.position.y = 1.46
  group.add(cap)

  // 定位器呼吸光点
  const beaconMat = new THREE.MeshBasicMaterial({ color: 0x22d3ee, transparent: true })
  const beacon = new THREE.Mesh(new THREE.SphereGeometry(0.07, 8, 8), beaconMat)
  beacon.position.set(0.2, 1.12, 0)
  group.add(beacon)
  blinkers.push({ mat: beaconMat, phase: i * 1.3 })

  const label = makeLabel(gd.code || `SEC-0${i + 1}`, t('scene.onPatrol'))
  label.scale.set(3.4, 1.15, 1)
  label.position.set(0, 2.1, 0)
  group.add(label)

  group.userData = { type: 'patrol' }
  // 可点击：弹出保安详情
  body.userData = { type: 'guard', data: gd, root: group }
  head.userData = body.userData
  interactables.push(body, head)
  const path = GUARD_LOOPS[i % GUARD_LOOPS.length]
  const seg = i % path.length
  const dist = (i % 2) * 30
  const a = path[seg], b = path[(seg + 1) % path.length]
  group.position.set(a.x, 0, a.z)
  group.rotation.y = Math.atan2(b.x - a.x, b.z - a.z)
  scene.add(group)
  if (gd.status === 'ON_PATROL') {
    movers.push({ group, kind: 'walk', path, seg, dist, speed: 1.6 })
  } else {
    // 非巡检状态在入口大门旁待命
    group.position.set(-32, 0, RING_Z - 8 - i * 3)
  }
}

function rebuild() {
  const key = JSON.stringify([props.warehouses, props.vehicles, props.forklifts, props.containers, props.buildings, props.patrolVehicles, props.guards])
  if (key === lastDataKey) return
  lastDataKey = key
  // 清空旧对象
  for (const obj of interactables.splice(0)) {
    const root = obj.userData?.root
    if (root && root.parent) root.parent.remove(root)
  }
  movers.splice(0)
  blinkers.splice(0)
  // 移除场景中的 labels / 遗留
  for (let i = scene.children.length - 1; i >= 0; i--) {
    const child = scene.children[i]
    if (child.userData?.type) scene.remove(child)
  }

  renderDecoBuildings()
  // 仓库按参考图固定槽位摆放（B1/B2 仓库区）
  props.warehouses.forEach((wh, i) => {
    const s = WH_SLOTS[i % WH_SLOTS.length]
    buildWarehouse({ ...wh, locationX: s.x / MAP_SCALE, locationZ: s.z / MAP_SCALE })
  })
  // 非在途车辆按序停入停车场车位
  let stallIdx = 0
  props.vehicles.forEach((v, i) => buildVehicle(v, i, v.status === 'TRANSIT' ? -1 : stallIdx++))
  props.forklifts.forEach(buildForklift)
  props.containers.forEach(buildContainer)
  // 园区巡逻车 + 巡检保安（优先后台数据，无数据回退默认 2 车 4 人）
  const pvs = props.patrolVehicles.length ? props.patrolVehicles : DEFAULT_PATROLS
  const gds = props.guards.length ? props.guards : DEFAULT_GUARDS
  pvs.forEach((pv, i) => buildPatrolCar(pv, i))
  gds.forEach((gd, i) => buildGuard(gd, i))
}

function onPointerMove(e) {
  const el = containerRef.value
  const rect = el.getBoundingClientRect()
  mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1
  mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1

  raycaster.setFromCamera(mouse, camera)
  const hits = raycaster.intersectObjects(interactables, false)
  if (hits.length > 0) {
    const obj = hits[0].object
    if (hovered !== obj) {
      resetHover()
      hovered = obj
      if (obj.material.emissive) {
        obj.userData._emissive = obj.material.emissive.getHex()
        obj.material.emissive.setHex(0x3b82f6)
        obj.material.emissiveIntensity = 0.35
      }
      el.style.cursor = 'pointer'
      const d = obj.userData.data
      const name = d.name || d.plateNo || d.code || d.warehouse || t('scene.pallet')
      tooltip.value = { visible: true, x: e.clientX - rect.left + 14, y: e.clientY - rect.top + 8, text: `${typeText(obj.userData.type)} · ${name}` }
    }
    tooltip.value.x = e.clientX - rect.left + 14
    tooltip.value.y = e.clientY - rect.top + 8
  } else {
    resetHover()
    el.style.cursor = 'grab'
    tooltip.value.visible = false
  }
}

function resetHover() {
  if (hovered && hovered.material.emissive && hovered.userData._emissive !== undefined) {
    hovered.material.emissive.setHex(hovered.userData._emissive)
    hovered.material.emissiveIntensity = 0
  }
  hovered = null
}

function onClick() {
  if (hovered) {
    emit('select', { type: hovered.userData.type, data: hovered.userData.data })
  }
}

function typeText(tp) {
  return t('typeName.' + tp) !== 'typeName.' + tp ? t('typeName.' + tp) : tp
}

function animate() {
  animationId = requestAnimationFrame(animate)
  const now = performance.now()
  const dt = Math.min((now - lastTime) / 1000, 0.1)
  lastTime = now

  for (const m of movers) {
    if (m.kind === 'truck') {
      m.t = (m.t + m.speed * dt) % RING_PERIM
      const p = ringPoint(m.t)
      m.group.position.set(p.x, 0, p.z)
      m.group.rotation.y = p.rot
    } else if (m.kind === 'forklift') {
      m.t += m.speed * dt
      const nx = m.cx + Math.cos(m.t) * m.radius
      const nz = m.cz + Math.sin(m.t) * m.radius
      m.group.rotation.y = -m.t
      m.group.position.set(nx, 0, nz)
    } else if (m.kind === 'walk') {
      // 沿步行环线前进（seg 为当前边，dist 为边上已走距离）
      const a = m.path[m.seg], b = m.path[(m.seg + 1) % m.path.length]
      const len = Math.hypot(b.x - a.x, b.z - a.z)
      m.dist += m.speed * dt
      if (m.dist >= len) {
        m.dist -= len
        m.seg = (m.seg + 1) % m.path.length
      }
      const k = m.dist / len
      m.group.position.set(a.x + (b.x - a.x) * k, 0, a.z + (b.z - a.z) * k)
      m.group.rotation.y = Math.atan2(b.x - a.x, b.z - a.z)
    }
  }
  // 警灯 / 定位器呼吸闪烁
  const bt = now * 0.004
  for (const b of blinkers) b.mat.opacity = 0.35 + 0.65 * Math.abs(Math.sin(bt + b.phase))
  controls.update()
  renderer.render(scene, camera)
}

function onResize() {
  const el = containerRef.value
  if (!el) return
  camera.aspect = el.clientWidth / el.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(el.clientWidth, el.clientHeight)
}
</script>

<template>
  <div ref="containerRef" class="scene3d">
    <div v-if="tooltip.visible" class="scene-tooltip glass" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
      {{ tooltip.text }}
    </div>
  </div>
</template>

<style scoped>
.scene3d {
  position: absolute;
  inset: 0;
  cursor: grab;
}
.scene3d:active { cursor: grabbing; }
.scene-tooltip {
  position: absolute;
  padding: 4px 10px;
  font-size: var(--fs-xs);
  color: var(--text-primary);
  pointer-events: none;
  z-index: 10;
  white-space: nowrap;
}
</style>
