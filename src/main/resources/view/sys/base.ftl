<#macro header title="">
  <!DOCTYPE html>
  <html>
  <head>
    <meta charset="utf-8">
    <title>${title}</title>

    <meta name="viewport" content="width=device-width">
    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.2/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.bootcss.com/AlertifyJS/1.8.0/css/alertify.min.css" />
    <link rel="stylesheet" href="https://cdn.bootcss.com/AlertifyJS/1.8.0/css/themes/default.min.css" />
  </head>

  <body>
  <!--[if lte IE 8]>
  <div style="height:2000px">抱歉，检测到浏览器不兼容，请使用chrome浏览器或者360、腾讯、搜狐浏览器的<a href="http://jingyan.baidu.com/article/e8cdb32b3a66ef37052bad2a.html">极速模式</a>!</div>
  <![endif]-->
</#macro>

<#macro nav title="">
  <nav class="navbar navbar-default navbar-fixed-top hidden-xs">
    <div class="container-fluid">
      <div class="navbar-header">
        <a class="navbar-brand" href="#">${title}</a>
      </div>
      <ul class="nav navbar-nav">
        <li>
          <p class="navbar-btn">
            <input id="filter" class="form-control" />
          </p>
        </li>
        <li style="margin-left:4px">
          <button id="save" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-save"></span> 保存</button>
          <button id="load" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-refresh"></span> 刷新</button>
        </li>
      </ul>
    </div>
  </nav>
  <div style=" top:51px;left:0;right:0;bottom:0;position:absolute;width:auto;height:auto;">
    <div id="grid" style="width:100%;height:100%;" class="ag-fresh"></div>
  </div>
</#macro>

<#macro footer>
  <script src="https://cdn.bootcss.com/jquery/2.2.3/jquery.min.js"></script>
  <script src="https://cdn.bootcss.com/AlertifyJS/1.8.0/alertify.min.js"></script>
  <script src="https://cdn.bootcss.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
  <script src="https://cdn.bootcss.com/ag-grid/5.0.1/ag-grid.min.js"></script>
   <#nested>

  </body>
  </html>
</#macro>
