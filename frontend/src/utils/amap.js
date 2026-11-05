import AMapLoader from '@amap/amap-jsapi-loader'

/**
 * 高德地图配置。
 *
 * Key 类型：Web端(JS API)。JS API 2.0 起必须配合「安全密钥」使用，
 * 否则控制台会报 INVALID_USER_SCODE，地图直接空白。
 * 安全密钥放在前端是高德的官方设计（配合控制台域名白名单使用）。
 */
export const AMAP_KEY = 'caae559398c46473dd76566b9ae31f76'
export const AMAP_SECURITY_JS_CODE = '332b997576ca6ab8b280b2a106b782af'

/** 地图默认中心：青岛市市南区 */
export const DEFAULT_CENTER = [120.3826, 36.0671]

let loadPromise = null

/**
 * 懒加载高德 JS API（只在进入销售地点页并打开地图时调用）。
 * 同一页面里多次调用只会真正加载一次。
 */
export function loadAMap () {
  if (loadPromise) return loadPromise

  // 必须在 loader 之前设置，否则 2.0 会因缺少安全密钥被拒
  window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_JS_CODE }

  loadPromise = AMapLoader.load({
    key: AMAP_KEY,
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.Geocoder']
  })
  return loadPromise
}
