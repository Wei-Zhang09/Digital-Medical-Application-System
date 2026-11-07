<template>
  <div class="register-page">
    <div class="register-brand">
      <img class="brand-logo" src="../assets/logo.png" alt="logo" />
      <div class="brand-text">
        <h1>慧医数字医疗应用系统</h1>
        <p>Digital Medical Application System</p>
      </div>
    </div>

    <div class="register-card">
      <h2 class="card-title">创建账户</h2>

      <el-form ref="registerForm" :model="form" :rules="rules" label-position="top" @submit.native.prevent="handleRegister">
        <el-form-item label="登录名" prop="uname">
          <el-input v-model="form.uname" placeholder="4-20 位，字母/数字/下划线/汉字" prefix-icon="el-icon-user" clearable />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="form.realname" placeholder="请输入真实姓名" prefix-icon="el-icon-postcard" clearable />
        </el-form-item>

        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="form.phonenumber" placeholder="11 位手机号" prefix-icon="el-icon-mobile-phone" clearable maxlength="11" />
        </el-form-item>

        <el-form-item label="密码" prop="pwd">
          <el-input v-model="form.pwd" type="password" placeholder="6-20 位" prefix-icon="el-icon-lock" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input v-model="form.confirmPwd" type="password" placeholder="请再次输入密码" prefix-icon="el-icon-lock" show-password
                    @keyup.enter.native="handleRegister" />
        </el-form-item>

        <el-button class="register-button" type="primary" :loading="loading" @click="handleRegister">注 册</el-button>

        <p class="register-tip" v-if="errorMsg">{{ errorMsg }}</p>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <el-link type="primary" :underline="false" @click="goLogin">返回登录</el-link>
      </div>
    </div>

    <p class="page-footer">慧医数字医疗应用系统 · 软件工程232 张伟</p>
  </div>
</template>

<script>
import { register } from '../api/Account'
import app from '../utils/app'

export default {
  name: 'Register',
  data () {
    const checkConfirm = (rule, value, callback) => {
      if (value !== this.form.pwd) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      errorMsg: '',
      form: {
        uname: '',
        realname: '',
        phonenumber: '',
        pwd: '',
        confirmPwd: ''
      },
      rules: {
        uname: [
          { required: true, message: '请输入登录名', trigger: 'blur' },
          { min: 4, max: 20, message: '长度需在 4-20 个字符之间', trigger: 'blur' },
          { pattern: /^[A-Za-z0-9_\u4e00-\u9fa5]+$/, message: '只能包含字母、数字、下划线或汉字', trigger: 'blur' }
        ],
        realname: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phonenumber: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        pwd: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度需在 6-20 位之间', trigger: 'blur' }
        ],
        confirmPwd: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: checkConfirm, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleRegister () {
      this.$refs.registerForm.validate(valid => {
        if (!valid) return
        this.loading = true
        this.errorMsg = ''
        register({
          uname: this.form.uname.trim(),
          realname: this.form.realname.trim(),
          phonenumber: this.form.phonenumber.trim(),
          pwd: this.form.pwd,
          confirmPwd: this.form.confirmPwd
        })
          .then(() => {
            app.success('注册成功，请使用新账号登录')
            this.goLogin()
          })
          .catch(err => {
            this.errorMsg = (err && err.mess) || '注册失败，请稍后重试'
          })
          .finally(() => {
            this.loading = false
          })
      })
    },
    goLogin () {
      this.$router.replace('/user/login').catch(() => {})
    }
  }
}
</script>

<style scoped>
.register-page {
  position: relative;
  width: 100%;
  min-height: 100vh;
  background: url('../assets/RegisterBackground.jpg') no-repeat center center;
  background-size: cover;
  overflow: auto;
}

.register-brand {
  position: absolute;
  top: 32px;
  left: 64px;
  display: flex;
  align-items: center;
}
.brand-logo {
  width: 56px;
  height: 56px;
  margin-right: 14px;
  border-radius: 50%;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.18);
  flex: none;
}
.brand-text h1 {
  margin: 0;
  font-size: 24px;
  color: #ffffff;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.brand-text p {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1px;
}

.register-card {
  position: relative;
  width: 420px;
  margin: 130px auto 40px;
  padding: 26px 36px 20px;
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.card-title {
  margin: 0 0 18px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  text-align: center;
  letter-spacing: 2px;
}

.register-card >>> .el-form-item {
  margin-bottom: 14px;
}
.register-card >>> .el-form-item__label {
  padding-bottom: 2px;
  line-height: 20px;
  font-size: 13px;
  color: #606266;
}

.register-button {
  width: 100%;
  height: 40px;
  margin-top: 4px;
  font-size: 15px;
  letter-spacing: 4px;
  background-color: #13b7a5;
  border-color: #13b7a5;
}
.register-button:hover,
.register-button:focus {
  background-color: #17c9b6;
  border-color: #17c9b6;
}

.register-tip {
  margin: 10px 0 0;
  font-size: 13px;
  color: #f56c6c;
  text-align: center;
}

.register-footer {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

.page-footer {
  margin: 0 0 16px;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
