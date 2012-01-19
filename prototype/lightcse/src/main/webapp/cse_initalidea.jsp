<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<!DOCTYPE html>
<html>
<head>
<title>CSE Test</title>
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
#content, #lesson, #search_results {
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

.search_button {
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
#search_query {
	display: inline-block;
	height: 27px;
	
	width: 250px;
	font-size: 16px;
	border: solid 1px gray;
	background-color: white;
	margin: 1px;
}

#search_results {
	float: right;
	display: none;
}
#lesson {
	width: 100%;
	padding-top: 1px;
}

#search_results > div:hover, #lesson > div:hover {
	
}

#search_results > div, #lesson > div {
	margin: 2px;
	padding: 8px;
	border-bottom: 2px solid #F1F1F1;
	border-right: 2px solid #F1F1F1;
	font-size: 18px;
}

#content.show_results > #search_results, #content.show_results > #lesson {
	display: block;
	width: 50%;
}

</style>
</head>
<body>
<!--<div id="cse" style="width: 100%;">Loading</div>
<script src="http://www.google.com/jsapi" type="text/javascript"></script>
<script type="text/javascript"> 
  google.load('search', '1', {language : 'en'});
  google.setOnLoadCallback(function() {
    var customSearchOptions = {};
    var customSearchControl = new google.search.CustomSearchControl(
      '001369170667164983739:xqelsiji0y8', customSearchOptions);
    customSearchControl.setResultSetSize(google.search.Search.FILTERED_CSE_RESULTSET);
    customSearchControl.draw('cse');
  }, true);
</script>
<link rel="stylesheet" href="http://www.google.com/cse/style/look/default.css" type="text/css" />-->

<script>
/*$(document).ready(function() {
	$("search_button").click(
		$("content").removeClass("show_results");
		$("content").empty();
	);
});*/
</script>

<div id="header"><form method="GET">Look for materials <input type="text" id="search_query" name="q" /> <input type="submit" class="search_button" value="Search" /></form></div>
<div id="content" <c:if test="${searchResultsCount > 0 }"> class="show_results">
<div id="search_results">
<c:forEach var="searchResult" items="${searchResults}">
<div><a href="${searchResult.link}">${searchResult.title}</a><br/>${searchResult.description}</div>
</c:forEach>
</div</c:if>>
<div id="lesson">
<div>Module 1</div>
<div>Module 2</div>
<div>Module 3</div>
<div>Module 4</div>
</div>
</div>

</body>
</html>
