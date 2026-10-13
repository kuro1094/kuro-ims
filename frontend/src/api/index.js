import request from './request'

// ---------- 认证 ----------
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)
export const refreshToken = (data) => request.post('/auth/refresh', data)
export const getProfile = () => request.get('/auth/profile')

// ---------- 统计 ----------
export const getOverview = () => request.get('/stats/overview')

// ---------- 通用 CRUD ----------
const crud = (path) => ({
  page: (params) => request.get(path, { params }),
  list: () => request.get(`${path}/list`),
  get: (id) => request.get(`${path}/${id}`),
  create: (data) => request.post(path, data),
  update: (id, data) => request.put(`${path}/${id}`, data),
  remove: (id) => request.delete(`${path}/${id}`)
})

export const warehouseApi = crud('/warehouses')
export const containerApi = {
  ...crud('/containers'),
  calculate: (data) => request.post('/containers/calculate', data)
}
export const goodsApi = crud('/goods')
export const vehicleApi = crud('/vehicles')
export const forkliftApi = crud('/forklifts')
export const taskApi = crud('/tasks')
export const deviceApi = crud('/devices')
export const buildingApi = crud('/buildings')
export const workerApi = crud('/workers')
export const patrolVehicleApi = crud('/patrol-vehicles')
export const guardApi = crud('/guards')
