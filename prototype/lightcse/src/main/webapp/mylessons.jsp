<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<!DOCTYPE html>
<html>
<head>
<title>Light</title>
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
#content, #search_results {
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

#lessons {
	padding: 5px;
}

#lessons > div:hover {
	background-color: #F1F1F1;
}

#lessons > div {
	margin: 2px;
	padding: 8px;
	border-bottom: 2px solid #F1F1F1;
	border-right: 2px solid #F1F1F1;
	font-size: 18px;
}

</style>
</head>
<body>
<div id="header"><form method="GET" action="/search">Look for materials <input type="text" value="${query}" id="search_query" name="q" /> <input type="submit" class="search_button" value="Search" /></form></div>
<div id="content"><c:if test="${lessonsCount > 0 }">
<div id="lessons">
<c:forEach var="lesson" items="${lessons}">
<div><a href="/lesson?id=${lesson.id}">${lesson.name}</a><br/>Link for students: ${baseUrl}/lesson?id=${lesson.id}</div>
</c:forEach>

</div></c:if>
<c:if test="${lessonsCount == 0 }">No Lessons</c:if>
</div>

</body>
</html>
