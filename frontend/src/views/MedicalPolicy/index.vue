<template>
  <el-card shadow="never">
    <div slot="header" class="card-header">
      <span>医保政策管理</span>
    </div>
    <el-empty description="医保政策列表、新增、修改、删除（按城市生效）" />
    <p class="todo-tip">页面开发中（T8 任务），后端接口已可用。</p>
  </el-card>
</template>

<script>
export default {
  name: 'MedicalPolicy'
}
</script>

<style scoped>
.card-header {
  font-weight: 600;
  color: #303133;
}
.todo-tip {
  margin: 12px 0 0;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
</style>
