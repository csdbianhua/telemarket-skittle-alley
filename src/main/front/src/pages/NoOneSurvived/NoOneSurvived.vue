<template>
    <el-container id="container">

    </el-container>
</template>
<script>

  import * as PIXI from 'pixi.js';
  import 'pixi-sound';

  PIXI.sound.volumeAll = 0.05;

  const resourcePattern = new RegExp('/.*?/.*?_(\\d+)\\.\\w*?\\.?\\w+?');
  const PI = Math.PI;
  const WIDTH = 1400;
  const HEIGHT = 800;

  let Application = PIXI.Application,
      loader = PIXI.loader,
      Container = PIXI.Container,
      resources = PIXI.loader.resources,
      TextureCache = PIXI.utils.TextureCache,
      InteractionManager = PIXI.interaction.InteractionManager,
      Sprite = PIXI.Sprite;

  class Lifecycle {
    destroy() {}
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

  const ZOMBIE_STATUS = {
    IDLE: 'idle', MOVE: 'move', ATTACK: 'attack',
  };

  /**
   * 任务状态优先级
   * @type {{}}
   */
  const ZOMBIE_STATUS_PRIORITY = {
    'idle': 0,
    'move': 1,
    'attack': 2,
  };

  class Zombie {
    static speed = 4;

    constructor(resourceMap, ctx) {
      // 标识物体自身状态(ZOMBIE_STATUS)，可能存在多个状态，优先级根据 ZOMBIE_STATUS_PRIORITY 获取
      this._status = new Set();
      // 移动方位
      this.spriteMap = {};
      this.container = this._initContainer(resourceMap);
      this.frameIndex = 0;
      this.targetPosition = {
        x: this.container.x + 1, y: this.container.y,
      };
      this.ctx = ctx;
      this.status = ZOMBIE_STATUS.IDLE;
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
  }

  const PERSON_STATUS = {
    IDLE: 'idle', MOVE: 'move', SHOOT: 'shoot', RELOAD: 'reload',
  };

  /**
   * 任务状态优先级
   * @type {{}}
   */
  const PERSON_STATUS_PRIORITY = {
    'idle': 0,
    'move': 1,
    'shoot': 2,
    'reload': 3,
  };

  class Person {

    static SPEED = 5;

    /**
     * 构造Person对象
     * @param resourceMap 资源map, key为不同的资源名称，value为精灵数组
     * @param ctx vue上下文
     */
    constructor(resourceMap, ctx) {
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
      this.status = PERSON_STATUS.IDLE;
    }

    _removeStatus(status) {
      if (this._status.delete(status)) {
        this._unVisibleStatus(status);
      }
    }

    _unVisibleStatus(status) {
      this.spriteMap[status].visible = false;
    }

    /**
     * 删除某个状态
     * @param status 状态
     */
    deleteStatus(status) {
      this._removeStatus(status);
      this.spriteMap[this.status].visible = true;
    }

    set status(newStatus) {
      if (this._status.has(newStatus)) {
        return;
      }
      if (this._status.size === 0) {
        this._unVisibleStatus(PERSON_STATUS.IDLE);
      }
      switch (newStatus) {
        case PERSON_STATUS.IDLE:
          this._removeStatus(PERSON_STATUS.MOVE);
          break;
        case PERSON_STATUS.MOVE:
          this._removeStatus(PERSON_STATUS.IDLE);
          break;
        case PERSON_STATUS.SHOOT:
          this._removeStatus(PERSON_STATUS.RELOAD);
          break;
        case PERSON_STATUS.RELOAD:
          this._removeStatus(PERSON_STATUS.SHOOT);
          break;
      }

      for (let status of this._status) {
        this._unVisibleStatus(status);
      }
      this._status.add(newStatus);
      this.spriteMap[this.status].visible = true;
    }

    get status() {
      let result = null;
      let priority;
      this._status.forEach(e => {
        if (!priority || PERSON_STATUS_PRIORITY[e] > priority) {
          result = e;
          priority = PERSON_STATUS_PRIORITY[e];
        }
      });
      return result || PERSON_STATUS.IDLE;
    };

    set direction(newVal) {
      this._direction.push(newVal);
      this.status = PERSON_STATUS.MOVE;
    }

    deleteDirection(val) {
      let index;
      while ((index = this._direction.indexOf(val)) !== -1) {
        this._direction.splice(index, 1);
      }
      if (this._direction.length === 0) {
        this.deleteStatus(PERSON_STATUS.MOVE);
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
          that.deleteStatus(PERSON_STATUS.SHOOT);
        },
        'pointermove': function(e) {
          that.targetPosition = e.data.global;
        },
        'pointerdown': function() {
          that.status = PERSON_STATUS.SHOOT;
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
      if (this._status.has(PERSON_STATUS.MOVE)) {
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
      if (this._status.has(PERSON_STATUS.SHOOT)) {
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
          || this.container.y <= -200) {
        this.destroy();
      }
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
      };
    },
    computed: {},
    watch: {},
    methods: {
      // 子弹设计 需要重构到武器类中
      shootBullet(options) {
        let bullet = new Bullet(this.resourceMap.bullet, options, this);
        resources[this.resourceMap.sound.rifle[0]].sound.play();
        this.app.stage.addChild(bullet.container);
        this.items.add(bullet);
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
        this.me = new Person(this.resourceMap.person, this);
        this.me.registListener();
        this.app.stage.addChild(this.me.container);
        this.items.add(this.me);

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
      this.interactionManager && this.interactionManager.destroy();
      for (let item of this.items) {
        item.destroy && item.destroy();
      }
      this.app.destroy();
    },
  };
</script>
<style lang="scss" scoped>

</style>