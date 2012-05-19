<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Light</title>
    <link type="text/css" rel="stylesheet" href="/css/base.css">
    ${preload}
    <script src="/js/light/build/loader.js"></script>
    <!-- TODO(waltercacau): move this into a CSS -->
    <style>
    #searchHeader {
        min-width: 700px;
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
        top: 10px;
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
    
    
    </style>
  </head>
  <body>
    <div id="searchHeader">
        <div id="searchBar"></div>
        <div id="loginToolbar"></div>
    </div>
    <div id="searchResults"></div>
  </body>
</html>