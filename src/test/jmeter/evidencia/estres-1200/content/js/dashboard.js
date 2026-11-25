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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.1496655920704422, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.13105316627158822, 500, 1500, "GET listar procesos"], "isController": false}, {"data": [0.17934970598408856, 500, 1500, "GET detalle proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear gateway"], "isController": false}, {"data": [1.0, 500, 1500, "Crear lane"], "isController": false}, {"data": [1.0, 500, 1500, "Crear actividad inicial"], "isController": false}, {"data": [0.09217724288840262, 500, 1500, "POST crear actividad"], "isController": false}, {"data": [1.0, 500, 1500, "Crear arco"], "isController": false}, {"data": [0.19502262443438914, 500, 1500, "GET listar arcos"], "isController": false}, {"data": [0.18788163457022075, 500, 1500, "GET listar gateways"], "isController": false}, {"data": [0.1405146316851665, 500, 1500, "GET listar lanes"], "isController": false}, {"data": [1.0, 500, 1500, "Crear proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear rol"], "isController": false}, {"data": [1.0, 500, 1500, "Registrar empresa"], "isController": false}, {"data": [1.0, 500, 1500, "Iniciar sesion"], "isController": false}, {"data": [0.12428348097967691, 500, 1500, "GET listar roles"], "isController": false}, {"data": [0.12121212121212122, 500, 1500, "GET listar actividades"], "isController": false}, {"data": [0.1650461837627613, 500, 1500, "GET listar eventos"], "isController": false}, {"data": [1.0, 500, 1500, "Crear evento de envio"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 20783, 0, 0.0, 3687.3678487224965, 14, 69726, 2841.0, 6087.700000000004, 11082.750000000004, 24507.24000000012, 253.0870211159551, 4350.943202540856, 137.20446253790277], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET listar procesos", 2953, 0, 0.0, 2961.088046054867, 21, 69726, 2930.0, 4804.6, 5637.299999999999, 7022.040000000001, 37.67831168499758, 33.63083679696073, 19.64865082010616], "isController": false}, {"data": ["GET detalle proceso", 2891, 0, 0.0, 2627.988931165685, 14, 9390, 2486.0, 4795.8, 5744.0, 7128.5999999999985, 40.00442802385598, 29.96425419364994, 20.978884617979162], "isController": false}, {"data": ["Crear gateway", 1, 0, 0.0, 27.0, 27, 27, 27.0, 27.0, 27.0, 27.0, 37.03703703703704, 24.66724537037037, 23.18431712962963], "isController": false}, {"data": ["Crear lane", 1, 0, 0.0, 26.0, 26, 26, 26.0, 26.0, 26.0, 26.0, 38.46153846153847, 20.094651442307693, 22.047776442307693], "isController": false}, {"data": ["Crear actividad inicial", 1, 0, 0.0, 29.0, 29, 29, 29.0, 29.0, 29.0, 29.0, 34.48275862068965, 20.13739224137931, 22.35991379310345], "isController": false}, {"data": ["POST crear actividad", 1828, 0, 0.0, 3100.61105032823, 54, 11649, 2941.0, 5173.0, 5937.699999999999, 7321.370000000002, 25.458900866271133, 15.945325339127042, 17.01440027784741], "isController": false}, {"data": ["Crear arco", 1, 0, 0.0, 30.0, 30, 30, 30.0, 30.0, 30.0, 30.0, 33.333333333333336, 17.708333333333336, 19.303385416666668], "isController": false}, {"data": ["GET listar arcos", 2210, 0, 0.0, 2480.23891402715, 22, 10962, 2277.5, 4727.4000000000015, 5349.499999999998, 7126.799999999997, 30.513482541041324, 16.031497663164288, 16.180489277134217], "isController": false}, {"data": ["GET listar gateways", 2129, 0, 0.0, 2609.655237200561, 34, 15178, 2505.0, 4542.0, 5213.5, 8140.099999999987, 29.367542589144076, 19.415846028174357, 15.658865482102213], "isController": false}, {"data": ["GET listar lanes", 1982, 0, 0.0, 2745.612512613523, 58, 9298, 2637.0, 4763.8, 5523.449999999999, 7104.870000000001, 27.429109176711552, 13.928844503798835, 14.78600416557107], "isController": false}, {"data": ["Crear proceso", 1, 0, 0.0, 66.0, 66, 66, 66.0, 66.0, 66.0, 66.0, 15.151515151515152, 11.422821969696969, 9.67684659090909], "isController": false}, {"data": ["Crear rol", 1, 0, 0.0, 20.0, 20, 20, 20.0, 20.0, 20.0, 20.0, 50.0, 27.685546875, 29.638671875], "isController": false}, {"data": ["Registrar empresa", 1, 0, 0.0, 288.0, 288, 288, 288.0, 288.0, 288.0, 288.0, 3.472222222222222, 2.8110080295138893, 1.5190972222222223], "isController": false}, {"data": ["Iniciar sesion", 1, 0, 0.0, 206.0, 206, 206, 206.0, 206.0, 206.0, 206.0, 4.854368932038835, 5.1245828276699035, 1.1993704490291264], "isController": false}, {"data": ["GET listar roles", 1919, 0, 0.0, 2853.4236581552923, 49, 10490, 2753.0, 4830.0, 5595.0, 7212.799999999999, 26.176867778853893, 23.2115194757806, 13.778644270314695], "isController": false}, {"data": ["GET listar actividades", 2805, 0, 0.0, 9726.47914438502, 22, 39124, 6344.0, 22812.8, 25512.999999999993, 31003.460000000006, 35.60548362528561, 4374.376155313848, 19.089268076447066], "isController": false}, {"data": ["GET listar eventos", 2057, 0, 0.0, 2618.7340787554695, 33, 14322, 2549.0, 4609.8, 5296.799999999997, 7033.120000000003, 28.355986876568057, 19.965494666021062, 15.091809421610929], "isController": false}, {"data": ["Crear evento de envio", 1, 0, 0.0, 30.0, 30, 30, 30.0, 30.0, 30.0, 30.0, 33.333333333333336, 23.600260416666668, 22.135416666666668], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 20783, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
