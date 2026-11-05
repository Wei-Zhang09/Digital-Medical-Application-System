<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按药店名称搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增销售地点</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="saleId" label="ID" width="80" align="center" />
        <el-table-column prop="saleName" label="药店名称" width="260" show-overflow-tooltip />
        <el-table-column prop="salePhone" label="联系电话" width="180" align="center" />
        <el-table-column prop="createtime" label="创建时间" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="140" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.saleName, deleteSale)">删除</el-button>
          </template>
        </el-table-column>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="520px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="药店名称" prop="saleName">
          <el-input v-model="form.saleName" maxlength="100" placeholder="如：青岛同仁堂大药房" />
        </el-form-item>
        <el-form-item label="联系电话" prop="salePhone">
          <el-input v-model="form.salePhone" maxlength="30" placeholder="如：13800001111" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import crudPage from '../../mixins/crudPage'
import app from '../../utils/app'
import { getSaleList, addSale, updateSale, deleteSale } from '../../api/admin/saleManage'

export default {
  name: 'SaleManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'saleId',
      rules: {
        saleName: [{ required: true, message: '请输入药店名称', trigger: 'blur' }],
        salePhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      getSaleList(this.pageNum, this.pageSize, this.keyword)
        .then(res => {
          const page = res.data.salePageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { saleName: '', salePhone: '' }
    },
    toForm (row) {
      return { saleName: row.saleName, salePhone: row.salePhone }
    },
    save () {
      const payload = { saleName: this.form.saleName.trim(), salePhone: this.form.salePhone.trim() }
      const request = this.editId ? updateSale(this.editId, payload) : addSale(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteSale (saleId) {
      return deleteSale(saleId)
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
  width: 240px;
}
.toolbar-right {
  margin-left: auto;
}
.danger {
  color: #f56c6c;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
