<template>
    <el-container id="container">

    </el-container>
</template>
<script>

  import * as PIXI from 'pixi.js';

  const resourcePattern = new RegExp('/.*?/.*?_(\\d+)\\.\\w*?\\.?\\w+?');
  const PI = 3.1415926;
  const WIDTH = 1400;
  const HEIGHT = 800;

  let Application = PIXI.Application,
      loader = PIXI.loader,
      Container = PIXI.Container,
      resources = PIXI.loader.resources,
      TextureCache = PIXI.utils.TextureCache,
      Sprite = PIXI.Sprite;

  class KeyBoardListener {
    constructor(keyCode) {
      this.code = keyCode;
      this.isDown = false;
      this.isUp = true;
      this.press = undefined;
      this.release = undefined;
      window.addEventListener('keydown', this.downHandler.bind(this), false);
      window.addEventListener('keyup', this.upHandler.bind(this), false);
    }

    downHandler(event) {
      if (event.key === this.code) {
        if (this.isUp && this.press) this.press();
        this.isDown = true;
        this.isUp = false;
        event.preventDefault();
      }
    };

    upHandler(event) {
      if (event.key === this.code) {
        if (this.isDown && this.release) this.release();
        this.isDown = false;
        this.isUp = true;
        event.preventDefault();
      }
    }

    destroy() {
      window.removeEventListener('keydown', this.downHandler);
      window.removeEventListener('keyup', this.upHandler);
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

    speed = 5;

    constructor(resourceMap, ctx) {
      this._status = new Set();
      this._status.add(PERSON_STATUS.IDLE);
      this._angle = 0;
      this.spriteMap = {};
      this.container = this._initContainer(resourceMap);
      this.frameIndex = 0;
      this.container.x = 200;
      this.container.y = 200;
      this.container.scale.set(0.5, 0.5);
      this.lastShootTime = Date.now();
      this.ctx = ctx;
    }

    __removeStatus(status) {
      if (this._status.has(status)) {
        this.spriteMap[status].forEach(e => e.visible && (e.visible = false));
        this._status.delete(status);
      }
    }

    deleteStatus(status) {
      this.__removeStatus(status);
    }

    set status(status) {
      switch (status) {
        case PERSON_STATUS.IDLE:
          this.__removeStatus(PERSON_STATUS.MOVE);
          break;
        case PERSON_STATUS.MOVE:
          this.__removeStatus(PERSON_STATUS.IDLE);
          break;
        case PERSON_STATUS.SHOOT:
          this.__removeStatus(PERSON_STATUS.RELOAD);
          break;
        case PERSON_STATUS.RELOAD:
          this.__removeStatus(PERSON_STATUS.SHOOT);
          break;
      }
      for (let status of this._status) {
        this.spriteMap[status].forEach(e => e.visible && (e.visible = false));
      }
      this._status.add(status);
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
      return result;
    };

    set angle(newVal) {
      this.container.rotation = newVal / 360 * 2 * PI;
      this._angle = newVal;
    }

    get angle() {
      return this._angle;
    }

    registKeyboardEvent() {
      let that = this;

      function press(angle) {
        return function() {
          that.angle = angle;
          that.status = PERSON_STATUS.MOVE;
        };
      }

      function release(angle) {
        return function() {
          if (that.angle === angle) {
            that.status = PERSON_STATUS.IDLE;
          }
        };
      }

      let upListener = new KeyBoardListener('w');
      upListener.press = press(270);
      upListener.release = release(270);
      let downListener = new KeyBoardListener('s');
      downListener.press = press(90);
      downListener.release = release(90);
      let leftListener = new KeyBoardListener('a');
      leftListener.press = press(180);
      leftListener.release = release(180);
      let rightListener = new KeyBoardListener('d');
      rightListener.press = press(0);
      rightListener.release = release(0);
      let shootListener = new KeyBoardListener(' ');
      shootListener.press = () => {
        that.status = PERSON_STATUS.SHOOT;
      };
      shootListener.release = () => {
        that.deleteStatus(PERSON_STATUS.SHOOT);
      };
      this._listeners = Array.of(upListener, downListener, leftListener, rightListener, shootListener);
    }

    destroy() {
      this._listeners && this._listeners.forEach(listener => listener.destroy && listener.destroy());
    }

    nextAction() {
      let status = this.status;
      let sprites = this.spriteMap[status];
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
      if (this._status.has(PERSON_STATUS.MOVE)) {
        switch (this.angle) {
          case 0:
            if (this.container.x >= WIDTH) { return; }
            this.container.x += this.speed;
            break;
          case 90:
            if (this.container.y >= HEIGHT) { return; }
            this.container.y += this.speed;
            break;
          case 180:
            if (this.container.x <= 0) { return; }
            this.container.x -= this.speed;
            break;
          case 270:
            if (this.container.y <= 0) { return; }
            this.container.y -= this.speed;
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

    _initContainer(resourceMap) {
      let container = new Container();
      let spriteMap = this.spriteMap;
      container.visible = true;
      Object.keys(resourceMap).forEach(function(key) {
        let resources = resourceMap[key];
        resources.sort(function(left, right) {
          let leftNumber = resourcePattern.exec(left)[1];
          let rightNumber = resourcePattern.exec(right)[1];
          return Number(leftNumber) - Number(rightNumber);
        });
        let arr = [];
        for (let i = 0; i < resources.length; i++) {
          let resource = resources[i];
          let sprite = new Sprite(TextureCache[resource]);
          sprite.anchor.x = 0.5;
          sprite.anchor.y = 0.5;
          sprite.visible = false;
          container.addChild(sprite);
          arr.push(sprite);
        }
        spriteMap[key] = arr;
      });
      return container;
    }
  }

  const BULLET_STATUS = {
    IDLE: 'idle', MOVE: 'move',
  };

  class Bullet {
    speed = 25;
    xoffset = 30;
    yoffset = 30;

    constructor(resourceMap, options, ctx) {
      this._angle = options.angle;
      this._status = BULLET_STATUS.MOVE;
      this.container = this._initContainer(resourceMap, options);
      this.frameIndex = 0;
      this.container.x = options.x;
      this.container.y = options.y;
      switch (this._angle) {
        case 0:
          this.container.x += this.xoffset;
          this.container.y += this.yoffset;
          break;
        case 90:
          this.container.x -= this.xoffset;
          this.container.y += this.yoffset;
          break;
        case 180:
          this.container.x -= this.xoffset;
          this.container.y -= this.yoffset;
          break;
        case 270:
          this.container.x += this.xoffset;
          this.container.y -= this.yoffset;
          break;
      }
      this.ctx = ctx;
      this.container.rotation = options.angle / 360 * 2 * PI;
    }

    _initContainer(resourceMap, options) {
      let container = new Container();
      let resources = resourceMap[options.type];
      container.visible = true;
      resources.sort(function(left, right) {
        let leftNumber = resourcePattern.exec(left)[1];
        let rightNumber = resourcePattern.exec(right)[1];
        return Number(leftNumber) - Number(rightNumber);
      });
      for (let i = 0; i < resources.length; i++) {
        let resource = resources[i];
        let sprite = new Sprite(TextureCache[resource]);
        sprite.visible = false;
        sprite.anchor.x = 0.5;
        sprite.anchor.y = 0.5;
        container.addChild(sprite);
      }
      return container;
    }

    nextAction() {
      if (this._status !== BULLET_STATUS.MOVE) {
        this.ctx.app.stage.removeChild(this.container);
        return;
      }
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
      switch (this._angle) {
        case 0:
          if (this.container.x >= WIDTH + 200) {
            this.destroy();
            return;
          }
          this.container.x += this.speed;
          break;
        case 90:
          if (this.container.y >= HEIGHT + 200) {
            this.destroy();
            return;
          }
          this.container.y += this.speed;
          break;
        case 180:
          if (this.container.x <= -200) {
            this.destroy();
            return;
          }
          this.container.x -= this.speed;
          break;
        case 270:
          if (this.container.y <= -200) {
            this.destroy();
            return;
          }
          this.container.y -= this.speed;
          break;
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
      return {
        app: new Application({
              width: WIDTH,
              height: HEIGHT,
              antialias: true,
              transparent: false,
              backgroundColor: 0xffffff,
              resolution: 1,
            },
        ),
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
      shootBullet(options) {
        let bullet = new Bullet(this.resourceMap.bullet, options, this);
        this.app.stage.addChild(bullet.container);
        this.items.add(bullet);
      },
      loadAllResource(resourcesMap) {
        let that = this;
        let allResources = Object.values(resourcesMap).
            map(spriteResourceMap => Object.values(spriteResourceMap)).flat(Infinity).filter(key => !resources[key]);
        loader.add(allResources).load(function() {
          that.resourceMap = resourcesMap;
          that.afterLoadResources();
        });
      },
      afterLoadResources() {
        let p = new Person(this.resourceMap.person, this);
        p.registKeyboardEvent();
        this.app.stage.addChild(p.container);
        this.items.add(p);

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
      let that = this;

      function importAll(r) {
        return r.keys().map(key => r(key));
      }

      let moveResources = importAll(require.context('@/assets/no_one_survived/rifle/move', true));
      let idleResources = importAll(require.context('@/assets/no_one_survived/rifle/idle', true));
      let reloadResources = importAll(require.context('@/assets/no_one_survived/rifle/reload', true));
      let shootResources = importAll(require.context('@/assets/no_one_survived/rifle/shoot', true));
      let bulletPurpleResources = importAll(require.context('@/assets/no_one_survived/bullet/purple', true));
      this.loadAllResource({
        person: {
          move: moveResources,
          idle: idleResources,
          reload: reloadResources,
          shoot: shootResources,
        },
        bullet: {
          purple: bulletPurpleResources,
        },
      });
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
      for (let item of this.items) {
        item.destroy && item.destroy();
      }
      this.app.destroy();
    },
  };
</script>
<style lang="scss" scoped>

</style>