Telemarket-skittle-alley
===

在线小游戏集合。

数据库为嵌入式h2，存储到本地文件中。

使用webflux、netty reactor、vue。

本项目前后端分离，

## 如何开发

在开发环境下可以分别单独启动前端项目和后端项目。也可以将前端合并到后端项目一起运行。需要安装`node 8`以上以执行前端资源编译。

``` bash
cd src/main/front
npm install
```

### 1.分别单独启动
后端服务执行main方法即可

前端项目需要
``` bash
cd src/main/front
npm run start
```
访问 http://localhost:8080/index.html 即可，此时后端请求会被代理


### 2. 合并启动

执行`mvn process-resources`后正常启动后端项目即可
 
> 请注意：第一次使用ide启动或者前端资源发生变动后，都需要执行前端资源生成。
> 手动执行方式为到`src/main/front`下执行`npm i && npm run build`(前端资源编译)然后复制`dist`到`resources`目录下

比较推荐使用第一种方式开发，前端服务处于开发模式，修改能够快速地反馈。

## 游戏列表

### 你画我猜

可以使用完整的游戏过程。

词汇的表在 `sql/data.sql`

也可以运行时自行提交

> 如果运行过程无法正确地进行游戏状态迁移（具体表现为倒计时结束后无响应），请确认IDE在编译过程中执行了aspectj的编译。

