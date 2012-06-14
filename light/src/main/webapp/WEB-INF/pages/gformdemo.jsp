<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>GForm Demo</title>
    <link type="text/css" rel="stylesheet" href="/css/base.css">
    <link type="text/css" rel="stylesheet" href="/css/servebase.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
    <script>
/* http://stackoverflow.com/questions/1060008/is-there-a-way-to-detect-if-a-browser-window-is-not-currently-active */
(function() {
    if (/*@cc_on!@*/false) // IE 9 and lower
        document.onfocusin = document.onfocusout = onchange
    else
        window.onfocus = window.onblur = onchange;

    function onchange (evt) {
        var body = document.body;
        evt = evt || window.event;

        if (evt.type == "focus" || evt.type == "focusin")
            onWindowFocus();
        else if (evt.type == "blur" || evt.type == "focusout")
            onWindowBlur();
    }
})();

function onWindowFocus() {
}
function onWindowBlur() {
}

var lastOpenedWindow = null;
var lastFormUrl = null;
var poolTimeout = setTimeout(function() {}, 0);

function poolForOpenedWindowToClose() {
    if(lastOpenedWindow != null && lastOpenedWindow.closed) {
        lastOpenedWindow = null;
        hide($("#editingMessage"));
        if(lastFormUrl != null) {
            show($("#previewContainer"));
            $("#preview").attr("src", lastFormUrl);
        }
    }
    poolTimeout = setTimeout(poolForOpenedWindowToClose, 1000);
}

function createGForm() {
    lastFormUrl = null;
    hide($("#previewContainer"));
    if(lastOpenedWindow != null) {
        lastOpenedWindow.close();
    }
    lastOpenedWindow=window.open("/gform?action=create",'gform','width=700,height=600');
    clearTimeout(poolTimeout);
    poolForOpenedWindowToClose();
}

function hide(jq) {
    jq.css("display", "none");
}
function show(jq) {
    jq.css("display", "block");
}

function creationCallback(formUrl) {
    lastFormUrl = formUrl;
    show($("#editingMessage"));
}

    </script>
    <style>
        #editingMessage {
            padding-top: 10px;
            color: red;
         }
    </style>
  </head>
  <body class="visible">
    <div class="outerContainer">
        <div class="innerContainer">
            <h1 class="collectionTitle">GForm Demo</h1><br/>
            <div class="collectionContent">
            <c:choose>
                <c:when test="${isLogged == 'true'}">
                    Hello ${name}!
                    
                    <c:choose>
                        <c:when test="${hasCredentials == 'true'}">
                        
                        <br />
                        <br />
                        <button onclick="createGForm()">Create GForm</button>
                        
                        <div id="previewContainer" style="display: none">
                            <iframe id="preview" src="" width="760" height="572" frameborder="0" marginheight="0" marginwidth="0"></iframe>
                        </div>
                        <div id="editingMessage" style="display: none">You are currently editing a form. When you are done, just close the editor window and your form will appear here.</div>
                        </c:when>
                    <c:otherwise>
                        <a href="/oauth2/google_doc?redirectPath=/gform">Authorize GDoc Access</a>
                    </c:otherwise>
                    </c:choose>
                    
                </c:when>
                <c:otherwise>
                    <a href="/login/google?redirectPath=/gform">Click here to login first</a>
                </c:otherwise>
            </c:choose>
            </div>
        </div>
    </div>
  </body>
</html>