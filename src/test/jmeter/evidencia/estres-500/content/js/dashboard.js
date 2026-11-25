/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.29417272923719656, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.3166573504751258, 500, 1500, "GET listar procesos"], "isController": false}, {"data": [0.34668192219679633, 500, 1500, "GET detalle proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear gateway"], "isController": false}, {"data": [1.0, 500, 1500, "Crear lane"], "isController": false}, {"data": [1.0, 500, 1500, "Crear actividad inicial"], "isController": false}, {"data": [0.2611441307578009, 500, 1500, "POST crear actividad"], "isController": false}, {"data": [1.0, 500, 1500, "Crear arco"], "isController": false}, {"data": [0.33913328956007877, 500, 1500, "GET listar arcos"], "isController": false}, {"data": [0.325853985264568, 500, 1500, "GET listar gateways"], "isController": false}, {"data": [0.30780042164441324, 500, 1500, "GET listar lanes"], "isController": false}, {"data": [1.0, 500, 1500, "Crear proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear rol"], "isController": false}, {"data": [1.0, 500, 1500, "Registrar empresa"], "isController": false}, {"data": [1.0, 500, 1500, "Iniciar sesion"], "isController": false}, {"data": [0.2980561555075594, 500, 1500, "GET listar roles"], "isController": false}, {"data": [0.14386376981796828, 500, 1500, "GET listar actividades"], "isController": false}, {"data": [0.3089041095890411, 500, 1500, "GET listar eventos"], "isController": false}, {"data": [1.0, 500, 1500, "Crear evento de envio"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 13883, 0, 0.0, 2086.9046315637893, 23, 20356, 1571.0, 3910.6000000000004, 5510.599999999991, 11911.519999999997, 196.18178221179664, 2335.149264499371, 106.62615996294123], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET listar procesos", 1789, 0, 0.0, 1676.0726662940197, 42, 5889, 1511.0, 3238.0, 3938.5, 4813.999999999995, 27.40754358550112, 24.463373864402364, 14.292605736970309], "isController": false}, {"data": ["GET detalle proceso", 1748, 0, 0.0, 1558.0503432494272, 27, 5835, 1413.5, 2925.600000000001, 3751.2999999999997, 4663.51, 26.614695940802097, 19.935031041596883, 13.957120820518286], "isController": false}, {"data": ["Crear gateway", 1, 0, 0.0, 26.0, 26, 26, 26.0, 26.0, 26.0, 26.0, 38.46153846153847, 25.615985576923077, 24.076021634615387], "isController": false}, {"data": ["Crear lane", 1, 0, 0.0, 26.0, 26, 26, 26.0, 26.0, 26.0, 26.0, 38.46153846153847, 20.094651442307693, 22.047776442307693], "isController": false}, {"data": ["Crear actividad inicial", 1, 0, 0.0, 23.0, 23, 23, 23.0, 23.0, 23.0, 23.0, 43.47826086956522, 25.390625, 28.192934782608695], "isController": false}, {"data": ["POST crear actividad", 1346, 0, 0.0, 1778.8424962852894, 81, 6571, 1611.0, 3340.6, 3868.299999999997, 4858.629999999998, 21.055924912006258, 13.177842075674619, 14.062026422565507], "isController": false}, {"data": ["Crear arco", 1, 0, 0.0, 33.0, 33, 33, 33.0, 33.0, 33.0, 33.0, 30.303030303030305, 16.098484848484848, 17.548532196969695], "isController": false}, {"data": ["GET listar arcos", 1523, 0, 0.0, 1584.585686145765, 51, 6673, 1386.0, 2978.8, 3663.6, 4823.24, 23.20832634899349, 12.193437085701659, 12.306758991702603], "isController": false}, {"data": ["GET listar gateways", 1493, 0, 0.0, 1590.4139316811797, 35, 5658, 1420.0, 2892.600000000001, 3614.2999999999993, 4864.799999999998, 22.972765040775503, 15.188048762309588, 12.249150109632252], "isController": false}, {"data": ["GET listar lanes", 1423, 0, 0.0, 1683.095572733661, 54, 5686, 1471.0, 3286.4000000000005, 3901.2, 4910.32, 22.324019892380342, 11.336416351599391, 12.034041973236276], "isController": false}, {"data": ["Crear proceso", 1, 0, 0.0, 39.0, 39, 39, 39.0, 39.0, 39.0, 39.0, 25.64102564102564, 19.330929487179485, 16.376201923076923], "isController": false}, {"data": ["Crear rol", 1, 0, 0.0, 25.0, 25, 25, 25.0, 25.0, 25.0, 25.0, 40.0, 22.1484375, 23.7109375], "isController": false}, {"data": ["Registrar empresa", 1, 0, 0.0, 438.0, 438, 438, 438.0, 438.0, 438.0, 438.0, 2.28310502283105, 1.8483340468036529, 0.9988584474885844], "isController": false}, {"data": ["Iniciar sesion", 1, 0, 0.0, 139.0, 139, 139, 139.0, 139.0, 139.0, 139.0, 7.194244604316547, 7.59470548561151, 1.7774842625899279], "isController": false}, {"data": ["GET listar roles", 1389, 0, 0.0, 1698.0691144708424, 45, 7713, 1479.0, 3241.0, 3803.5, 4630.1999999999925, 21.949369488954204, 19.46291747653361, 11.553427885299138], "isController": false}, {"data": ["GET listar actividades", 1703, 0, 0.0, 5232.953611274225, 41, 20356, 4066.0, 11471.2, 13131.199999999997, 16134.560000000001, 25.499737965111926, 2346.709298027252, 13.671246233248485], "isController": false}, {"data": ["GET listar eventos", 1460, 0, 0.0, 1645.3657534246552, 52, 5655, 1470.0, 3063.6000000000004, 3761.4000000000005, 4669.080000000007, 22.882219261813336, 16.111406335710367, 12.1785249000862], "isController": false}, {"data": ["Crear evento de envio", 1, 0, 0.0, 31.0, 31, 31, 31.0, 31.0, 31.0, 31.0, 32.25806451612903, 22.838961693548388, 21.421370967741936], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 13883, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
