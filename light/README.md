# Introduction

This document will show some tips and ticks to get started with Light project.

This file is formated using Markdown and you can see an HTML version
of it by using a converting tool, like Showdown.js:

<http://softwaremaniacs.org/playground/showdown-highlight/>

# Before starting
Before importing the project on eclipse, be sure to run:
	mvn clean package

If it fails because of tests, just ignore the tests initially
	mvn clean package -DskipTests=true

This will ensure the project has the right structure and all dependecies
already downloaded (including the Dojo Toolkit).

# Javascript development notes

## Unit Tests

For running automated unit tests using maven we are relying on Jasmine Maven
Plugin which uses Selenium Webdriver to start a browser and run the tests
in it. By default, the pom.xml is configured to use Google Chrome to run tests
and for that you will need to download and put in your path the ChromeDriver.
See the link below for more details:

<http://code.google.com/p/selenium/wiki/ChromeDriver#Getting_Started>

When developing the javascript code, you can use the following jasmine goal
to keep running your tests:

	mvn jasmine:bdd

It will start a server at http://localhost:8234/ and if you access it you
can run/rerun your javascript tests. You can change the 8234 port to whatever port
you want by adding in the maven command line the argument "-Djasmine.serverPort=PORT".

## Build profiles / JS Optimization

By default the maven profile light-prod is active, so during maven build
your javascript will be compiled and minimized. For development, that may not
be such a good thing and you can deactivate this profile and activate
the light-dev profile by using the following -Dlight-dev=true flag. Example:

	mvn clean package -Dlight-dev=true

The light-dev profile basically copies the raw javascript files and a 
development loader (src/main/javascript/light/loader-dev.js) to the supposed 
built loader location. This development loader instead of trying to access
built code, it will try to load the raw javascript files.

That will allow you to use browser's debbuggers
and also it will allow you to configure eclipse to automatically output
stuff from src/main/javascript/light into target/light-*/js/light, so you don't
need to restart the webserver. 

One simple and productive workflow would be:
1) Build the project using the light-dev profile:

	mvn clean package -Dlight-dev=true

2) Configure eclipse to output src/main/javascript/light into
   target/light-*/js/light

3) Start the development server without rebuilding the project.

	mvn gae:start -Dgae.wait=true

4) Edit files from the client side in eclipse, reload the page affected
   and have fun!

For more information about the build system, please refer to *light/README.BUILD.md*.
