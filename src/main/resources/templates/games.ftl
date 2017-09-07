<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<#include "common/common_head.ftl">
    <title>联机游戏</title>
</head>
<body>
<#assign name="games"/>
<#include "common/navbar.ftl"/>
<div class="container-fluid">
<#list games as game>
    <img width="200" height="200" src="/asserts/image/games/${game.identify}.jpg"/>
    <a style="font-size: large" href="/games/${game.identify}">${game.name}</a>
</#list>
</div>
<#include "common/common_foot.ftl">
</body>
</html>