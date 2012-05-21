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
    <link type="text/css" rel="stylesheet" href="/css/servebase.css"><c:if test="${usesIframe == 'true'}">
    <style>
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
        margin: 0px auto;
        position: absolute;
        top: 4em;
        left: 0;
        right: 0;
        bottom: 4px;
        max-width: 55em;
    }
    .moduleContent iframe {
        border: 0px;
    }</style></c:if>
  </head>
  <body>
    <div class="outerContainer">
        <div class="innerContainer">
            <h1 class="moduleTitle">${moduleTitle}</h1>
            <div class="moduleContent">${moduleContent}</div>
        </div>
    </div>
  </body>
</html>