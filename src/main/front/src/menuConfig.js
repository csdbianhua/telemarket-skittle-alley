// 菜单配置
// headerMenuConfig：头部导航配置
// asideMenuConfig：侧边导航配置

const headerMenuConfig = [];

const asideMenuConfig = [
  {
    path: '/games',
    name: '游戏列表',
    icon: 'el-icon-menu',
    children: [
      {
        path: '/draw_guess',
        name: '你画我猜',
      },
    ],
  },
];

export {headerMenuConfig, asideMenuConfig};
