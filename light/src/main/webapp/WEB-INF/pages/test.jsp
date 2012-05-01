<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN"
           "http://www.w3.org/TR/html4/strict.dtd">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <title>Light</title>
    <link href="/js/external/djk/dojo/resources/dojo.css" type="text/css" rel="stylesheet" media="screen">
    <link href="/js/external/djk/dijit/themes/claro/claro.css" type="text/css" rel="stylesheet" media="screen">
    <script>
    dojoConfig = {
      async: true,
      isDebug: true,
      waitSeconds: 2,
      has: {
        'light-dev': 1
      },
      deferredOnError: function(e) { console.log(e.message, e.stack); },
        packages: [{
            name: 'light',
            location: '/js/light'
        },
        { name: 'dojo', location: '/js/external/djk/dojo' },
        { name: 'dijit', location: '/js/external/djk/dijit' },
        { name: 'dojox', location: '/js/external/djk/dojox' }
        ]
    };
    </script>
    <script src="/js/external/djk/dojo/dojo.js"></script>
    <script>
    var tester = {
      _nextStep: null,
      _steps: [],
      _currentRunNumber: 0,
      _stopAtRun: 0,
      setSteps: function(steps) {
        this._steps = steps;
      },
      run: function() {
        if(this._steps.length == 0)
            throw new Error('Steps list must be non empty');
        this._nextStep = 0;
        this._currentRunNumber = 0;
        this._stopAtRun = 0;
        this._runStep();
      },
      _runStep: function() {
        if(this._nextStep == 0) {
            this._currentRunNumber++;
            if(this._stopAtRun == this._currentRunNumber)
                return;
        }
        this._steps[this._nextStep]();
        this._nextStep=(this._nextStep+1)%this._steps.length;
        var self = this;
        setTimeout(function() { self._runStep() }, 100);
      },
      stop: function() {
        this._stopAtRun = this._currentRunNumber+1;
        return this._currentRunNumber;
      }
    };
    
    // Dialog Test
    /*
    require(['light/utils/DialogUtils'], function(DialogUtils) {
        var dialog = null;
        tester.setSteps([
            function() {
                //console.log('A');
                dialog = new DialogUtils._PromptDialog();
                document.body.appendChild(dialog.domNode);
                dialog.show();
            },
            function() {
                //console.log('B');
                dialog._onClickCancel();
                dialog = null;
            }
        ]);
    });
    // */
    
    // Search View Test
    require(['light/views/SearchResultListView'], function(SearchResultListView) {
        var req = {
                query: 'additionx',
                page: 2,
                clientLanguageCode: 'en-us'
        };
        var data = {
          "items" : [ {
            "title" : "Recitation Transcript – Orthogonal Vectors and Subspaces",
            "description" : "In <b>addition, x</b> must also be orthogonal to 1, 3, 2, 2. So any vector x that&#39;s an S <br>  perp must be orthogonal to both of these vectors. So what we can do is we can <b>...</b>",
            "link" : "http://ocw.mit.edu/courses/mathematics/18-06sc-linear-algebra-fall-2011/least-squares-determinants-and-eigenvalues/orthogonal-vectors-and-subspaces/MIT18_06SC_110706_D1_300k-mp4.pdf"
          }, {
            "title" : "2: Reactions to Know",
            "description" : "(anti <b>addition. X</b>=Br or Cl). ROH. X. 2. Reduction of Alkynes. - cis-Alkene <br>  formation. H2. H. H. (cis product formed). Lindlar catalyst. - trans-Alkene <br>  formation. Na <b>...</b>",
            "link" : "http://ocw.mit.edu/courses/chemistry/5-13-organic-chemistry-ii-fall-2006/lecture-notes/jw_2.pdf"
          }, {
            "title" : "Basic Properties of Real Numbers: Symbols and Notations",
            "description" : "May 31, 2009 <b>...</b> Symbols of Operation If we let x and y each represent a number, we have the <br>  following notations: <b>Addition x</b> + y Subtraction x - y Multiplication x <b>...</b>",
            "link" : "http://cnx.org/content/m18872/1.5/source"
          }, {
            "title" : "A Bilinear Form for the DFT 1.3 2008/10/07 12:16:28 GMT-5 2008/11 <b>...</b>",
            "description" : "Nov 7, 2008 <b>...</b> The <b>addition x</b> ( 0 ) to each of the elements of also requires only one complex <br>  addition. By adding x ( 0 ) to the first element of C t R - t P J Q s w <b>...</b>",
            "link" : "http://cnx.org/content/m18134/1.3/source"
          }, {
            "title" : "2.035: Midterm Exam - Part 1 Spring 2007 SOLUTION",
            "description" : "where x1 and x2 range over all real numbers; let. 0 0 o = 0 0 be the null vector; <br>  and define <b>addition, x</b> + y, and scalar multiplication, αx, in the natural way by <b>...</b>",
            "link" : "http://ocw.mit.edu/courses/mechanical-engineering/2-035-special-topics-in-mathematics-with-applications-linear-algebra-and-the-calculus-of-variations-spring-2007/exams/midterm_1_sol.pdf"
          }, {
            "title" : "Understanding Parallelism - Dependencies",
            "description" : "Aug 25, 2010 <b>...</b> For instance, those tuples that represent binary operations, such as <b>addition ( X</b>=<br>  A+B ), form a portion of the DAG with two inputs ( A and B ) <b>...</b>",
            "link" : "http://cnx.org/content/m32777/1.3/"
          }, {
            "title" : "18.727 Topics in Algebraic Geometry: Algebraic Surfaces",
            "description" : "Note that H2(X, TX ) = 0: in <b>addition, X</b> is projective and H2(X, OX ) = 0, so by <br>  SGA1, Theorem III, 7.3, there is a smooth projective morphism f : U → V = Spec (A<br>  ) <b>...</b>",
            "link" : "http://ocw.mit.edu/courses/mathematics/18-727-topics-in-algebraic-geometry-algebraic-surfaces-spring-2008/lecture-notes/lect10.pdf"
          }, {
            "title" : "8.21 The Physics of Energy",
            "description" : "<b>Addition: (x</b> + iy)+(a + ib)=(x + a) + i(y + b). Multiplication: (x + iy) × (a + ib)=(xa − <br>  yb) + i(ya + xb). (reiθ)(seiψ) = rsei(θ+ψ). Complex conjugation: ¯z = z∗ = x − iy <b>...</b>",
            "link" : "http://ocw.mit.edu/courses/physics/8-21-the-physics-of-energy-fall-2009/lecture-notes/MIT8_21s09_lec06.pdf"
          }, {
            "title" : "Pointers and Arrays",
            "description" : "Jul 7, 2009 <b>...</b> *ptr *= 2.5; // Multiply x by 2.5. y = *ptr + 0.5; // Assign y the result of the <b>addition x</b> <br>  + 0.5. Do not confuse the asterisk (*) in a pointer declaration <b>...</b>",
            "link" : "http://cnx.org/content/m27769/latest/?collection=col10776/latest"
          }, {
            "title" : "Properties",
            "description" : "addition, subtraction, multiplication, and division. If we let x and y each represent <br>  a number, we have the following notations: <b>Addition x</b> + y. Subtraction x — y <b>...</b>",
            "link" : "http://cnx.org/content/m18872/1.1/01%20Chapter.pdf"
          } ],
          "hasNextPage" : true,
          "suggestion" : "<b><i>additions</i></b>",
          "suggestionQuery" : "additions"
        };
        /*
        var view = new SearchResultListView();
        document.body.appendChild(view.domNode);
        tester.setSteps([
            function() {
                view.show(req, data);
            },
            function() {
                view.clear();
            }
        ]);*/
        
        var view = null;
        tester.setSteps([
            function() {
                view = new SearchResultListView();
                document.body.appendChild(view.domNode);
                view.show(req, data);
            },
            function() {
                view.destroyRecursive();
                view = null;
            }
        ]);
    });
    
    require(['light/widgets/ListWidget', 'light/utils/TemplateUtils', 'dojo/domReady!'], function(ListWidget, TemplateUtils) {
        var listNode = document.createElement('div');
        document.body.appendChild(listNode);
        var listWidget = new ListWidget(listNode, {
          accept: [],
          type: 'SomeSourceType',
          selfAccept: false,
          copyOnly: true,
          selfCopy: false,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom('<div><a href="\${link}">\${title}</a>\${desc}</div>', item);
            console.log(node);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['someItemType']
            };
          }
        });
        
        listWidget.insertNodes(false /* addSelected */, [
            {title: 'Foo 1', desc: 'Bar 1', link: 'http://foobar.com/1'},
            {title: 'Foo 2', desc: 'Bar 2', link: 'http://foobar.com/2'},
            {title: 'Foo 3', desc: 'Bar 3', link: 'http://foobar.com/3'}
        ]);
    });
    </script>
  </head>
  <body class="claro">
  </body>
</html>