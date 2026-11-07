<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按登录名 / 姓名搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增账号</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="id" label="ID" width="90" align="center" />
        <el-table-column prop="uname" label="登录名" min-width="150" sortable="custom" />
        <el-table-column prop="realname" label="真实姓名" min-width="130" sortable="custom">
          <template slot-scope="{ row }">{{ row.realname || '—' }}</template>
        </el-table-column>
        <el-table-column prop="phonenumber" label="手机号" min-width="140" align="center">
          <template slot-scope="{ row }">{{ row.phonenumber || '—' }}</template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.utype === 'ROLE_1' ? 'danger' : 'success'" size="small">
              {{ row.utype === 'ROLE_1' ? '管理员' : '医师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createtime" label="创建时间" min-width="180" align="center" sortable="custom" />
        <el-table-column v-if="isAdmin" label="操作" width="200" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" @click="handleResetPwd(row)">重置口令</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.uname, removeAccount)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="480px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="登录名" prop="uname">
          <el-input v-model="form.uname" :disabled="!!editId" placeholder="4-20 位，字母/数字/下划线/汉字" />
          <p class="field-tip" v-if="editId">登录名创建后不可修改</p>
        </el-form-item>
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="form.realname" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="form.phonenumber" placeholder="11 位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="角色" prop="utype">
          <el-radio-group v-model="form.utype">
            <el-radio label="ROLE_2">医师</el-radio>
            <el-radio label="ROLE_1">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!editId">
          <p class="field-tip">新增账号的初始口令为 123456，请提醒用户首次登录后修改。</p>
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
  getAccountList,
  addAccount,
  updateAccount,
  resetAccountPassword,
  deleteAccount
} from '../../api/admin/accountManage'

export default {
  name: 'AccountManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'id',
      rules: {
        uname: [
          { required: true, message: '请输入登录名', trigger: 'blur' },
          { min: 4, max: 20, message: '长度需在 4-20 个字符之间', trigger: 'blur' },
          { pattern: /^[A-Za-z0-9_\u4e00-\u9fa5]+$/, message: '只能包含字母、数字、下划线或汉字', trigger: 'blur' }
        ],
        realname: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        phonenumber: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        utype: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      getAccountList(this.pageNum, this.pageSize, this.keyword.trim(), this.sort, this.order)
        .then(res => {
          const page = res.data.accountPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { uname: '', realname: '', phonenumber: '', utype: 'ROLE_2' }
    },
    toForm (row) {
      return {
        uname: row.uname,
        realname: row.realname,
        phonenumber: row.phonenumber,
        utype: row.utype
      }
    },
    save () {
      const payload = {
        uname: this.form.uname.trim(),
        realname: this.form.realname.trim(),
        phonenumber: this.form.phonenumber.trim(),
        utype: this.form.utype
      }
      const request = this.editId
        ? updateAccount(this.editId, payload)
        : addAccount(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功，初始口令 123456'))
    },
    handleResetPwd (row) {
      app.confirm(`确定把「${row.uname}」的口令重置为 123456 吗？`).then(ok => {
        if (!ok) return
        resetAccountPassword(row.id).then(() => app.success('口令已重置为 123456')).catch(() => {})
      })
    },
    removeAccount (id) {
      return deleteAccount(id)
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
.field-tip {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
