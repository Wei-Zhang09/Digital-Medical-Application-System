<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按操作人 / 模块 / 动作搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="90" align="center" />
        <el-table-column label="操作人" min-width="150">
          <template slot-scope="{ row }">
            <span>{{ row.realname || '—' }}</span>
            <span class="muted" v-if="row.uname">（{{ row.uname }}）</span>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="130" align="center" />
        <el-table-column label="动作" width="110" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="tagType(row.action)" size="small">{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="请求" min-width="280" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" align="center" />
        <el-table-column prop="createtime" label="操作时间" width="180" align="center" />
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        :current-page="pageNum"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>
  </div>
</template>

<script>
import crudPage from '../../mixins/crudPage'
import { getLogList } from '../../api/admin/operationLog'

export default {
  name: 'OperationLog',
  mixins: [crudPage],
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      getLogList(this.pageNum, this.pageSize, this.keyword.trim())
        .then(res => {
          const page = res.data.logPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    tagType (action) {
      if (action === '新增') return 'success'
      if (action === '删除') return 'danger'
      if (action === '修改' || action === '重置口令') return 'warning'
      return 'info'
    }
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.search-input {
  width: 280px;
}
.muted {
  color: #909399;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
