<#include "base.ftl" >
  <@header title="Metric" />
  <@nav title="Metric" />
  <@footer>
  <script>

    $(function(){
      $('#save').toggle();
	  var gridInstance={};
      ajaxLoadData();

	  function ajaxLoadData() {
	   $.ajax({
    	url: '/api/metric/list',
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
			{headerName: "统计对象", field: "name",width:200},
			{headerName: "总耗时(毫秒)", field: "totalTime",width:100},
			{headerName: "调用次数", field: "count",width:100},
			{headerName: "平均频率(每秒)", field: "meanRate", width:100},
			{headerName: "最近1分钟频率", field: "oneMinuteRate", width:100},
			{headerName: "最近5分钟频率", field: "fiveMinuteRate", width:100},
			{headerName: "最近15分钟频率", field: "fifteenMinuteRate", width:100},
			{headerName: "平均耗时(毫秒)", field: "meanMs",width:100},
			{headerName: "中位数耗时", field: "medianMs",width:100},
			{headerName: "最长耗时", field: "maxMs",width:100},
			{headerName: "最短耗时", field: "minMs",width:100},
			{headerName: "75%耗时不超过", field: "75thMs",width:100},
			{headerName: "95%耗时不超过", field: "95thMs",width:100},
			{headerName: "99%耗时不超过", field: "99thMs",width:100}
			],
		  rowData:res,
		  enableFilter: true,
		  enableSorting: true,
    	  enableColResize: true,
    	  suppressMovableColumns: true,
    	  onGridReady : function(para) {
    	    this.api.sizeColumnsToFit();
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

    });
	</script>
</@footer>