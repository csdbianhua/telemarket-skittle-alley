<!DOCTYPE html>
<html>
<head>
<#include "../common/common_head.ftl">
    <title>配置</title>
    <style>
        .content {
            width: 35em;
            margin: 0 auto;
            font-family: Tahoma, Verdana, Arial, sans-serif;
        }
    </style>
</head>
<body>
<#assign name="other"/>
<#include "../common/navbar.ftl"/>
<div class="content">
    <h1>监控邮件账号配置</h1>
    <br/>
    <div class="alert alert-warning">
        1.仅支持安全的SMTP协议,在查看所使用邮箱的SMTP设置帮助时,请找到安全连接的端口<br>
        2.现在无法支持qq邮箱<br>
        3.密码明文保存,请选择不重要的邮箱<br>
        4.保存时将验证可以成功发送邮件再保存,发送邮件需要时间,请耐心等待<br>
        5.<a href="/other/help#config">点此查看详细帮助</a>
    </div>
    <form id="email_config_form" action="javascript:void();">
        <div class="input-group input-group-lg">
            <span class="input-group-addon" id="sizing-addon1">电子邮件:</span>
            <input type="email" name="address" placeholder="example@example.com" autocomplete="off" width="auto"
                   class="form-control" value="${emailConfig.address!}"
                   aria-describedby="sizing-addon1">
        </div>
        <br/>
        <div class="input-group input-group-lg">
            <span class="input-group-addon" id="sizing-addon2">邮件密码:</span>
            <input type="text" autocomplete="off" name="password" width="auto"
                   class="form-control" value="${emailConfig.password!}" aria-describedby="sizing-addon2">
        </div>
        <br/>
        <div class="input-group input-group-lg">
            <span class="input-group-addon" id="sizing-addon3">SMTP地址:</span>
            <input type="text" name="host" placeholder="smtp.example.com" autocomplete="off" width="auto"
                   class="form-control" value="${emailConfig.host!}" aria-describedby="sizing-addon3">
        </div>
        <br/>
        <div class="input-group input-group-lg">
            <span class="input-group-addon" id="sizing-addon4">SMTP端口:</span>
            <input type="number" min="0" placeholder="587" autocomplete="off" step="1" name="port" width="auto"
                   class="form-control" value="${emailConfig.port!}" aria-describedby="sizing-addon4">
        </div>
        <br/>
        <div align="center">
            <button id="email_button" disabled="disabled" class="btn btn-success">提交</button>
            <img src="/image/load.gif" id="load_img" hidden>
        </div>
    </form>
</div>
<#include "../common/common_foot.ftl">
<script>
    $(function () {
        var $emailButton = $("#email_button");
        $('input').on('change', function () {
            $emailButton.attr("disabled", false);
        });

        $emailButton.on("click", function (e) {
            e.preventDefault();
            var haveBlank = false;
            $('input').each(function () {
                if (!$(this).val()) {
                    $.alert("不可以有空值");
                    haveBlank = true;
                }
            });
            if (haveBlank) {
                return;
            }
            forbidden_all();
            $.ajax({
                type: "POST",
                url: "/rest/config/email",
                data: $('#email_config_form').serialize(),
                async: true,
                error: function () {
                    $.alert("保存失败,网络异常");
                    release_all();
                },
                success: function (data) {
                    if (data.code === 0) {
                        $.alert("保存成功");
                    } else {
                        $.alert("保存失败", data.data);
                    }
                    release_all();
                }
            });
        });

        function forbidden_all() {
            $('input').attr('readonly', true);
            $emailButton.attr("disabled", true);
            $emailButton.hide();
            $("#load_img").show();
        }

        function release_all() {
            $('input').attr('readonly', false);
            $emailButton.show();
            $("#load_img").hide();
        }
    });
</script>
</body>
</html>
