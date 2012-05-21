<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>${collectionTitle}</title>
    <link type="text/css" rel="stylesheet" href="/css/base.css">
    ${preload}
    <script src="/js/light/build/loader.js"></script>
    <link type="text/css" rel="stylesheet" href="/css/servebase.css">
    <style>
    .collectionContent ol {
        list-style-type: none;
    }
    </style>
  </head>
  <body>
    <div class="outerContainer">
        <div class="innerContainer hiddenNode">
            <h1 class="collectionTitle">${collectionTitle}</h1>
            <div class="collectionContent">${collectionContent}</div>
        </div>
    </div>
  </body>
</html>