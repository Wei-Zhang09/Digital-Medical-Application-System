import request from '../../utils/request'

/** 销售地点 */
export function getSaleList (pageNum, pageSize, name) {
  return request.get(`/sales/${pageNum}/${pageSize}`, { params: { name } })
}
export function getAllSale () {
  return request.get('/sales/all')
}
export function addSale (data) {
  return request.post('/sales', data)
}
export function updateSale (saleId, data) {
  return request.put(`/sales/${saleId}`, data)
}
export function deleteSale (saleId) {
  return request.delete(`/sales/${saleId}`)
}
