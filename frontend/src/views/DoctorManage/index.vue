<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按医师姓名搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增医师</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="age" label="年龄" width="70" align="center" />
        <el-table-column label="性别" width="70" align="center">
          <template slot-scope="{ row }">{{ row.sex === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="levelName" label="职称" min-width="100" align="center" />
        <el-table-column prop="typeName" label="科室" min-width="100" align="center" />
        <el-table-column prop="phone" label="联系电话" min-width="120" align="center" />
        <el-table-column prop="hospital" label="所属医院" min-width="140" show-overflow-tooltip />
        <el-table-column prop="uname" label="登录名" min-width="120" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="200" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleResetPwd(row)">重置口令</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.name, deleteDoctor)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="620px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="18" :max="100" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="form.sex">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="levelId">
              <el-select v-model="form.levelId" placeholder="请选择" style="width: 100%">
                <el-option v-for="l in levels" :key="l.id" :label="l.name" :value="l.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室" prop="typeId">
              <el-select v-model="form.typeId" placeholder="请选择" style="width: 100%">
                <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" maxlength="20" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="所属医院" prop="hospital">
          <el-input v-model="form.hospital" maxlength="100" placeholder="如：青岛第一人民医院" />
        </el-form-item>

        <el-form-item v-if="!editId" label="登录名">
          <el-input v-model="form.uname" maxlength="50" placeholder="留空则自动生成：姓名 + 手机号后 4 位" />
          <p class="field-tip">初始口令固定为 123456，新增后可在列表中重置。</p>
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
import {
  getDoctorList, getDoctorLevels, getDoctorTypes,
  addDoctor, updateDoctor, deleteDoctor, resetDoctorPassword
} from '../../api/admin/doctorManage'

export default {
  name: 'DoctorManage',
  mixins: [crudPage],
  data () {
    return {
      levels: [],
      types: [],
      rules: {
        name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
        sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
        levelId: [{ required: true, message: '请选择职称', trigger: 'change' }],
        typeId: [{ required: true, message: '请选择科室', trigger: 'change' }],
        phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
        hospital: [{ required: true, message: '请输入所属医院', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
    this.loadOptions()
  },
  methods: {
    loadOptions () {
      getDoctorLevels().then(res => { this.levels = res.data.levels || [] }).catch(() => {})
      getDoctorTypes().then(res => { this.types = res.data.types || [] }).catch(() => {})
    },
    fetchList () {
      this.loading = true
      getDoctorList(this.pageNum, this.pageSize, this.keyword)
        .then(res => {
          const page = res.data.doctorPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { name: '', age: 30, sex: 1, levelId: undefined, typeId: undefined, phone: '', hospital: '', uname: '' }
    },
    toForm (row) {
      return {
        name: row.name,
        age: row.age,
        sex: row.sex,
        levelId: row.levelId,
        typeId: row.typeId,
        phone: row.phone,
        hospital: row.hospital,
        uname: row.uname
      }
    },
    save () {
      const payload = {
        name: this.form.name.trim(),
        age: this.form.age,
        sex: this.form.sex,
        levelId: this.form.levelId,
        typeId: this.form.typeId,
        phone: this.form.phone.trim(),
        hospital: this.form.hospital.trim()
      }
      if (!this.editId && this.form.uname) payload.uname = this.form.uname.trim()
      const request = this.editId ? updateDoctor(this.editId, payload) : addDoctor(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功，初始口令 123456'))
    },
    deleteDoctor (id) {
      return deleteDoctor(id)
    },
    handleResetPwd (row) {
      app.confirm(`确定把「${row.name}」的登录口令重置为 123456 吗？`).then(ok => {
        if (!ok) return
        resetDoctorPassword(row.id).then(() => app.success('口令已重置为 123456')).catch(() => {})
      })
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

<style scoped>
.field-tip {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}
</style>
