<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="content-language" content="zh-CN" />
<title>Insert title here</title>
</head>
<body>
        <div id="roomRemark"></div>     
        <div style="position:relative;width:40%;height:80%;overflow:auto;display:none;margin-left:5px;"  id="chatMsg"></div>
        <div style="position:relative;width:40%;height:20%;overflow:auto;display:none;margin-left:5px;"  id="chatSendMsg">
        	<textarea rows="10" cols="10" id="chatmsgtext" name="chatmsgtext"></textarea>
        	<button onclick="sendMessage('111')"></button>
        </div>
</body>
<script type="text/javascript">
var roomid="00001";
var username="11111";
var deptSortName="aaaaaa";
var isSysMsg="0";

var stompClient=null;
var content=null;  
$(function(){  
    connect();  
})  
//connect the server  
function connect(){  
    var socket=new SockJS("/webchat");   
    stompClient=Stomp.over(socket);  
    stompClient.connect('','',function(frame){  
        console.log('Connected: '+frame);  
        //用户聊天订阅   
        //alert("hello: "+frame);  
        stompClient.subscribe("/userChat/chat"+roomid,function(chat){  
            showChat(JSON.parse(chat.body));  
        });   
            
        //初始化  
        stompClient.subscribe("/app/initChat/"+roomid,function(initData){  
            //alert("初始化聊天室");      
            console.log(initData);      
            content=JSON.parse(initData.body);  
            //content=body.document.content;    
            //alert(content+":"+content.document.content);     
            content.forEach(function(item){  
                showChat(item);    
            });  
            sendMessage("1","进入");   
        });  
    },function(){   
        connect();  
    });   
}
//显示聊天信息  
function showChat(message){   
     var htmlMsg=decodeURIComponent(message.chatContent);  
       var userMsg=decodeURIComponent(message.deptName)  
       				+"--"+decodeURIComponent(message.userName)+"   "+decodeURIComponent(message.curTime)+"</font>";  
       htmlMsg=userMsg+"<br/>    "+htmlMsg;    
       if(htmlMsg!="") {  
           if($("#chatMsg").html()!=""){  
                if(message.isSysMsg=="1")    
                    $("#chatMsg").append("<br/><div style='text-align:center'><font color='gray'>"+htmlMsg+"</div>");  
                else      
                    $("#chatMsg").append("<br/><font color='blue'>"+htmlMsg);     
           }else {  
                if(message.isSysMsg=="1")    
                    $("#chatMsg").append("<div style='text-align:center'><font color='gray'>"+htmlMsg+"</div>");  
                else  
                    $("#chatMsg").append("<font color='blue'>"+htmlMsg);     
           }  
                 
            $("#chatMsg")[0].scrollTop=$("#chatMsg")[0].scrollHeight;      
       }   
} 

//发送信息
function sendMessage(textMsg){  
    var chatCont=document.getElementById("chatmsgtext").value;
    if(isSysMsg=="1"){  
        chatCont="<font color='gray'>"+textMsg+"聊天室</font>";  
    }  
    stompClient.send("/app/userChat",{},JSON.stringify({  
        'roomid':encodeURIComponent(roomid),  
        'userName':encodeURIComponent(username),  
        'deptName':encodeURIComponent(deptSortName),  
        'chatContent':encodeURIComponent(chatCont),     
        'isSysMsg':encodeURIComponent(isSysMsg)  
    }))  
} 
</script>
</html>