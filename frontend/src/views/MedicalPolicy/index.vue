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

      <el-table :data="list" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="id" label="编号" width="80" align="center" />
        <el-table-column prop="title" label="政策标题" min-width="280" show-overflow-tooltip sortable="custom" />
        <el-table-column label="生效城市" width="130" align="center">
          <template slot-scope="{ row }">{{ cityLabel(row.cityId) }}</template>
        </el-table-column>
        <el-table-column prop="message" label="政策内容" min-width="240" />
        <el-table-column prop="updateTime" label="更新时间" width="180" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.title, deleteMedicalPolicy)">删除</el-button>
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
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="生效城市" prop="cityId">
          <el-select v-model="form.cityId" placeholder="请选择城市（一条政策对应一个城市）" filterable style="width: 100%">
            <el-option v-for="c in cities" :key="c.cityId" :label="c.cityName ? c.cityName + '（' + c.cityNumber + '）' : '城市编号 ' + c.cityNumber" :value="c.cityId" />
          </el-select>
        </el-form-item>
        <el-form-item label="政策标题" prop="title">
          <el-input v-model="form.title" maxlength="100" placeholder="如：青岛市职工医保门诊报销政策" />
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
import { getMedicalPolicyList, addMedicalPolicy, updateMedicalPolicy, deleteMedicalPolicy } from '../../api/admin/medicalPolicyManage'
import { getAllCity } from '../../api/admin/cityManage'

export default {
  name: 'MedicalPolicy',
  mixins: [crudPage],
  data () {
    return {
      cities: [],
      rules: {
        cityId: [{ required: true, message: '请选择城市', trigger: 'change' }],
        title: [{ required: true, message: '请输入政策标题', trigger: 'blur' }],
        message: [{ required: true, message: '请输入政策内容', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
    this.loadCities()
  },
  methods: {
    loadCities () {
      getAllCity().then(res => { this.cities = res.data.cities || [] }).catch(() => {})
    },
    cityLabel (cityId) {
      const hit = this.cities.find(c => c.cityId === cityId)
      if (!hit) return cityId
      return hit.cityName ? hit.cityName : '编号 ' + hit.cityNumber
    },
    fetchList () {
      this.loading = true
      getMedicalPolicyList(this.pageNum, this.pageSize, this.keyword, this.sort, this.order)
        .then(res => {
          const page = res.data.medicalPolicyPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { cityId: undefined, title: '', message: '' }
    },
    toForm (row) {
      return { cityId: row.cityId, title: row.title, message: row.message }
    },
    save () {
      const payload = { cityId: this.form.cityId, title: this.form.title.trim(), message: this.form.message }
      const request = this.editId ? updateMedicalPolicy(this.editId, payload) : addMedicalPolicy(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteMedicalPolicy (id) {
      return deleteMedicalPolicy(id)
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
