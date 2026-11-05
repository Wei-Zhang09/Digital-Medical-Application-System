<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按政策标题搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增政策</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="title" label="政策标题" width="220" show-overflow-tooltip />
        <el-table-column label="所属医药公司" width="220" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ companyName(row.companyId) }}</template>
        </el-table-column>
        <el-table-column prop="message" label="政策内容" show-overflow-tooltip />
        <el-table-column prop="updateTime" label="更新时间" width="170" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="140" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.title, deleteCompanyPolicy)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px">
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="所属医药公司" prop="companyId">
          <el-select v-model="form.companyId" placeholder="请选择医药公司" filterable style="width: 100%">
            <el-option v-for="c in companies" :key="c.companyId" :label="c.companyName" :value="c.companyId" />
          </el-select>
        </el-form-item>
        <el-form-item label="政策标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="如：2026 年度药品配送政策" />
        </el-form-item>
        <el-form-item label="政策内容" prop="message">
          <el-input v-model="form.message" type="textarea" :rows="6" placeholder="填写政策正文" />
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
import { getCompanyPolicyList, addCompanyPolicy, updateCompanyPolicy, deleteCompanyPolicy } from '../../api/admin/companyPolicyManage'
import { getAllCompany } from '../../api/admin/companyManage'

export default {
  name: 'CompanyPolicy',
  mixins: [crudPage],
  data () {
    return {
      companies: [],
      rules: {
        companyId: [{ required: true, message: '请选择医药公司', trigger: 'change' }],
        title: [{ required: true, message: '请输入政策标题', trigger: 'blur' }],
        message: [{ required: true, message: '请输入政策内容', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
    this.loadCompanies()
  },
  methods: {
    loadCompanies () {
      getAllCompany().then(res => { this.companies = res.data.companies || [] }).catch(() => {})
    },
    companyName (companyId) {
      const hit = this.companies.find(c => c.companyId === companyId)
      return hit ? hit.companyName : companyId
    },
    fetchList () {
      this.loading = true
      getCompanyPolicyList(this.pageNum, this.pageSize, this.keyword)
        .then(res => {
          const page = res.data.companyPolicyPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { companyId: undefined, title: '', message: '' }
    },
    toForm (row) {
      return { companyId: row.companyId, title: row.title, message: row.message }
    },
    save () {
      const payload = { companyId: this.form.companyId, title: this.form.title.trim(), message: this.form.message }
      const request = this.editId ? updateCompanyPolicy(this.editId, payload) : addCompanyPolicy(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteCompanyPolicy (id) {
      return deleteCompanyPolicy(id)
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
