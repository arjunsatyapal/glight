<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Light</title>
    ${preload}
    <script src="/js/light/build/loader.js"></script>
    <!-- TODO(waltercacau): move this into a CSS -->
    <style>
    #header {
        position: absolute;
        top: 0;
        background: #F1F1F1;
        background: -webkit-gradient(radial,100 36,0,100 -40,120,from(#FAFAFA),to(#F1F1F1)),#F1F1F1
        border-bottom: 1px solid #666;
        border-color: #E5E5E5;
        color: #666;
        padding-top: 10px;
        text-align: center;
        height: 50px;
    }
    #content {
        position: absolute;
        top: 60px;
        bottom: 0px;
    }
    body[dir="rtl"] #header, body[dir="rtl"] #content {
        right: 400px;
        left: 0px;
    }
    body[dir="ltr"] #header, body[dir="ltr"] #content {
        left: 400px;
        right: 0px;
    }
    #header .dijitTextBox {
        font-size: 20px;
    }
    #header {
        font-size: 18px;
    }
    #loginToolbar {
        position: absolute;
        top: 10px;
        right: 10px;
    }
    body[dir="rtl"] #loginToolbar {
        left: 10px;
        right: auto;
    }
    
    .searchResult:hover, .gdocListItem:hover {
        background-color: #F1F1F1;
    }
    
    .searchResult, .gdocListItem {
        margin: 2px;
        padding: 8px;
        border-bottom: 2px solid #F1F1F1;
        border-right: 2px solid #F1F1F1;
        font-size: 18px;
    }
    
    .searchInfo {
        font-size: 18px;
        text-align: center;
        padding: 5px;
    }
    
    body {
        font-size: 20px;
    }
    
    /* Sidebar CSS */
    html, body {
        height: 100%;
    }
    body{
        margin: 0;
        padding: 0;
        overflow: hidden;
    }
    #sidebar {
        width: 400px;
        height:100%;
        background-color: #E1E1E1;
    }
    body[dir="ltr"] #sidebar {
        float: left;
    }
    body[dir="rtl"] #sidebar {
        float: right;
    }
    #sidebar, #content {
        overflow: auto;
    }
    #content > *, #header {
        min-width: 700px;
    }
    .gdocListItem img {
        margin-right: 5px;
    }
    .gdocListItemNotSupported {
        text-decoration: line-through;
    }
    #importModule .dojoDndItemSelected, #importModule .dojoDndItemAnchor,
    #searchResults .dojoDndItemSelected, #searchResults .dojoDndItemAnchor {
      background-repeat: repeat-x;
      background-color: #cfe5fa;
      background-image: url("/js/external/djk/dijit/themes/claro/images/commonHighlight.png");
      border: solid 1px #759dc0;
      color: #000000;
    }
    /* Disabling scrollbar's in dijit tree because of dojo bug with drag & drop */
    .dijitTree {
        overflow: hidden;
    }
    /* Allow wrapping because we are not allowing horizontal scrollbar on the tree */
    .dijitTreeRow, .dijitTreeContent {
        white-space: normal;
    }
    
    .addCollectionButton {
        background-image: url("/js/external/djk/dijit/themes/claro/images/dnd.png");
        background-repeat: no-repeat;
        background-position: 0px 0px;
        width: 16px;
        height: 16px;
        margin: 4px;
    }
    body[dir="rtl"] .addCollectionButton {
        float: left;
    }
    body[dir="ltr"] .addCollectionButton {
        float: right;
    }
    
    #importModule {
        padding: 10px;
    }
    #importModule .firstForm {
        padding: 10px;
        text-align: center;
    }
    
    </style>
    <!--<link rel="stylesheet/less" type="text/css" href="/css/search.less">
    <script src="/js/external/less-1.3.0.min.js" type="text/javascript"></script>-->
  </head>
  <body>
    <div id="sidebar"></div>
    <div id="header">
        <div id="searchBar"></div>
        <div id="loginToolbar"></div>
    </div>
    <div id="content">
        <div id="searchResults"></div>
        <div id="importModule"></div>
        <div id="collectionEditor"></div>
    </div>
  </body>
</html>