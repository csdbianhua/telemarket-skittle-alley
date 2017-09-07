<!DOCTYPE html>
<html>
<head>
<#include "../common/common_head.ftl">
    <title>帮助</title>
</head>
<body>
<#assign name="other"/>
<#include "../common/navbar.ftl"/>
<div class="container-fluid">
    <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
            <ul class="nav nav-sidebar">
                <li><a href="#overview">概述</a></li>
                <li><a href="#search">电影字幕搜索</a></li>
                <li><a href="#monitor">字幕监控</a></li>
                <li><a href="#config">配置</a></li>
            </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
            <h1 class="page-header">Subtitle Assistant使用介绍</h1>
            <h2 class="sub-header" id="overview">概述</h2>
            <p>字幕搜索主要用于搜索电影信息与获得原版字幕的信息,字幕监控用于监控最新出的字幕</p>
            <h2 class="sub-header" id="search">电影字幕搜索</h2>
            <h2 class="sub-header" id="monitor">字幕监控</h2>
            <h2 class="sub-header" id="config">配置</h2>
            <p>由于监控到新出的字幕后需要发送邮件，所以必须设置一个邮件账号。</p>
            <p>以<a href="https://mail.sina.com.cn/" target="_blank">新浪邮箱</a>为例</p>
            <p>登录新浪邮箱以后，点击右上角的设置。<img src="/image/help/1.png"></p>
            <p>然后点击左侧导航栏中包含SMTP的条目。<img src="/image/help/2.png"></p>
            <p>SMTP为发送邮件的服务，任选一个有SMTP服务的开启即可，比如此处选择POP3/SMTP服务。可以看到此处SMTP服务器为smtp.sina.cn。开启后点击保存。</p>
            <img src="/image/help/3.png">
            <p>打开Subtitle Assistant的<a href="/other/config" target="_blank">设置页</a></p>
            <p>填入你的电子邮件账户、密码、SMTP地址。(注意目前密码都是明文保存的，请避免被他人看到或选择不重要的邮箱。)</p>
            <p>SMTP端口号需要查看对应邮箱的帮助页面。比如新浪的点击”如何设置POP服务？“。</p>
            <p>随便选一个主流邮箱客户端设置详解，找到端口设置的位置，并找到“SSL加密传输”的端口。</p>
            <img src="/image/help/4.png">
            <img src="/image/help/5.png">
            <p>将其填入Subtitle Assistant设置即可。</p>
            <img src="/image/help/6.png">
            <p>点击提交后需要等待校验是否能正常发送邮件。校验成功你所填邮箱将会收到一封邮件。</p>
            <img src="/image/help/7.png">
            <p>然后就可以使用监控功能了。</p>
        </div>
    </div>
</div>
<#include "../common/common_foot.ftl">
</body>
</html>
