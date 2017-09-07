<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <span class="navbar-brand">Telemarketer Skittle-Alley</span>
        </div>
        <div class="collapse navbar-collapse">
        <#assign url_name = (name!"")/>
            <ul class="nav navbar-nav">
                <li <#if url_name = "games">class="active"</#if>><a href="/games">联机游戏</a></li>
            </ul>
        </div>
    </div>
</nav>