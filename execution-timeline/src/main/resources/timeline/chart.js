function testsChart() {
    var margin = {
        top: 70,
        right: 40,
        bottom: 20,
        left: 150
    };

    var dataHeight = 18;
    var lineSpacing = 30;
    var paddingTopHeading = -50;
    var paddingBottom = 10;
    var paddingLeft = -150;
    var width = 1200 - margin.left - margin.right;

    var div = d3.select('body').append('div')
        .attr('class', 'tooltip')
        .style('opacity', 0);

    function renderLegend(svg, success, failed, dataset) {
        var legend = svg.select('#g_title')
            .append('g')
            .attr('id', 'g_legend')
            .attr('transform', 'translate(0,-12)');

        legend.append('rect')
            .attr('x', width + margin.right - 150)
            .attr('y', paddingTopHeading)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_passed_test');

        legend.append('text')
            .attr('x', width + margin.right - 150 + 20)
            .attr('y', paddingTopHeading + 8.5)
            .text('Passed test = ' + success)
            .attr('class', 'legend');

        legend.append('rect')
            .attr('x', width + margin.right - 150)
            .attr('y', paddingTopHeading + 17)
            .attr('height', 15)
            .attr('width', 15)
            .attr('class', 'rect_failed_test');

        legend.append('text')
            .attr('x', width + margin.right - 150 + 20)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Failed test = ' + failed)
            .attr('class', 'legend');

        var deviceCount = dataset.measures.length;
        var idle = dataset.executionStats.idleTimeMillis;
        var averageIdle = idle / deviceCount;

        legend.append('text')
            .attr('x', width + margin.right - 350)
            .attr('y', paddingTopHeading + 8.5 + 15 + 2)
            .text('Summary idle: ' + Math.round(moment.duration(idle).asSeconds()).toFixed(2) + " sec")
            .attr('class', 'legend');

        legend.append('text')
            .attr('x', width + margin.right - 350)
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
            .text('Test run results')
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

    function renderTests(svg, dataset, startSet, endSet, xScale) {
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
                return xScale(d.startDate);
            })
            .attr('y', lineSpacing)
            .attr('width', function (d) {
                return (xScale(d.endDate) - xScale(d.startDate));
            })
            .attr('height', dataHeight)
            .attr('class', function (d) {
                switch (d.status) {
                    case "PASSED": {
                        return 'rect_passed_test'
                    }
                    case "FAILED": {
                        return 'rect_failed_test'
                    }
                    case "IGNORED": {
                        return 'rect_ignored_test'
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
                    switch (d.status) {
                        case "PASSED": {
                            output = '<i class="fa fa-fw fa-check tooltip_passed_test"></i>';
                            break;
                        }
                        case "FAILED": {
                            output = '<i class="fa fa-fw fa-times tooltip_failed_test"></i>';
                            break;
                        }
                        case "IGNORED": {
                            output = '<i class="fa fa-fw fa-times tooltip_ignored_test"></i>';
                            break;
                        }
                    }
                    return output + d.testName + '</br>'
                        + moment(convertDate(d.startDate)).format('LTS') + ' - '
                        + moment(convertDate(d.endDate)).format('LTS') + '| variance = ' + d.variance.toFixed(2) + ' expected = ' + d.expectedValue.toFixed(2);
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

    function renderTime(svg, xAxis) {
        svg.select('#g_axis').append('g')
            .attr('class', 'axis')
            .call(xAxis);
    }

    function renderGrid(svg, xScale, noOfDatasets, dataset) {
        svg.select('#g_axis').selectAll('line.vert_grid').data(xScale.ticks().concat(xScale.domain()))
            .enter()
            .append('line')
            .attr({
                'class': 'vert_grid',
                'x1': function (d) {
                    return xScale(d);
                },
                'x2': function (d) {
                    return xScale(d);
                },
                'y1': 0,
                'y2': dataHeight * noOfDatasets + lineSpacing * noOfDatasets - 1 + paddingBottom
            });

        svg.select('#g_axis').selectAll('line.horz_grid').data(dataset.measures)
            .enter()
            .append('line')
            .attr({
                'class': 'horz_grid',
                'x1': 0,
                'x2': width,
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
                    if (!(d.measure_html != null)) {
                        return d.measure;
                    }
                })
                .attr('transform', function (d, i) {
                    return 'translate(0,' + ((lineSpacing + dataHeight) * i) + ')';
                })
                .attr('class', function (d) {
                    var returnCSSClass = 'ytitle';
                    if (d.measure_url != null) {
                        returnCSSClass = returnCSSClass + ' link';
                    }
                    return returnCSSClass;
                })
                .on('click', function (d) {
                    if (d.measure_url != null) {
                        return window.open(d.measure_url);
                    }
                    return null;
                });

            labels.append('foreignObject')
                .attr('x', paddingLeft)
                .attr('y', lineSpacing)
                .attr('transform', function (d, i) {
                    return 'translate(0,' + ((lineSpacing + dataHeight) * i) + ')';
                })
                .attr('width', -1 * paddingLeft)
                .attr('height', dataHeight)
                .attr('class', 'ytitle')
                .html(function (d) {
                    if (d.measure_html != null) {
                        return d.measure_html;
                    }
                });

            renderGrid(svg, scale, noOfDatasets, dataset);

            renderTime(svg, axis);

            renderTests(svg, dataset, startSet, endSet, scale);

            renderTitle(svg, startDate, finishDate);

            renderLegend(svg, success, failed, dataset);
        });
    }

    return chart;
}
