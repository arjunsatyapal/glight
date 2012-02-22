(function() {
  var srcPrefix = "src/main/webapp/js/";
  var srcTestPrefix = "src/test/webapp/js/";
  var testPrepend = "test/";
  jstestdriver.AmdPlugin.rewriteModuleName = function(name) {
    // We need to overcome the fact that the JsTestDriver plugin for eclipse is outdated
    // and includes the fullpath of the resources when loading.
    //jstestdriver.console.log("Original name:", name);
    var srcIndex = name.indexOf(srcPrefix);
    var srcTestIndex = name.indexOf(srcTestPrefix);
    var cut = 0;
    var prepend = "";
    if(srcIndex != -1) {
      cut = srcIndex + srcPrefix.length;
    } else if(srcTestIndex != -1) {
      cut = srcTestIndex + srcTestPrefix.length;
      prepend = testPrepend;
    }
    name = prepend+name.substring(cut, name.length);
    
    jstestdriver.console.log("New name:", name);
    return name;
  }
  if(jstestdriver.JasmineAdapter) {
      var oldDescribe = describe.original || describe;
      describe = function() {
        var moduleName = jstestdriver.AmdPlugin.currentModuleName;
        if(jstestdriver.JasmineAdapter.isCollectMode() && moduleName && moduleName.indexOf(testPrepend) == 0)
          arguments[0] = moduleName.substring(testPrepend.length, moduleName.length)+"."+arguments[0];
        return oldDescribe.apply(this, arguments);
      }
      describe.original = oldDescribe;
    
  }
})();
define("jquery-ui", ["jquery"], function($) { return $; });