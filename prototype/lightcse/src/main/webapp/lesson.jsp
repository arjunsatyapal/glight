<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<!DOCTYPE html>
<html>
<head>
<title>Review</title>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
<style>
html,body {
	margin: 0px;
	padding: 0px;
	font-size: small;
	font-family: arial,sans-serif;
	color: black;
	height: 100%;
}
#header {
	background: #F1F1F1;
	background: -webkit-gradient(radial,100 36,0,100 -40,120,from(#FAFAFA),to(#F1F1F1)),#F1F1F1
	border-bottom: 1px solid #666;
	border-color: #E5E5E5;
	width: 100%;
	color: #666;
	padding-top: 10px;
	padding-bottom: 10px;
	text-align: center;
	height: 30px;
}

.button {
	background-color: #4D90FE;
	background-image: -webkit-gradient(linear,left top,left bottom,from(#4d90fe),to(#4787ed));
	background-image: -webkit-linear-gradient(top,#4d90fe,#4787ed);
	background-image: -moz-linear-gradient(top,#4d90fe,#4787ed);
	background-image: -ms-linear-gradient(top,#4d90fe,#4787ed);
	background-image: -o-linear-gradient(top,#4d90fe,#4787ed);
	background-image: linear-gradient(top,#4d90fe,#4787ed);
	filter: progid:DXImageTransform.Microsoft.gradient(startColorStr='#4d90fe',EndColorStr='#4787ed');
	border: 1px solid #3079ED;
	-moz-border-radius: 2px;
	-webkit-border-radius: 2px;
	border-radius: 2px;
	-moz-user-select: none;
	-webkit-user-select: none;
	color: white;
	cursor: default;
	font-size: 14px;
	font-weight: bold;
	height: 29px;
	min-width: 54px;
	text-align: center;
	padding: 0 8px;
	line-height: 31px;
	display: inline-block;
}


html {
	overflow-y: hidden;
}
#header, #content, iframe {
	position: absolute;
	width: 100%;
	left: 0;
}
#header {
	top: 0;
}
#content {
	top: 50px;
	bottom: 0px;
}
iframe {
	border: 0 none;
	height: 100%;
}

.button.disabled {
	opacity: 0.5;
}
#title {
	font-size: 18px;
}

</style>

<script>
$(document).ready(function(){
	var lesson = ${lesson};
	
	$("#title").text(lesson.name);
	
	if(lesson.links.length == 0)
		return;
	
	var pos = 0;
	var next = $("#next");
	var prev = $("#prev");
	var frame = $("#frame");
	
	function adjustUI() {
		prev.toggleClass("disabled", pos==0);
		next.toggleClass("disabled", pos==lesson.links.length-1);
		frame.attr("src",lesson.links[pos]);
		$("#title").text(lesson.name+" ("+(pos+1)+"/"+lesson.links.length+")");
	}
	adjustUI();
	
	$("#next").click(function() {
		if($(this).hasClass("disabled")) return;
		pos++;
		adjustUI();
	})
	$("#prev").click(function() {
		if($(this).hasClass("disabled")) return;
		pos--;
		adjustUI();
	})
	
});
</script>

</head>
<body>

<div id="header"><div id="prev" class="button">Previous</div> <span id="title"></span> <div id="next" class="button">Next</div></div>
<div id="content">
<iframe id="frame" frameborder="0"></iframe>
</div>

</body>
</html>
