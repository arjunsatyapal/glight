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
	padding: 5px;
}

#search_results > div:hover {
	background-color: #F1F1F1;
}

#search_results > div {
	margin: 2px;
	padding: 8px;
	border-bottom: 2px solid #F1F1F1;
	border-right: 2px solid #F1F1F1;
	font-size: 18px;
}

.pageInfo {
	font-size: 18px;
	text-align: center;
	padding: 5px;
}

#other {
	float:right;
	padding-right: 10px;
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
$(document).ready(function() {
	$("search_button").click(
		$("#mylessonsbutton").click(function() {
			window.location.href="/mylessons";
		})
	);
});

</script>

<div id="header">
	<div id="other"><div id="mylessonsbutton" class="button">My Lessons</div></div>
	<form method="GET" action="/search">Look for materials <input type="text" value="${query}" id="search_query" name="q" /> <input type="submit" class="button" value="Search" /></form>
	
</div>
<div id="content">

	<c:if test="${searchResultsCount > 0 }">
	<div id="search_results">
	<c:forEach var="searchResult" items="${searchResults}">
	<div><a href="${searchResult.link}">${searchResult.title}</a><br/>${searchResult.description}</div>
	</c:forEach>
	
	<div class="pageInfo">
	<c:if test="${hasPrevPage}"><a href="${prevPageLink}">Prev</a> | </c:if>
	Page ${page}
	<c:if test="${hasNextPage}"> | <a href="${nextPageLink}">Next</a></c:if>
	</div>
	
	</div></c:if>
	
	<c:if test="${showSuggestion}">
	<div class="pageInfo">
	Suggestion: <a href="${suggestionUrl}">${suggestion}</a>
	</div>
	</c:if>
	
	<c:if test="${showNoResultsMessage}">
	<div class="pageInfo">
	No results for this search
	</div>
	</c:if>

</div>

</body>
</html>
