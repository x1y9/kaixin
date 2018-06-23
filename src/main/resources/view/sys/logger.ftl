<#include "base.ftl" >
  <@header title="Logger" />
  <@nav title="Logger" />
  <@footer>
  <script>
    $(function(){
	  var gridInstance={};
      ajaxLoadData();

	  function ajaxLoadData() {
	   $.ajax({
    	url: '/api/logger/list',
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
			{headerName: "类", field: "name",width:300,editable:true},
			{headerName: "级别", field: "level",width:100,
			  editable:true,
			  cellEditor: 'select',
        	  cellEditorParams: {
			    values: ['', 'TRACE','DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF']
        		}
        	}],
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
	      url: '/api/logger/save' ,
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
