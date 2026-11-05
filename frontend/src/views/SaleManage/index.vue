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

        <div class="toolbar-right">
          <!-- 扩展功能：列表 / 地图切换 -->
          <span class="map-switch-label">地图展示</span>
          <el-switch v-model="mapMode" active-color="#13b7a5" @change="handleMapModeChange" />
          <el-button
            v-if="isAdmin"
            type="primary"
            icon="el-icon-plus"
            class="add-btn"
            @click="handleAdd"
          >新增销售地点</el-button>
        </div>
      </div>

      <!-- 列表视图 -->
      <template v-if="!mapMode">
        <el-table :data="list" v-loading="loading" border stripe>
          <el-table-column prop="saleId" label="ID" width="90" align="center" />
          <el-table-column prop="saleName" label="药店名称" width="240" show-overflow-tooltip />
          <el-table-column prop="salePhone" label="联系电话" width="150" align="center" />
          <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip>
            <template slot-scope="{ row }">{{ row.address || '—' }}</template>
          </el-table-column>
          <el-table-column label="经纬度" width="190" align="center">
            <template slot-scope="{ row }">
              <span v-if="row.lng && row.lat">{{ row.lng.toFixed(4) }}, {{ row.lat.toFixed(4) }}</span>
              <span v-else class="muted">未标注</span>
            </template>
          </el-table-column>
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
      </template>

      <!-- 地图视图 -->
      <template v-else>
        <div class="map-tip">
          <i class="el-icon-info"></i>
          <span v-if="placing">已进入选点模式：在地图上点击即可新增药店</span>
          <span v-else>点击标记可修改药店；管理员点「新增销售地点」后在地图上落点</span>
        </div>
        <div ref="mapContainer" v-loading="!amapReady" class="map-box"></div>
      </template>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="560px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="药店名称" prop="saleName">
          <el-input v-model="form.saleName" maxlength="100" placeholder="如：青岛同仁堂大药房" />
        </el-form-item>
        <el-form-item label="联系电话" prop="salePhone">
          <el-input v-model="form.salePhone" maxlength="30" placeholder="如：13800001111" />
        </el-form-item>
        <el-form-item label="门店地址">
          <el-input v-model="form.address" maxlength="200" placeholder="可在地图上点选后自动带出" />
        </el-form-item>
        <el-form-item label="经纬度">
          <span v-if="form.lng && form.lat" class="coord">{{ form.lng.toFixed(6) }}, {{ form.lat.toFixed(6) }}</span>
          <span v-else class="muted">未标注（可在开启地图后点选）</span>
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
import { getSaleList, getAllSale, addSale, updateSale, deleteSale } from '../../api/admin/saleManage'
import { loadAMap, DEFAULT_CENTER } from '../../utils/amap'

export default {
  name: 'SaleManage',
  mixins: [crudPage],
  data () {
    return {
      idField: 'saleId',
      mapMode: false,
      placing: false,
      amapReady: false,
      allSales: [],
      rules: {
        saleName: [{ required: true, message: '请输入药店名称', trigger: 'blur' }],
        salePhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
      }
    }
  },
  created () {
    this.fetchList()
  },
  beforeDestroy () {
    this.destroyMap()
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
      return { saleName: '', salePhone: '', address: '', lng: null, lat: null }
    },
    toForm (row) {
      return {
        saleName: row.saleName,
        salePhone: row.salePhone,
        address: row.address || '',
        lng: row.lng,
        lat: row.lat
      }
    },
    save () {
      const payload = {
        saleName: this.form.saleName.trim(),
        salePhone: this.form.salePhone.trim(),
        address: this.form.address ? this.form.address.trim() : null,
        lng: this.form.lng,
        lat: this.form.lat
      }
      const request = this.editId ? updateSale(this.editId, payload) : addSale(payload)
      return request.then(() => {
        app.success(this.editId ? '修改成功' : '新增成功')
        this.fetchList()
        if (this.mapMode) this.refreshMarkers()
      })
    },
    deleteSale (saleId) {
      return deleteSale(saleId).then(res => {
        if (this.mapMode) this.refreshMarkers()
        return res
      })
    },

    /* ---------------- 地图相关 ---------------- */
    handleMapModeChange (on) {
      if (on) {
        this.$nextTick(() => this.initMap())
      } else {
        this.placing = false
        this.destroyMap()
      }
    },
    initMap () {
      if (!this.$refs.mapContainer) return
      this.amapReady = false
      loadAMap()
        .then(AMap => {
          this.AMap = AMap
          this.map = new AMap.Map(this.$refs.mapContainer, {
            zoom: 12,
            center: DEFAULT_CENTER,
            viewMode: '2D'
          })
          this.map.addControl(new AMap.Scale())
          this.map.addControl(new AMap.ToolBar({ position: 'RB' }))
          this.geocoder = new AMap.Geocoder({ city: '青岛' })

          // 地图点击：选点模式下落点并回填地址
          this.map.on('click', e => {
            if (!this.placing || !this.isAdmin) return
            const lng = Number(e.lnglat.getLng().toFixed(6))
            const lat = Number(e.lnglat.getLat().toFixed(6))
            this.placing = false
            this.editId = null
            this.dialogTitle = '新增销售地点'
            this.form = Object.assign(this.initForm(), { lng, lat })
            this.dialogVisible = true
            this.reverseGeocode(lng, lat)
            this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
          })

          this.amapReady = true
          this.refreshMarkers()
        })
        .catch(err => {
          this.amapReady = true
          app.error('地图加载失败：' + (err && err.message ? err.message : '请检查 Key 与安全密钥'))
        })
    },
    /** 重新拉全量药店并重画标记 */
    refreshMarkers () {
      getAllSale()
        .then(res => {
          this.allSales = res.data.sales || []
          this.renderMarkers()
        })
        .catch(() => {})
    },
    renderMarkers () {
      if (!this.map || !this.AMap) return
      if (this.markers && this.markers.length) {
        this.map.remove(this.markers)
      }
      this.markers = []
      this.allSales.forEach(sale => {
        if (!sale.lng || !sale.lat) return
        const marker = new this.AMap.Marker({
          position: [sale.lng, sale.lat],
          title: sale.saleName,
          anchor: 'bottom-center'
        })
        marker.on('click', () => {
          if (!this.isAdmin) return
          this.editId = sale.saleId
          this.dialogTitle = '修改销售地点'
          this.form = this.toForm(sale)
          this.dialogVisible = true
          this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
        })
        this.markers.push(marker)
      })
      if (this.markers.length) {
        this.map.add(this.markers)
        this.map.setFitView(this.markers)
      }
    },
    reverseGeocode (lng, lat) {
      if (!this.geocoder) return
      this.geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          this.form.address = result.regeocode.formattedAddress
        }
      })
    },
    destroyMap () {
      if (this.map) {
        this.map.destroy()
        this.map = null
        this.markers = []
      }
    },
    /** 地图模式下点「新增」：进入选点模式 */
    handleAdd () {
      if (this.mapMode && this.amapReady) {
        this.placing = true
        app.info('请在地图上点击选择药店位置')
        return
      }
      this.openAdd()
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
  display: flex;
  align-items: center;
  gap: 10px;
}
.map-switch-label {
  font-size: 13px;
  color: #606266;
}
.add-btn {
  margin-left: 6px;
}
.danger {
  color: #f56c6c;
}
.muted {
  color: #c0c4cc;
  font-size: 12px;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
.map-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #909399;
}
.map-box {
  width: 100%;
  height: 560px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}
.coord {
  font-family: Consolas, Monaco, monospace;
  color: #13b7a5;
}
</style>
