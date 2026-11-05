import request from '../../utils/request'

/** 首页数据面板：四个数量 + 职称柱状图 + 科室饼图 + 最新资讯 */
export function getDashboard () {
  return request.get('/dashboard')
}
