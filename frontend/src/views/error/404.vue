<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" size="small" @click="$router.replace('/home')">回到首页</el-button>
  </div>
</template>

<script>
export default {
  name: 'NotFound'
}
</script>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #606266;
}
.not-found h1 {
  font-size: 72px;
  margin: 0;
  color: #c0c4cc;
}
.not-found p {
  margin: 8px 0 20px;
}
</style>
