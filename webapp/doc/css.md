[peakaitse](http://ux.netgroupdigital.com/peakaitse/) | [Documentation table of contents](TOC.md)

# The CSS

Every developer can add/change design in empty `custom.css` what has been added to the project. Use that please and never write css into a html document. Mark your name at the section start for clarity and further questions.

We avoid mixing javascript and css classes. In general:

* id is for javascript, shall not be used in css. Use camelCase to make it obvious.
* class is for css. In case you need a class in javascript, prepend it with `js-` e.g. `.js-foo`.
* if a javascript and css class have to be used together, use prefix `is-` e.g. `.is-foo`.

Avoid using `important` rule if possible. Read more about keeping your css neat and tidy in [Scalable and Modular Architecture for CSS](https://smacss.com/)

myToyota UI's CSS includes:

* [Bootstrap.css](#bootstrapcss)
* [Media queries](#media-queries)
* [Icons and images](#icons-images)

## Bootstrap.css

The Bootstrap version in use is Bootstrap-sass 3.3.5.

You will find the documentation for css usage in [Bootstraps css-page](http://getbootstrap.com/css/) and [Bootstraps components page](http://getbootstrap.com/components/)
In order to make keep the file sizes minimal we will not always add all the css Bootstrap offers. If in doubt, wether a part of css is included or not, check the file app.scss: it gives you overview of all the css used in app.

It is very important that you understand the grid sytem of Bootstrap before starting.

## Media Queries

Bootstrap makes it easy for you to get started with a
[_mobile first_](http://www.lukew.com/presos/preso.asp?26) and [_responsive web
design_](http://www.alistapart.com/articles/responsive-web-design/) approach to
development.

We use bootstraps standard media queries and adjust them for special cases. We start mobile first, so our media query looks like: @media (min-width: 768px) - not max width.

## Icons and images

We use Bootstaps Glyphicons for icons - this is a font and can be handled as font. In case of special icons/logos we prefere .svg format as it is scalable on mobile and retina devices.

## Custom css

We amend Bootstrap css and add app-specific components as far as we can. Additionaly can every developer add/change design - for that purpose have we added an empty `custom.css` to the project. Use that please and never write css into a html document. Avoid using `important` rule if possible. Mark your name at the section start for clarity and further questions.
