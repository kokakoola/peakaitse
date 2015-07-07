[peakaitse](http://ux.netgroupdigital.com/peakaitse/) | [Documentation
table of contents](TOC.md)

# The JavaScript

Information about the default JavaScript included in the project. Note that we avoid mixing javascript with html - javascript has to be kept in separate file. As well we avoid mixing javascript and css classes. In general:

* id is for javascript, shall not be used in css. Use camelCase to make it obvious.
* class is for css. In case you need a class in javascript, prepend it with `js-` e.g. `.js-foo`.
* if a javascript and css class have to be used together, use prefix `is-` e.g. `.is-foo`.

Read more about keeping your css neat and tidy in [Scalable and Modular Architecture for CSS](https://smacss.com/)

## app.min.js

This file contains minified version of all javascript and needed plugins so far. We have provided an empty `custom.js` for you to get started with adding scripts if needed.

In case the project is bigger and you need more structure in javascript files, please follow the contention:

### main.js

This file can be used to contain or reference your site/app JavaScript code.
For larger projects, you can make use of a JavaScript module loader, like
[Require.js](http://requirejs.org/), to load any other scripts you need to
run.

### plugins.js

This file can be used to contain all your plugins, such as jQuery plugins and
other 3rd party scripts.

One approach is to put jQuery plugins inside of a `(function($){ ...
})(jQuery);` closure to make sure they're in the jQuery namespace safety
blanket. Read more about [jQuery plugin
authoring](https://learn.jquery.com/plugins/#Getting_Started).

By default the `plugins.js` file contains a small script to avoid `console`
errors in browsers that lack a `console`. The script will make sure that, if
a console method isn't available, that method will have the value of empty
function, thus, preventing the browser from throwing an error.


### vendor

This directory can be used to contain all 3rd party library code.

Minified versions of the latest jQuery and Modernizr libraries are included by
default. You may wish to create your own [custom Modernizr
build](http://www.modernizr.com/download/).
