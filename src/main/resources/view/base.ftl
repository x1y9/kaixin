<#macro header title="${profile.name}">
<!DOCTYPE html>
  <!DOCTYPE html>
  <html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <meta name="renderer" content="webkit"> <!-- 让双核浏览器默认用webkit -->
    <title>${title!}</title>
    <link rel="stylesheet" href="/public/css/kaixin.css"/>
    <#nested>
  </head>
  <body>
</#macro>


<#macro navbar title="${profile.name}">
  <nav class="border-shadow py1 ">
    <div class="container-fluid">
      <div class="frow">
        <div class="fcol-4">
          <a href="/">${title!}</a>
        </div>

        <div class="fcol-4 x-center ">
        </div>

        <div class="fcol-4 x-right">
          <#if loginUser??>
            <a href="/public/admin">${locale.get('Admin')}</a>
            <a href="/auth/logout">${locale.get('Logout')}</a>
            <#else>
              <a class="button fg-primary" href="/signin">${locale.get('Login')}</a>
              <#if sysProperties['signup.enable'] == 'true'>
                <a  class="button fg-primary" href="/signup">${locale.get('Signup')}</a>
              </#if>
          </#if>
        </div>

      </div>
    </div>
  </nav>
</#macro>

<!--底部-->
<#macro footer>
  <footer class="footer bg-e py2 fs13 x-center" >
    Copyright ©2016
  </footer>
  <script src="https://cdn.bootcss.com/jquery/2.2.3/jquery.min.js"></script>
  <#nested>
  </body>
</html>
</#macro>