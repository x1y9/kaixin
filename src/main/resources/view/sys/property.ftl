<#include "base.ftl" >
  <@header title="Property" />
  <@nav title="Property" />

  <@footer>
  <script>
    $(function(){
	  var gridInstance={};
      ajaxLoadData();

	  function ajaxLoadData() {
	   $.ajax({
    	url: '/api/property/list',
	  	dataType: 'json',
	  	type: 'GET',
	  	success: function(res) { createGrid(res); },
  		error: function (data) { alertify.error("加载失败"); }
	   });
	  }

	  function createGrid(res){
	    if (gridInstance.api)
	    	gridInstance.api.destroy();

	    gridInstance = {
	      rowSelection: 'multiple',
    	  columnDefs: [
			{headerName: "属性名", field: "name",width:200},
			{headerName: "类型", field: "type",width:60},
			{headerName: "当前值", field: "value",width:100,editable:true, cellRenderer : valueRenderer},
			{headerName: "缺省值", field: "defaultValue",width:100},
			{headerName: "重启", field: "needRestart",width:50, cellRenderer : needRestartRenderer},
			{headerName: "帮助", field: "help",width:500}
			],
		  rowData:res,
		  enableFilter: true,
		  enableSorting: true,
    	  enableColResize: true,
    	  suppressMovableColumns: true,
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


		new agGrid.Grid(document.querySelector('#grid'), gridInstance);

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

	  function valueRenderer(params) {
		if (params.value !== params.data.defaultValue)
		   return '<b>' + escapeHtml(params.value) + '</b>';
		else
		   return escapeHtml(params.value);
	  }

	  function needRestartRenderer(params) {
		if (params.value === true) {
		   return '<span class="glyphicon glyphicon-refresh"/>';
		} else {
			return '';
		}
	  }

	  $('#filter').on('input', function() {
		gridInstance.api.setQuickFilter($('#filter').val());
	  });


      $("#load").click(function(){
	    $('#filter').val('');
	    ajaxLoadData();
	  });


 	  $("#save").click(function(){
 	     var changedData=[];
 	     gridInstance.api.forEachLeafNode(function(node){
 	     	if (node._changed) {
 	     		changedData.push(node.data);
				node._changed=false;
			}
 	     });

	     if (changedData.length == 0) {
		   alertify.log("没有条目修改");
	       return;
	     }

         $.ajax({
	      url: '/api/property/save' ,
	      dataType: "json",
	      contentType: 'application/json',
	      type: "POST",
	      data: JSON.stringify({"data": changedData}),
	      success: function (res) { gridInstance.api.refreshView();},
	      error: function (data) { alertify.error('保存失败'); }
	    });
      });

    });
	</script>
</@footer>