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

        getChartData();

        // setMomentLocaleEt();
    });

    $(function() {
      var toGo = $('#js-toGo');
      var toCome = $('#js-toCome');
      var button = $('#js-Next');

      button.on('click', function(event){
        event.preventDefault();
        toGo.hide();
        toCome.addClass('animated bounceInRight').removeClass('hide');
      });
    });

    $(function() {
      var toGo = $('#js-goOnMobileID');
      var toCome = $('#js-comeOnMobileID');
      var button = $('#js-codeSent');

      button.on('click', function(event){
        event.preventDefault();
        toGo.hide();
        toCome.addClass('animated bounceInRight').removeClass('hide');
      });
    });

    var storeData;
    var chart;
    var unit = 'kWh';

    // change address and get new data
    $('.js-addressList').on('change', function () {
        var eic = $(this).val();
        getChartData(eic);
    });

    function getChartData(eic){
        $.ajax({
            // url: '../data/longtest.json',
            url: 'data/longtest.json', // ux.netgroupdigital.com/peakaitse url uses GET
            // url: 'https://xenon.netgroupdigital.com:8443/mainfuse/data', // uses POST method
            data: { eic: eic },
            dataType: 'json',
            type: 'GET',
        })
        .done(function(data){
            console.log(data);
            initChart(data, '#chart', 'kWh');
            storeData = data;

            $('.header-toggle span:first-child').text(data.name + ' ' + data.surname);
            $.each(data.addressList, function (i, item) {
                $('.js-addressList').append('<option value="' + item.eic + '"' + (data.address==item.address ? 'selected' : '') + '>' + item.address + ', eic: ' + item.eic + '</option>');
            });

            $.each(data.fuseValues, function (i, item) {
                $('.js-fuseList').append('<option value="'+ item +'">' + item + '</option>');
            });
        })
        .fail(function(jqXHR, status){
            console.log('Ajax Error Triggered : ' + status);
            console.log('Error: ' + jqXHR);
        });
    }

    function initChart(seriesCallback, chartId, unit) {
        var chartDaysDifference = 20;
        var seriesGraphReadings = seriesCallback.graphReadings;
        var seriesLength = seriesGraphReadings.length;
        var isFirstLoad = true;
        var isFirstBrush = true;
        var isFirstZoom = true;

        chart = c3.generate({
            bindto: chartId,
            padding: {
                bottom: 0
            },
            data: {
                x: 'x',
                columns: [],
                type: 'area-spline'
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        fit: false,
                        format: function (x) {
                            return axisFormatter(x);
                        },
                        centered: true,
                        localtime: true
                    }
                }
            },
            tooltip: {
                format: {
                    value: function(value, ratio, id, index) {
                        var format = d3.format('.1f');
                        return format(value);
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
            point: {
                show: false
            },
            size: {
                height: 500
            },
            color: {
                pattern: ['#fff']
            },
            zoom: {
                enabled: true,
                onzoom: function (domain) {
                    if (!isFirstLoad) {
                        var domainDiff = daysDifference(domain[1], domain[0]);
                        console.log('onzoom domaindiff: ' + domainDiff);

                        // change x axis format
                        changeFormatter(domainDiff);

                        // if domain difference is smaller than 20 eq 20 days then show full data chart
                        if (domainDiff <= chartDaysDifference) {

                                debounce(function() {
                                    if (unit == 'A1' || unit == 'A3') {
                                        generateAllFuseDataChart(chart, seriesLength, seriesGraphReadings, unit, domain[0], domain[1]);
                                    } else if (unit == 'kWh') {
                                        generateAllKWHDataChart(chart, seriesLength, seriesGraphReadings, domain[0], domain[1]);
                                    }
                                }, 200);

                        } else {

                            if (!isFirstZoom) {
                                debounce(function() {
                                    if (unit == 'A1' || unit == 'A3') {
                                        generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, unit);
                                    } else if (unit == 'kWh') {
                                        generateSumKWHDataChart(chart, seriesLength, seriesGraphReadings);
                                    }
                                }, 200);
                            }

                            isFirstZoom = false;

                        }
                    }

                    isFirstLoad = false;
                }
            },
            subchart: {
                show: true,
                onbrush: function (domain) {
                    var domainDiff = daysDifference(domain[1], domain[0]);
                    console.log('onbrush domaindiff: ' + domainDiff);

                    // change x axis format
                    changeFormatter(domainDiff);

                    // if domain difference is smaller than 35 eq 35 days then show full data chart
                    if (domainDiff <= chartDaysDifference) {

                        debounce(function(){
                            if (unit == 'A1' || unit == 'A3') {
                                generateAllFuseDataChart(chart, seriesLength, seriesGraphReadings, unit, domain[0], domain[1]);
                            } else if (unit == 'kWh') {
                                generateAllKWHDataChart(chart, seriesLength, seriesGraphReadings, domain[0], domain[1]);
                            }
                        }, 200);

                    } else {

                        if (!isFirstBrush) {
                            if (unit == 'A1' || unit == 'A3') {
                                generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, unit);
                            } else if (unit == 'kWh') {
                                generateSumKWHDataChart(chart, seriesLength, seriesGraphReadings);
                            }
                        }

                        isFirstBrush = false;
                    }
                }
            }
        });

        if (seriesLength > 0) {
            switch(unit) {
                case 'kWh':
                updateFormatter('month');
                generateSumKWHDataChart(chart, seriesLength, seriesGraphReadings);
                break;
                case 'A1':
                updateFormatter('date');
                generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, unit);
                generateChartOptions(chart, seriesLength, seriesCallback);
                break;
                case 'A3':
                updateFormatter('date');
                generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings,unit);
                generateChartOptions(chart, seriesLength, seriesCallback);
                break;
            }
        } else {
            chart.load({
                columns: [0,0]
            });
        }

        hideLoader($('#loader'));
    };

    $('.js-showChart').on('click', function (e) {
        e.preventDefault();
        var unit = $('input:radio[name="amp"]:checked').val();
        var currentUsableFuseValue = $('.js-fuseList').val();

        initChart(storeData, '#chart', unit);

        var $xAxisArray = [currentUsableFuseValue, storeData.recommendedFuseSize, storeData.calculatedMinFuseSize];
        var maxValue = getMaxOfArray($xAxisArray);
        chart.axis.range({max: {y: maxValue}, min: {y: 2}});

        chart.ygrids.add([
            {value: currentUsableFuseValue, text: 'Kasutatav peakaitsme suurus: ' + currentUsableFuseValue + 'A'}
        ]);

        changeChartButtonState($('.js-changeUnit'), unit);
    });

    $('.js-changeUnit').on('click', function () {
        showLoader($('#loader'));
        var $this = $(this);
        var unit = $this.data('unit');
        var currentUsableFuseValue = $('.js-fuseList').val();

        initChart(storeData, '#chart', unit);

        // if (unit == 'A1' || unit == 'A3') {
        //     var $xAxisArray = [currentUsableFuseValue, storeData.recommendedFuseSize, storeData.calculatedMinFuseSize];
        //     var maxValue = getMaxOfArray($xAxisArray);
        //     chart.axis.range({max: {y: maxValue}, min: {y: 2}});
        //
        //     chart.ygrids.add([
        //         {value: currentUsableFuseValue, text: 'Kasutatav peakaitsme suurus: ' + currentUsableFuseValue + 'A'}
        //     ]);
        // }

        changeChartButtonState($this, unit);
    });

    // execute function only once in given time
    var debounce = (function(){
        var timer = 0;
        return function(callback, ms){
            clearTimeout (timer);
            timer = setTimeout(callback, ms);
        };
    })();

    // update x axis format
    var axisFormatter;
    function updateFormatter(zoomLevel) {
        if (zoomLevel == 'month') {
            axisFormatter = d3.time.format('%B');
        } else if (zoomLevel == 'date') {
            axisFormatter = d3.time.format('%d.%m');
        } else if (zoomLevel == 'datetime') {
            axisFormatter = d3.time.format('%H:%M %d.%m');
        }
    }

    // change x axis format
    function changeFormatter(domainDiff) {
        if (domainDiff > 150) {
            updateFormatter('month');
        } else if (domainDiff <= 150 && domainDiff > 7) {
            updateFormatter('date');
        } else {
            updateFormatter('datetime');
        }
    }

    function generateAllKWHDataChart(chart, seriesLength, seriesGraphReadings, domain0, domain1) {
        console.log('generateAllKWHDataChart');
        var time_array = ['x'];
        var data_array = ['kWh'];
        var domain0 = moment.utc(domain0).subtract(7, 'days');
        var domain1 = moment.utc(domain1).add(7, 'days');

        for (var i = 0; i<seriesLength; i++) {
            if (moment.utc(seriesGraphReadings[i].time) >= domain0 && moment.utc(seriesGraphReadings[i].time) <= domain1) {
                time_array.push(moment.utc(seriesGraphReadings[i].time));
                data_array.push(seriesGraphReadings[i].kWh);
            }
        }

        console.log(time_array);
        console.log(data_array);
        chart.load({
            columns: [
                time_array,
                data_array
            ]
        });
    };

    function generateSumKWHDataChart(chart, seriesLength, seriesGraphReadings) {
        console.log('generateSumKWHDataChart');
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
                // var unix_timestamp = Date.parse(seriesGraphReadings[i-1].time.slice(0,10))/1000;
                // format date with moment and push to time array (x axis)
                time_array.push(seriesGraphReadings[i-1].time.slice(0,10));
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
    };

    function generateAllFuseDataChart(chart, seriesLength, seriesGraphReadings, fuse, domain0, domain1) {
        console.log('generateAllFuseDataChart');
        var time_array = ['x'];
        var data_array = [fuse];
        var domain0 = moment.utc(domain0).subtract(7, 'days');
        var domain1 = moment.utc(domain1).add(7, 'days');

        for (var i = 0; i<seriesLength; i++) {
            if (moment.utc(seriesGraphReadings[i].time) >= domain0 && moment.utc(seriesGraphReadings[i].time) <= domain1) {
                var data;
                if (fuse == 'A1') {
                    data = seriesGraphReadings[i].A1;
                } else if (fuse == 'A3') {
                    data = seriesGraphReadings[i].A3;
                }
                // var unix_timestamp = Date.parse(seriesGraphReadings[i].time)/1000;
                time_array.push(moment.utc(seriesGraphReadings[i].time));
                data_array.push(data);
            }
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
    function generateAvgFuseDataChart(chart, seriesLength, seriesGraphReadings, fuse) {
        console.log('generateAvgFuseDataChart');
        var time_array = ['x'];
        var data_array = [fuse];
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
                // var unix_timestamp = Date.parse(seriesGraphReadings[i-1].time.slice(0,10))/1000;
                // format date with moment and push to time array (x axis)
                time_array.push(seriesGraphReadings[i-1].time.slice(0,10));
                // make day array empty
                day_array.splice(0, day_array.length);
                // take next day
                date_str = seriesGraphReadings[i].time.slice(0,10);
            }
            // push every value to specific day array
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

    function generateChartOptions(chart, seriesLength, seriesCallback) {
        // add ygrid lines for recommended and required fuse
        if (seriesCallback.recommendedFuseSize == seriesCallback.calculatedMinFuseSize) {
            chart.ygrids.add([
                {value: seriesCallback.recommendedFuseSize, text: 'YAY! Atta boy ' + seriesCallback.recommendedFuseSize + 'A'}
            ]);
        } else {
            chart.ygrids.add([
                {value: seriesCallback.recommendedFuseSize, text: 'Soovitatav peakaitse: ' + seriesCallback.recommendedFuseSize + 'A'},
                {value: seriesCallback.calculatedMinFuseSize, text: 'Peakaitse tarbimise ühtlustamisel: ' + seriesCallback.calculatedMinFuseSize + 'A'}
            ]);
        }

        // set regions colors for fuse values
        setRegionsColors(chart, 0, 16, 'regionX');

        // take bigger fuse number
        var maxFuseNumber = Math.max(seriesCallback.recommendedFuseSize, seriesCallback.calculatedMinFuseSize);

        // set y axis range
        chart.axis.range({max: {y: maxFuseNumber}, min: {y: 2}});

        setZoomDomain(chart, firstDayInPreviousMonth());
    }

    function setRegionsColors(chart, start, end, classname) {
        chart.regions.add([
            {axis: 'y', start: start, end: end, class: classname}
        ]);
    }

    function setZoomDomain(chart, daysFrom) {
        var endDate = new Date();
        var daysDiff = daysDifference(endDate, daysFrom);
        var startDate = new Date();
        new Date(startDate.setDate(endDate.getDate()-daysDiff));
        chart.zoom([startDate, endDate]);
    }

    function firstDayInPreviousMonth() {
        var now = new Date();
        return new Date(now.getFullYear(), now.getMonth() - 1, 1);
    }

    function lastMonth() {
        var d = new Date();
        return new Date(d.setDate(d.getDate() - 31));
    }

    function lastWeek() {
        var d = new Date();
        return new Date(d.setDate(d.getDate() - 7));
    }

    function lastDay() {
        var d = new Date();
        return new Date(d.setDate(d.getDate() - 1));
    }

    function daysDifference(from, to) {
        return Math.floor(( Date.parse(from) - Date.parse(to) ) / 86400000);
    }

    function getMaxOfArray(array) {
        return Math.max.apply(null, array);
    }

    function showLoader($loader) {
        $loader.show();
    }

    function hideLoader($loader) {
        $loader.fadeOut('slow');
    }

    function changeChartButtonState($button, unit) {
        var $activeButton;
        $.each($button, function (i, item) {
            if ($(item).data('unit') == unit) {
                $activeButton = $(item);
            }
        });
        $activeButton.siblings().removeClass('active');
        $activeButton.addClass('active');
        $activeButton.siblings().find('.glyphicon').removeClass('glyphicon-ok-sign').addClass('glyphicon-one-fine-empty-dot');
        $activeButton.find('.glyphicon').removeClass('glyphicon-one-fine-empty-dot').addClass('glyphicon-ok-sign');
    }

    d3.select('.js-month').on('click', function () {
        updateFormatter('datetime');
        setZoomDomain(chart, lastMonth());
    });

    d3.select('.js-week').on('click', function () {
        updateFormatter('datetime');
        setZoomDomain(chart, lastWeek());
    });

    d3.select('.js-day').on('click', function () {
        updateFormatter('datetime');
        setZoomDomain(chart, lastDay());
    });

    d3.select('.js-unZoom').on('click', function () {
        updateFormatter('month');
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
