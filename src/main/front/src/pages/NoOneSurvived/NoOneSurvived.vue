<template>
    <el-container>
        <el-col :span="19" id="container">

        </el-col>
        <el-col :span="5">
            <el-row>
                <ul class="chat-thread" ref="charThread">
                    <li v-for="msg in msgList"><b>{{ msg.name }} </b>: {{ msg.content }}</li>
                </ul>
            </el-row>
            <el-row>
                <el-input type="text" v-model="inputMsg" v-on:keyup.enter.native="sendText">
                    <el-button slot="append" plain @click="sendText" type="primary">发送</el-button>
                </el-input>

            </el-row>
        </el-col>
    </el-container>
</template>
<script>

  import * as PIXI from 'pixi.js';
  import 'pixi-sound';

  PIXI.sound.volumeAll = 0.05;

  const resourcePattern = new RegExp('/.*?/.*?_(\\d+)\\.\\w*?\\.?\\w+?');
  const PI = Math.PI;
  const WIDTH = 1400;
  const HEIGHT = 900;
  const MAX_MSG_COUNT = 50;

  let Application = PIXI.Application,
      loader = PIXI.loader,
      Container = PIXI.Container,
      resources = PIXI.loader.resources,
      TextureCache = PIXI.utils.TextureCache,
      InteractionManager = PIXI.interaction.InteractionManager,
      Sprite = PIXI.Sprite;

  function hitTestRectangle(r1, r2) {

    let hit, combinedHalfWidths, combinedHalfHeights, vx, vy;

    hit = false;

    r1.centerX = r1.x + r1.width / 2;
    r1.centerY = r1.y + r1.height / 2;
    r2.centerX = r2.x + r2.width / 2;
    r2.centerY = r2.y + r2.height / 2;

    r1.halfWidth = r1.width / 2;
    r1.halfHeight = r1.height / 2;
    r2.halfWidth = r2.width / 2;
    r2.halfHeight = r2.height / 2;

    vx = r1.centerX - r2.centerX;
    vy = r1.centerY - r2.centerY;

    combinedHalfWidths = r1.halfWidth + r2.halfWidth;
    combinedHalfHeights = r1.halfHeight + r2.halfHeight;

    if (Math.abs(vx) < combinedHalfWidths) {
      hit = Math.abs(vy) < combinedHalfHeights;
    } else {
      hit = false;
    }
    return hit;
  }

  class Lifecycle {
    destroy() {}
  }

  class Item extends Lifecycle {

    constructor() {
      super();
      this._status = new Set();
      this.spriteMap = {};
    }

    _removeStatus(status) {
      if (this._status.delete(status)) {
        this._setVisibleStatus(status, false);
      }
    }

    _setVisibleStatus(status, flag) {
      this.spriteMap[status].visible = flag;
    }

    /**
     * 删除某个状态
     * @param status 状态
     */
    deleteStatus(status) {
      this._removeStatus(status);
      this._setVisibleStatus(this.status, true);
    }

    get status() {
      let result = null;
      let priority;
      let STATUS_PRIORITY = this._getStatusPriority();
      this._status.forEach(e => {
        if (!priority || STATUS_PRIORITY[e] > priority) {
          result = e;
          priority = STATUS_PRIORITY[e];
        }
      });
      return result || this._getDefaultStatus();
    };

    /**
     * 设置已存在的状态
     * @param newStatus
     * @private
     */
    _setExistsStatus(newStatus) {

    }

    /**
     * 获得状态优先级
     * @private
     */
    _getStatusPriority() {

    }

    /**
     * 获得默认状态
     * @private
     */
    _getDefaultStatus() {

    }

    set status(newStatus) {
      if (this._status.has(newStatus)) {
        return;
      }
      this._setExistsStatus(newStatus);
      this._status.add(newStatus);
      let nowStatus = this.status;
      for (let status of this._status) {
        this._setVisibleStatus(status, status === nowStatus);
      }
    }

    nextAction() {}

  }

  class MouseListener extends Lifecycle {
    constructor(interactionManager, listeners = {}) {
      super();
      this.listeners = listeners;
      Object.keys(listeners).forEach(key => {
        let listener = listeners[key];
        interactionManager.addListener(key, listener);
      });
      this.interactionManager = interactionManager;

    }

    destroy() {
      Object.keys(this.listeners).forEach(key => {
        let listener = this.listeners[key];
        this.interactionManager.removeListener(key, listener);
      });
    }
  }

  class KeyBoardListener extends Lifecycle {
    constructor(keyCode, press, release) {
      super();
      this.code = keyCode;
      this.isDown = false;
      this.isUp = true;
      this.press = press;
      this.release = release;
      if (press) {
        window.addEventListener('keydown', this._downHandler.bind(this), false);
      }
      if (release) {
        window.addEventListener('keyup', this._upHandler.bind(this), false);
      }
    }

    _downHandler(event) {
      if (event.key === this.code) {
        if (this.isUp && this.press) this.press();
        this.isDown = true;
        this.isUp = false;
        event.preventDefault();
      }
    };

    _upHandler(event) {
      if (event.key === this.code) {
        if (this.isDown && this.release) this.release();
        this.isDown = false;
        this.isUp = true;
        event.preventDefault();
      }
    }

    destroy() {
      window.removeEventListener('keydown', this._downHandler);
      window.removeEventListener('keyup', this._upHandler);
    }
  }

  const DIRECTION = {
    UP: 270,
    RIGHT: 0,
    DOWN: 90,
    LEFT: 180,
  };

  class Zombie extends Item {
    static STATUS = {
      IDLE: 'idle', MOVE: 'move', ATTACK: 'attack',
    };

    /**
     * 任务状态优先级
     * @type {{}}
     */
    static STATUS_PRIORITY = {
      'idle': 0,
      'move': 1,
      'attack': 2,
    };

    static speed = 4;

    constructor(resourceMap, ctx) {
      super();
      this.container = this._initContainer(resourceMap);
      this.frameIndex = 0;
      this.targetPosition = {
        x: this.container.x + 1, y: this.container.y,
      };
      this.ctx = ctx;
      this.status = Zombie.STATUS.IDLE;
    }

    _getStatusPriority() {
      return Zombie.STATUS_PRIORITY;
    }

    _getDefaultStatus() {
      return Zombie.STATUS.IDLE;
    }

    _initContainer(resourceMap) {
      let parentContainer = new Container();
      let spriteMap = this.spriteMap;
      parentContainer.visible = true;
      Object.keys(resourceMap).forEach(function(key) {
        let resources = resourceMap[key];
        let childContainer = new Container();
        for (let i = 0; i < resources.length; i++) {
          let resource = resources[i];
          let sprite = new Sprite(TextureCache[resource]);
          sprite.anchor.x = 0.5;
          sprite.anchor.y = 0.5;
          sprite.visible = false;
          childContainer.addChild(sprite);
        }
        spriteMap[key] = childContainer;
        childContainer.visible = false;
        parentContainer.addChild(childContainer);
      });
      parentContainer.x = 800;
      parentContainer.y = 200;
      parentContainer.scale.set(0.5, 0.5);
      return parentContainer;
    }

    nextAction() {
      let status = this.status;
      let sprites = this.spriteMap[status].children;
      let length = sprites.length;
      let frameIndex = this.frameIndex++;
      if (frameIndex >= length) {
        frameIndex = this.frameIndex = 0;
      }
      for (let i = 0; i < length; i++) {
        let child = sprites[i];
        if (i === frameIndex) {
          child.visible = true;
        } else if (child.visible) {
          child.visible = false;
        }
      }
      // this.container.rotation = this.angle;

      if (this._status.has(Zombie.STATUS.MOVE)) {
        // todo
      }
    }
  }

  class Person extends Item {
    static STATUS = {
      IDLE: 'idle', MOVE: 'move', SHOOT: 'shoot', RELOAD: 'reload',
    };

    /**
     * 任务状态优先级
     * @type {{}}
     */
    static STATUS_PRIORITY = {
      'idle': 0,
      'move': 1,
      'shoot': 2,
      'reload': 3,
    };
    static SPEED = 5;

    /**
     * 构造Person对象
     * @param resourceMap 资源map, key为不同的资源名称，value为精灵数组
     * @param ctx vue上下文
     */
    constructor(resourceMap, ctx) {
      super();
      // 标识物体自身状态(PERSON_STATUS)，可能存在多个状态，优先级根据 PERSON_STATUS_PRIORITY 获取
      this._status = new Set();
      // 移动方位
      this._direction = [];
      this.spriteMap = {};
      this.container = this._initContainer(resourceMap.rifle);
      this.frameIndex = 0;
      this.targetPosition = {
        x: this.container.x + 1, y: this.container.y,
      };
      this.lastShootTime = Date.now();
      this.ctx = ctx;
      this.status = Person.STATUS.IDLE;
    }

    _setExistsStatus(newStatus) {
      if (this._status.size === 0) {
        this._setVisibleStatus(Person.STATUS.IDLE, false);
      }
      switch (newStatus) {
        case Person.STATUS.IDLE:
          this._removeStatus(Person.STATUS.MOVE);
          break;
        case Person.STATUS.MOVE:
          this._removeStatus(Person.STATUS.IDLE);
          break;
        case Person.STATUS.SHOOT:
          this._removeStatus(Person.STATUS.RELOAD);
          break;
        case Person.STATUS.RELOAD:
          this._removeStatus(Person.STATUS.SHOOT);
          break;
      }
    }

    _getStatusPriority() {
      return Person.STATUS_PRIORITY;
    }

    _getDefaultStatus() {
      return Person.STATUS.IDLE;
    }

    set direction(newVal) {
      this._direction.push(newVal);
      this.status = Person.STATUS.MOVE;
    }

    deleteDirection(val) {
      let index;
      while ((index = this._direction.indexOf(val)) !== -1) {
        this._direction.splice(index, 1);
      }
      if (this._direction.length === 0) {
        this.deleteStatus(Person.STATUS.MOVE);
      }
    }

    get direction() {
      return this._direction[this._direction.length - 1];
    }

    get x() {
      return this.container.x;
    }

    get y() {
      return this.container.y;
    }

    get angle() {
      let position = this.targetPosition;
      let y = position.y - this.y;
      let x = position.x - this.x;
      return Math.atan2(y, x);
    }

    registListener() {
      let that = this;

      function press(direction) {
        return () => {
          that.direction = direction;
        };
      }

      function release(direction) {
        return () => {
          that.deleteDirection(direction);
        };
      }

      let upListener = new KeyBoardListener('w', press(DIRECTION.UP), release(DIRECTION.UP));
      let downListener = new KeyBoardListener('s', press(DIRECTION.DOWN), release(DIRECTION.DOWN));
      let leftListener = new KeyBoardListener('a', press(DIRECTION.LEFT), release(DIRECTION.LEFT));
      let rightListener = new KeyBoardListener('d', press(DIRECTION.RIGHT), release(DIRECTION.RIGHT));

      let mouseListener = new MouseListener(this.ctx.interactionManager, {
        'pointerup': function() {
          that.deleteStatus(Person.STATUS.SHOOT);
        },
        'pointermove': function(e) {
          that.targetPosition = e.data.global;
        },
        'pointerdown': function() {
          that.status = Person.STATUS.SHOOT;
        },
      });

      this._listeners = Array.of(mouseListener, upListener, downListener, leftListener, rightListener);
    }

    destroy() {
      this._listeners && this._listeners.forEach(listener => listener.destroy && listener.destroy());
    }

    nextAction() {
      let status = this.status;
      let sprites = this.spriteMap[status].children;
      let length = sprites.length;
      let frameIndex = this.frameIndex++;
      if (frameIndex >= length) {
        frameIndex = this.frameIndex = 0;
      }
      for (let i = 0; i < length; i++) {
        let child = sprites[i];
        if (i === frameIndex) {
          child.visible = true;
        } else if (child.visible) {
          child.visible = false;
        }
      }
      this.container.rotation = this.angle;
      if (this._status.has(Person.STATUS.MOVE)) {
        switch (this.direction) {
          case DIRECTION.RIGHT:
            if (this.container.x >= WIDTH) { return; }
            this.container.x += Person.SPEED;
            break;
          case DIRECTION.DOWN:
            if (this.container.y >= HEIGHT) { return; }
            this.container.y += Person.SPEED;
            break;
          case DIRECTION.LEFT:
            if (this.container.x <= 0) { return; }
            this.container.x -= Person.SPEED;
            break;
          case DIRECTION.UP:
            if (this.container.y <= 0) { return; }
            this.container.y -= Person.SPEED;
            break;
        }
      }
      if (this._status.has(Person.STATUS.SHOOT)) {
        if (Date.now() - this.lastShootTime > 200) {
          this.ctx.shootBullet({
            angle: this.angle,
            x: this.container.x,
            y: this.container.y,
            type: 'purple',
          });
          this.lastShootTime = Date.now();
        }
      }
    }

    /**
     * 初始化 container
     * @param resourceMap
     * @returns {PIXI.Container}
     * @private
     */
    _initContainer(resourceMap) {
      let parentContainer = new Container();
      let spriteMap = this.spriteMap;
      parentContainer.visible = true;
      Object.keys(resourceMap).forEach(function(key) {
        let resources = resourceMap[key];
        let childContainer = new Container();
        for (let i = 0; i < resources.length; i++) {
          let resource = resources[i];
          let sprite = new Sprite(TextureCache[resource]);
          sprite.anchor.x = 0.5;
          sprite.anchor.y = 0.5;
          sprite.visible = false;
          childContainer.addChild(sprite);
        }
        spriteMap[key] = childContainer;
        childContainer.visible = false;
        parentContainer.addChild(childContainer);
      });
      parentContainer.x = 200;
      parentContainer.y = 200;
      parentContainer.scale.set(0.5, 0.5);
      return parentContainer;
    }
  }

  class Bullet extends Lifecycle {
    static SPEED = 25;
    static X_OFFSET = 30;
    static Y_OFFSET = 30;

    constructor(resourceMap, options, ctx) {
      super();
      this._angle = options.angle;
      this.container = this._initContainer(resourceMap, options);
      this.frameIndex = 0;
      this.ctx = ctx;
      this.container.rotation = this._angle;
    }

    _initContainer(resourceMap, options) {
      let container = new Container();
      let resources = resourceMap[options.type];
      container.visible = true;
      for (let i = 0; i < resources.length; i++) {
        let resource = resources[i];
        let sprite = new Sprite(TextureCache[resource]);
        sprite.visible = false;
        sprite.anchor.x = 0.5;
        sprite.anchor.y = 0.5;
        container.addChild(sprite);
      }
      let xOffset = Bullet.Y_OFFSET * Math.cos(this._angle) + Bullet.X_OFFSET * Math.sin(this._angle);
      let yOffset = Bullet.Y_OFFSET * Math.sin(this._angle) - Bullet.X_OFFSET * Math.cos(this._angle);
      container.x = options.x - xOffset;
      container.y = options.y - yOffset;
      return container;
    }

    nextAction() {
      let sprites = this.container.children;
      let length = sprites.length;
      let frameIndex = this.frameIndex++;
      if (frameIndex >= length) {
        frameIndex = this.frameIndex = 0;
      }
      for (let i = 0; i < length; i++) {
        let child = sprites[i];
        if (i === frameIndex) {
          child.visible = true;
        } else if (child.visible) {
          child.visible = false;
        }
      }
      let angle = this._angle;
      this.container.position.set(this.container.x += Bullet.SPEED * Math.cos(angle),
          this.container.y += Bullet.SPEED * Math.sin(angle));
      if (this.container.x >= WIDTH + 200
          || this.container.y >= HEIGHT + 200
          || this.container.x <= -200
          || this.container.y <= -200 || this._hitAnyThing()) {
        this.destroy();
      }
    }

    _hitAnyThing() {
      for (let item of this.ctx.items) {
        if (item instanceof Zombie && hitTestRectangle(this.container, item.container)) {
          return true;
        }
      }
      return false;
    }

    destroy() {
      this.ctx.app.stage.removeChild(this.container);
      this.ctx.items.delete(this);
      this.ctx = null;
    }
  }

  export default {
    name: 'NoOneSurvived',
    data() {
      let app = new Application({
            width: WIDTH,
            height: HEIGHT,
            antialias: true,
            transparent: false,
            backgroundColor: 0xffffff,
            resolution: 1,
          },
      );
      return {
        app: app,
        interactionManager: null,
        me: null,
        resourceMap: {},
        items: new Set(),
        eventList: [],
        eventResident: {},
        keyboardListeners: [],
        inputMsg: '',
        msgList: [],
        websocket: null,
      };
    },
    computed: {},
    watch: {
      msgList: function() {
        if (this.msgList.length > MAX_MSG_COUNT) {
          this.msgList.splice(0, this.msgList.length - MAX_MSG_COUNT);
        }
        let charThread = this.$refs.charThread;
        process.nextTick(function() {
          charThread.scrollTop = charThread.scrollHeight;
        });
      },
    },
    methods: {
      initWebsocket() {
        let websocket = this.websocket = new WebSocket(`ws://${window.location.host}/ws/games/nos`);
        let that = this;
        let msgProcessors = {
          '4': function(res) { // msg
            let data = res.data;
            if (Array.isArray(data)) {
              data.forEach(function(val) {
                that.msgList.push(val);
              });
            } else {
              that.msgList.push(data);
            }
          },
        };
        websocket.onmessage = function(event) {
          if (event.data[0] !== '{') {
            return;
          }
          let res = JSON.parse(event.data);
          let msgProcessor = msgProcessors[res.code.toString()];
          if (msgProcessor) {
            msgProcessor(res);
          }
        };
        websocket.onopen = function() {
          that.$message('连接成功');
        };
        websocket.onclose = function(error) {
          that.$message.error('服务器连接失败，请刷新页面');
          console.error(error);
        };
        websocket.onerror = function(err) {
          console.error('错误' + err);
        };
      },
      // 子弹设计 需要重构到武器类中
      shootBullet(options) {
        let bullet = new Bullet(this.resourceMap.bullet, options, this);
        resources[this.resourceMap.sound.rifle[0]].sound.play();
        this.app.stage.addChild(bullet.container);
        this.items.add(bullet);
      },
      sendText() {
        if (this.inputMsg) {
          this.websocket.send(JSON.stringify({code: 4, msg: this.inputMsg}));
          this.inputMsg = '';
        }
      },
      loadAllResource(resourcesMap) {
        let that = this;
        let allResources = this.flatObjectToArray(resourcesMap).filter(key => !resources[key]);
        loader.add(allResources).load(function() {
          that.resourceMap = resourcesMap;
          that.afterLoadResources();
        });
      },
      flatObjectToArray(obj) {
        if (obj instanceof Array) {
          return obj;
        }
        return Object.values(obj).map(o => this.flatObjectToArray(o)).flat();
      },
      afterLoadResources() {
        this.initWebsocket();
        this.me = new Person(this.resourceMap.person, this);
        this.me.registListener();
        this.app.stage.addChild(this.me.container);
        this.items.add(this.me);

        let zombie = new Zombie(this.resourceMap.zombie, this);
        this.app.stage.addChild(zombie.container);
        this.items.add(zombie);

        let items = this.items;
        this.eventResident['itemAction'] = function() {
          for (let item of items) {
            item.nextAction();
          }
        };
      },
    },
    created() {
    },
    mounted() {
      this.interactionManager = new InteractionManager(this.app.renderer);

      let that = this;

      function importAll(r) {
        let result = {};
        r.keys().forEach(key => {
          let value = r(key);
          let parts = key.split('/');
          let node = result;
          let lastIndex = parts.length - 2;
          for (let i = 1; i < lastIndex; i++) {
            if (!node[parts[i]]) { node[parts[i]] = {}; }
            node = node[parts[i]];
          }
          if (!node[parts[lastIndex]]) {
            node[parts[lastIndex]] = [];
          }
          node[parts[lastIndex]].push(value);
        });

        function comparator(left, right) {
          let leftNumber = resourcePattern.exec(left)[1];
          let rightNumber = resourcePattern.exec(right)[1];
          return Number(leftNumber) - Number(rightNumber);
        }

        let sort = function(obj) {
          Object.values(obj).forEach(value => {
            if (value instanceof Array) {
              value.sort(comparator);
            } else {
              sort(value);
            }
          });
        };
        sort(result);
        return result;
      }

      let resourcesMap = importAll(require.context('@/assets/no_one_survived/', true));
      this.loadAllResource(resourcesMap);
      document.getElementById('container').appendChild(this.app.view);

      function gameLoop() {
        while (that.eventList.length > 0) {
          that.eventList.shift()();
        }
        Object.values(that.eventResident).forEach(action => action());
      }

      this.app.ticker.add(delta => gameLoop(delta));
    },
    destroyed() {
      try {
        this.interactionManager && this.interactionManager.destroy();
        for (let item of this.items) {
          item.destroy && item.destroy();
        }
        this.app.destroy();
      } finally {
        this.websocket.close();
      }

    },
  };
</script>
<style lang="scss" scoped>
    .chat-thread {
        list-style: none;
        overflow-y: auto;
        overflow-x: hidden;
        padding-left: 0;
        height: 830px;
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
</style>