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
    #searchHeader {
        position: relative;
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
    #searchHeader .dijitTextBox {
        font-size: 20px;
    }
    #searchHeader {
        font-size: 18px;
    }
    #loginToolbar {
        position: absolute;
        right: 10px;
    }
    body[dir="rtl"] #loginToolbar {
        left: 10px;
        right: auto;
    }
    
    .searchResult:hover {
        background-color: #F1F1F1;
    }
    
    .searchResult {
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
        height:100%;
    }
    #content > * {
        min-width: 700px;
    }
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
    </style>
    <!--<link rel="stylesheet/less" type="text/css" href="/css/search.less">
    <script src="/js/external/less-1.3.0.min.js" type="text/javascript"></script>-->
  </head>
  <body>
    <div id="sidebar">There will be a collections editor here. Please, use your imagination :D</div>
    <div id="content">
        <div id="searchHeader">
            <div id="loginToolbar"></div>
            <div id="searchBar"></div>
        </div>
        <div id="searchResults"></div>
    </div>
  </body>
</html>