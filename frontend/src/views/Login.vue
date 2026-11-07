<template>
  <div class="login-page">
    <div class="login-brand">
      <img class="brand-logo" src="../assets/logo.png" alt="logo" />
      <div class="brand-text">
        <h1>慧医数字医疗应用系统</h1>
        <p>Digital Medical Application System</p>
      </div>
    </div>

    <div class="login-card">
      <h2 class="card-title">用户登录</h2>

      <el-form ref="loginForm" :model="form" :rules="rules" @submit.native.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            clearable
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>

        <el-button
          class="login-button"
          type="primary"
          :loading="loading"
          @click="handleLogin"
        >登 录</el-button>

        <p class="login-tip" v-if="errorMsg">{{ errorMsg }}</p>
      </el-form>

      <div class="demo-accounts">
        <p class="demo-title">演示账号</p>
        <p>管理员：admin_1 / 123456　医师：王小明4125 / 123456</p>
      </div>
    </div>

    <p class="login-footer">慧医数字医疗应用系统 · 软件工程232 张伟</p>
  </div>
</template>

<script>
import { login } from '../api/Login'
import app from '../utils/app'

export default {
  name: 'Login',
  data () {
    return {
      loading: false,
      errorMsg: '',
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    handleLogin () {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.errorMsg = ''
        login(this.form.username.trim(), this.form.password)
          .then(res => {
            const data = res.data || {}
            this.$store.commit('SET_LOGIN', {
              token: data.token,
              role: data.role,
              user: data.user || {}
            })
            app.success('登录成功')
            // 路由守卫会去拉菜单并挂载路由；守卫里会 next({...to}) 重进，
            // 原始这次导航会被判定为 redirected，这里吞掉即可（否则 dev 环境弹红屏）
            this.$router.replace('/home').catch(() => {})
          })
          .catch(err => {
            // request.js 已经弹过提示，这里只在页面上再留一行，方便答辩时看
            this.errorMsg = (err && err.mess) || '登录失败，请检查用户名和密码'
          })
          .finally(() => {
            this.loading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: url('../assets/LoginBackground.jpg') no-repeat center center;
  background-size: cover;
  overflow: hidden;
}

.login-brand {
  position: absolute;
  top: 48px;
  left: 64px;
  display: flex;
  align-items: center;
}
.brand-logo {
  width: 64px;
  height: 64px;
  margin-right: 16px;
  border-radius: 50%;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
  flex: none;
}
.brand-text h1 {
  margin: 0;
  font-size: 28px;
  color: #ffffff;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.brand-text p {
  margin: 6px 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1px;
}

.login-card {
  position: absolute;
  left: 50%;
  top: 54%;
  transform: translate(-50%, -50%);
  width: 380px;
  padding: 32px 36px 24px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.card-title {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  letter-spacing: 2px;
}

.login-button {
  width: 100%;
  height: 42px;
  font-size: 15px;
  letter-spacing: 4px;
  background-color: #13b7a5;
  border-color: #13b7a5;
}
.login-button:hover,
.login-button:focus {
  background-color: #17c9b6;
  border-color: #17c9b6;
}

.login-tip {
  margin: 12px 0 0;
  font-size: 13px;
  color: #f56c6c;
  text-align: center;
}

.demo-accounts {
  margin-top: 20px;
  padding-top: 14px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
  text-align: center;
}
.demo-title {
  margin: 0 0 2px;
  color: #606266;
  font-weight: 600;
}

.login-footer {
  position: absolute;
  bottom: 18px;
  width: 100%;
  margin: 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
