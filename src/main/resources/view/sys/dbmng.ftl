<!DOCTYPE html>
<html>
	<head>
	    <meta charset="utf-8">
	    <title>dbMng-${profile.name}</title>
	    
	    <meta name="viewport" content="width=device-width">
	    <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.2/css/bootstrap.min.css" />
		<link rel="stylesheet" href="https://cdn.bootcss.com/AlertifyJS/1.8.0/css/alertify.min.css" />
		<link rel="stylesheet" href="https://cdn.bootcss.com/AlertifyJS/1.8.0/css/themes/default.min.css" />
	    
	   
	</head>

	<body>
<!--[if lte IE 8]>
	<div style="height:2000px">抱歉，检测到浏览器不兼容，请使用chrome浏览器或者360、腾讯、搜狐浏览器的极速模式!</div>
<![endif]-->

		<nav class="navbar navbar-default navbar-fixed-top hidden-xs">
		  <div class="container-fluid">
		    <div class="navbar-header">
		      <a class="navbar-brand" href="#">${profile.name}</a>
		    </div>
		    <ul class="nav navbar-nav">
		      <li>
		       <p class="navbar-btn">
		        <select class="models" id="eng-models" style="height:32px;">

				</select>
				</p>
			  </li>
		      <li style="margin-left:4px">
		        <button id="save" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-save"></span> 保存</button>		      
		      	<button id="delete" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-remove"></span> 删除</button>
		      	<button id="load" class="btn btn-default navbar-btn"><span class="glyphicon glyphicon-refresh"></span> 刷新</button>
		      </li>
		      
		    </ul>
		    <ul class="nav navbar-nav navbar-right">
			  <li class="dropdown"> 
			    <a class="dropdown-toggle" href="#" data-toggle="dropdown">高级 <strong class="caret"></strong></a>
		        <div class="dropdown-menu" style="padding: 10px;">
				  <div class="form-inline"><p><span>页面大小: </span><input id="set-pagesize" type="text" class="form-control click-no-close" style="height:24px" placeholder="100"></p></div>
				  <button id="export" class="btn btn-default btn-sm">导出当前页</button>
				  <button id="set-null" class="btn btn-default btn-sm">设置当前单元NULL</button>
				  <hr>
		          <textarea rows="2" cols="40" id="sql" class="click-no-close" placeholder="输入sql语句"></textarea><br/>
		          <button id="execute" class="btn btn-default btn-xs"><span class="glyphicon glyphicon-wrench"></span> 执行</button>
		        </div>
		        </li>
		    </ul>		    
		  </div>
		</nav>
		<div style=" top:51px;left:0;right:0;bottom:0;position:absolute;width:auto;height:auto;">
    		<div id="dbmng" style="width:100%;height:100%;" class="ag-fresh"></div>
    	</div>

		<script src="https://cdn.bootcss.com/jquery/2.2.3/jquery.min.js"></script>
		<script src="https://cdn.bootcss.com/AlertifyJS/1.8.0/alertify.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>
		<script src="https://cdn.bootcss.com/ag-grid/5.0.1/ag-grid.min.js"></script>
    
    <script>
    
    $(function(){
	  var changedRows=[];
	  var gridInstance={};
	  var pageSize = Number($('#set-pagesize').attr("placeholder"));
		ajaxLoadMeta();

	  function ajaxLoadMeta() {
	   $.ajax({
    	url: '/api/dbmng/_meta',
	  	dataType: 'json',
	  	type: 'GET',
	  	success: function(res) {
	  	   for(var i =0; i< res.length; i++) {
	  	 	   $("#eng-models").append( $("<option>")
     	       .val(res[i]).html(res[i])
           );
         }
         $('#eng-models').trigger('change');
	  	 },
  		error: function (data) { alertify.error("加载失败"); }
	   });
	  }

	  function ajaxLoadQuery() {
	   changedRows=[];
	   $.ajax({
    	url: '/api/dbmng/' + $('#eng-models').val() + '/column',
	  	dataType: 'json',
	  	type: 'GET',
	  	success: function(res) { createQueryTable(res); },
  		error: function (data) { alertify.error("加载失败"); }
	   });		
	  } 
	  
	  function ajaxLoadPage(params) {
		var url = '/api/dbmng/' + $('#eng-models').val() + '?start=' + params.startRow + '&end=' + params.endRow;
		if (params.sortModel.length > 0)
			url += '&sort=' + params.sortModel[0].colId + '&order=' + params.sortModel[0].sort;
			
		$.ajax({
	    	url: url,
		  	dataType: 'json',
		  	type: 'GET',
		  	success: function(res) { 
		  	  var lastRow = -1;
		  	  //扣除后台自动添加的一行空数据
        	  if (params.startRow + res.rowData.length <= params.endRow) {
            	lastRow = params.startRow + res.rowData.length - 1;
        	  }				  	  
		  	  params.successCallback(res.rowData, lastRow); 
		  	},
	  		error: function (data) { 
	  		  alertify.error("加载数据失败"); 
	  		  params.failCallback();
	  		}
		});	  
	  }
	  
	  function createQueryTable(res){
	    if (gridInstance.api)
	    	gridInstance.api.destroy();
	    
	    gridInstance = {
	      rowModelType: 'pagination',
	      rowSelection: 'multiple',	      
    	  columnDefs: res.columnDefs.map(function(item){return $.extend(item,{cellRenderer : cellRenderer,minWidth:60})}),
 		  enableServerSideSorting: true,
    	  suppressMovableColumns: true,
    	  suppressMultiSort: true,
    	  enableColResize: true,
    	  onCellValueChanged : function(para){
    	    para.node._changed = true;
    	    this.api.refreshRows([para.node]);    	    
    	   },
    	  onGridReady : function(para) {
    	    this.api.sizeColumnsToFit();
    	  }, 
    	  getRowStyle: function(params) {
    	  	if (params.node._changed)
        		return {'background-color': '#ff0'}
    		return null;
    	   } 
          };
          

		var dataSource = {
        	pageSize: pageSize, 
        	getRows: ajaxLoadPage
    	};
          	
		new agGrid.Grid(document.querySelector('#dbmng'), gridInstance);
		gridInstance.api.setDatasource(dataSource);
		
		$('#save').prop('disabled', false);			
		$('#delete').prop('disabled', false);
      }
    
    function escapeHtml(text) {
    	var entityMap = {
    		"&": "&amp;",
    		"<": "&lt;",
    		">": "&gt;",
    		'"': '&quot;',
    		"'": '&#39;',
    		"/": '&#x2F;'
  		};
	    return text.replace(/[&<>"'\/]/g, function (s) {
  			return entityMap[s];
		});
    }
    function cellRenderer(params) {

		if (params.value===null || params.value===undefined) {
		   return '<span style="color:#ddd">NULL</span>';
		} else if(typeof params.value === 'string' || params.value instanceof String){
		    return escapeHtml(params.value);
		} else {
			return params.value;
		}
    }
          
	function createSqlTable(res){
	    if (gridInstance.api)
	    	gridInstance.api.destroy();
	    
	    gridInstance = {
    	  columnDefs: res.columnDefs.map(function(item){return $.extend(item,{cellRenderer : cellRenderer, minWidth:60})}),
    	  rowData: res.rowData,
	      enableSorting: true,
	      rowSelection: 'multiple',	      
    	  suppressMovableColumns: true,
    	  suppressMultiSort: true,
    	  enableColResize: true,
    	  onCellValueChanged : function(para){
    	    para.node._changed = true;
    	    this.api.refreshRows([para.node]);    	    
    	   },
    	  onGridReady : function(para) {
    	    this.api.sizeColumnsToFit();
    	  }, 
    	  getRowStyle: function(params) {
    	  	if (params.node._changed) {
        		return {'background-color': '#ff0'}
    		}
    		return null;
    	   } 
          };
          
		new agGrid.Grid(document.querySelector('#dbmng'), gridInstance);
    	
		if (res["readOnly"]===true) {
			$('#save').prop('disabled', true);			
			$('#delete').prop('disabled', true);
		}
		else {
			$('#save').prop('disabled', false);			
			$('#delete').prop('disabled', false);
		}	
      }  

	  $('.models').on('change', function () {
		$('.models').val($(this).val());
	    ajaxLoadQuery();
        return false;
      });
      
      $("#load").click(function(){
	    ajaxLoadQuery();
	  }); 		

	  $("#set-pagesize").change(function() {
  		pageSize = Number(this.value);
  		ajaxLoadQuery();
	  });
	  
 	  $("#save").click(function(){
 	     var changedData=[];
 	     gridInstance.api.forEachLeafNode(function(node){
 	     	if (node._changed)
 	     		changedData.push(node.data);
 	     });
 	     
	     if (changedData.length == 0) {
		   alertify.success("没有记录修改");
	       return;
	     }
	     
         $.ajax({ 
	      url: '/api/dbmng/'+ $('#eng-models').val() ,
	      dataType: "json",
	      contentType: 'application/json',
	      type: "POST",
	      data: JSON.stringify({"data": changedData}),
	      success: function (res) {
	      	if ('updatedRows' in res)
	        	alertify.success("修改了" + res.updatedRows + "条记录");
	        ajaxLoadQuery();
	      },
	      error: function (data) { alertify.error('保存失败'); }
	    });
      });

      $("#delete").click(function(){
	     var selected = gridInstance.api.getSelectedRows();
	     if (selected.length <= 0) {
	       alertify.success("请先选中要删除的记录");
	       return;
	     }

		alertify.confirm("确认要删除选中的" + selected.length + "条记录吗?", function (e) {
			if (e) {
				 $.ajax({
					 url: '/api/dbmng/'+ $('#eng-models').val(),
					 dataType: 'json',
					 contentType: 'application/json',
					 type: 'DELETE',
					 data: JSON.stringify({"data": selected}),
					 success: function(res){
				      	if ('updatedRows' in res)
	        				alertify.success("删除了" + res.updatedRows + "条记录");
						ajaxLoadQuery(); 
					 },
					 error: function (data) {alertify.error('删除失败'); }
				 });			
			}
		});   
	  }); 
	  
	  $("#execute").click(function(){
       $.ajax({
    	url: '/api/dbmng/'+ $('#eng-models').val() +'/sql',
	  	dataType: 'json',
	  	contentType: 'application/json',
	  	type: 'POST',
	  	data: JSON.stringify({"statement": $("#sql")[0].value}),
	  	success: function(res){	
	  		createSqlTable(res);
	  		if ('updatedRows' in res)
	  			alertify.success('SQL语句操作了' + res.updatedRows + '条记录');
	  		else
	  			alertify.success('SQL语句返回了' + res.rowData.length + '条记录');
	  			
	  	},
  		error: function (data) { alertify.error('执行失败'); }
	    });
	  });
	  
	  $("#export").click(function(){
	  	gridInstance.api.exportDataAsCsv();
	  });
	  
	  $("#set-null").click(function(){
	  	var cell = gridInstance.api.getFocusedCell();
	  	gridInstance.api.forEachLeafNode(function(node){
 	     	if (cell && node.childIndex === cell.rowIndex) {
 	     		node.data[cell.column.colId]=null;
 	     		node._changed=true;
 	     		gridInstance.api.refreshRows([node]);
 	     	}
 	     });
	  });
	  
	  $('.click-no-close').click(function (e) {
    	e.stopPropagation();
  	  });	
        
    });   
	</script>

</body>
</html>
