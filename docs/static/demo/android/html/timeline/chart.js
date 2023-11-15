function testsChart() {
    var margin = {
        top: 70,
        right: 40,
        bottom: 20,
        left: 0
    };

    var dataHeight = 18;
    var lineSpacing = 30;
    var paddingTopHeading = -50;
    var paddingBottom = 10;
    var paddingLeft = 0;
    var width = 1200 - margin.left - margin.right;

    var div = d3.select('body').append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);

    function renderLegend(svg, success, failed, dataset, textWidth) {
        var legend = svg.select('#g_title')
            .append('g')
            .attr('id', 'g_legend')
            .attr('transform', 'translate(0,-12)');

        legend.append('rect')
            .attr('x', width - 150 + textWidth)
            .attr('y', paddingTopHeading)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_passed_test');

        legend.append('text')
            .attr('x', width - 150 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5)
            .text('Passed test = ' + success)
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width - 150 + textWidth)
            .attr('y', paddingTopHeading + 17)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_failed_test');

        legend.append('text')
            .attr('x', width - 150 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Failed test = ' + failed)
            .attr('class', 'legend');

        //
        legend.append('text')
            .attr('x', width - 500 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5)
            .text('Provider init')
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width - 500 + textWidth)
            .attr('y', paddingTopHeading)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_device_provider_initialise');

        legend.append('text')
            .attr('x', width - 500 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Device prepare')
            .attr('class', 'legend');

        legend.append('rect')
             .attr('x', width - 500 + textWidth)
             .attr('y', paddingTopHeading + 17)
             .attr('height', 15)
             .attr('width', 15)
             .attr('class', 'rect_device_initialise');

        //
        legend.append('text')
            .attr('x', width - 600 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5)
            .text('Ignored')
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width - 600 + textWidth)
            .attr('y', paddingTopHeading)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_ignored_test');

        legend.append('text')
            .attr('x', width - 600 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Incomplete')
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width - 600 + textWidth)
            .attr('y', paddingTopHeading + 17)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_incomplete_test');

          //
        legend.append('text')
            .attr('x', width - 750 + 20 + textWidth)
            .attr('y', paddingTopHeading + 8.5)
            .text('Assumption failure')
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width - 750 + textWidth)
            .attr('y', paddingTopHeading)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_assumption_failure');

        var deviceCount = dataset.measures.length;
        var idle = dataset.executionStats.idleTimeMillis;
        var averageIdle = idle / deviceCount;

        legend.append('text')
            .attr('x', width - 350 + textWidth)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Summary idle: ' + Math.round(moment.duration(idle).asSeconds()).toFixed(2) + " sec")
            .attr('class', 'legend');

        legend.append('text')
            .attr('x', width - 350 + textWidth)
            .attr('y', paddingTopHeading + 8.5)
            .text('Average Idle: ' + Math.round(moment.duration(averageIdle).asSeconds()).toFixed(2) + " sec")
            .attr('class', 'legend');
    }

    function convertDate(date) {
        var dateFormat = d3.time.format('%Y-%m-%d %H:%M:%S');
        return dateFormat(date)
    }

    function renderTitle(svg, startDate, finishDate) {
        svg.select('#g_title')
            .append('text')
            .attr('x', paddingLeft)
            .attr('y', paddingTopHeading)
            .text('Execution timeline')
            .attr('class', 'heading');

        var subtitleText = 'from ' + moment(convertDate(startDate)).format('l') + ' '
            + moment(convertDate(startDate)).format('LTS') + ' to '
            + moment(convertDate(finishDate)).format('l') + ' '
            + moment(convertDate(finishDate)).format('LTS');

        svg.select('#g_title')
            .append('text')
            .attr('x', paddingLeft)
            .attr('y', paddingTopHeading + 17)
            .text(subtitleText)
            .attr('class', 'subheading');
    }

    function renderTests(svg, dataset, startSet, endSet, xScale, textWidth) {
        var g = svg.select('#g_data').selectAll('.g_data')
            .data(dataset.measures.slice(startSet, endSet))
            .enter()
            .append('g')
            .attr('transform', function (d, i) {
                return 'translate(0,' + ((lineSpacing + dataHeight) * i) + ')';
            })
            .attr('class', 'dataset');

        g.selectAll('rect')
            .data(function (d) {
                return d.data;
            })
            .enter()
            .append('rect')
            .attr('x', function (d) {
                return xScale(d.startDate) + textWidth;
            })
            .attr('y', lineSpacing)
            .attr('width', function (d) {
                return (xScale(d.endDate) - xScale(d.startDate));
            })
            .attr('height', dataHeight)
            .attr('class', function (d) {
                switch (d.metricType) {
                    case "PASSED": {
                        return 'rect_passed_test'
                    }
                    case "FAILURE": {
                        return 'rect_failed_test'
                    }
                    case "IGNORED": {
                        return 'rect_ignored_test'
                    }
                    case "INCOMPLETE": {
                        return 'rect_incomplete_test'
                    }
                    case "ASSUMPTION_FAILURE": {
                        return 'rect_assumption_failure'
                    }
                    case "DEVICE_PROVIDER_INIT": {
                        return 'rect_device_provider_initialise'
                    }
                    case "DEVICE_PREPARE": {
                        return 'rect_device_initialise'
                    }
                }
            })
            .on('mouseover', function (d, i) {
                var matrix = this.getScreenCTM().translate(+this.getAttribute('x'), +this.getAttribute('y'));
                div.transition()
                    .duration(200)
                    .style('opacity', 0.9);
                div.html(function () {
                    var output = '';
                    switch (d.metricType) {
                        case "PASSED": {
                            output = '<i class="fa fa-fw fa-check tooltip_passed_test"></i>';
                            break;
                        }
                        case "FAILURE": {
                            output = '<i class="fa fa-fw fa-times tooltip_failed_test"></i>';
                            break;
                        }
                        case "IGNORED": {
                            output = '<i class="fa fa-fw fa-times tooltip_ignored_test"></i>';
                            break;
                        }
                        case "ASSUMPTION_FAILURE": {
                            output = '<i class="fa fa-fw fa-times tooltip_ignored_test"></i>';
                            break;
                        }
                        case "DEVICE_PROVIDER_INIT": {
                            d.testName = 'provider init'
                            output = '<i class="fa fa-fw fa-times"></i>';
                            break;
                        }
                        case "DEVICE_PREPARE": {
                            d.testName = 'device init'
                            output = '<i class="fa fa-fw fa-times"></i>';
                            break;
                        }
                    }
                    return output + d.testName + '</br>'
                        + moment(convertDate(d.endDate))
                          .preciseDiff(moment(convertDate(d.startDate))) + ' | '
                        + moment(convertDate(d.startDate)).format('LTS') + ' - '
                        + moment(convertDate(d.endDate)).format('LTS') + '</br>'
                }).style('left', function () {
                    return window.pageXOffset + matrix.e + 'px';
                }).style('top', function () {
                    return window.pageYOffset + matrix.f - 25 + 'px';
                }).style('height', dataHeight + 25 + 'px');
            })
            .on('mouseout', function () {
                div.transition()
                    .duration(500)
                    .style('opacity', 0);
            });
    }

    function renderTime(svg, xAxis, textWidth) {
        svg.select('#g_axis').append('g')
            .attr('class', 'axis')
            .attr('transform', function (d, i) {
                return 'translate(' + (textWidth) + ',1)';
            })
            .call(xAxis);
    }

    function renderGrid(svg, xScale, noOfDatasets, dataset, textWidth) {
        svg.select('#g_axis').selectAll('line.vert_grid').data(xScale.ticks().concat(xScale.domain()))
            .enter()
            .append('line')
            .attr({
                'class': 'vert_grid',
                'x1': function (d) {
                    return xScale(d) + textWidth;
                },
                'x2': function (d) {
                    return xScale(d) + textWidth;
                },
                'y1': 0,
                'y2': dataHeight * noOfDatasets + lineSpacing * noOfDatasets - 1 + paddingBottom
            });


        svg.select('#g_axis').selectAll('line.horz_grid').data(dataset.measures)
            .enter()
            .append('line')
            .attr({
                'class': 'horz_grid',
                'x1': textWidth,
                'x2': width + textWidth,
                'y1': function (d, i) {
                    return ((lineSpacing + dataHeight) * i) + lineSpacing + dataHeight / 2;
                },
                'y2': function (d, i) {
                    return ((lineSpacing + dataHeight) * i) + lineSpacing + dataHeight / 2;
                }
            });
    }

    function calculateScale(startDate, finishDate) {
        return d3.time.scale()
            .domain([startDate, finishDate])
            .range([0, width])
            .clamp(1);
    }

    function calculateAxis(xScale) {
        return d3.svg.axis()
            .scale(xScale)
            .tickFormat(d3.time.format("%M:%S"))
            .orient('top');
    }

    function prepareDates(dataset) {
        dataset.measures.forEach(function (d) {
            d.data.forEach(function (d1) {
                d1.startDate = new Date(d1.startDate);
                d1.endDate = new Date(d1.endDate);
            });
            d.data.sort(function (a, b) {
                return a.startDate - b.startDate;
            });
        });
    }

    function chart(selection) {
        selection.each(function drawGraph(dataset) {
            var startSet = 0;
            var endSet = dataset.measures.length;

            var noOfDatasets = endSet - startSet;
            var height = dataHeight * noOfDatasets + lineSpacing * noOfDatasets - 1;

            var success = dataset.passedTests;
            var failed = dataset.failedTests;

            prepareDates(dataset);

            var startDate = 0;
            var finishDate = 0;
            var firstFinishDate = 0;

            dataset.measures.forEach(function (series) {
                if (series.data.length > 0) {
                    if (startDate === 0) {
                        startDate = series.data[0].startDate;
                        finishDate = series.data[series.data.length - 1].endDate;
                        firstFinishDate = series.data[series.data.length - 1].endDate;
                    } else {
                        if (series.data[0].startDate < startDate) {
                            startDate = series.data[0].startDate;
                        }
                        if (series.data[series.data.length - 1].endDate > finishDate) {
                            finishDate = series.data[series.data.length - 1].endDate;
                        }
                        if (series.data[series.data.length - 1].endDate < firstFinishDate) {
                            firstFinishDate = series.data[series.data.length - 1].endDate;
                        }
                    }
                }
            });

            var scale = calculateScale(startDate, finishDate);

            var axis = calculateAxis(scale);

            axis.tickValues(scale.ticks().concat(scale.domain()));

            var svg = d3.select(this).append('svg')
                .attr('width', width + margin.left + margin.right)
                .attr('height', height + margin.top + margin.bottom)
                .append('g')
                .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

            svg.append('g').attr('id', 'g_title');
            svg.append('g').attr('id', 'g_axis');
            svg.append('g').attr('id', 'g_data');

            var labels = svg.select('#g_axis').selectAll('text')
                .data(dataset.measures)
                .enter();


            labels.append('text')
                .attr('x', paddingLeft)
                .attr('y', lineSpacing + dataHeight / 2)
                .text(function (d) {
                    return d.measure;
                })
                .attr('transform', function (d, i) {
                    return 'translate(0,' + ((lineSpacing + dataHeight) * i) + ')';
                })
                .attr('class', 'ytitle');

            function getTextWidth(text, fontSize, fontFace) {
                var canvas = document.createElement('canvas');
                var context = canvas.getContext('2d');
                context.font = fontSize + 'px ' + fontFace;
                return context.measureText(text).width;
            }

            var longest = "";
            dataset.measures.forEach(a => {
                if (longest.length < a.measure.length) {
                    longest = a.measure;
                }
            });

            var width = getTextWidth(longest, 12, 'Arial');

            labels.append('foreignObject')
                .attr('x', paddingLeft)
                .attr('y', lineSpacing)
                .attr('transform', function (d, i) {
                    return 'translate(0,' + ((lineSpacing + dataHeight) * i) + ')';
                })
                .attr('width', -1 * paddingLeft)
                .attr('height', dataHeight)
                .attr('class', 'ytitle');

            renderGrid(svg, scale, noOfDatasets, dataset, width);

            renderTime(svg, axis, width);

            renderTests(svg, dataset, startSet, endSet, scale, width);

            renderTitle(svg, startDate, finishDate, width);

            renderLegend(svg, success, failed, dataset, width);
        });
    }

    return chart;
}

(function(moment) {
    var STRINGS = {
        nodiff: '',
        year: 'year',
        years: 'years',
        month: 'month',
        months: 'months',
        day: 'day',
        days: 'days',
        hour: 'hour',
        hours: 'hours',
        minute: 'minute',
        minutes: 'minutes',
        second: 'second',
        seconds: 'seconds',
        delimiter: ' '
    };

    function pluralize(num, word) {
        return num + ' ' + STRINGS[word + (num === 1 ? '' : 's')];
    }

    function buildStringFromValues(yDiff, mDiff, dDiff, hourDiff, minDiff, secDiff){
        var result = [];

        if (yDiff) {
            result.push(pluralize(yDiff, 'year'));
        }
        if (mDiff) {
            result.push(pluralize(mDiff, 'month'));
        }
        if (dDiff) {
            result.push(pluralize(dDiff, 'day'));
        }
        if (hourDiff) {
            result.push(pluralize(hourDiff, 'hour'));
        }
        if (minDiff) {
            result.push(pluralize(minDiff, 'minute'));
        }
        if (secDiff) {
            result.push(pluralize(secDiff, 'second'));
        }

        return result.join(STRINGS.delimiter);
    }

    function buildValueObject(yDiff, mDiff, dDiff, hourDiff, minDiff, secDiff, firstDateWasLater) {
        return {
            "years"   : yDiff,
            "months"  : mDiff,
            "days"    : dDiff,
            "hours"   : hourDiff,
            "minutes" : minDiff,
            "seconds" : secDiff,
            "firstDateWasLater" : firstDateWasLater
        }
    }
    moment.fn.preciseDiff = function(d2, returnValueObject) {
        return moment.preciseDiff(this, d2, returnValueObject);
    };

    moment.preciseDiff = function(d1, d2, returnValueObject) {
        var m1 = moment(d1), m2 = moment(d2), firstDateWasLater;

        m1.add(m2.utcOffset() - m1.utcOffset(), 'minutes'); // shift timezone of m1 to m2

        if (m1.isSame(m2)) {
            if (returnValueObject) {
                return buildValueObject(0, 0, 0, 0, 0, 0, false);
            } else {
                return STRINGS.nodiff;
            }
        }
        if (m1.isAfter(m2)) {
            var tmp = m1;
            m1 = m2;
            m2 = tmp;
            firstDateWasLater = true;
        } else {
            firstDateWasLater = false;
        }

        var yDiff = m2.year() - m1.year();
        var mDiff = m2.month() - m1.month();
        var dDiff = m2.date() - m1.date();
        var hourDiff = m2.hour() - m1.hour();
        var minDiff = m2.minute() - m1.minute();
        var secDiff = m2.second() - m1.second();

        if (secDiff < 0) {
            secDiff = 60 + secDiff;
            minDiff--;
        }
        if (minDiff < 0) {
            minDiff = 60 + minDiff;
            hourDiff--;
        }
        if (hourDiff < 0) {
            hourDiff = 24 + hourDiff;
            dDiff--;
        }
        if (dDiff < 0) {
            var daysInLastFullMonth = moment(m2.year() + '-' + (m2.month() + 1), "YYYY-MM").subtract(1, 'M').daysInMonth();
            if (daysInLastFullMonth < m1.date()) { // 31/01 -> 2/03
                dDiff = daysInLastFullMonth + dDiff + (m1.date() - daysInLastFullMonth);
            } else {
                dDiff = daysInLastFullMonth + dDiff;
            }
            mDiff--;
        }
        if (mDiff < 0) {
            mDiff = 12 + mDiff;
            yDiff--;
        }

        if (returnValueObject) {
            return buildValueObject(yDiff, mDiff, dDiff, hourDiff, minDiff, secDiff, firstDateWasLater);
        } else {
            return buildStringFromValues(yDiff, mDiff, dDiff, hourDiff, minDiff, secDiff);
        }


    };
}(moment));
