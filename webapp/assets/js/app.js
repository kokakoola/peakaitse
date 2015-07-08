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

(function () {
    'use strict';

    /*!
    * IE10 viewport hack for Surface/desktop Windows 8 bug
    * Copyright 2014 Twitter, Inc.
    * Licensed under the Creative Commons Attribution 3.0 Unported License. For
    * details, see http://creativecommons.org/licenses/by/3.0/.
    */

    // See the Getting Started docs for more information:
    // http://getbootstrap.com/getting-started/#support-ie10-width
    if (navigator.userAgent.match(/IEMobile\/10\.0/)) {
        var msViewportStyle = document.createElement('style')
        msViewportStyle.appendChild(
            document.createTextNode(
                '@-ms-viewport{width:auto!important}'
            )
        )
        document.querySelector('head').appendChild(msViewportStyle)
    }

    $(function () {
        $('[data-toggle="tooltip"]').tooltip();

        updateChart('#chart', '1F');

        setMomentLocaleEt();
    });

    //http://jsfiddle.net/ha3L5z3b/
    var blueGradient =
    '<linearGradient id="blueGradient" x1="100%" y1="0%" x2="100%" y2="100%" gradientUnits="objectBoundingBox">'+
    '  <stop offset="4%" style="stop-color:rgb(241, 251, 255); stop-opacity:0"></stop>'+
    '  <stop offset="46%" style="stop-color:rgb(0, 174, 230); stop-opacity:1"></stop>'+
    '  <stop offset="98%" style="stop-color:rgb(230, 249, 255); stop-opacity:0"></stop>'+
    '</linearGradient>';

    var updateChart = function(chartId, unit){
        function getChartData(){
            $.ajax({
                type: "POST",
                url: '../data/longtest.json',
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
            var seriesGraphReadings = seriesCallback.graphReadings;
            var seriesLength = seriesGraphReadings.length;
            var isZoomed = false;
            var isZoomedBigger = false;

            var chart = c3.generate({
                bindto: chartId,
                padding: {
                    bottom: 20
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
                            fit: false
                        }
                    }
                },
                grid: {
                    y: {
                        show: true
                    }
                },
                legend: {
                    show: false
                },
                color: {
                    pattern: ['url(#blueGradient)']
                },
                oninit: function() {
                    this.svg[0][0].getElementsByTagName('defs')[0].innerHTML += blueGradient;
                },
                zoom: {
                    enabled: true,
                    onzoom: function (domain) {
                        // console.log(domain[0]);
                        // console.log(domain[1]);
                        switch(unit) {
                            case '1F':
                                var domainDiff = domain[1] - domain[0];
                                console.log('domainDiff: ' +domainDiff);
                                // if domain difference is smaller than 35 eq 35 days then show full data chart
                                if (domainDiff < 35) {
                                    if (!isZoomed) {
                                        var time_array = ['x'];
                                        var data_array = ['1F'];
                                        generateAllFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, 'A1');
                                    }
                                    isZoomed = true;
                                    isZoomedBigger = false;
                                } else {
                                    if (!isZoomedBigger) {
                                        var time_array = ['x'];
                                        var data_array = ['1F'];
                                        generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, 'A1');
                                    }
                                    isZoomedBigger = true;
                                    isZoomed = false;
                                }
                            break;
                        }
                    }
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

            if (seriesLength > 0) {
                switch(unit) {
                    case 'kwh':
                        var time_array = ['x'];
                        var data_array = ['kWh'];
                        var day_array = [];
                        var date_str = seriesGraphReadings[0].time.slice(0,10);
                        for (var i = 0; i<seriesLength; i++) {

                            if (seriesGraphReadings[i].time.slice(0,10) != date_str) {
                                var data = 0;
                                var dayArrayLength = day_array.length;
                                // add up all days values
                                for (var j = 0; j< dayArrayLength; j++) {
                                    var int = parseFloat(day_array[j]) ? day_array[j] : 0;
                                    data += int;
                                }
                                // push to data array
                                data_array.push(data);
                                // parse unix timestamp
                                var unix_timestamp = Date.parse(seriesGraphReadings[i-1].time.slice(0,10))/1000;
                                // format date with moment and push to time array (x axis)
                                time_array.push(moment.unix(unix_timestamp).utc().format('ll'));
                                // make day array empty
                                day_array.splice(0, day_array.length);
                                // take next day
                                date_str = seriesGraphReadings[i].time.slice(0,10);
                            }

                            // push every kWh to specific day array
                            day_array.push(seriesGraphReadings[i].kWh);
                        }

                        // load x and y data to chart
                        console.log(time_array);
                        console.log(data_array);
                        chart.load({
                            columns: [
                                time_array,
                                data_array
                            ]
                        });
                    break;
                    case '1F':
                        var time_array = ['x'];
                        var data_array = ['1F'];

                        generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, 'A1');

                        generateChartOptions(chart, seriesLength, seriesCallback, time_array, data_array);
                    break;
                    case '3F':
                        var time_array = ['x'];
                        var data_array = ['3F'];
                        generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, 'A3');

                        generateChartOptions(chart, seriesLength, seriesCallback, time_array, data_array);
                    break;
                }
            } else {
                chart.load({
                    columns: [0,0]
                });
            }

            hideLoader($('#loader'));
        }
    };

    function generateAllFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, fuse) {
        console.log('generateAllFuseDataChart');
        chart.unload({
            ids: ['time', '1F', '3F']
        });
        for (var i = 0; i<seriesLength; i++) {
            var data;
            if (fuse == 'A1') {
                data = seriesGraphReadings[i].A1;
            } else if (fuse == 'A3') {
                data = seriesGraphReadings[i].A3;
            }
            var unix_timestamp = Date.parse(seriesGraphReadings[i].time)/1000;
            time_array.push(moment.unix(unix_timestamp).utc().format('HH:mm') + '     ' + moment.unix(unix_timestamp).utc().format('DD.MM'));
            data_array.push(data);
        }
        console.log(time_array);
        console.log(data_array);
        chart.load({
            columns: [
                time_array,
                data_array
            ]
        });
    }
    function generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, time_array, data_array, fuse) {
        console.log('generateAvgFuseDataChart');
        var day_array = [];
        var date_str = seriesGraphReadings[0].time.slice(0,10);
        for (var i = 0; i<seriesLength; i++) {

            if (seriesGraphReadings[i].time.slice(0,10) != date_str) {
                var data = 0;
                var dayArrayLength = day_array.length;
                // add up all days values
                for (var j = 0; j< dayArrayLength; j++) {
                    var int = parseFloat(day_array[j]) ? day_array[j] : 0;
                    data += int;
                }
                // get day avg
                var avg = data / dayArrayLength;
                // push to data array
                data_array.push(avg);
                // parse unix timestamp
                var unix_timestamp = Date.parse(seriesGraphReadings[i-1].time.slice(0,10))/1000;
                // format date with moment and push to time array (x axis)
                time_array.push(moment.unix(unix_timestamp).utc().format('ll'));
                // make day array empty
                day_array.splice(0, day_array.length);
                // take next day
                date_str = seriesGraphReadings[i].time.slice(0,10);
            }
            // push every 1F to specific day array
            if (fuse == 'A1') {
                day_array.push(seriesGraphReadings[i].A1);
            } else if (fuse == 'A3') {
                day_array.push(seriesGraphReadings[i].A3);
            }
        }

        // load x and y data to chart
        console.log(time_array);
        console.log(data_array);
        chart.load({
            columns: [
                time_array,
                data_array
            ]
        });
    }

    function generateChartOptions(chart, seriesLength, seriesCallback, time_array, data_array) {
        // add ygrid lines for recommended and required fuse
        if (seriesCallback.recommendedFuseSize == seriesCallback.calculatedMinFuseSize) {
            chart.ygrids.add([
                {value: seriesCallback.recommendedFuseSize, text: 'YAY! Atta boy ' + seriesCallback.recommendedFuseSize + 'A'}
            ]);
        } else {
            chart.ygrids.add([
                {value: seriesCallback.recommendedFuseSize, text: 'Soovituslik ' + seriesCallback.recommendedFuseSize + 'A'},
                {value: seriesCallback.calculatedMinFuseSize, text: 'Vajalik ' + seriesCallback.calculatedMinFuseSize + 'A'}
            ]);
        }

        // set regions colors for fuse values
        chart.regions.add([
            {axis: 'y', start: 0, end: 16, class: 'regionX'}
        ]);
        // remove first element from array because it's string
        data_array.shift();
        // take bigger fuse number
        var maxFuseNumber = Math.max(seriesCallback.recommendedFuseSize, seriesCallback.calculatedMinFuseSize);
        // get max fuse value from data array
        var maxFuseNumberFromDataArray = getMaxOfArray(data_array);
        var maxRange;
        if (maxFuseNumberFromDataArray < maxFuseNumber) {
            maxRange = maxFuseNumber;
        } else {
            maxRange = maxFuseNumberFromDataArray;
        }
        // set y axis range
        chart.axis.range({max: {y: maxRange}, min: {y: 0}});

        // set zoom domain
        var startpoint = seriesLength - (daysDifference(firstDayInPreviousMonth()));
        chart.zoom([startpoint, seriesLength]);
    }

    function getMaxOfArray(array) {
        return Math.max.apply(null, array);
    }

    function showLoader($loader) {
        $loader.show();
    }
    function hideLoader ($loader) {
        $loader.fadeOut('slow');
    }

    function firstDayInPreviousMonth() {
        var now = new Date();
        return new Date(now.getFullYear(), now.getMonth() - 1, 1);
    }

    function daysDifference (to) {
        var now = new Date();
        return Math.floor(( Date.parse(now) - Date.parse(to) ) / 86400000);
    }

    $('.js-changeUnit').on('click', function () {
        showLoader($('#loader'));
        var $this = $(this);
        $this.siblings().removeClass('active');
        $this.addClass('active');
        var unit = $this.data('unit');
        updateChart('#chart', unit);
    });

    $('.js-unZoom').on('click', function () {
        chart.unzoom();
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

})();
