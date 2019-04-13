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
  chainWebpack: webpackConfig => {
    webpackConfig.module.rule('images').
        use('url-loader').
        loader('url-loader').
        tap(options => {
          // 修改以免发生转换为base64导致无法正确排序帧数,不过改成精灵图会更加合适
          options.limit = 1;
          return options;
        });
  },
};