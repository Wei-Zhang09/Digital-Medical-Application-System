<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按标题搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <el-button icon="el-icon-download" :loading="exporting" @click="handleExport">导出</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增材料</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip sortable="custom" />
        <el-table-column prop="message" label="内容" min-width="260" />
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.title, deleteMaterial)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="如：入院必备材料" />
        </el-form-item>
        <el-form-item label="内容" prop="message">
          <el-input v-model="form.message" type="textarea" :rows="6" placeholder="填写材料清单或说明" />
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
import { getMaterialList, addMaterial, updateMaterial, deleteMaterial } from '../../api/admin/materialManage'

export default {
  name: 'MaterialManage',
  mixins: [crudPage],
  data () {
    return {
      exportModule: 'material',
      rules: {
        title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { max: 100, message: '最多 100 个字符', trigger: 'blur' }],
        message: [{ required: true, message: '请输入内容', trigger: 'blur' }, { max: 2000, message: '最多 2000 个字符', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      getMaterialList(this.pageNum, this.pageSize, this.keyword, this.sort, this.order)
        .then(res => {
          const page = res.data.materialPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { title: '', message: '' }
    },
    toForm (row) {
      return { title: row.title, message: row.message }
    },
    save () {
      const payload = { title: this.form.title.trim(), message: this.form.message }
      const request = this.editId ? updateMaterial(this.editId, payload) : addMaterial(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteMaterial (id) {
      return deleteMaterial(id)
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
