# Light Build system

## Introduction

This document will explain the basics behind Light's build system.
It was designed as being a hand's on document, so be prepared to get your hands dirty :D.

This file is formated using Markdown and you can see an HTML version
of it by using a converting tool, like Showdown.js:

<http://softwaremaniacs.org/playground/showdown-highlight/>

## Requirements

It is assumed that you are familiar with the following concepts/tools
(some links and short descriptions are provided for reference).

*   Asynchronous Module Definition (AMD): A modular format for structuring your javascript code
    that allows easy dependency management and code reuse.
    * <https://github.com/amdjs/amdjs-api/wiki/AMD>
    * <http://requirejs.org/docs/whyamd.html>
*   Google Closure Compiler: A javascript optimizer
    * <https://developers.google.com/closure/compiler/>
*   Dojo Toolkit: 
    * <http://dojotoolkit.org/>
*   Dojo has() feature:
    * <http://dojotoolkit.org/reference-guide/1.7/dojo/has.html>
*   Maven
    * <http://maven.apache.org/>

If you never heard about one of theses, spending some time getting familiar with their basic
usage will help you understanding the rest of this document. You don't really need to know
them profoundly to understand the rest of document, but at least know them.

## Terminology

Now let's get familiar with some terms that will be used:

*   Module: A javascript module using the AMD format.
*   Package: An AMD package. It is basically a top level group of modules. Example of packages
    are *light*, *dojo* and *dijit*.
*   Layer: A set of modules that are bundled together for reducing the number of HTTP requests
    needed during loading efficiency.

## Hand's on

### Concepts

Light's JS build system is based on Dojo's Build System which is hooked inside the Maven Build.
The build process can output two kinds of JS code:

*   Development code: An unminified and unbundled code that eases your work when debugging
    and developing.
*   Production code: A minified and bundled into layers code.

By default building the project will give you a production code inside
light/target/light-VERSION/js .

### Production code

Try building the project using the following command inside *light* folder:

    mvn clean package -DskipTests=true

If you open light/target/light-VERSION/js, you will see a folder named as *light* and other
folders containing Dojo specific code.

Inside *light/target/light-VERSION/js/light/build* you will see the js files that contains the
built layers of our code. These build layers are defined inside 
*light/src/main/javascript/light.profile.js*. You can find documentation about each built layer
in the comments of this file. Also note that each layer contains a uncompressed copy for
debugging purposes.

It's worth to notice that the actual result of the Dojo Build System is a optimized copy of all
sources from Dojo Toolkit and Light, but we actually only use the built layers and some
UI resources, which actually is just a small fraction of the result. For optimizing deployment, 
the maven build is configured to cleanup the result by deleting most of the unused files.
You can see the result of the build without the cleaning up by running:

    mvn clean package -P \!light-prod-cleanup -DskipTests=true

Note that as the number of files is pretty big, it might be possible you can't deploy Light
to Google Appengine without this cleaning up step. Also, the cleaning up process will speed up
your deployment because you will upload fewer files.

To see what gets cleaned up or not, look in the *light/pom.xml* for the profile
*light-prod-cleanup*.

### Development code

Try building the project using the following command inside *light* folder:

    mvn clean package -Dlight-dev=true -DskipTests=true

This will activate a maven profile that instead of calling Dojo's Build System, it will
emulate it by simply coping the Dojo's and Light's source files into the equivalent locations
as the production code but without bundling them nor minimizing.

This will allow developers to skip the optimization step during development for a faster
change code and check results cycle.
For more details on that, please refer to *light/README.md*.

### Bootstrapping

All light's page are configured to load the file */js/light/build/loader.js* . This file
should take care of bootstrapping and loading other layers. When using development code,
no layers are built, so technically there shouldn't be a /js/light/build/loader.js file.
What actually happens is when building the project using Maven with the -Dlight-dev=true
flag, Maven will copy the file *light/src/main/javascript/light/loader-dev.js*
to *light/target/light-VERSION/js/light/build/loader.js*. This fake loader file will
take care of loading the development code.

The actual code that gets triggered and decides which kind of code (Development or Production)
to load is *light/src/main/javascript/light/main/LoaderMain.js*. It uses the has() feature
to detect the 'light-dev' build flag.

### Mapping kinds of JS code to build/test stages

These three bullets summarize everything you need to know about when to use which kind of code:

*   Unit tests will always run with unminified code.
    
    There is no build flag on maven that will make your unit tests
    run against minified code because they directly access the src/ directory 
    and they are run before the Maven packaging phase

*   Integration Tests, DEV_SERVER, QA and PROD can run both on unminified code or minified code.
    
    It only depends on the built flag light-dev being set to true or not.
        
    Note: It might be possible you can't deploy on appengine the unminified code because
    it might just have too many files.

*   Before deploying, run your Integration Tests with minified code!
    These will make easier for you to find build system problems before going live.

### Gotchas

Here are a few gotchas that are not shown in the available documentation:

*   By using the packages approach to build a project using dojo build system, the layers that
    are compiled for each package are the layers specified by the .profile.js file linked from
    the package.json of each package that you specified in the .profile.js that you give to
    dojo build system. 
    
    That means if you create a .profile.js, say light.custom.profile.js to customize some build,
    you can't set any custom layers for the light package, because the package.json for the light
    package points to light.profile.js, where the layers are specified.

### References

Some interesting references used when trying to structure the build system:

* <https://github.com/csnover/dojo-boilerplate>
* <http://dojotoolkit.org/reference-guide/1.7/build/buildSystem.html#build-buildsystem>

