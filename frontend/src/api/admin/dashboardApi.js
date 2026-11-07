import request from '../../utils/request'

/** 首页数据面板：四个数量 + 职称柱状图 + 科室饼图 + 最新资讯 */
export function getDashboard () {
  return request.get('/dashboard')
}

/**
 * 首页资讯详情。
 * @param {string} source medical / company / material
 * @param {number} id 对应表的 id
 */
export function getNewsDetail (source, id) {
  return request.get(`/dashboard/news/${source}/${id}`)
}
