<%@ page import="org.springframework.context.annotation.Import"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="content-language" content="zh-CN" />
<title>文件上传</title>
<link href="${rs }css/stream-v1.css" type="text/css" rel="stylesheet">
<link href="http://cdn.bootcss.com/bootstrap/3.1.1/css/bootstrap.min.css" rel="stylesheet">
<script src="${rs }js/stream-v1.js" type="text/javascript"></script>
<script type="text/javascript">
</script>
</head>
<body>

<div class="container">
	<div class="row clearfix">
		<div class="col-md-7 column">
			<div class="page-header">
			  <h1>Stream上传插件 <small>bootstrap style demo</small></h1>
			</div>

			<div class="dropzone dz-clickable" id="i_stream_dropzone">
			</div>

			<div class="btn-toolbar" role="toolbar">
				<div class="btn-group">
					<div type="button" class="btn btn-default" id="i_select_files"><span class="glyphicon glyphicon-plus-sign"></span> 添加文件</div>
					<button type="button" class="btn btn-default" onclick="javascript:_t.upload();"><span class="glyphicon glyphicon-upload"></span> 开始上传</button>
					<button type="button" class="btn btn-default" onclick="javascript:_t.stop();"><span class="glyphicon glyphicon-stop"></span> 停止上传</button>
					<button type="button" class="btn btn-default" onclick="javascript:_t.cancel();"><span class="glyphicon glyphicon-remove-sign"></span> 取消上传</button>
				</div>
				<div class="btn-group">
					<button type="button" class="btn btn-default" onclick="javascript:_t.disable();"><span class="glyphicon glyphicon-minus-sign"></span> 禁用选择</button>
					<button type="button" class="btn btn-default" onclick="javascript:_t.enable();"><span class="glyphicon glyphicon-ok-sign"></span> 激活选择</button>
				</div>
				<div class="btn-group">
					<button type="button" class="btn btn-default" onclick="javascript:_t.hideBrowseBlock();"><span class="glyphicon glyphicon-minus-sign"></span> 隐藏文件选择</button>
					<button type="button" class="btn btn-default" onclick="javascript:_t.showBrowseBlock();"><span class="glyphicon glyphicon-ok-sign"></span> 显示文件选择</button>
				</div>
			</div>

			<table id="data_table" class="table tablesorter">
				<thead>
					<tr><th>编号</th>
						<th>文件</th>
						<th>大小</th>
						<th>操作</th>
					</tr>
				</thead>
				<tbody id="bootstrap-stream-container">
				</tbody>
				<tfoot id="stream_total_progress_bar">
					<tr><th colspan="2">
							<div class="progress">
							  <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">
							  </div>
							</div>
						</th>
						<th colspan="2"><span class="stream_total_size"></span>
							<span class="stream_total_percent"></span>
						</th>
					</tr>
				</tfoot>
			</table>
		</div>
	</div>	
</div>

<script src="http://cdn.bootcss.com/jquery/1.10.2/jquery.min.js"></script>
<script src="http://cdn.bootcss.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
<script type="text/javascript" src="/js/stream-v1.js"></script>
<script type="text/javascript">
/**
 * 配置文件（如果没有默认字样，说明默认值就是注释下的值）
 * 但是，on*（onSelect， onMaxSizeExceed...）等函数的默认行为
 * 是在ID为i_stream_message_container的页面元素中写日志
 */
 
	var config = {
		enabled: true, /** 是否启用文件选择，默认是true */
		customered: true,
		multipleFiles: true, /** 是否允许同时选择多个文件，默认是false */	
		autoRemoveCompleted: false, /** 是否自动移除已经上传完毕的文件，非自定义UI有效(customered:false)，默认是false */
		autoUploading: false, /** 当选择完文件是否自动上传，默认是true */
		fileFieldName: "FileData", /** 相当于指定<input type="file" name="FileData">，默认是FileData */
		maxSize: 2147483648, /** 当_t.bStreaming = false 时（也就是Flash上传时），2G就是最大的文件上传大小！所以一般需要 */
		simLimit: 10, /** 允许同时选择文件上传的个数（包含已经上传过的） */
//		extFilters: [".txt", ".gz", ".jpg", ".png", ".jpeg", ".gif", ".avi", ".html", ".htm"], /** 默认是全部允许，即 [] */
		browseFileId : "i_select_files", /** 文件选择的Dom Id，如果不指定，默认是i_select_files */
		browseFileBtn : "<div>请选择文件</div>", /** 选择文件的按钮内容，非自定义UI有效(customered:false) */
		dragAndDropArea: "i_stream_dropzone",
		filesQueueId : "i_stream_files_queue", /** 文件上传进度显示框ID，非自定义UI有效(customered:false) */
		filesQueueHeight : 450, /** 文件上传进度显示框的高，非自定义UI有效(customered:false)，默认450px */
		messagerId : "i_stream_message_container", /** 消息框的Id，当没有自定义onXXX函数，系统会显示onXXX的部分提示信息，如果没有i_stream_message_container则不显示 */
		postVarsPerFile : { /** 上传文件时传入的参数，默认: {} */
			param1: "val1",
			param2: "val2"
		},
//		frmUploadURL : "http://customers.duapp.com/fd;", /** Flash上传的URI */
		tokenURL : "${ap }/tk",
      	uploadURL : "${ap }/upload",
		onSelect: function(files) {
			//console && console.log("-------------onSelect-------------------");
			//console && console.log(files);
			//console && console.log("-------------onSelect-------------------End");
		},
		onMaxSizeExceed: function(file) {
			//console && console.log("-------------onMaxSizeExceed-------------------");
			//console && console.log(file);
			$("#i_error_tips > span.text-message").append("文件[name="+file.name+", size="+file.formatSize+"]超过文件大小限制‵"+file.formatLimitSize+"‵，将不会被上传！<br>");
			//console && console.log("-------------onMaxSizeExceed-------------------End");
		},
		onFileCountExceed : function(selected, limit) {
			//console && console.log("-------------onFileCountExceed-------------------");
			//console && console.log(selected + "," + limit);
			$("#i_error_tips > span.text-message").append("同时最多上传<strong>"+limit+"</strong>个文件，但是已选择<strong>"+selected+"</strong>个<br>");
			//console && console.log("-------------onFileCountExceed-------------------End");
		},
		onExtNameMismatch: function(info) {
			//console && console.log("-------------onExtNameMismatch-------------------");
			//console && console.log(info);
			$("#i_error_tips > span.text-message").append("<strong>"+info.name+"</strong>文件类型不匹配[<strong>"+info.filters.toString() + "</strong>]<br>");
			//console && console.log("-------------onExtNameMismatch-------------------End");
		},
		onAddTask: function(file) {
			 var file = '<tr id="' + file.id + '" class="template-upload fade in">' +
		     '<td><span class="preview">'+file.id+'</span></td>' +
		     '<td><p class="name">' + file.name + '</p>' +
		     '    <div><span class="label label-info">进度：</span> <span class="message-text"></span></div>' +
		     '    <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">' +
			'			<div class="progress-bar progress-bar-success" title="" style="width: 0%;"></div>' +
			'		</div>' +
		     '</td>' +
		     '<td><p class="size">' + file.formatSize + '</p>' +
		     '</td>' +
		     '<td><span class="glyphicon glyphicon-remove" onClick="javascript:_t.cancelOne(\'' + file.id + '\')"></span>' +
		     '</td></tr>';
			
			$("#bootstrap-stream-container").append(file);
		},
		onUploadProgress: function(file) {
			//console && console.log("-------------onUploadProgress-------------------");
			//console && console.log(file);
			
			var $bar = $("#"+file.id).find("div.progress-bar");
			$bar.css("width", file.percent + "%");
			var $message = $("#"+file.id).find("span.message-text");
			$message.text("已上传:" + file.formatLoaded + "/" + file.formatSize + "(" + file.percent + "%" + ") 速  度:" + file.formatSpeed);
			
			var $total = $("#stream_total_progress_bar");
			$total.find("div.progress-bar").css("width", file.totalPercent + "%");
			$total.find("span.stream_total_size").html(file.formatTotalLoaded + "/" + file.formatTotalSize);
			$total.find("span.stream_total_percent").html(file.totalPercent + "%");
			
			//console && console.log("-------------onUploadProgress-------------------End");
		},
		onStop: function() {
			//console && console.log("-------------onStop-------------------");
			//console && console.log("系统已停止上传！！！");
			//console && console.log("-------------onStop-------------------End");
		},
		onCancel: function(file) {
			//console && console.log("-------------onCancel-------------------");
			//console && console.log(file);
			
			$("#"+file.id).remove();
			
			var $total = $("#stream_total_progress_bar");
			$total.find("div.progress-bar").css("width", file.totalPercent + "%");
			$total.find("span.stream_total_size").text(file.formatTotalLoaded + "/" + file.formatTotalSize);
			$total.find("span.stream_total_percent").text(file.totalPercent + "%");
			//console && console.log("-------------onCancel-------------------End");
		},
		onCancelAll: function(numbers) {
			//console && console.log("-------------onCancelAll-------------------");
			//console && console.log(numbers + " 个文件已被取消上传！！！");
			$("#i_error_tips > span.text-message").append(numbers + " 个文件已被取消上传！！！");
			
			//console && console.log("-------------onCancelAll-------------------End");
		},
		onComplete: function(file) {
			//console && console.log("-------------onComplete-------------------");
			//console && console.log(file);
			
			/** 100% percent */
			var $bar = $("#"+file.id).find("div.progress-bar");
			$bar.css("width", file.percent + "%");
			var $message = $("#"+file.id).find("span.message-text");
			$message.text("已上传:" + file.formatLoaded + "/" + file.formatSize + "(" + file.percent + "%" + ")");
			/** remove the `cancel` button */
			var $cancelBtn = $("#"+file.id).find("td:last > span");
			$cancelBtn.remove();
			
			/** modify the total progress bar */
			var $total = $("#stream_total_progress_bar");
			$total.find("div.progress-bar").css("width", file.totalPercent + "%");
			$total.find("span.stream_total_size").text(file.formatTotalLoaded + "/" + file.formatTotalSize);
			$total.find("span.stream_total_percent").text(file.totalPercent + "%");
			
			//console && console.log("-------------onComplete-------------------End");
		},
		onQueueComplete: function(msg) {
			//console && console.log("-------------onQueueComplete-------------------");
			//console && console.log(msg);
			//console && console.log("-------------onQueueComplete-------------------End");
		},
		onUploadError: function(status, msg) {
			//console && console.log("-------------onUploadError-------------------");
			//console && console.log(msg + ", 状态码:" + status);
			
			$("#i_error_tips > span.text-message").append(msg + ", 状态码:" + status);
			
			//console && console.log("-------------onUploadError-------------------End");
		}
	};
	var _t = new Stream(config);
	/** 不支持拖拽，隐藏拖拽框 */
	if (!_t.bDraggable) {
		$("#i_stream_dropzone").hide();
	}
	/** Flash最大支持2G */
	if (!_t.bStreaming) {
		_t.config.maxSize = 2147483648;
	}
</script>
</body>
</html>
