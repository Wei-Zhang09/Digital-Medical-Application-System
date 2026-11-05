/**
 * 前端构建配置（与官方任务书一致：publicPath 相对路径、关闭 ESLint 阻断、开发端口 9092）
 * 端口 9092 是为了避开后端 8080。
 */
module.exports = {
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static',
  lintOnSave: false,
  productionSourceMap: false,
  devServer: {
    host: '0.0.0.0',
    port: 9092,
    open: false,
    // 开发态前端直连 8080（见 src/utils/request.js）；若要改成同源代理，解开下面配置并把 baseURL 改成 /api
    // proxy: {
    //   '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true },
    //   '/image': { target: 'http://127.0.0.1:8080', changeOrigin: true }
    // }
  }
}
