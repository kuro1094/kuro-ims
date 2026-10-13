// 轻量双语 i18n：默认英文，localStorage 持久化
import { ref } from 'vue'

export const locale = ref(localStorage.getItem('kuro-locale') || 'en')

export function setLocale(l) {
  locale.value = l
  localStorage.setItem('kuro-locale', l)
}

export function toggleLocale() {
  setLocale(locale.value === 'zh' ? 'en' : 'zh')
}

const dict = {
  en: {
    common: {
      save: 'Save', cancel: 'Cancel', edit: 'Edit', delete: 'Delete',
      actions: 'Actions', noData: 'No data', name: 'Name', status: 'Status', type: 'Type',
      confirmDelete: 'Delete this record?'
    },
    status: {
      RUNNING: 'Running', MAINTAIN: 'Maintenance', STOP: 'Stopped',
      IDLE: 'Idle', LOADING: 'Loading', TRANSIT: 'In Transit', UNLOADING: 'Unloading',
      WORKING: 'Working', CHARGING: 'Charging', EMPTY: 'Empty', FULL: 'Full', SHIPPED: 'Shipped',
      PATROL: 'Patrolling', ON_PATROL: 'On Patrol'
    },
    taskStatus: { PENDING: 'Pending', DOING: 'In Progress', DONE: 'Done', CANCEL: 'Cancelled' },
    taskType: { INBOUND: 'Inbound', OUTBOUND: 'Outbound', TRANSFER: 'Transfer', LOADING: 'Loading' },
    deviceStatus: { RUNNING: 'Running', IDLE: 'Standby', ALARM: 'Alarm', STOP: 'Stopped' },
    vehicleStatus: { IDLE: 'Idle', LOADING: 'Loading', TRANSIT: 'In Transit', UNLOADING: 'Unloading' },
    workerStatus: { ON: 'Active', OFF: 'On Leave', LEAVE: 'Resigned' },
    warehouseType: { NORMAL: 'General', COLD: 'Cold Chain', HAZARD: 'Hazmat' },
    vehicleType: { TRUCK: 'Truck', VAN: 'Van', CONTAINER: 'Container Truck' },
    typeName: { warehouse: 'Warehouse', vehicle: 'Truck', forklift: 'Forklift', container: 'Container', pallet: 'Pallet', patrolVehicle: 'Patrol Vehicle', guard: 'Security Guard' },
    home: {
      brandSub: 'Park Warehouse Digital Twin',
      searchPh: 'Search warehouse / plate / code, Enter to locate',
      doingTasks: 'Active Tasks', transitVehicles: 'In Transit', alarms: 'Alarms',
      admin: 'Admin', containerCalc: 'Load Calc', logout: 'Logout',
      hint: '🖱 Drag to rotate · Scroll to zoom · Click buildings / vehicles / forklifts for details'
    },
    side: {
      overview: 'Operations Overview',
      stat: {
        warehouseCount: 'Warehouses', containerCount: 'Containers', goodsCount: 'Goods',
        vehicleCount: 'Vehicles', forkliftCount: 'Forklifts', alarmDeviceCount: 'Alarms'
      },
      tab: { tasks: 'Tasks', devices: 'Devices', vehicles: 'Vehicles' },
      empty: { tasks: 'No tasks', devices: 'No devices', vehicles: 'No vehicles' },
      standby: 'Standby',
      unit: { warehouse: '', container: '', goods: '', vehicle: '', forklift: '', alarm: '' }
    },
    detail: {
      title: 'Details',
      field: {
        code: 'Code', type: 'Type', manager: 'Manager', capacity: 'Capacity',
        utilization: 'Utilization', status: 'Status', size: 'Dimensions', description: 'Description',
        plateNo: 'Plate No.', driver: 'Driver', phone: 'Phone', route: 'Route',
        progress: 'Progress', cargo: 'Cargo', eta: 'ETA',
        name: 'Name', operator: 'Operator', battery: 'Battery', currentTask: 'Current Task',
        containerType: 'Container Type', innerSize: 'Inner Size', volume: 'Volume',
        maxWeight: 'Max Load', location: 'Location',
        belongWarehouse: 'Warehouse', layer: 'Layer',
        area: 'Area', locatorNo: 'Locator No.'
      },
      palletSlots: 'pallet slots', auto: 'Auto', layerN: 'Layer {n}', storing: 'Stored'
    },
    login: {
      sub: 'Smart Workshop Monitoring · 3D Warehouse Visualization',
      tabLogin: 'Sign In', tabRegister: 'Sign Up',
      username: 'Username', password: 'Password', confirmPassword: 'Confirm Password',
      nickname: 'Nickname (optional)',
      loading: 'Please wait…', submitLogin: 'Sign In', submitRegister: 'Sign Up',
      demo: 'Demo account: admin / 123456',
      hasAccount: 'Have an account?', backToLogin: 'Back to Sign In',
      errFill: 'Please fill in all fields', errPwdMismatch: 'Passwords do not match', errFailed: 'Operation failed'
    },
    admin: {
      sub: 'Admin Console', back: 'Back to Dashboard',
      menu: { map: 'Map Editor', warehouse: 'Warehouses', vehicle: 'Vehicles', forklift: 'Forklifts', worker: 'Staff', patrol: 'Patrol Cars', guard: 'Guards' },
      title: { warehouse: 'Warehouse', vehicle: 'Vehicle', forklift: 'Forklift', worker: 'Staff', patrol: 'Patrol Vehicle', guard: 'Guard' },
      add: 'Add', edit: 'Edit', del: 'Delete', actions: 'Actions', noData: 'No data',
      cancel: 'Cancel', save: 'Save', saveFailed: 'Save failed',
      confirmDelete: 'Delete this record?', list: '{title} List',
      field: {
        plateNo: 'Plate No.', driver: 'Driver', phone: 'Phone', type: 'Type', status: 'Status',
        line: 'Route', cargoDesc: 'Cargo', origin: 'Origin', destination: 'Destination',
        progress: 'Progress %', cargoDescLong: 'Cargo Description',
        code: 'Code', name: 'Name', operator: 'Operator', battery: 'Battery %', currentTask: 'Current Task',
        workerCode: 'Employee ID', workerName: 'Full Name', role: 'Role', department: 'Department', area: 'Area', remark: 'Remark',
        manager: 'Manager', capacity: 'Capacity', used: 'Used', sizeX: 'Length m', sizeZ: 'Width m', sizeY: 'Height m', description: 'Description',
        locatorNo: 'Locator No.', route: 'Route'
      }
    },
    map: {
      models: 'Building Models',
      model: {
        WAREHOUSE: 'Warehouse', WORKSHOP: 'Workshop', OFFICE: 'Office',
        CANTEEN: 'Canteen', QC: 'QC Center', VISITOR: 'Visitor Center'
      },
      hintPlacing: 'Click anywhere on the map to place; click the button again to cancel',
      hintIdle: 'Select a model, then click the map to place it; drag to move buildings',
      parkingLot: 'Parking Lot',
      props: 'Building Properties',
      prop: {
        name: 'Name', model: 'Model', width: 'Width', height: 'Height', depth: 'Depth',
        roofColor: 'Roof Color', roofColorPh: '#6b9bd1 (default blue)', remark: 'Remark',
        code: 'Code', manager: 'Manager', capacity: 'Capacity', used: 'Used', status: 'Status', description: 'Description'
      },
      saved: 'Saved', emptyTip: 'Click a building on the map to edit',
      confirmDelete: 'Delete "{name}"?'
    },
    scene: {
      inTransit: 'In Transit {n}%', pallet: 'Pallet', palletSlots: 'slots',
      patrolling: 'Patrolling', onPatrol: 'On Patrol',
      fireLane: '消防通道', fireLaneEn: 'Fire Lane',
      parkingLot: '停车场', parkingLotEn: 'Parking Lot'
    },
    calc: {
      title: 'Container Load Calculator',
      params: 'Container Parameters',
      containerCode: 'Container Code', containerName: 'Container Name', type: 'Type',
      maxWeight: 'Max Load kg', innerL: 'Length cm', innerW: 'Width cm', innerH: 'Height cm',
      goodsList: 'Cargo List', addGoods: '+ Add Cargo',
      col: { name: 'Name', qty: 'Qty', length: 'L(cm)', width: 'W(cm)', height: 'H(cm)', weight: 'kg/pc' },
      goodsNamePh: 'Cargo name',
      start: 'Calculate', calculating: 'Calculating…', failed: 'Calculation failed',
      result: 'Result',
      volumeRate: 'Volume Utilization', weightRate: 'Weight Utilization',
      feasibility: 'Feasibility', feasibleYes: 'Feasible', feasibleNo: 'Exceeded',
      allFit: 'All cargo fits',
      volume: 'Volume', weight: 'Weight',
      loaded: 'Loaded', unloaded: 'Not Loaded',
      pcs: '{n} pcs', left: '{n} left',
      defaultName: 'Simulated Container'
    }
  },
  zh: {
    common: {
      save: '保存', cancel: '取消', edit: '编辑', delete: '删除',
      actions: '操作', noData: '暂无数据', name: '名称', status: '状态', type: '类型',
      confirmDelete: '确认删除该记录？'
    },
    status: {
      RUNNING: '运行中', MAINTAIN: '维护中', STOP: '已停用',
      IDLE: '空闲', LOADING: '装货中', TRANSIT: '运输中', UNLOADING: '卸货中',
      WORKING: '作业中', CHARGING: '充电中', EMPTY: '空柜', FULL: '已满', SHIPPED: '已发运',
      PATROL: '巡逻中', ON_PATROL: '巡检中'
    },
    taskStatus: { PENDING: '待执行', DOING: '进行中', DONE: '已完成', CANCEL: '已取消' },
    taskType: { INBOUND: '入库', OUTBOUND: '出库', TRANSFER: '转运', LOADING: '装柜' },
    deviceStatus: { RUNNING: '运行', IDLE: '待机', ALARM: '告警', STOP: '停机' },
    vehicleStatus: { IDLE: '空闲', LOADING: '装货', TRANSIT: '在途', UNLOADING: '卸货' },
    workerStatus: { ON: '在职', OFF: '休假', LEAVE: '离职' },
    warehouseType: { NORMAL: '普通仓', COLD: '冷链仓', HAZARD: '危险品仓' },
    vehicleType: { TRUCK: '卡车', VAN: '厢式货车', CONTAINER: '集装箱车' },
    typeName: { warehouse: '仓库', vehicle: '货车', forklift: '叉车', container: '货柜', pallet: '托盘', patrolVehicle: '巡逻车', guard: '巡检保安' },
    home: {
      brandSub: '园区仓储数字孪生监控',
      searchPh: '搜索仓库 / 车牌 / 编号，回车定位',
      doingTasks: '进行中任务', transitVehicles: '在途车辆', alarms: '告警',
      admin: '后台管理', containerCalc: '货柜计算', logout: '退出',
      hint: '🖱 拖拽旋转 · 滚轮缩放 · 点击建筑/车辆/叉车查看详情'
    },
    side: {
      overview: '运营总览',
      stat: {
        warehouseCount: '仓库', containerCount: '货柜', goodsCount: '货物',
        vehicleCount: '车辆', forkliftCount: '叉车', alarmDeviceCount: '告警'
      },
      tab: { tasks: '任务', devices: '设备', vehicles: '车辆' },
      empty: { tasks: '暂无任务', devices: '暂无设备', vehicles: '暂无车辆' },
      standby: '待命',
      unit: { warehouse: '座', container: '个', goods: '批', vehicle: '辆', forklift: '台', alarm: '条' }
    },
    detail: {
      title: '详情',
      field: {
        code: '编码', type: '类型', manager: '负责人', capacity: '容量',
        utilization: '利用率', status: '状态', size: '尺寸', description: '说明',
        plateNo: '车牌号', driver: '司机', phone: '联系电话', route: '线路',
        progress: '运输进度', cargo: '货物', eta: '预计到达',
        name: '名称', operator: '操作员', battery: '电量', currentTask: '当前任务',
        containerType: '柜型', innerSize: '内尺寸', volume: '容积',
        maxWeight: '载重上限', location: '所在位置',
        belongWarehouse: '所属仓库', layer: '层数',
        area: '区域', locatorNo: '定位器编号'
      },
      palletSlots: '托位', auto: '自动', layerN: '第 {n} 层', storing: '存储中'
    },
    login: {
      sub: '智能车间监控 · 3D 仓储可视化',
      tabLogin: '登录', tabRegister: '注册',
      username: '用户名', password: '密码', confirmPassword: '确认密码',
      nickname: '昵称（可选）',
      loading: '请稍候…', submitLogin: '登 录', submitRegister: '注 册',
      demo: '演示账号：admin / 123456',
      hasAccount: '已有账号？', backToLogin: '返回登录',
      errFill: '请填写完整信息', errPwdMismatch: '两次输入的密码不一致', errFailed: '操作失败'
    },
    admin: {
      sub: '后台管理', back: '返回监控大屏',
      menu: { map: '地图编辑', warehouse: '仓库管理', vehicle: '车辆管理', forklift: '叉车管理', worker: '人员管理', patrol: '巡逻车', guard: '保安' },
      title: { warehouse: '仓库', vehicle: '车辆', forklift: '叉车', worker: '人员', patrol: '巡逻车', guard: '保安' },
      add: '新增', edit: '编辑', del: '删除', actions: '操作', noData: '暂无数据',
      cancel: '取消', save: '保存', saveFailed: '保存失败',
      confirmDelete: '确认删除该记录？', list: '{title}列表',
      field: {
        plateNo: '车牌号', driver: '司机', phone: '联系电话', type: '类型', status: '状态',
        line: '线路', cargoDesc: '货物', origin: '起点', destination: '终点',
        progress: '运输进度 %', cargoDescLong: '货物描述',
        code: '编号', name: '名称', operator: '操作员', battery: '电量 %', currentTask: '当前任务',
        workerCode: '工号', workerName: '姓名', role: '岗位', department: '部门', area: '负责区域', remark: '备注',
        manager: '负责人', capacity: '容量', used: '已用', sizeX: '面宽 m', sizeZ: '进深 m', sizeY: '高度 m', description: '说明',
        locatorNo: '定位器编号', route: '巡逻路线'
      }
    },
    map: {
      models: '建筑模型',
      model: {
        WAREHOUSE: '仓库厂房', WORKSHOP: '生产车间', OFFICE: '办公楼',
        CANTEEN: '餐厅', QC: '质检中心', VISITOR: '访客中心'
      },
      hintPlacing: '点击地图任意位置放置，再次点击按钮取消',
      hintIdle: '选中模型后在地图上点击放置；拖拽可移动建筑',
      parkingLot: '停车场',
      props: '建筑属性',
      prop: {
        name: '名称', model: '模型', width: '面宽', height: '高度', depth: '进深',
        roofColor: '屋面颜色', roofColorPh: '#6b9bd1（默认蓝）', remark: '备注',
        code: '编号', manager: '负责人', capacity: '容量', used: '已用', status: '状态', description: '描述'
      },
      saved: '已保存', emptyTip: '点击地图中的建筑进行编辑',
      confirmDelete: '确认删除「{name}」？'
    },
    scene: {
      inTransit: '在途 {n}%', pallet: '托盘', palletSlots: '托位',
      patrolling: '巡逻中', onPatrol: '巡检中',
      fireLane: '消防通道', fireLaneEn: 'Fire Lane',
      parkingLot: '停车场', parkingLotEn: 'Parking Lot'
    },
    calc: {
      title: '货柜装载计算',
      params: '柜型参数',
      containerCode: '货柜编码', containerName: '货柜名称', type: '柜型',
      maxWeight: '载重上限 kg', innerL: '内长 cm', innerW: '内宽 cm', innerH: '内高 cm',
      goodsList: '货物清单', addGoods: '+ 添加货物',
      col: { name: '名称', qty: '数量', length: '长cm', width: '宽cm', height: '高cm', weight: '单件kg' },
      goodsNamePh: '货物名称',
      start: '开始计算', calculating: '计算中…', failed: '计算失败',
      result: '计算结果',
      volumeRate: '容积利用率', weightRate: '载重利用率',
      feasibility: '装载可行性', feasibleYes: '可装', feasibleNo: '超限',
      allFit: '全部货物可装入',
      volume: '容积', weight: '载重',
      loaded: '已装入', unloaded: '未装入',
      pcs: '{n} 件', left: '剩余 {n} 件',
      defaultName: '模拟货柜'
    }
  }
}

// t('home.admin') → 按当前语言取值，缺省回退英文 → key；支持 {n} 参数插值
export function t(key, params) {
  const resolve = (d) => key.split('.').reduce((o, k) => (o == null ? o : o[k]), d)
  let v = resolve(dict[locale.value]) ?? resolve(dict.en)
  if (v == null) return key
  if (params) {
    for (const [k, val] of Object.entries(params)) v = v.replaceAll(`{${k}}`, val)
  }
  return v
}
