import request from '../utils/request'

/**
 * 自助注册。
 *
 * <p>后端 /api/register 是免登录接口，走 JSON 提交；
 * 注册成功只创建登录账号（ROLE_2 医师），医师档案由管理员补全。</p>
 */
export function register (payload) {
  return request.post('/register', payload)
}
