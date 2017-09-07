$(function () {

        // 获取通知权限
        Notification.requestPermission(function (status) {
            if (Notification.permission !== status) {
                Notification.permission = status;
            }
        });


        var $submitTask = $("#submitTask");
        var $taskForm = $('#taskForm');
        $submitTask.on("click", function (e) {
            e.preventDefault();
            $submitTask.attr("disabled", true);
            $.ajax({
                type: "POST",
                url: "/rest/monitor/tasks",
                data: $taskForm.serialize(),
                async: true,
                error: function () {
                    $.alert("提交失败,网络异常");
                    $submitTask.attr("disabled", false);
                },
                success: function (data) {
                    if (data.code === 0) {
                        $.alert("提交成功");
                        $taskForm.find("input").val('');
                        $taskForm.find('input[name=language]').val('English');
                    } else if (data.code === -2) {
                        $.alert("未设置监控邮件账号", '<a href="/other/config" target="_blank">点此设置</a>');
                    } else {
                        $.alert(data.data);
                    }
                    $submitTask.attr("disabled", false);
                }
            });
        });

        var $running_task = $('#task-running-list');
        var $completed_task = $('#task-completed-list');
        var tasks_map = {};

        var websocket = new SockJS("/ws/tasks");
        websocket.onmessage = function (event) {
            var res = JSON.parse(event.data);
            res.timeStamp = event.timeStamp;
            msg_processors[res.code.toString()](res);
        };
        websocket.onopen = function () {
            websocket.send(JSON.stringify({code: 0}));
        };
        websocket.onclose = function () {
            $.alert("服务器连接失败，请刷新页面");
        };

        function compare_update_time(task, update_time) {
            var ori_task = tasks_map[task.id];
            if (!ori_task) {
                return true;
            }
            var task_time = ori_task.update_time;
            return !task_time || task_time < update_time;
        }

        function format_task(task) {
            var html = ['<div class="list-group-item" id="', task.id, '">',
                '<h2>', task.taskName, '</h2>',
                '<p><b>上一次检查时间:</b>', (task.lastWatch === task.createAt) ? '暂无' : new Date(task.lastWatch).toLocaleString(), '</p>',
                '<p><b>创建时间:</b>', new Date(task.createAt).toLocaleString(), '</p>',
                '<p><b>字幕下载页:</b>', task.result ? ('<a href="' + task.result + '">' + task.result + '</a>') : '暂无', '</p>',
                '<p><b>关键字:</b>', task.keywords, '</p>',
                '<p><b>监控邮件收件人:</b>', task.emails, '</p>',
                '<p><b>语言:</b>', task.language, '</p>',
                '<p><b>状态:</b>',
                (task.done === 1) ? '<span style="color: red;">已完成</span>' : (task.process === 1 ? '<span style="color: blue;">正在检查</span>' : '等待下一次检查'),
                '</p>',
                '<button class="btn btn-warning delete-btn" style="margin-right: 20px;">删除任务</button>',
                '<button class="btn btn-info copy-btn">复制任务</button>',
                '</div>'];
            return html.join('');
        }

        var $list = $('.list-group');
        $list.on('click', '.copy-btn', function (e) {
            e.preventDefault();
            var task_id = $(this).parent('.list-group-item').attr('id');
            var task = tasks_map[task_id];
            $taskForm.find('input[name=language]').val(task.language);
            $taskForm.find('input[name=taskName]').val(task.taskName);
            $taskForm.find('input[name=keyWords]').val(task.keywords);
            $taskForm.find('input[name=emailAddrs]').val(task.emails);
        });

        $list.on('click', '.delete-btn', function (e) {
            e.preventDefault();
            var task_id = $(this).parent('.list-group-item').attr('id');
            $.ajax({
                method: "DELETE",
                url: "/rest/monitor/tasks/" + task_id,
                async: true,
                error: function () {
                    $.alert("网络异常，删除失败");
                }
            });
        });

        var msg_processors = {
            "0": function (res) { // 新进行中任务
                var update_time = res.timeStamp;
                for (var i = 0, l = res.data.length; i < l; i++) {
                    var e = res.data[i];
                    $running_task.prepend(format_task(e));
                    e.update_time = update_time;
                    tasks_map[e.id] = e;
                }
            },
            "1": function (res) { // 更新属性
                var update_time = res.timeStamp;
                for (var i = 0, l = res.data.length; i < l; i++) {
                    var e = res.data[i];
                    if (!compare_update_time(e, update_time)) {
                        continue;
                    }
                    var $element = $('#' + e.id);
                    var old_done = parseInt((tasks_map[e.id] || {}).done) === 1;
                    var new_done = parseInt(e.done) === 1;
                    if ($element.length > 0 && new_done === old_done) {
                        $element.replaceWith(format_task(e));
                    } else if (new_done) {
                        $element.remove();
                        $completed_task.prepend(format_task(e));
                    } else {
                        $running_task.prepend(format_task(e));
                    }
                    if (new_done && !old_done && window.Notification && Notification.permission !== "denied") {
                        Notification.requestPermission(function () {
                            var n = new Notification('出字幕啦!', {body: '任务名:' + e.taskName});
                            n.onshow = function () {
                                setTimeout(n.close.bind(n), 5000);
                            }
                        });
                    }
                    e.update_time = update_time;
                    tasks_map[e.id] = e;
                }
            },
            "2": function (res) { // 删除
                var update_time = res.timeStamp;
                for (var i = 0, l = res.data.length; i < l; i++) {
                    var e = res.data[i];
                    $('#' + e.id).remove();
                    tasks_map[e.id] = {id: e.id, update_time: update_time};//防止删除后触发更新属性，保留原更新时间
                }
            },
            "3": function (res) { // 页面全部数据
                var tmp = {};
                var tasks = res.data;
                var update_time = res.timeStamp;
                $.each(tasks, function (i, task) {
                    if (!task.done) {
                        $running_task.append(format_task(task));
                    } else {
                        $completed_task.append(format_task(task));
                    }
                    task.update_time = update_time;
                    tmp[task.id] = task;
                });
                tasks_map = tmp;
            }
        };

    }
);