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

    updateC3Chart('#chart');
});

//http://jsfiddle.net/ha3L5z3b/
var grad2 =
    '<linearGradient id="grad2" x1="0%" y1="100%" x2="100%" y2="0%" >'+
    '  <stop offset="0%" style="stop-color:rgb(0,0,0);stop-opacity:1" />'+
    '  <stop offset="50%" style="stop-color:rgb(255,0,0);stop-opacity:1" />'+
    '  <stop offset="100%" style="stop-color:rgb(255,0,0);stop-opacity:0" />'+
    '</linearGradient>';

var updateC3Chart = function(chartId){
    console.log('updateC3Chart(' + chartId + ')');
    function onDataReceived(seriesCallback){
        console.log(seriesCallback);

        var chart = c3.generate({
            bindto: chartId,
            data: {
                x: 'x',
                columns: [],
                type: 'area-spline'
            },
            axis: {
                x: {
                    type: 'category',
                }
            },
            color: {
                pattern: ['url(#grad2)']
            },
            oninit: function() {
                this.svg[0][0].getElementsByTagName('defs')[0].innerHTML += grad2;
            },
            zoom: {
                enabled: true
            },
            subchart: {
                show: true
            },
            point: {
                show: false
            },
            size: {
                height: 600
            }
        });

        var seriesLength = seriesCallback.length;
        if (seriesLength > 0) {
            var time_array = ['x'];
            var kwh_array = ['kwh'];
            for (var i = 0; i<seriesLength; i++) {
                time_array.push(seriesCallback[i].time);
                kwh_array.push(seriesCallback[i].kWh);
            }
            console.log(time_array);
            console.log(kwh_array);
            chart.load({
                columns: [
                    time_array,
                    kwh_array
                ]
            });
        } else {
            chart.load({
                columns: [0,0]
            });
        }

        // hideChartLoader();
    }
    function getChartData(){
        $.ajax({
            type: "POST",
            url: '../data/test.json',
            dataType: 'json',
            method: 'GET'
        })
        .done(function(data){
            console.log(data.graphReadings);
            onDataReceived(data.graphReadings);
        })
        .fail(function(jqXHR, status){
            console.log('Ajax Error Triggered : ' + status);
            console.log('Error: ' + jqXHR);
        });
    }
    getChartData();
};
