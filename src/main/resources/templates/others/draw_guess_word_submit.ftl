<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "../common/common_head.ftl">
    <title>你画我猜词汇提交</title>
    <link rel="stylesheet" href="/asserts/css/games/draw_guess.css">
</head>
<body>
<#include "../common/navbar.ftl"/>
<div class="container">
    <form class="col-lg-4 col-lg-offset-4" data-toggle="validator" method="post">
    <#if tip??>
        <div class="alert alert-success" id="save_alert" role="alert">${tip}</div>
    </#if>
        <div class="form-group has-feedback">
            <label for="word">词汇</label>
            <input type="text" class="form-control" required
                   maxlength="10" id="word" name="word" <#if tip??>onblur="$('#save_alert').hide()"</#if>>
            <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
            <div class="help-block with-errors"></div>
        </div>
        <div class="form-group has-feedback">
            <label for="wordTip">词汇提示</label>
            <input type="text" maxlength="15" required class="form-control" id="wordTip" name="wordTip">
            <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
            <div class="help-block with-errors"></div>
        </div>
        <button type="submit" class="btn btn-default">提交</button>
    </form>
</div>

<#include "../common/common_foot.ftl">
<script src="/asserts/js/cm/validator.min.js"></script>
</body>
</html>