<template>
    <el-container id="container">

    </el-container>
</template>
<script>

  import * as PIXI from 'pixi.js';

  const resourcePattern = new RegExp('/.*?/.*?_(\\d+)\\.\\w*\\.?\\w+?');
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
      }
      event.preventDefault();
    };

    upHandler(event) {
      if (event.key === this.code) {
        if (this.isDown && this.release) this.release();
        this.isDown = false;
        this.isUp = true;
      }
      event.preventDefault();
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
    'move': 0,
    'shoot': 0,
    'reload': 0,
  };

  class Person {

    constructor(resourceMap) {
      this._status = new Set();
      this._status.add(PERSON_STATUS.IDLE);
      this._angle = 0;
      this.spriteMap = {};
      this.container = this._initContainer(resourceMap);
      this.frameIndex = 0;
      this.container.x = 200;
      this.container.y = 200;
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
            this.container.x += 5;
            break;
          case 90:
            if (this.container.y >= HEIGHT) { return; }
            this.container.y += 5;
            break;
          case 180:
            if (this.container.x <= 0) { return; }
            this.container.x -= 5;
            break;
          case 270:
            if (this.container.y <= 0) { return; }
            this.container.y -= 5;
            break;
        }
      }
    }

    _initContainer(resourceMap) {
      let container = new Container();
      let spriteMap = this.spriteMap;
      container.visible = true;
      Object.getOwnPropertyNames(resourceMap).forEach(function(key) {
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
        items: [],
        eventList: [],
        eventResident: {},
        keyboardListeners: [],
      };
    },
    computed: {},
    watch: {},
    methods: {
      loadAllResource(resourcesMap) {
        let that = this;

        let allResources = Object.values(resourcesMap).
            flat().
            filter(key => !resources[key]);
        loader.add(allResources).load(function() {
          that.afterLoadResources(resourcesMap);
        });
      },
      afterLoadResources(resourceMap) {
        let p = new Person(resourceMap);
        p.registKeyboardEvent();
        this.app.stage.addChild(p.container);
        let items = this.items;
        items.push(p);
        this.eventResident['personAction'] = function() {
          items.forEach(item => item.nextAction());
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
      this.loadAllResource({
        move: moveResources,
        idle: idleResources,
        reload: reloadResources,
        shoot: shootResources,
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
      this.app.destroy();
      this.items.forEach(item => item.destroy && item.destroy());
    },
  };
</script>
<style lang="scss" scoped>

</style>