// Avoid `console` errors in browsers that lack a console.
(function() {
    var method;
    var noop = function () {};
    var methods = [
        'assert', 'clear', 'count', 'debug', 'dir', 'dirxml', 'error',
        'exception', 'group', 'groupCollapsed', 'groupEnd', 'info', 'log',
        'markTimeline', 'profile', 'profileEnd', 'table', 'time', 'timeEnd',
        'timeline', 'timelineEnd', 'timeStamp', 'trace', 'warn'
    ];
    var length = methods.length;
    var console = (window.console = window.console || {});

    while (length--) {
        method = methods[length];

        // Only stub undefined methods.
        if (!console[method]) {
            console[method] = noop;
        }
    }
}());

/*!
 * IE10 viewport hack for Surface/desktop Windows 8 bug
 * Copyright 2014 Twitter, Inc.
 * Licensed under the Creative Commons Attribution 3.0 Unported License. For
 * details, see http://creativecommons.org/licenses/by/3.0/.
 */

// See the Getting Started docs for more information:
// http://getbootstrap.com/getting-started/#support-ie10-width

(function () {
    'use strict';
    if (navigator.userAgent.match(/IEMobile\/10\.0/)) {
        var msViewportStyle = document.createElement('style')
        msViewportStyle.appendChild(
            document.createTextNode(
                '@-ms-viewport{width:auto!important}'
            )
        )
        document.querySelector('head').appendChild(msViewportStyle)
    }
})();

$(function () {
    $('[data-toggle="tooltip"]').tooltip();

//http://jsfiddle.net/ha3L5z3b/
    var grad2 = 
        '<linearGradient id="grad2" x1="100%" y1="0%" x2="8%" y2="100%" gradientUnits="objectBoundingBox">'+
        '  <stop offset="4%" style="stop-color:rgb(241, 251, 255); stop-opacity:1"></stop>'+
        '  <stop offset="46%" style="stop-color:rgb(0, 174, 230); stop-opacity:1"></stop>'+
        '  <stop offset="98%" style="stop-color:rgb(230, 249, 255); stop-opacity:1"></stop>'+
        '</linearGradient>';

    var chart = c3.generate({
        bindto: '#chart',
        data: {
            x: 'x',
            url: '../data/quarter.json',
            mimeType: 'json',
            keys: {
                x: 'STARTTIME',
                value: ['ENERGY']
            },
            names: {
                'ENERGY': 'kWh'
            },
            type: 'area-spline'
            },
            size: {
              height: 600
            },
            color: {
                pattern: ['url(#grad2)']
            },
            // color: {
            //     ENERGY: 'url(#grad2)'
            // },
            oninit: function() {
                this.svg[0][0].getElementsByTagName('defs')[0].innerHTML += grad2;
            },
            axis: {
                x: {
                   type: 'category',
                    tick: {
                      culling: true,
                    }
                }
            },
            zoom: {
                enabled: true
            },
            subchart: {
                show: true
            },
            point: {
              show: false
            }
        });

});
