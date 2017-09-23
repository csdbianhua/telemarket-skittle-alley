<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../common/common_head.ftl">
    <title>你画我猜</title>
    <link rel="stylesheet" href="/asserts/css/games/draw_guess.css">
</head>
<body>
<#assign name="games"/>
<#include "../common/navbar.ftl"/>
<div class="container-fluid">
    <div class="col-md-2"><h2 align="left" id="left_text"></h2></div>
    <div class="col-md-8"><h1 align="center" id="head_text">正在连接中...</h1></div>
    <div class="col-md-2">
        <h2 align="right" id="right_text">
        </h2>
    </div>

</div>
<div class="container-fluid">
    <div class="col-md-2">
        <ul class="user-thread">
        </ul>
        <div class="input-group">
            <input type="text" id="name_text" class="form-control">
            <span class="input-group-btn">
        <button class="btn btn-warning user_btn" id="name_btn" type="button">改名!</button>
        <button class="btn btn-danger user_btn" id="ready_btn" type="button">准备!</button>
      </span>

        </div>
    </div>
    <div class="col-md-8">
        <div align="center">
            <canvas id="myCanvas" width="800" height="416" style="border:2px solid #6699cc"></canvas>
            <div class="control-ops">
                <button type="button" class="btn btn-primary" disabled="disabled" id="clear_btn">清空画板</button>
                线宽 : <select id="selWidth" disabled="disabled">
            <#list 1..11 as item>
                <option value="${item}" <#if DEFAULT_WIDTH = item>selected="selected"</#if>>${item}</option>
            </#list>
            </select>
                颜色 : <select id="selColor" disabled="disabled">
            <#list ['black','blue','red','green','yellow','gray'] as item>
                <option value="${item}" <#if DEFAULT_COLOR = item>selected="selected"</#if>>${item}</option>
            </#list>
            </select>
            </div>
        </div>
    </div>
    <div class="col-md-2">
        <ul class="chat-thread">
        </ul>
        <div class="input-group">
            <input type="text" id="send_text" class="form-control">
            <span class="input-group-btn">
        <button class="btn btn-info" id="send_btn" type="button">发送!</button>
      </span>

        </div>

    </div>
<#include "../common/common_foot.ftl">
    <script src="/asserts/js/cm/sockjs-1.1.1.min.js"></script>
    <script src="/asserts/js/games/draw_guess.js?v=1"></script>
</body>
</html>