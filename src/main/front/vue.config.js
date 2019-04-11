module.exports = {
  publicPath: '/index.html',
  devServer: {
    disableHostCheck: true,
    proxy: {
      '/': {
        target: 'http://localhost:9607',
        changeOrigin: true,
        ws: true,
        pathRewrite: {},
        router: {},
      },
    },
  },
};