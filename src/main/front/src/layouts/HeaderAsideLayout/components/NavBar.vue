<template>
    <el-menu class="navbar" mode="horizontal">
        <div class="user-profile-container" trigger="click">
            <div class="user-profile-content">
                <div class="user-profile-body">
                    <img class="user-avatar" src="https://img.alicdn.com/tfs/TB1ONhloamWBuNjy1XaXXXCbXXa-200-200.png">
                    <span class="user-name">我</span>
                </div>
            </div>
        </div>
    </el-menu>
</template>

<script>

  export default {
    name: 'NavBar',
    methods: {},
  };
</script>

<style lang="css" scoped>
    .navbar .navbar {
      height: 64px;
      box-shadow: 0 1px 4px rgba(0, 21, 41, .08);
    }
    .navbar .user-profile-container {
      position: absolute;
      right: 20px;
      cursor: pointer;
    }
    .navbar .user-profile-container .user-profile-content {
      display: flex;
      padding: 20px 0;
    }
    .navbar .user-profile-container .menu-icons {
      display: flex;
      align-items: center;
    }
    .navbar .user-profile-container .menu-icons .menu-icon {
      padding: 0 12px;
    }
    .navbar .user-profile-container .menu-icons .menu-icon .icon {
      display: inline-block;
      font-size: 18px;
      text-align: center;
    }
    .navbar .user-profile-container .user-profile-body {
      position: relative;
      display: flex;
      flex-direction: row;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding-right: 14px;
    }
    .navbar .user-profile-container .user-avatar {
      width: 24px;
      height: 24px;
      margin: 0 8px 0 12px;
      border-radius: 4px;
    }
    .navbar .user-profile-container .user-name {
      color: rgba(0, 0, 0, .65);
    }
    .navbar .user-profile-container .user-department {
      font-size: 12px;
      color: rgba(102, 102, 102, 0.65);
    }
    .navbar .user-profile-container .el-icon-caret-bottom {
      position: absolute;
      right: 0;
      top: 13px;
      font-size: 12px;
    }
</style>

