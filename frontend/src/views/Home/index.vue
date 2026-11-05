<template>
  <div class="home" v-loading="loading">
    <!-- 四个统计数字 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.key">
        <el-card shadow="never" class="stat-card">
          <div class="stat-body">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <i :class="card.icon"></i>
            </div>
            <div class="stat-text">
              <p class="stat-value">{{ data[card.key] || 0 }}</p>
              <p class="stat-label">{{ card.label }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 两张图 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="14">
        <el-card shadow="never">
          <div slot="header" class="card-title">医师职称分布</div>
          <div ref="levelChart" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <div slot="header" class="card-title">医师科室分布</div>
          <div ref="typeChart" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新资讯 -->
    <el-card shadow="never" class="news-card">
      <div slot="header" class="card-title">最新资讯</div>
      <el-table :data="data.news || []" size="small" :show-header="false">
        <el-table-column prop="title" show-overflow-tooltip />
        <el-table-column width="110" align="center">
          <template slot-scope="{ row }">
            <el-tag size="mini" type="info">{{ row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" width="180" align="center" />
      </el-table>
      <p v-if="!(data.news || []).length" class="empty-tip">暂无资讯</p>
    </el-card>
  </div>
</template>

<script>
import echarts from 'echarts'
import { getDashboard } from '../../api/admin/dashboardApi'

export default {
  name: 'Home',
  data () {
    return {
      loading: false,
      data: {},
      cards: [
        { key: 'doctorCount', label: '医师数量', icon: 'el-icon-user', bg: '#e8f8f5', color: '#13b7a5' },
        { key: 'companyCount', label: '医药公司', icon: 'el-icon-office-building', bg: '#eef4ff', color: '#4a7dff' },
        { key: 'drugCount', label: '药品数量', icon: 'el-icon-first-aid-kit', bg: '#fff4e6', color: '#f5a623' },
        { key: 'saleCount', label: '药店数量', icon: 'el-icon-location-outline', bg: '#f4eeff', color: '#8a5cf6' }
      ]
    }
  },
  mounted () {
    this.loadDashboard()
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.resizeCharts)
    if (this.levelChart) this.levelChart.dispose()
    if (this.typeChart) this.typeChart.dispose()
  },
  methods: {
    loadDashboard () {
      this.loading = true
      getDashboard()
        .then(res => {
          this.data = res.data.dashboard || {}
          this.$nextTick(() => this.renderCharts())
        })
        .catch(() => {})
        .finally(() => { this.loading = false })
    },
    renderCharts () {
      this.renderLevelChart()
      this.renderTypeChart()
    },
    renderLevelChart () {
      const list = this.data.levelChart || []
      this.levelChart = echarts.init(this.$refs.levelChart)
      this.levelChart.setOption({
        color: ['#13b7a5'],
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: 40, right: 20, top: 20, bottom: 30 },
        xAxis: {
          type: 'category',
          data: list.map(i => i.name),
          axisLine: { lineStyle: { color: '#dcdfe6' } },
          axisLabel: { color: '#606266' }
        },
        yAxis: {
          type: 'value',
          minInterval: 1,
          axisLine: { show: false },
          axisTick: { show: false },
          splitLine: { lineStyle: { color: '#f0f2f5' } },
          axisLabel: { color: '#909399' }
        },
        series: [{
          type: 'bar',
          barWidth: '45%',
          data: list.map(i => i.value),
          itemStyle: { borderRadius: [4, 4, 0, 0] },
          label: { show: true, position: 'top', color: '#606266' }
        }]
      })
    },
    renderTypeChart () {
      const list = this.data.typeChart || []
      this.typeChart = echarts.init(this.$refs.typeChart)
      this.typeChart.setOption({
        color: ['#13b7a5', '#4a7dff', '#f5a623', '#8a5cf6', '#f56c6c', '#36cfc9', '#ffc069'],
        tooltip: { trigger: 'item', formatter: '{b}: {c} 人 ({d}%)' },
        legend: { bottom: 0, type: 'scroll', textStyle: { color: '#606266', fontSize: 12 } },
        series: [{
          type: 'pie',
          radius: ['42%', '65%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 13, fontWeight: 'bold' } },
          data: list.map(i => ({ name: i.name, value: i.value }))
        }]
      })
    },
    resizeCharts () {
      if (this.levelChart) this.levelChart.resize()
      if (this.typeChart) this.typeChart.resize()
    }
  }
}
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
}
.stat-body {
  display: flex;
  align-items: center;
  gap: 14px;
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}
.stat-value {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.stat-label {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}
.chart-row {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  color: #303133;
}
.chart {
  height: 260px;
}
.news-card {
  margin-bottom: 16px;
}
.empty-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}
</style>
