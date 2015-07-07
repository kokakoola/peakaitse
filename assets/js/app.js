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

    updateChart('#chart', 'kwh');

    setMomentLocaleEt();
});

//http://jsfiddle.net/ha3L5z3b/
    var grad2 =
        '<linearGradient id="grad2" x1="100%" y1="0%" x2="8%" y2="100%" gradientUnits="objectBoundingBox">'+
        '  <stop offset="4%" style="stop-color:rgb(241, 251, 255); stop-opacity:1"></stop>'+
        '  <stop offset="46%" style="stop-color:rgb(0, 174, 230); stop-opacity:1"></stop>'+
        '  <stop offset="98%" style="stop-color:rgb(230, 249, 255); stop-opacity:1"></stop>'+
        '</linearGradient>';

var updateChart = function(chartId, unit){
    function getChartData(){
        $.ajax({
            type: "POST",
            url: '../data/test.json',
            dataType: 'json',
            method: 'GET'
        })
        .done(function(data){
            console.log(data);
            onDataReceived(data);
        })
        .fail(function(jqXHR, status){
            console.log('Ajax Error Triggered : ' + status);
            console.log('Error: ' + jqXHR);
        });
    }

    getChartData();

    function onDataReceived(seriesCallback){
        var chart = c3.generate({
            bindto: chartId,
            padding: {
                bottom: 100
            },
            data: {
                x: 'x',
                columns: [],
                type: 'area-spline'
            },
            axis: {
                x: {
                    type: 'category',
                    tick: {
                        culling: true,
                    }
                }
            },
            grid: {
                y: {
                    show: true,
                    lines: [
                        {value: 0.5, text: 'Lable 16 for y'}
                    ]
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

        var seriesGraphReadings = seriesCallback.graphReadings;
        var seriesLength = seriesGraphReadings.length;
        if (seriesLength > 0) {
            var time_array = ['x'];
            switch(unit) {
                case 'kwh':
                    var data_array = ['kwh'];
                    for (var i = 0; i<seriesLength; i++) {
                        var data = seriesGraphReadings[i].kWh;
                        var unix_timestamp = Date.parse(seriesGraphReadings[i].time)/1000;
                        time_array.push(moment.unix(unix_timestamp).utc().format('ll HH:mm'));
                        data_array.push(data);
                    }
                break;
                case '1F':
                    var data_array = ['1F'];
                    for (var i = 0; i<seriesLength; i++) {
                        var data = seriesGraphReadings[i].A1;
                        var unix_timestamp = Date.parse(seriesGraphReadings[i].time)/1000;
                        time_array.push(moment.unix(unix_timestamp).utc().format('ll HH:mm'));
                        data_array.push(data);
                    }
                    chart.axis.range({max: {y: 16}, min: {y: 0}});
                break;
                case '3F':
                    var data_array = ['3F'];
                    for (var i = 0; i<seriesLength; i++) {
                        var data = seriesGraphReadings[i].A3;
                        var unix_timestamp = Date.parse(seriesGraphReadings[i].time)/1000;
                        time_array.push(moment.unix(unix_timestamp).utc().format('ll HH:mm'));
                        data_array.push(data);
                    }
                    chart.axis.range({max: {y: 16}, min: {y: 0}});
                break;
            }
            console.log(time_array);
            console.log(data_array);
            chart.load({
                columns: [
                    time_array,
                    data_array
                ]
            });
        } else {
            chart.load({
                columns: [0,0]
            });
        }

        // var fuseLength = seriesCallback.fuseValues.length;
        // if (fuseLength > 0) {
        //     var y_array = ['y_values'];
        //     for (var i = 0; i < fuseLength; i++) {
        //         y_array.push(seriesCallback.fuseValues[i]);
        //     };
        //     console.log(y_array);
        //     chart.load({
        //         columns: [
        //             y_array
        //         ]
        //     });
        //     chart.data.axes({
        //         y_values: 'y'
        //     });
        // }

        // hideChartLoader();
    }
};

$('.js-changeUnit').on('click', function () {
    var $this = $(this);
    var unit = $this.data('unit');
    updateChart('#chart', unit);
});

function et__processRelativeTime(number, withoutSuffix, key, isFuture) {
    var format = {
        's' : ['mõne sekundi', 'mõni sekund', 'paar sekundit'],
        'm' : ['ühe minuti', 'üks minut'],
        'mm': [number + ' minuti', number + ' minutit'],
        'h' : ['ühe tunni', 'tund aega', 'üks tund'],
        'hh': [number + ' tunni', number + ' tundi'],
        'd' : ['ühe päeva', 'üks päev'],
        'M' : ['kuu aja', 'kuu aega', 'üks kuu'],
        'MM': [number + ' kuu', number + ' kuud'],
        'y' : ['ühe aasta', 'aasta', 'üks aasta'],
        'yy': [number + ' aasta', number + ' aastat']
    };
    if (withoutSuffix) {
        return format[key][2] ? format[key][2] : format[key][1];
    }
    return isFuture ? format[key][0] : format[key][1];
}

function setMomentLocaleEt() {
    moment.locale('et', {
        months        : 'jaanuar_veebruar_märts_aprill_mai_juuni_juuli_august_september_oktoober_november_detsember'.split('_'),
        monthsShort   : 'jaan_veebr_märts_apr_mai_juuni_juuli_aug_sept_okt_nov_dets'.split('_'),
        weekdays      : 'pühapäev_esmaspäev_teisipäev_kolmapäev_neljapäev_reede_laupäev'.split('_'),
        weekdaysShort : 'P_E_T_K_N_R_L'.split('_'),
        weekdaysMin   : 'P_E_T_K_N_R_L'.split('_'),
        longDateFormat : {
            LT   : 'H:mm',
            LTS : 'LT:ss',
            L    : 'DD.MM.YYYY',
            LL   : 'D. MMMM YYYY',
            LLL  : 'D. MMMM YYYY LT',
            LLLL : 'dddd, D. MMMM YYYY LT'
        },
        calendar : {
            sameDay  : '[Täna,] LT',
            nextDay  : '[Homme,] LT',
            nextWeek : '[Järgmine] dddd LT',
            lastDay  : '[Eile,] LT',
            lastWeek : '[Eelmine] dddd LT',
            sameElse : 'L'
        },
        relativeTime : {
            future : '%s pärast',
            past   : '%s tagasi',
            s      : et__processRelativeTime,
            m      : et__processRelativeTime,
            mm     : et__processRelativeTime,
            h      : et__processRelativeTime,
            hh     : et__processRelativeTime,
            d      : et__processRelativeTime,
            dd     : '%d päeva',
            M      : et__processRelativeTime,
            MM     : et__processRelativeTime,
            y      : et__processRelativeTime,
            yy     : et__processRelativeTime
        },
        ordinalParse: /\d{1,2}\./,
        ordinal : '%d.',
        week : {
            dow : 1, // Monday is the first day of the week.
            doy : 4  // The week that contains Jan 4th is the first week of the year.
        }
    });
}
