/**
 * 高德地图 JS API 配置
 * Key 类型：Web端(JS API)
 * 注意：JS API 2.0 起需要配合「安全密钥」使用（window._AMapSecurityConfig）。
 * 若控制台报 INVALID_USER_SCODE，说明这个 Key 绑定了安全密钥，需把 securityJsCode 填进来。
 */
export const AMAP_KEY = 'caae559398c46473dd76566b9ae31f76'

// 安全密钥（若控制台未开启"安全密钥"，留空即可）
export const AMAP_SECURITY_JS_CODE = ''

export function setAmapSecurityConfig () {
  if (AMAP_SECURITY_JS_CODE) {
    window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_JS_CODE }
  }
}
