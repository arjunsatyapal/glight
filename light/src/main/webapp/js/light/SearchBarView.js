define(["dojo/_base/declare", "dijit/_Widget", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dojo/text!./SearchBarView.html", "dijit/form/TextBox", "dijit/form/Button"], 
        function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, template) {
    // , "dojo/text!./templates/SomeWidget.html"
    return declare("light.SearchBarView", [_Widget, _TemplatedMixin, _WidgetsInTemplateMixin], {
        templateString: template
    });
     
});