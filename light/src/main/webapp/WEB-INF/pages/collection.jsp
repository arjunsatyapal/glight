<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>${collectionTitle}</title>
    <style>
    html, body {
        margin: 0;
        padding: 0;
        height: 100%;
    }
    body {
        font: 100% "Open Sans", sans-serif;
        background-color: #666;
    }
    .container {
        margin: -4px auto;
        padding: 2px 1em 1em 1em;
        max-width: 55em;
        background-color: white;
    }
    .collectionContent ol {
      counter-reset: section;
      list-style-type: none;
    }
    
    .collectionContent ol li { counter-increment: section; }
                
    .collectionContent ol li:before  { content: counters(section, ".") ". "; }
    </style>
  </head>
  <body>
    <div class="container">
        <h1 class="collectionTitle">${collectionTitle}</h1>
        <div class="collectionContent">${collectionContent}</div>
    </div>
  </body>
</html>