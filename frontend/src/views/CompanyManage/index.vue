<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按公司名称搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <el-button icon="el-icon-download" :loading="exporting" @click="handleExport">导出</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增公司</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="companyId" label="公司ID" width="90" align="center" />
        <el-table-column prop="companyName" label="公司名称" min-width="200" show-overflow-tooltip sortable="custom" />
        <el-table-column prop="companyPhone" label="联系电话" min-width="150" align="center" sortable="custom" />
        <el-table-column prop="createtime" label="创建时间" min-width="180" align="center" sortable="custom" />
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.companyName, deleteCompany)">删除</el-button>
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
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" maxlength="100" placeholder="如：青岛华仁药业" />
        </el-form-item>
        <el-form-item label="联系电话" prop="companyPhone">
          <el-input v-model="form.companyPhone" maxlength="30" placeholder="如：0532-88886666" />
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
import { getCompanyList, addCompany, updateCompany, deleteCompany } from '../../api/admin/companyManage'

export default {
  name: 'CompanyManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'companyId',
      exportModule: 'company',
      rules: {
        companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
        companyPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      getCompanyList(this.pageNum, this.pageSize, this.keyword, this.sort, this.order)
        .then(res => {
          const page = res.data.companyPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { companyName: '', companyPhone: '' }
    },
    toForm (row) {
      return { companyName: row.companyName, companyPhone: row.companyPhone }
    },
    save () {
      const payload = { companyName: this.form.companyName.trim(), companyPhone: this.form.companyPhone.trim() }
      const request = this.editId ? updateCompany(this.editId, payload) : addCompany(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteCompany (companyId) {
      return deleteCompany(companyId)
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
