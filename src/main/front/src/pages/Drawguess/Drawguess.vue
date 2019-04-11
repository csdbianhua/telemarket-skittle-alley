<template>
    <el-container>
        <el-header>
            <el-row type="flex" justify="space-between" :gutter="20">
                <el-col :span="6" type="flex" align="middle" justify="center"><span v-if="ctxGame.currentUser">当前画师 {{ currentUserName }}</span>
                </el-col>
                <el-col :span="12" type="flex" align="middle" justify="center"><h2 v-html="title"></h2></el-col>
                <el-col :span="6" type="flex" align="middle" justify="center"><span v-if="clockSeconds"> 还剩{{  clockSeconds }}秒 </span>
                </el-col>
            </el-row>
        </el-header>
        <el-main>
            <el-row :gutter="20">
                <el-col :span="6">
                    <el-row>
                        <ul class="user-thread">
                            <li v-for="(userInfo, id) in allUserInfo"
                                v-bind:style="{ color: myId === id ? '#5cb85c' : '' }">
                                <span class="user_name"> <span v-if="userInfo.status > 0"
                                                               class="el-icon-success"></span> {{ userInfo.name }}  </span>
                                <span class="user_score">[ {{ userInfo.score }} ]</span>
                            </li>
                        </ul>
                    </el-row>
                    <el-row>
                        <el-input type="text" v-model="inputName"
                                  :disabled="this.ctxGame.status !== constants.GAME_READY">
                            <el-button slot="append" plain type="primary" @click="sendNewName"
                                       :disabled="this.ctxGame.status !== constants.GAME_READY"
                            >改名
                            </el-button>
                            <el-button slot="append" plain type="primary" @click="ready"
                                       :disabled="this.ctxGame.status !== constants.GAME_READY || !flagUser"
                            > {{ myInfo.status > 0 ? '取消' : '准备' }}
                            </el-button>
                        </el-input>

                    </el-row>
                </el-col>
                <el-col :span="12">
                    <div style="align-content: center;">
                        <el-row type="flex" align="middle" justify="center">
                            <canvas id="canvas" width="800" height="420"></canvas>
                        </el-row>
                        <el-row type="flex" align="middle" justify="center">
                            线宽 :
                            <el-select v-model="ctxGame.width" :disabled="!isCurrentUser">
                                <el-option v-for="item in 20" :key="item" :label="item" :value="item"></el-option>
                            </el-select>
                            颜色 :
                            <el-select v-model="ctxGame.color" :disabled="!isCurrentUser">
                                <el-option v-for="item in ['black','blue','red','green','yellow','gray']" :key="item"
                                           :label="item" :value="item">
                                </el-option>
                            </el-select>
                            <el-button type="danger" :disabled="!isCurrentUser" @click="sendClearSig">清空画板
                            </el-button>
                        </el-row>
                    </div>
                </el-col>
                <el-col :span="6">
                    <el-row>
                        <ul class="chat-thread" ref="charThread">
                            <li v-for="msg in msgList"><span v-html="msg"></span></li>
                        </ul>
                    </el-row>
                    <el-row>
                        <el-input type="text" v-model="inputMsg">
                            <el-button slot="append" plain @click="sendText" type="primary">发送</el-button>
                        </el-input>

                    </el-row>
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
        inputName: '',
        inputMsg: '',
        clockSeconds: 0,
        timeOffset: 0,
        myId: '',
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
      allUserInfoBridge: function() {
        return Object.assign({}, this.allUserInfo);
      },
      isCurrentUser: function() {
        return this.myId && this.myId === this.ctxGame.currentUser;
      },
      myInfo: function() {
        return this.myId ? this.allUserInfo[this.myId] : {};
      },
    },
    watch: {
      'ctxGame.players': {
        deep: true,
        handler(newPlayers) {
          let players = Object.values(newPlayers);
          if (players.length > 0) {
            players.forEach(this.addOrUpdateUser);
          } else {
            Object.values(this.allUserInfo).forEach(function(info) {
              info.status = 0;
              info.score = 0;
            });
          }
        },
      },
      'ctxGame.width': function(newWidth, oldWidth) {
        if (newWidth === oldWidth || !this.isCurrentUser) {
          return;
        }
        this.sendChangeBrush('width', newWidth);
      },
      'ctxGame.color': function(newColor, oldColor) {
        if (newColor === oldColor || !this.isCurrentUser) {
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
      allUserInfoBridge: {
        handler(newMap, oldMap) {
          let newUserIds = Object.getOwnPropertyNames(newMap);
          let oldUserIds = Object.getOwnPropertyNames(oldMap);
          if (newUserIds.length > oldUserIds.length) {
            let meJoined;
            for (let i = 0; i < newUserIds.length; i++) {
              let userId = newUserIds[i];
              if (oldMap[userId]) {
                continue;
              }
              let userInfo = newMap[userId];
              let msg;
              if (this.myId === userInfo.id) {
                meJoined = `<b>你</b> 进入了房间。名称为:${userInfo.name}。`;
                continue;
              } else {
                msg = `<b>${userInfo.name}</b> 进入了房间。`;
              }
              this.msgList.push(msg);
            }
            if (meJoined) {
              this.msgList.push(meJoined);
            }
          } else if (oldUserIds.length > newUserIds.length) {
            for (let i = 0; i < oldUserIds.length; i++) {
              let userId = oldUserIds[i];
              if (newMap[userId]) {
                continue;
              }
              let userInfo = oldMap[userId];
              this.msgList.push(`<b>${userInfo.name}</b> 离开了房间。`);
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
            that.draw(data.x, data.y, data.isBegin);
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
              that.initCtx(data);
            } else {
              that.addOrUpdateUser(data.info);
            }
          },
          '11': function(res) { // ready
            let data = res.data;
            that.addOrUpdateUser(data);
          },
          '12': function(res) { // leave
            that.removeUser(res.data);
          },
          '13': function(res) { // change name
            that.addOrUpdateUser(res.data);
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
        if (this.ctxCanvas) {
          return;
        }
        let canvas = document.getElementById('canvas');
        this.ctxCanvas = canvas.getContext('2d');
        let that = this;

        function getXAndY(mouseEvent, canvas, isBegin) {
          let rect = canvas.getBoundingClientRect();
          return {
            x: mouseEvent.clientX - rect.left,
            y: mouseEvent.clientY - rect.top,
            isBegin: isBegin,
          };
        }

        canvas.addEventListener('mousedown', function(e) {
          if (that.isCurrentUser) {
            that.mousePressed = true;
            that.sendMoveSig(getXAndY(e, canvas, true));
          }

        });

        canvas.addEventListener('mousemove', function(e) {
          if (that.isCurrentUser && that.mousePressed) {
            that.sendMoveSig(getXAndY(e, canvas, false));
          }
        });

        canvas.addEventListener('mouseup', function(e) {
          that.mousePressed = false;
        });
        canvas.addEventListener('mouseleave', function(e) {
          that.mousePressed = false;
        });
      },
      initCtx: function(data) {
        this.updateCtx(data.ctx);
        // 同步所有在线用户
        this.allUserInfo = data.players;
        this.myId = data.info;
        if (data.timestamp) {
          this.timeOffset = data.timestamp - new Date().getTime();
          console.log('初始化服务器时间差' + this.timeOffset + 'ms');
        }
      },
      sendMoveSig: function(xy) {
        this.websocket.send(JSON.stringify({code: 1, msg: xy}));
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
      draw: function(x, y, isBegin) {
        let ctxCanvas = this.ctxCanvas;
        if (!isBegin) {
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
        this.disableUserBtn(true);
        this.websocket.send(JSON.stringify(
            {code: 11, msg: {id: this.myId, status: !(this.myInfo.status > 0)}}));
        let that = this;
        setTimeout(function() {
          that.disableUserBtn(false);
        }, 1000);
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
        this.$delete(this.allUserInfo, userInfo.id);
      },
      addOrUpdateUser: function(userInfo) {
        this.$set(this.allUserInfo, userInfo.id, userInfo);
      },
      updateCtx: function(newCtx) {
        this.ctxGame = newCtx;
      },
      readyGame: function() {
        this.stopClockAndTimeout();
        this.clearArea();
        this.title = '你画我猜';
        this.clockSeconds = 0;
      },
      runGame: function() {
        this.clearArea();
        this.startClock();
        if (this.isCurrentUser) {
          this.title = '请作画:' + this.ctxGame.currentWord.word;
        } else {
          this.title = '快猜!';
          this.startTips();
        }
      },
      waitGame: function(ctx) {
        this.title = `答案是${this.ctxGame.currentWord.word}。共有<b>${this.ctxGame.rightNumber}</b>人猜对`;
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
      countdown: function(targetTime) {
        let serverTime = this.getServerTime();
        let diff = targetTime - serverTime;
        if (diff <= 0) {
          diff = 0;
        }
        diff /= 1000;

        this.clockSeconds = Math.floor(diff);
        if (diff <= 0) {
          this.stopClockAndTimeout();
        }
      },
      stopClockAndTimeout: function() {
        if (this.clockIds.length > 0) {
          this.clockIds.forEach(function(val) {
            clearInterval(val);
          });
          this.clockIds.splice(0, this.clockIds.length);
        }
        if (this.timeoutIds.length > 0) {
          this.timeoutIds.forEach(function(val) {
            clearTimeout(val);
          });
          this.timeoutIds.splice(0, this.timeoutIds.length);
        }
      },
    },
    created() {
    },
    mounted() {
      this.initCanvas();
      this.initWebsocket();
    },
    destroyed() {
      this.websocket.close();
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

    #canvas {
        border: 2px solid #6699cc;
        padding: 16px 0 16px 0;

    }
</style>