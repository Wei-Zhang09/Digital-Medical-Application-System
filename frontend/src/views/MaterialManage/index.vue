<template>
  <el-card shadow="never">
    <div slot="header" class="card-header">
      <span>必备材料管理</span>
    </div>
    <el-empty description="必备材料列表、新增、修改、删除" />
    <p class="todo-tip">页面开发中（T8 任务），后端接口已可用。</p>
  </el-card>
</template>

<script>
export default {
  name: 'MaterialManage'
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
