Telemarket-skittle-alley
===

在线小游戏集合。

数据库为嵌入式h2，存储到本地文件中。

使用的webflux、netty reactor。

启动后访问根目录即可。

## 你画我猜

可以使用完整的游戏过程。

词汇的表在 `sql/data.sql`

也可以运行时自行提交 uri为 `/games/draw_guess/word_submit`

> 如果运行过程无法正确地进行游戏状态迁移（具体表现为倒计时结束后无响应），请确认IDE在编译过程中执行了aspectj的编译。

