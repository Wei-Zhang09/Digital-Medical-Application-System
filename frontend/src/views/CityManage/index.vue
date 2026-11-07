<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按城市编号搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <el-button icon="el-icon-download" :loading="exporting" @click="handleExport">导出</el-button>
        <div class="toolbar-right" v-if="isAdmin">
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增城市</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe @sort-change="handleSortChange">
        <el-table-column prop="cityId" label="城市ID" width="110" align="center" />
        <el-table-column prop="cityName" label="城市名称" min-width="140">
          <template slot-scope="{ row }">{{ row.cityName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="cityNumber" label="行政区划代码" width="150" align="center" sortable="custom" />
        <el-table-column prop="createtime" label="创建时间" min-width="180" align="center" sortable="custom" />
        <el-table-column v-if="isAdmin" label="操作" width="120" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" class="danger" @click="handleDelete(row, '城市 ' + row.cityNumber, deleteCity)">删除</el-button>
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
        <el-form-item label="城市编号" prop="cityNumber">
          <el-input-number v-model="form.cityNumber" :min="1" :max="99999999" controls-position="right" />
          <p class="field-tip">城市编号用于医保政策关联，如青岛 370200</p>
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
import { getCityList, addCity, deleteCity } from '../../api/admin/cityManage'

export default {
  name: 'CityManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'cityId',
      exportModule: 'city',
      rules: {
        cityNumber: [{ required: true, message: '请输入城市编号', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    fetchList () {
      this.loading = true
      // 关键字是数字编号，空串要传 undefined，否则后端转 Integer 会失败
      const cityNumber = this.keyword === '' ? undefined : Number(this.keyword)
      getCityList(this.pageNum, this.pageSize, cityNumber, this.sort, this.order)
        .then(res => {
          const page = res.data.cityPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { cityNumber: undefined }
    },
    toForm (row) {
      return { cityNumber: row.cityNumber }
    },
    save () {
      // 城市只支持新增
      return addCity({ cityNumber: this.form.cityNumber }).then(() => app.success('新增成功'))
    },
    deleteCity (cityId) {
      return deleteCity(cityId)
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
