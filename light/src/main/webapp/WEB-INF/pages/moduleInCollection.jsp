<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>${moduleTitle}</title>
    <link type="text/css" rel="stylesheet" href="/css/base.css">
    ${preload}
    <script src="/js/light/build/loader.js"></script>
    <link type="text/css" rel="stylesheet" href="/css/servebase.css">
    <style>
    .navigationBar {
        text-align: center;
        padding: 10px;
    }<c:if test="${usesIframe == 'true'}">
    .moduleTitle {
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      height: 1.5em;
    }
    .moduleContent div {
        height: 100%;
        width: 100%;
    }
    .moduleContent {
        margin: -4px auto;
        position: absolute;
        top: 4em;
        left: 0;
        right: 0;
        bottom: 40px;
        max-width: 55em;
    }
    .moduleContent iframe {
        border: 0px;
    }
    .navigationBar {
        position: absolute;
        left: 0;
        right: 0;
        bottom: 5px;
    }</c:if>
    </style>
  </head>
  <body>
    <div class="outerContainer">
        <div class="innerContainer">
            <h1 class="moduleTitle hiddenNode">${moduleTitle}</h1>
            <div class="moduleContent">${moduleContent}</div>
            <div class="navigationBar hiddenNode">${navigationBar}</div>
        </div>
    </div>
  </body>
</html>