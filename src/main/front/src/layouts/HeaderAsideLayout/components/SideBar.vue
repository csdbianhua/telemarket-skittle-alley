<template>
    <scroll-bar>
        <div class="logo">
            <img :src="img_logo" width="40">
            <span class="site-name"><router-link to="/">Telemarket</router-link></span>
        </div>
        <el-menu
                mode="vertical"
                :show-timeout="200"
                background-color="#00142a"
                text-color="hsla(0, 0%, 100%, .65)"
                active-text-color="#409EFF"
        >
            <template v-for="item in asideMenuConfig">
                <router-link v-if="!item.children" :to="item.path" :key="item.name">
                    <el-menu-item :index="item.path">
                        <i v-if="item.icon" :class="item.icon"></i>
                        <span v-if="item.name" slot="title">{{item.name}}</span>
                    </el-menu-item>
                </router-link>

                <el-submenu v-else :index="item.name || item.path" :key="item.name">
                    <template slot="title">
                        <i v-if="item && item.icon" :class="item.icon"></i>
                        <span v-if="item && item.name" slot="title">{{item.name}}</span>
                    </template>
                    <template v-for="child in item.children" v-if="!child.hidden">
                        <router-link :to="item.path + child.path" :key="child.name">
                            <el-menu-item :index="item.path + child.path">
                                <span v-if="child && child.name" slot="title">{{child.name}}</span>
                            </el-menu-item>
                        </router-link>
                    </template>
                </el-submenu>

            </template>
        </el-menu>
    </scroll-bar>
</template>

<script>
  import ScrollBar from './ScrollBar';
  import img_logo from '@/assets/logo.png';
  import {asideMenuConfig} from '../../../menuConfig';

  export default {
    components: {ScrollBar},
    name: 'SideBar',
    props: {},
    data() {
      return {
        img_logo,
        asideMenuConfig,
      };
    },
  };
</script>

<style lang="css" scoped>
.logo {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 64px;
  line-height: 64px;
  background: #002140;
  color: #fff;
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  overflow: hidden;
}
.site-name {
  margin-left: 10px;
}
.sidebar-container {
  box-shadow: 2px 0 6px rgba(0, 21, 41, .35);
  transition: width 0.28s;
  width: 256px !important;
  height: 100%;
  position: fixed;
  top: 0;
  bottom: 0;
  left: 0;
  z-index: 1001;
  overflow: hidden;
}
.sidebar-container a {
  display: inline-block;
  width: 100%;
}
.sidebar-container .el-menu {
  padding-top: 16px;
  width: 100% !important;
  border: none;
}
.sidebar-container .el-submenu .el-menu-item {
  min-width: 256px !important;
  padding-left: 48px !important;
  background-color: #000c17 !important;
}
.sidebar-container .el-submenu .el-menu-item:hover {
  color: #fff !important;
}
.sidebar-container .el-menu-item.is-active, .sidebar-container .el-submenu .el-menu-item.is-active {
  background-color: #188fff !important;
  color: #fff !important;
}
.sidebar-container .el-submenu__title i {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.65);
}

</style>
