<template>
  <div class="home">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div>
          <h2>欢迎使用慧医数字医疗应用系统</h2>
          <p>当前登录：{{ userName }}（{{ roleText }}）</p>
        </div>
        <img src="../../assets/logo.png" alt="logo" class="welcome-logo" />
      </div>
    </el-card>

    <el-row :gutter="16" class="tip-row">
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header">能做什么</div>
          <p class="tip-text">管理员维护医药公司、销售地点、城市、药品、政策、医生与必备材料；医师按角色查询。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header">权限怎么来</div>
          <p class="tip-text">菜单由后端按角色下发，改删接口由 @RolesAllowed 控制，前端隐藏按钮只是体验。</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div slot="header">数据面板</div>
          <p class="tip-text">医师/公司/药品/药店数量、职称柱状图与科室饼图将在「功能扩充」阶段接入。</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Home',
  computed: {
    userName () {
      const user = this.$store.state.user || {}
      return user.realname || user.uname || ''
    },
    roleText () {
      return this.$store.state.role === 'ROLE_1' ? '管理员' : '医师'
    }
  }
}
</script>

<style scoped>
.welcome-card {
  margin-bottom: 16px;
}
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.welcome h2 {
  margin: 0 0 8px;
  font-size: 20px;
  color: #303133;
}
.welcome p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}
.welcome-logo {
  width: 64px;
  height: 64px;
}
.tip-text {
  margin: 0;
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
}
</style>
