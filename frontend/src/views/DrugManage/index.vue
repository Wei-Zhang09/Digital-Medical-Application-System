<template>
  <div>
    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="按药名搜索"
          clearable
          @keyup.enter.native="handleSearch"
          @clear="handleSearch"
        />
        <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
        <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        <div class="toolbar-right">
          <!-- 医师也能新增药品（官方任务 21：新增放给角色 1、2） -->
          <el-button type="primary" icon="el-icon-plus" @click="openAdd">新增药品</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column label="图片" width="90" align="center">
          <template slot-scope="{ row }">
            <el-image
              v-if="row.drugImg"
              class="drug-img"
              :src="imageUrl(row.drugImg)"
              :preview-src-list="[imageUrl(row.drugImg)]"
              fit="cover"
            >
              <div slot="error" class="img-error"><i class="el-icon-picture-outline"></i></div>
            </el-image>
            <span v-else class="img-empty">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="drugName" label="药名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="drugInfo" label="药品成分" min-width="160" show-overflow-tooltip />
        <el-table-column prop="drugEffect" label="药品功能" min-width="180" show-overflow-tooltip />
        <el-table-column prop="saleNames" label="销售药店" min-width="160" show-overflow-tooltip>
          <template slot-scope="{ row }">{{ row.saleNames || '未关联' }}</template>
        </el-table-column>
        <el-table-column prop="updatetime" label="更新时间" width="160" align="center" />
        <el-table-column v-if="isAdmin" label="操作" width="150" fixed="right" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openEdit(row)">编辑</el-button>
            <el-button type="text" class="danger" @click="handleDelete(row, row.drugName, deleteDrug)">删除</el-button>
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

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="680px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="药名" prop="drugName">
          <el-input v-model="form.drugName" maxlength="100" placeholder="如：阿莫西林胶囊" />
        </el-form-item>

        <el-form-item label="药品成分" prop="drugInfo">
          <el-input v-model="form.drugInfo" type="textarea" :rows="3" placeholder="主要成分" />
        </el-form-item>

        <el-form-item label="药品功能" prop="drugEffect">
          <el-input v-model="form.drugEffect" type="textarea" :rows="3" placeholder="适应症 / 功效" />
        </el-form-item>

        <el-form-item label="销售药店">
          <el-select
            v-model="form.saleIds"
            multiple
            filterable
            placeholder="可选择多家药店"
            style="width: 100%"
          >
            <el-option v-for="s in sales" :key="s.saleId" :label="s.saleName" :value="s.saleId" />
          </el-select>
        </el-form-item>

        <el-form-item label="药品图片">
          <div class="upload-row">
            <el-upload
              class="uploader"
              action="#"
              :show-file-list="false"
              :http-request="doUpload"
              :before-upload="beforeUpload"
            >
              <div class="upload-box" v-if="!form.drugImg">
                <i class="el-icon-plus"></i>
                <span>上传图片</span>
              </div>
              <img v-else :src="imageUrl(form.drugImg)" class="upload-preview" alt="预览" />
            </el-upload>
            <div class="upload-tip">
              <p>支持 jpg / png / gif，大小不超过 1MB。</p>
              <p>点击图片可重新上传。</p>
            </div>
          </div>
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
  getDrugList, getDrugDetail, addDrug, updateDrug, deleteDrug, uploadDrugImage
} from '../../api/admin/drugInfoManage'
import { getAllSale } from '../../api/admin/saleManage'

/**
 * 图片地址：库里存的是相对路径 /image/xxx。
 * 开发态（9092）需要拼上后端 8080；生产态由 Nginx 反代 /image/，前缀为空。
 */
const IMAGE_BASE = process.env.VUE_APP_IMAGE_BASE || ''

export default {
  name: 'DrugManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'drugId',
      sales: [],
      rules: {
        drugName: [{ required: true, message: '请输入药名', trigger: 'blur' }],
        drugInfo: [{ required: true, message: '请输入药品成分', trigger: 'blur' }],
        drugEffect: [{ required: true, message: '请输入药品功能', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
    this.loadSales()
  },
  methods: {
    imageUrl (path) {
      if (!path) return ''
      return path.startsWith('/image/') ? IMAGE_BASE + path : path
    },
    loadSales () {
      getAllSale().then(res => { this.sales = res.data.sales || [] }).catch(() => {})
    },
    fetchList () {
      this.loading = true
      getDrugList(this.pageNum, this.pageSize, this.keyword)
        .then(res => {
          const page = res.data.drugPageInfo
          this.list = page.list
          this.total = page.total
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    initForm () {
      return { drugName: '', drugInfo: '', drugEffect: '', drugImg: '', saleIds: [] }
    },
    toForm (row) {
      return {
        drugName: row.drugName,
        drugInfo: row.drugInfo,
        drugEffect: row.drugEffect,
        drugImg: row.drugImg,
        saleIds: []
      }
    },
    /** 编辑时补拉详情，拿关联药店 id */
    openEdit (row) {
      getDrugDetail(row.drugId).then(res => {
        const drug = res.data.drug || {}
        this.editId = drug.drugId
        this.dialogTitle = '修改'
        this.form = {
          drugName: drug.drugName,
          drugInfo: drug.drugInfo,
          drugEffect: drug.drugEffect,
          drugImg: drug.drugImg,
          saleIds: drug.saleIds || []
        }
        this.dialogVisible = true
        this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
      }).catch(() => {})
    },
    save () {
      const payload = {
        drugName: this.form.drugName.trim(),
        drugInfo: this.form.drugInfo,
        drugEffect: this.form.drugEffect,
        drugImg: this.form.drugImg,
        saleIds: this.form.saleIds
      }
      const request = this.editId ? updateDrug(this.editId, payload) : addDrug(payload)
      return request.then(() => app.success(this.editId ? '修改成功' : '新增成功'))
    },
    deleteDrug (drugId) {
      return deleteDrug(drugId)
    },
    beforeUpload (file) {
      const okType = ['image/jpeg', 'image/png', 'image/gif'].includes(file.type)
      const okSize = file.size / 1024 / 1024 <= 1
      if (!okType) {
        app.error('只支持 jpg / png / gif 格式')
        return false
      }
      if (!okSize) {
        app.error('图片大小不能超过 1MB')
        return false
      }
      return true
    },
    doUpload ({ file }) {
      uploadDrugImage(file)
        .then(res => {
          this.form.drugImg = res.data.url
          app.success('图片上传成功')
        })
        .catch(() => {})
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
.drug-img {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  background: #f5f7fa;
}
.img-error {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
  background: #f5f7fa;
  font-size: 18px;
}
.img-empty {
  font-size: 12px;
  color: #c0c4cc;
}
.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}
.upload-box {
  width: 110px;
  height: 110px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 12px;
  gap: 6px;
  cursor: pointer;
}
.upload-box:hover {
  border-color: #13b7a5;
  color: #13b7a5;
}
.upload-box i {
  font-size: 22px;
}
.upload-preview {
  width: 110px;
  height: 110px;
  border-radius: 6px;
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #ebeef5;
}
.upload-tip p {
  margin: 0 0 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
