<%@page import="org.springframework.context.annotation.Import"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>文件上传</title>
</head>
<script type="text/javascript">
window.onbeforeunload=function(){
	    if(document.all){
	        if(event.clientY<0){
	            return "确定要离开吗？";
	        }
	    }else{
	        return "确定要离开吗？";
	    }
	}
</script>
<body>
	<video width="960" height="720" controls>
		<source src="${ap }/video/dovideo/052116_303-1pon-1080p_onekeybatch.mp4" type="video/mp4">
	</video>
</body>
</html>
