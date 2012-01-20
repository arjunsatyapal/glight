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

.dropdown {
	position: absolute;
	margin-top: 32px;
	border: 1px solid black;
	width: 150px;
	background-color: white;
	z-index:10;
	display: none;
}

.dropdownItem {
	color: black;
	text-align: left;
	padding: 3px;
	font-weight: normal;
}

.dropdownItem:hover {
	background-color: #F1F1F1;
}
</style>

<script>
$(document).ready(function(){
	function createCheckBoxCallback(lesson) {
		return function() {
			var target = $(this).is(":checked")?"/ajax/addLinkToLesson":"/ajax/removeLinkFromLesson";
			$.get(target,{
				lesson: lesson.id,
				link: "${jslink}"
			},function(data) {
				console.log("Done",target,data);
			});
		}
	}
	function addLessonToDropdown(lesson) {
		var item = $("<div/>").addClass("dropdownItem").appendTo(dropdown);
		item.append($("<input type=\"checkbox\" />").attr('checked', lesson.hasThisLink).change(createCheckBoxCallback(lesson)));
		item.append($("<span/>").text(lesson.name));
	}

	var lessons = ${lessons};
	var dropdown = $("<div />").addClass("dropdown").prependTo($("#add"));
	for(var i=0;i<lessons.length;i++) {
		addLessonToDropdown(lessons[i]);
	}
	
	
	$("#backToSearch").click(function() {
		window.location.href = "${backToSearchUrl}";
	});
	var timeout = null;
	
	function startHideProcess(interval) {
		if(timeout === null) {
			timeout = setTimeout(function() {
				dropdown.hide();
				timeout = null;
			}, interval);
		}
	}
	$("#add")
		.mouseenter(function() {
			dropdown.show();
			if(timeout !== null) {
				clearTimeout(timeout);
				timeout=null;
			}
		})
		.mouseleave(function() {
			startHideProcess(500);
		})
	
	var createNewLessonItem = $("<div/>").appendTo(dropdown).addClass("dropdownItem").text("Create New Lesson")
		.click(function() {
			var name = prompt("Enter the Lesson Name");
			if(name) {
				$.get("/ajax/createLesson",{
					name: name
				},function(lesson) {
					console.log("Done",lesson);
					addLessonToDropdown(lesson);
					createNewLessonItem.detach().appendTo(dropdown);
				});
			}
			
			startHideProcess(3000);
		});
	
});
</script>

</head>
<body>

<div id="header"><div id="backToSearch" class="button">Back to search</div> <div id="add" class="button">Add</div></div>
<div id="content">
<iframe src="${link}" frameborder="0"></iframe>
</div>

</body>
</html>
