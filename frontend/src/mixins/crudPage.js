import app from '../utils/app'
import { downloadExport } from '../utils/download'

/**
 * 列表页公共逻辑：分页、关键字、弹窗开关、删除确认。
 *
 * 各页面只需要实现 fetchList()（调自己的 api，把结果写进 list/total）
 * 和 save()（调新增或修改接口），其余交互都在这里统一，避免 8 个页面各写一遍。
 */
export default {
  data () {
    return {
      loading: false,
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      keyword: '',
      /** 排序：sort 是列 prop，order 是 asc/desc（后端有白名单校验） */
      sort: '',
      order: '',
      dialogVisible: false,
      dialogTitle: '新增',
      submitting: false,
      editId: null,
      form: {},
      /** 各表主键字段名不同（id / cityId / companyId / drugId / saleId），子类覆盖 */
      idField: 'id',
      /** 导出模块名，子类覆盖（doctor/drug/company/...） */
      exportModule: '',
      exporting: false
    }
  },
  computed: {
    /** 只有管理员能增删改，医师只读 */
    isAdmin () {
      return this.$store.state.role === 'ROLE_1'
    }
  },
  methods: {
    handleSearch () {
      this.pageNum = 1
      this.fetchList()
    },
    handleReset () {
      this.keyword = ''
      this.pageNum = 1
      this.fetchList()
    },
    handlePageChange (page) {
      this.pageNum = page
      this.fetchList()
    },
    handleSizeChange (size) {
      this.pageSize = size
      this.pageNum = 1
      this.fetchList()
    },
    /** 导出当前筛选条件下的全部数据 */
    handleExport () {
      if (!this.exportModule) return
      this.exporting = true
      downloadExport(this.exportModule, this.buildExportParams()).finally(() => { this.exporting = false })
    },
    /** 子类有额外筛选条件时覆盖 */
    buildExportParams () {
      return { keyword: this.keyword, sort: this.sort, order: this.order }
    },
    /** el-table 的 sort-change：prop 为空表示取消排序 */
    handleSortChange ({ prop, order }) {
      this.sort = order ? prop : ''
      this.order = order === 'descending' ? 'desc' : 'asc'
      this.pageNum = 1
      this.fetchList()
    },
    /** 打开新增弹窗：子类用 initForm() 给默认值 */
    openAdd () {
      this.editId = null
      this.dialogTitle = '新增'
      this.form = this.initForm()
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    /** 打开编辑弹窗：子类用 toForm(row) 把行数据转成表单 */
    openEdit (row) {
      this.editId = row[this.idField]
      this.dialogTitle = '修改'
      this.form = this.toForm(row)
      this.dialogVisible = true
      this.$nextTick(() => this.$refs.form && this.$refs.form.clearValidate())
    },
    /** 提交：子类实现 save() */
    submit () {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        this.save()
          .then(() => {
            this.dialogVisible = false
            this.fetchList()
          })
          .catch(() => {})
          .finally(() => { this.submitting = false })
      })
    },
    /** 删除确认 */
    handleDelete (row, name, remove) {
      app.confirm(`确定删除「${name}」吗？删除后不可恢复。`).then(ok => {
        if (!ok) return
        remove(row[this.idField]).then(() => {
          app.success('删除成功')
          // 删掉当前页最后一条时回退一页
          if (this.list.length === 1 && this.pageNum > 1) this.pageNum--
          this.fetchList()
        }).catch(() => {})
      })
    }
  }
}
