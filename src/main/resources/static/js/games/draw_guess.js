$(function () {
    var mousePressed = false;
    var lastX, lastY;
    var ctxCanvas;
    var ctxGame = {};
    var chat_thread = $('.chat-thread');
    var user_thread = $('.user-thread');
    var info = {};
    var msg_count = 1;
    const MAX_MSG_COUNT = 50;
    var time_offset = 0;

    var msg_processors = {
        "1": function (res) { // move
            var data = res.data;
            Draw(data.x, data.y, data.isDown);
        },
        "2": function () { // clear
            clearArea();
        },
        "3": function (res) { // clear
            var data = res.data;
            switch (data.type) {
                case 'color':
                    $('#selColor').val(data.value);
                    break;
                case 'width':
                    $('#selWidth').val(data.value);
                    break;
                default:
                    break;
            }
        },
        "4": function (res) { // msg
            var data = res.data;
            var count = 1;
            if (Array.isArray(data)) {
                data.forEach(function (val) {
                    chat_thread.append('<li>' + val + '</li>');
                });
                count = data.length;
            } else {
                chat_thread.append('<li>' + data + '</li>');
            }
            scrollToBottom(count);
        },
        "10": function (res) { // join
            var data = res.data;
            if (data.assign) {
                info = data.info;
                initCtx(data);
            } else {
                addUser(data.info);
            }
            scrollToBottom();
        },
        "11": function (res) { // ready
            var data = res.data;
            readyUser(data);
        },
        "12": function (res) { // leave
            removeUser(res.data);
            scrollToBottom();
        },
        "13": function (res) { // change name
            changeUser(res.data);
            scrollToBottom();
        },
        "20": function (res) { // update ctx
            updateCtx(res.data);
        }
    };

    var websocket = new SockJS("/ws/games/draw_guess");

    websocket.onmessage = function (event) {
        var res = JSON.parse(event.data);
        msg_processors[res.code.toString()](res);
    };
    websocket.onopen = function () {
        $headText.html(DEFAULT_HEAD);
    };
    websocket.onclose = function () {
        $.alert("服务器连接失败，请刷新页面");
        $headText.html("服务器连接失败，请刷新页面")
    };

    initCanvas();

    function initCanvas() {
        ctxCanvas = document.getElementById('myCanvas').getContext("2d");
        var $myCanvas = $('#myCanvas');
        $myCanvas.mousedown(function (e) {
            mousePressed = true;
            sendMoveSig(e.pageX - $(this).offset().left, e.pageY - $(this).offset().top, false);
        });

        $myCanvas.mousemove(function (e) {
            if (mousePressed) {
                sendMoveSig(e.pageX - $(this).offset().left, e.pageY - $(this).offset().top, true);
            }
        });

        $myCanvas.mouseup(function (e) {
            mousePressed = false;
        });
        $myCanvas.mouseleave(function (e) {
            mousePressed = false;
        });
    }

    function initCtx(data) {
        ctxGame = data.ctx;
        $selColor.val(ctxGame.color);
        $selWidth.val(ctxGame.width);
        data.players.forEach(function (val) {
            addUser(val);
            readyUser(val);
        });
        if (ctxGame.status !== GAME_READY) {
            disableUserBtn(true);
            updateCtx(ctxGame);
        }
        if (data.timestamp) {
            time_offset = data.timestamp - new Date().getTime();
            console.log("初始化服务器时间差" + time_offset + "ms");
        }
    }

    function sendMoveSig(x, y, isDown) {
        if (isCurrentUser()) {
            websocket.send(JSON.stringify({code: 1, msg: {x: x, y: y, isDown: isDown}}))
        }
    }

    function sendClearSig() {
        if (isCurrentUser()) {
            websocket.send(JSON.stringify({code: 2}))
        }
    }

    function sendText(msg) {
        websocket.send(JSON.stringify({code: 4, msg: msg}))
    }

    function sendChangeBrush(type, value) {
        if (isCurrentUser()) {
            websocket.send(JSON.stringify({code: 3, msg: {type: type, value: value}}))
        }
    }

    function getServerTime() {
        return new Date().getTime() + time_offset;
    }

    function isCurrentUser() {
        return info.id === ctxGame.currentUser;
    }

    function Draw(x, y, isDown) {
        if (isDown) {
            ctxCanvas.beginPath();
            ctxCanvas.strokeStyle = $('#selColor').val();
            ctxCanvas.lineWidth = $('#selWidth').val();
            ctxCanvas.lineJoin = "round";
            ctxCanvas.moveTo(lastX || x, lastY || y);
            ctxCanvas.lineTo(x, y);
            ctxCanvas.closePath();
            ctxCanvas.stroke();
        }
        lastX = x;
        lastY = y;
    }

    function clearArea() {
        // Use the identity matrix while clearing the canvas
        ctxCanvas.setTransform(1, 0, 0, 1, 0, 0);
        ctxCanvas.clearRect(0, 0, ctxCanvas.canvas.width, ctxCanvas.canvas.height);
    }

    function scrollToBottom(count) {
        chat_thread.animate({
            scrollTop: chat_thread.prop("scrollHeight") - chat_thread.height()
        }, 300);
        if (typeof count === 'undefined') {
            count = 1;
        }
        if (msg_count > MAX_MSG_COUNT) {
            for (var i = 0; i < count; i++) {
                $('.chat-thread>:first-child').remove();
            }
        } else {
            msg_count += count;
        }
    }

    var $selColor = $('#selColor');
    var $selWidth = $('#selWidth');
    var $clearBtn = $('#clear_btn');
    $selColor.on('change', function () {
        sendChangeBrush('color', $(this).val());
    });
    $selWidth.on('change', function () {
        sendChangeBrush('width', $(this).val());
    });
    $clearBtn.on('click', function () {
        sendClearSig();
    });

    var $sendBtn = $('#send_btn');
    var $sendText = $('#send_text');
    $sendText.on('keydown', function (e) {
        if (e.keyCode === 13) {
            $sendBtn.trigger('click');
        }
    });
    $sendBtn.on('click', function (e) {
        e.preventDefault();
        var val = $sendText.val();
        if (val !== '') {
            sendText(val);
            $sendText.val('');
        }
    });

    var $nameBtn = $('#name_btn');
    var $nameText = $('#name_text');
    var $readyBtn = $('#ready_btn');

    $nameBtn.on('click', function (e) {
        e.preventDefault();
        var newName = $nameText.val();
        if (newName !== '' && newName.length > 1 && newName.length < 10) {
            sendNewName(newName);
            $nameText.val('');
        } else {
            $.alert("名字不可小于1个字符大于10个字符");
        }
    });

    $readyBtn.on('click', function (e) {
        e.preventDefault();
        var $btn = $(this);
        $btn.attr("disabled", true);
        sendReady();
        setTimeout(function () {
            if ($nameBtn.prop('disabled')) { // 防止已开始
                return;
            }
            $btn.attr("disabled", false);
        }, 3000);
    });

    function sendReady() {
        websocket.send(JSON.stringify({code: 11, msg: {id: info.id, status: !(info.status > 0)}}));
    }

    function sendNewName(newName) {
        websocket.send(JSON.stringify({code: 13, msg: newName}));
    }

    function removeUser(user_info) {
        var item = $('#' + user_info.id);
        if (item.length > 0) {
            item.remove();
            chat_thread.append('<li><b>' + user_info.name + '</b> 离开了房间。</li>');
        }
    }

    function addUser(user_info) {
        if ($('#' + user_info.id).length > 0) {
            console.error("警告:重复添加用户" + user_info.id);
            return;
        }
        var element = '<li id="' + user_info.id + '"';
        if (info.id === user_info.id) {
            chat_thread.append('<li><b>你</b> 进入了房间。名称为:' + user_info.name + '</li>');
            element += ' style="color: #5cb85c"';
        } else {
            chat_thread.append('<li><b>' + user_info.name + '</b> 进入了房间。</li>');
        }
        element += '><span class="user_name">' + user_info.name + '</span><span class="user_score"></span></li>';
        user_thread.append(element);
    }

    function changeUser(user_info) {
        var item = $('#' + user_info.id);
        if (item.length <= 0) {
            addUser(user_info);
        }
        var ori = item.text();
        chat_thread.append('<li><b>' + ori + '</b> 更名为 <b>' + user_info.name + '</b>。</li>');
        item.find('.user_name').text(user_info.name);
    }

    function updateUserScore(user_info) {
        var item = $('#' + user_info.id);
        if (item.length <= 0) {
            addUser(user_info);
        }
        item.find('.user_score').text('[' + user_info.score + ']');
    }

    function readyUser(ready_info) {
        var element = $('#' + ready_info.id);
        var ready = ready_info.status > 0;
        if (ready) {
            element.append('<span class="glyphicon glyphicon-ok"></span>')
        } else {
            element.children('.glyphicon').remove();
        }
        if (ready_info.id === info.id) {
            info.status = ready_info.status;
            if (ready) {
                $readyBtn.attr("class", "btn btn-info user_btn");
                $readyBtn.text("不准!");
            } else {
                $readyBtn.attr("class", "btn btn-danger user_btn");
                $readyBtn.text("准备!");
            }
        }
    }

    var $headText = $("#head_text");
    var $leftText = $("#left_text");
    var $rightText = $("#right_text");
    const DEFAULT_HEAD = "你画我猜";
    const DEFAULT_TEXT = "";

    const GAME_READY = 0;
    const GAME_RUN = 1;
    const GAME_WAIT = 2;
    const GAME_END = 3;
    var game_status_handler = {};
    game_status_handler[GAME_READY] = readyGame;
    game_status_handler[GAME_RUN] = runGame;
    game_status_handler[GAME_WAIT] = waitGame;
    game_status_handler[GAME_END] = endGame;

    function updateCtx(new_ctx) {
        ctxGame = new_ctx;
        game_status_handler[ctxGame.status](ctxGame);
    }

    function readyGame(ctx) {
        disableDraw(true);
        disableUserBtn(false);
        stopClockAndTimeout();
        clearArea();
        user_thread.find('.glyphicon').remove();
        user_thread.find('.user_score').html('');
        info.status = 0;
        readyUser(info); // 改变按钮状态
        $headText.text(DEFAULT_HEAD);
        $leftText.text(DEFAULT_TEXT);
        $rightText.text(DEFAULT_TEXT);
    }

    function runGame(ctx) {
        clearArea();
        disableUserBtn(true);
        startClock(ctx);
        if (isCurrentUser()) {
            disableDraw(false);
            $headText.text("请作画:" + ctx.currentWord.word);
        } else {
            disableDraw(true);
            $headText.text("快猜!");
            startTips();
        }
        $leftText.text("当前画师:" + $('#' + ctx.currentUser).find('.user_name').text());
    }

    function waitGame(ctx) {
        if (isCurrentUser()) {
            disableDraw(true);
        }
        $headText.html("答案是" + ctx.currentWord.word + "。共有<b>" + ctx.rightNumber + "</b>人猜对");
        ctx.players.forEach(function (val) {
            updateUserScore(val);
        });
        startClock(ctx);
    }

    function endGame(ctx) {
        disableDraw(true);
        $headText.html("最终得分");
        ctx.players.forEach(function (val) {
            updateUserScore(val);
        });
        startClock(ctx);
    }

    function startTips() {
        timeout_id.push(setTimeout(countTimeout, 30000)); // TODO 可变时间
        timeout_id.push(setTimeout(typeTimeout, 10000)); // TODO 可变时间

    }

    function countTimeout() {
        var ori = $headText.html();
        $headText.html(ori + ctxGame.currentWord.wordCount + "个字！还猜不出来???")
    }

    function typeTimeout() {
        var ori = $headText.html();
        $headText.html(ori + ctxGame.currentWord.wordType + "!")
    }

    function startClock(ctx) {
        stopClockAndTimeout();
        $rightText.html('<span id="minus"></span>分<span id="seconds"></span>秒');
        countdown(ctx.endTime);
        clock_id.push(setInterval(countdown, 500, ctx.endTime));
    }

    var clock_id = [];
    var timeout_id = [];

    function stopClockAndTimeout() {
        if (clock_id.length > 0) {
            clock_id.forEach(function (val) {
                clearInterval(val);
            });
            clock_id = [];
        }
        if (timeout_id.length > 0) {
            timeout_id.forEach(function (val) {
                clearTimeout(val);
            });
            timeout_id = [];
        }
    }

    function disableDraw(flag) {
        $selColor.attr('disabled', flag);
        $selWidth.attr('disabled', flag);
        $clearBtn.attr('disabled', flag);
    }

    function disableUserBtn(flag) {
        $('.user_btn').attr('disabled', flag);
    }

    function countdown(time) {
        var $minus = $('#minus');
        var $seconds = $('#seconds');
        var djs = time;
        var b = getServerTime();
        var cc = djs - b;
        if (cc <= 0) {
            $minus.html(0);
            $seconds.html(0);
            return stopClockAndTimeout();
        }
        cc /= 1000;
        var m = Math.floor(cc / 60);
        var s = Math.floor(cc - m * 60);
        $minus.html(m);
        $seconds.html(s);
    }

});