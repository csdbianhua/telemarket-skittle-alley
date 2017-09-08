<nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
        <div class="navbar-header">
            <span class="navbar-brand">Telemarketer Skittle-Alley</span>
        </div>
        <div class="collapse navbar-collapse">
            <ul class="nav navbar-nav">
            <#list games as game>
                <li <#if url_name = "games">class="active"</#if>><a href="/games/${game.identify}">${game.name}</a></li>
            </#list>
            <#--<img width="200" height="200" src="/asserts/image/games/${game.identify}.jpg"/>-->
            </ul>
        </div>
    </div>
</nav>