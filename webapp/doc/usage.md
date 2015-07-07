[peakaitse](http://ux.netgroupdigital.com/peakaitse/) | [Documentation table of contents](TOC.md)

# Usage

Once you have cloned or downloaded myToyota UI, creating a site or app
usually involves the following:

1. Navigate to build catalogue.
2. Run your site locally to see how it looks.
3. Deploy your site.


## Basic structure

All the final files needed are located in build. The files outside build are for frontend use only.

A basic myToyota UI site from build catalogue initially looks something like this:

```
build
├── assets
│   └── css
│       ├── main.css
│       ├── main.min.css
│       └── custom.css
│   ├── fonts
│   ├── img
│   └── js
│       └── app.min.js
├── .editorconfig
├── bower.json
├── 404.html
├── apple-touch-icon.png
├── browserconfig.xml
├── index.html
├── humans.txt
├── robots.txt
├── crossdomain.xml
├── favicon.ico
├── tile-wide.png
└── tile.png
```

What follows is a general overview of each major part and how to use them.

### css

app.min.css - this file contains all your project's CSS. We have included an
initial CSS - custom.css - to help get you started with your own styling. Mixing html and css should be avoided - keep them in separate files. [About the
CSS](css.md).

### doc

This directory contains all the myToyota UI documentation.

### js

app.min.js - this file contains all your project's JS files. Additional libraries, plugins,
and custom code can all be included here. We have created an initial JS - custom.js - included into project where you can write your app-specific javascript. Mixing html and javascript should be avoided - keep them in separate files. [About the JavaScript](js.md).

### bower.json

We use [Bower](http://bower.io/) for external package management. [Install Bower](https://www.jetbrains.com/webstorm/help/using-bower-package-manager.html) and run `bower install` to get all the dependencies at once. The files in bower_components shall not be changed.

### 404.html

A helpful custom 404 to get you started.

### browserconfig.xml

This file contains all settings regarding custom tiles for IE11.

For more info on this topic, please refer to
[MSDN](https://msdn.microsoft.com/en-us/library/ie/dn455106.aspx).

### .editorconfig

The `.editorconfig` file is provided in order to encourage and help you and
your team to maintain consistent coding styles between different
editors and IDEs. [Read more about the `.editorconfig` file](misc.md#editorconfig).

### index.html

This is the default HTML skeleton that should form the basis of all pages on
your site. If you are using a server-side templating framework, then you will
need to integrate this starting HTML with your setup.

Make sure that you update the URLs for the referenced CSS and JavaScript if you
modify the directory structure at all.

If you are using Google Universal Analytics, make sure that you edit the
corresponding snippet at the bottom to include your analytics ID.

### humans.txt

Edit this file to include the team that worked on your site/app, and the
technology powering it.

### robots.txt

Edit this file to include any pages you need hidden from search engines.

### crossdomain.xml

A template for working with cross-domain requests. [About
crossdomain.xml](misc.md#crossdomainxml).

### Icons

Replace the default `favicon.ico`, `tile.png`, `tile-wide.png` and Apple
Touch Icon with your own.

If you want to use different Apple Touch Icons for different resolutions please
refer to the [according documentation](extend.md#apple-touch-icons).

You might want to check out Hans' handy [HTML5 Boilerplate Favicon and Apple
Touch Icon
PSD-Template](https://drublic.de/blog/html5-boilerplate-favicons-psd-template/).
