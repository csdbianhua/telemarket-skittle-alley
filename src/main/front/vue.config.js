module.exports = {
  devServer: {
    disableHostCheck: true,
    proxy: {
      '/ws': {
        target: 'http://localhost:9607',
        changeOrigin: true,
        ws: true,
        pathRewrite: {},
        router: {},
      },
      '/api': {
        target: 'http://localhost:9607',
        changeOrigin: true,
        pathRewrite: {},
        router: {},
      },
    },
  },
};