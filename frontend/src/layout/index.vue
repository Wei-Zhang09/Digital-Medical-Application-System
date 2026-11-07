<template>
  <el-container class="layout">
    <!-- 左侧菜单：菜单项来自后端 /api/permissions，不写死 -->
    <el-aside width="240px" class="layout-aside">
      <div class="logo-area">
        <img src="../assets/HomeLogo.png" alt="慧医" class="logo-img" />
      </div>

      <el-menu
        class="layout-menu"
        :default-active="$route.path"
        background-color="#1f2d3d"
        text-color="#c0c9d4"
        active-text-color="#ffffff"
        @select="handleSelect"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.path"
          :index="item.path"
        >
          <i :class="iconOf(item)"></i>
          <span slot="title">{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="page-title">{{ $route.meta.title || '' }}</span>
        </div>

        <div class="header-right">
          <span class="role-tag">{{ roleText }}</span>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <i class="el-icon-user-solid"></i>
              {{ userName }}
              <i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import app from '../utils/app'

export default {
  name: 'Layout',
  computed: {
    /** Layout 下的子菜单（后端已按角色过滤） */
    menus () {
      const layout = this.$store.state.permissions.find(p => p.component === 'Layout')
      return (layout && layout.children) || []
    },
    userName () {
      const user = this.$store.state.user || {}
      return user.realname || user.uname || '未登录'
    },
    roleText () {
      return this.$store.state.role === 'ROLE_1' ? '管理员' : '医师'
    }
  },
  methods: {
    handleSelect (path) {
      if (path !== this.$route.path) {
        this.$router.push(path)
      }
    },
    iconOf (item) {
      if (item.meta && item.meta.icon) return item.meta.icon
      // 后端没配图标时按路径给一个，保证菜单不秃
      const map = {
        '/home': 'el-icon-s-home',
        '/base/company': 'el-icon-office-building',
        '/base/sale': 'el-icon-location-outline',
        '/base/city': 'el-icon-map-location',
        '/manage/drug': 'el-icon-first-aid-kit',
        '/manage/medical/policy': 'el-icon-document',
        '/manage/company/policy': 'el-icon-document-checked',
        '/manage/doctor': 'el-icon-user',
        '/manage/material': 'el-icon-folder-opened'
      }
      return map[item.path] || 'el-icon-menu'
    },
    async handleCommand (command) {
      if (command !== 'logout') return
      const ok = await app.confirm('确定退出登录吗？', '提示')
      if (!ok) return
      this.$store.commit('CLEAR_LOGIN')
      // 动态路由已挂载，简单粗暴地整页刷新，避免残留路由
      window.location.hash = '#/user/login'
      window.location.reload()
    }
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.layout-aside {
  background-color: #1f2d3d;
  overflow-x: hidden;
}

.logo-area {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #18232f;
}
.logo-img {
  height: 34px;
}

.layout-menu {
  border-right: none;
}
/* 菜单文字不换行、不截断：中文标题最长 8 个字，240px 侧栏放得下 */
.layout-menu .el-menu-item {
  font-size: 13px;
  padding-left: 20px !important;
  white-space: nowrap;
}
.layout-menu .el-menu-item.is-active {
  background-color: #13b7a5 !important;
}
.layout-menu .el-menu-item:hover {
  background-color: #2a3b4d !important;
}

.layout-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-tag {
  padding: 2px 10px;
  font-size: 12px;
  color: #13b7a5;
  background: #e8f8f5;
  border-radius: 10px;
}

.user-info {
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.layout-main {
  background: #f4f6f9;
  padding: 16px;
  overflow-y: auto;
}
</style>
