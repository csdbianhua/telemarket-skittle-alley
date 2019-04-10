<template>
    <el-container>
        <el-header>
            <el-row :gutter="20">
                <el-col :span="6"><span v-if="ctxGame.currentUser">当前画师 {{ currentUserName }}</span></el-col>
                <el-col :span="12"><h2>{{ title }}</h2></el-col>
                <el-col :span="6"><span v-if="clockSeconds"> 还剩{{  clockSeconds }}秒 </span></el-col>
            </el-row>
        </el-header>
        <el-main>
            <el-row :gutter="20">
                <el-col :span="6">
                    <ul class="user-thread">
                    </ul>
                    <div>
                        <input type="text" v-model="inputName" :disabled="this.ctxGame.status !== constants.GAME_READY"
                               class="form-control">
                        <span class="input-group-btn">
                        <button class="btn btn-warning" @click="sendNewName"
                                :disabled="this.ctxGame.status !== constants.GAME_READY"
                                type="button">改名!</button>
                        <button class="btn btn-danger" @click="ready"
                                :disabled="this.ctxGame.status !== constants.GAME_READY || !flagUser"
                                type="button">准备!</button>
                    </span>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div style="align-content: center;">
                        <canvas id="canvas" width="800" height="416" style="border:2px solid #6699cc"></canvas>
                        <div class="control-ops">
                            <button :disabled="!isCurrentUser()" @click="sendClearSig">清空画板
                            </button>
                            线宽 :
                            <el-select v-model="ctxGame.width" :disabled="!isCurrentUser()">
                                <option v-for="item in 10" :value="item">{{ item }}</option>
                            </el-select>
                            颜色 :
                            <el-select v-model="ctxGame.color" :disabled="!isCurrentUser()">
                                <option v-for="item in ['black','blue','red','green','yellow','gray']" :value="item">
                                    {{ item }}
                                </option>
                            </el-select>
                        </div>
                    </div>
                </el-col>
                <el-col :span="6">
                    <ul class="chat-thread" ref="charThread">
                        <li v-for="msg in msgList"><span v-html="msg"></span></li>
                    </ul>
                    <div>
                        <input type="text" v-model="inputMsg">
                        <span>
                            <button @click="sendText" type="button">发送!</button>
                        </span>
                    </div>
                </el-col>
            </el-row>
        </el-main>
    </el-container>
</template>
<script>

  let lastX, lastY;
  const MAX_MSG_COUNT = 50;
  export default {
    name: 'draw_guess',
    data() {
      return {
        constants: {
          GAME_READY: 0,
          GAME_RUN: 1,
          GAME_WAIT: 2,
          GAME_END: 3,
        },
        title: '正在连接中...',
        currentUser: '',
        inputName: '',
        inputMsg: '',
        clockSeconds: 0,
        timeOffset: 0,
        myInfo: {},
        allUserInfo: {},
        msgList: [],
        ctxCanvas: null,
        ctxGame: {
          currentUser: '',
          status: 0,
          currentWord: {
            word: '',
            wordCount: 0,
            wordType: '',
          },
          endTime: 0,
          players: {},
          width: 1,
          color: 'black',
          rightNumber: 0,
        },
        // 是否可以绘画
        flagDraw: false,
        // 是否可以进行用户操作
        flagUser: true,
        mousePressed: false,
        clockIds: [],
        timeoutIds: [],
        websocket: null,
      };
    },
    computed: {
      currentUserName: function() {
        if (!this.ctxGame.currentUser) {
          return '';
        }
        return this.allUserInfo[this.ctxGame.currentUser].name;
      },
    },
    watch: {
      'ctxGame.width': function(newWidth, oldWidth) {
        if (newWidth === oldWidth) {
          return;
        }
        this.sendChangeBrush('width', newWidth);
      },
      'ctxGame.color': function(newColor, oldColor) {
        if (newColor === oldColor) {
          return;
        }
        this.sendChangeBrush('color', newColor);
      },
      'ctxGame.status': function(newStatus) {
        switch (newStatus) {
          case this.constants.GAME_READY:
            return this.readyGame();
          case this.constants.GAME_RUN:
            return this.runGame();
          case this.constants.GAME_WAIT:
            return this.waitGame();
          case this.constants.GAME_END:
            return this.endGame();
        }
      },
      msgList: function() {
        let charThread = this.$refs.charThread;
        charThread.animate({
          scrollTop: charThread.scrollHeight - charThread.scrollTop,
        }, 300);
        if (this.msgList.length > MAX_MSG_COUNT) {
          this.msgList.splice(0, MAX_MSG_COUNT - this.msgList.length);
        }
      },
      allUserInfo: {
        handler(newMap, oldMap) {
          let newUserIds = Object.getOwnPropertyNames(newMap);
          let oldUserIds = Object.getOwnPropertyNames(oldMap);
          if (newUserIds.length > oldUserIds.length) {
            for (let i = 0; i < newUserIds.length; i++) {
              let userId = newUserIds[i];
              if (oldMap[userId]) {
                continue;
              }
              let userInfo = newMap[userId];
              let msg;
              if (this.myInfo.id === userInfo.id) {
                msg = `<b>你</b> 进入了房间。名称为:${userInfo.name}。`;
              } else {
                msg = `<b>${userInfo.name}</b> 进入了房间。`;
              }
              this.msgList.push(msg);
            }
          } else if (oldUserIds.length > newUserIds.length) {
            for (let i = 0; i < oldUserIds.length; i++) {
              let userId = oldUserIds[i];
              if (newMap[userId]) {
                continue;
              }
              let userInfo = oldMap[userId];
              let msg;
              if (this.myInfo.id === userInfo.id) {
                msg = `<b>你</b> 进入了房间。名称为:${userInfo.name}。`;
              } else {
                msg = `<b>${userInfo.name}</b> 进入了房间。`;
              }
              this.msgList.push(msg);
            }
          } else {
            for (let i = 0; i < oldUserIds.length; i++) {
              let userId = oldUserIds[i];
              let newUserInfo = newMap[userId];
              let oldUserInfo = oldMap[userId];
              if (newUserInfo.name !== oldUserInfo.name) {
                this.msgList.push(`<b>${oldUserInfo.name}</b> 更名为 <b>${newUserInfo.name}</b>。`);
              }
            }

          }
        },
        deep: true,
      },
    },
    methods: {
      initWebsocket: function() {
        let websocket = this.websocket = new WebSocket(`ws://${window.location.host}/ws/games/draw_guess`);
        let that = this;
        let msgProcessors = {
          '1': function(res) { // move
            let data = res.data;
            that.draw(data.x, data.y, data.isDown);
          },
          '2': function() { // clear
            that.clearArea();
          },
          '3': function(res) { // change brush
            let data = res.data;
            that.ctxGame[data.type] = data.value;
          },
          '4': function(res) { // msg
            let data = res.data;
            if (Array.isArray(data)) {
              data.forEach(function(val) {
                that.msgList.push(val);
              });
            } else {
              that.msgList.append(data);
            }
          },
          '10': function(res) { // join
            let data = res.data;
            if (data.assign) {
              that.myInfo = data.info;
              that.initCtx(data);
            } else {
              that.addUser(data.myInfo);
            }
          },
          '11': function(res) { // ready
            let data = res.data;
            that.readyUser(data);
          },
          '12': function(res) { // leave
            that.removeUser(res.data);
          },
          '13': function(res) { // change name
            that.changeUser(res.data);
          },
          '20': function(res) { // update ctx
            that.updateCtx(res.data);
          },
        };
        websocket.onmessage = function(event) {
          if (event.data[0] !== '{') {
            return;
          }
          let res = JSON.parse(event.data);
          msgProcessors[res.code.toString()](res);
        };
        websocket.onopen = function() {
          that.title = '你画我猜';
        };
        websocket.onclose = function(error) {
          that.$message.error('服务器连接失败，请刷新页面');
          that.title = '连接丢失...';
          console.error(error);
        };
        websocket.onerror = function(err) {
          console.error('错误' + err);
        };
      },
      initCanvas: function() {
        let canvas = this.ctxCanvas = document.getElementById('canvas').getContext('2d');
        let that = this;
        canvas.onmousedown = function(e) {
          that.mousePressed = true;
          that.sendMoveSig(e.pageX - canvas.offset().left,
              e.pageY - canvas.offset().top, false);
        };

        canvas.onmousemove = function(e) {
          if (that.mousePressed) {
            that.sendMoveSig(e.pageX - canvas.offset().left,
                e.pageY - canvas.offset().top, true);
          }
        };

        canvas.onmouseup = function(e) {
          that.mousePressed = false;
        };
        canvas.onmouseleave = function(e) {
          that.mousePressed = false;
        };
      },
      initCtx: function(data) {
        this.allUserInfo = data.players;
        if (data.timestamp) {
          this.timeOffset = data.timestamp - new Date().getTime();
          console.log('初始化服务器时间差' + this.timeOffset + 'ms');
        }
        this.updateCtx(data.ctx);
      },
      sendMoveSig: function(x, y, isDown) {
        this.websocket.send(
            JSON.stringify({code: 1, msg: {x: x, y: y, isDown: isDown}}));
      },
      sendText: function() {
        if (this.inputMsg) {
          this.websocket.send(JSON.stringify({code: 4, msg: this.inputMsg}));
          this.inputMsg = '';
        }
      },
      getServerTime: function() {
        return Date.now() + this.timeOffset;
      },
      isCurrentUser: function() {
        return this.myInfo.id === this.ctxGame.currentUser;
      },
      draw: function(x, y, isDown) {
        let ctxCanvas = this.ctxCanvas;
        if (isDown) {
          ctxCanvas.beginPath();
          ctxCanvas.strokeStyle = this.ctxGame.color;
          ctxCanvas.lineWidth = this.ctxGame.width;
          ctxCanvas.lineJoin = 'round';
          ctxCanvas.moveTo(lastX || x, lastY || y);
          ctxCanvas.lineTo(x, y);
          ctxCanvas.closePath();
          ctxCanvas.stroke();
        }
        lastX = x;
        lastY = y;
      },
      clearArea: function() {
        // Use the identity matrix while clearing the canvas
        this.ctxCanvas.setTransform(1, 0, 0, 1, 0, 0);
        this.ctxCanvas.clearRect(0, 0, this.ctxCanvas.canvas.width, this.ctxCanvas.canvas.height);
      },
      sendClearSig: function() {
        this.websocket.send(JSON.stringify({code: 2}));
      },
      sendChangeBrush: function(type, val) {
        this.websocket.send(
            JSON.stringify({code: 3, msg: {type: type, value: val}}));
      },
      ready: function() {
        this.sendReady();
      },
      sendReady: function() {
        this.websocket.send(JSON.stringify(
            {code: 11, msg: {id: this.myInfo.id, status: !(this.myInfo.status > 0)}}));
      },
      sendNewName: function() {
        if (this.inputName) {
          if (this.inputName.length < 1 || this.inputName.length > 10) {
            this.$message.warning('名字不可小于1个字符大于10个字符');
            return;
          }
          this.websocket.send(JSON.stringify({code: 13, msg: this.inputName}));
          this.inputName = '';
        }
      },
      removeUser: function(userInfo) {
        delete this.allUserInfo[userInfo.id];
      },
      addUser: function(userInfo) {
        this.allUserInfo[userInfo.id] = userInfo;
      },
      changeUser: function(userInfo) {
        this.allUserInfo[userInfo.id] = userInfo;
      },
      readyUser: function(readyInfo) {
        if (readyInfo.id === this.myInfo.id) {
          this.myInfo = readyInfo;
        }
        this.allUserInfo[readyInfo.id] = readyInfo;
      },
      updateCtx: function(newCtx) {
        this.ctxGame = newCtx;
      },
      readyGame: function() {
        this.disableDraw(true);
        this.stopClockAndTimeout();
        this.clearArea();
      },
      runGame: function(ctx) {
        this.clearArea();
        this.startClock();
        if (this.isCurrentUser()) {
          this.title = '请作画:' + ctx.currentWord.word;
        } else {
          this.title = '快猜!';
          this.startTips();
        }
      },
      waitGame: function(ctx) {
        this.title = `答案是${this.ctxGame.currentWord.word}'。共有<b>${this.ctxGame.rightNumber}</b>人猜对`;
        this.allUserInfo = this.ctxGame.players;
        this.startClock(ctx);
      },
      endGame: function(ctx) {
        this.title = '最终得分';
        this.startClock(ctx);
      },
      startTips: function() {
        this.timeoutIds.push(setTimeout(this.countTimeout, 30000));
        this.timeoutIds.push(setTimeout(this.typeTimeout, 10000));
      },
      countTimeout: function() {
        this.title += this.ctxGame.currentWord.wordCount + '个字！还猜不出来???';
      },
      typeTimeout: function() {
        this.title += this.ctxGame.currentWord.wordType + '!';
      },
      startClock: function() {
        let ctx = this.ctxGame;
        this.stopClockAndTimeout();
        this.countdown(ctx.endTime);
        this.clockIds.push(setInterval(this.countdown, 500, ctx.endTime));
      },
      disableUserBtn: function(flag) {
        this.flagUser = !flag;
      },
      disableDraw: function(flag) {
        this.flagDraw = !flag;
      },
      countdown: function(targetTime) {
        let serverTime = this.getServerTime();
        let diff = targetTime - serverTime;
        if (diff <= 0) {
          diff = 0;
        }
        diff /= 1000;

        this.clockSeconds = diff;
        if (diff <= 0) {
          this.stopClockAndTimeout();
        }
      },
      stopClockAndTimeout: function() {
        if (this.clockIds.length > 0) {
          this.clockIds.forEach(function(val) {
            clearInterval(val);
          });
          this.clockIds.clear();
        }
        if (this.timeoutIds.length > 0) {
          this.timeoutIds.forEach(function(val) {
            clearTimeout(val);
          });
          this.timeoutIds.clear();
        }
      },
    },
    created() {
    },
    mounted() {
      let that = this;
      window.addEventListener('load', () => {
        that.initCanvas();
        that.initWebsocket();
      });
    },
  };
</script>
<style lang="scss" scoped>
    .chat-thread {
        list-style: none;
        overflow-y: auto;
        overflow-x: hidden;
        padding-left: 0;
        height: 420px;
    }

    .user-thread {
        list-style: none;
        overflow-y: auto;
        overflow-x: hidden;
        padding-left: 0;
        height: 420px;
    }

    .chat-thread li {
        position: relative;
        clear: both;
        display: inline-block;
        padding: 16px 20px 16px 20px;
        font-size: 12px;
        word-break: break-all;
        border-radius: 10px;
        background-color: rgba(25, 147, 147, 0.2);
        margin: 0 15px 20px 0;
        color: #0AD5C1;
    }

    .user-thread li {
        position: relative;
        clear: both;
        display: inline-block;
        padding: 16px 20px 16px 20px;
        font-size: 18px;
        font-weight: bold;
        word-break: break-all;
        border-radius: 10px;
        background-color: rgba(25, 147, 147, 0.2);
        margin: 0 15px 20px 0;
        color: #0AD5C1;
    }
</style>