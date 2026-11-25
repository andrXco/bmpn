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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.21059399877526025, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.20650224215246638, 500, 1500, "GET listar procesos"], "isController": false}, {"data": [0.25310630464795214, 500, 1500, "GET detalle proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear gateway"], "isController": false}, {"data": [1.0, 500, 1500, "Crear lane"], "isController": false}, {"data": [1.0, 500, 1500, "Crear actividad inicial"], "isController": false}, {"data": [0.1837491571139582, 500, 1500, "POST crear actividad"], "isController": false}, {"data": [1.0, 500, 1500, "Crear arco"], "isController": false}, {"data": [0.24303232998885171, 500, 1500, "GET listar arcos"], "isController": false}, {"data": [0.22943221320973348, 500, 1500, "GET listar gateways"], "isController": false}, {"data": [0.22305764411027568, 500, 1500, "GET listar lanes"], "isController": false}, {"data": [1.0, 500, 1500, "Crear proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear rol"], "isController": false}, {"data": [1.0, 500, 1500, "Registrar empresa"], "isController": false}, {"data": [1.0, 500, 1500, "Iniciar sesion"], "isController": false}, {"data": [0.20565302144249512, 500, 1500, "GET listar roles"], "isController": false}, {"data": [0.12635805384978743, 500, 1500, "GET listar actividades"], "isController": false}, {"data": [0.22549609140108237, 500, 1500, "GET listar eventos"], "isController": false}, {"data": [1.0, 500, 1500, "Crear evento de envio"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 16330, 0, 0.0, 2974.0842008573172, 21, 32611, 2202.5, 5651.399999999998, 8460.449999999999, 17250.040000000008, 219.14462471650765, 2994.557856240019, 118.90213600587786], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET listar procesos", 2230, 0, 0.0, 2779.236322869948, 40, 32611, 2331.5, 5559.300000000001, 6572.949999999998, 8134.220000000002, 32.531473836233936, 29.03688192023224, 16.964655301317304], "isController": false}, {"data": ["GET detalle proceso", 2173, 0, 0.0, 2079.1946617579365, 22, 9088, 1881.0, 3936.000000000001, 4679.8999999999905, 6809.119999999997, 31.499601362615056, 23.593939692505618, 16.5188339176995], "isController": false}, {"data": ["Crear gateway", 1, 0, 0.0, 38.0, 38, 38, 38.0, 38.0, 38.0, 38.0, 26.31578947368421, 17.526726973684212, 16.473067434210527], "isController": false}, {"data": ["Crear lane", 1, 0, 0.0, 36.0, 36, 36, 36.0, 36.0, 36.0, 36.0, 27.777777777777775, 14.512803819444446, 15.923394097222223], "isController": false}, {"data": ["Crear actividad inicial", 1, 0, 0.0, 49.0, 49, 49, 49.0, 49.0, 49.0, 49.0, 20.408163265306122, 11.918048469387754, 13.233418367346939], "isController": false}, {"data": ["POST crear actividad", 1483, 0, 0.0, 2272.9123398516485, 127, 8363, 2118.0, 4014.6000000000004, 4666.5999999999985, 5949.120000000003, 21.980465102491515, 13.75979153388964, 14.68279934581067], "isController": false}, {"data": ["Crear arco", 1, 0, 0.0, 58.0, 58, 58, 58.0, 58.0, 58.0, 58.0, 17.241379310344826, 9.159482758620689, 9.984509698275861], "isController": false}, {"data": ["GET listar arcos", 1794, 0, 0.0, 2208.9916387959834, 22, 10963, 1991.5, 4215.0, 5120.25, 7482.25, 26.124564954638785, 13.725601509370767, 13.85316286168834], "isController": false}, {"data": ["GET listar gateways", 1726, 0, 0.0, 2261.049246813443, 40, 9412, 2019.5, 4304.6, 5072.249999999999, 6696.92, 25.21695935481986, 16.67175926095024, 13.445761530987932], "isController": false}, {"data": ["GET listar lanes", 1596, 0, 0.0, 2186.441102756892, 46, 7617, 1944.5, 4201.099999999999, 4899.649999999999, 5829.629999999999, 23.522129371711543, 11.944831321572268, 12.679897864438255], "isController": false}, {"data": ["Crear proceso", 1, 0, 0.0, 33.0, 33, 33, 33.0, 33.0, 33.0, 33.0, 30.303030303030305, 22.845643939393938, 19.35369318181818], "isController": false}, {"data": ["Crear rol", 1, 0, 0.0, 21.0, 21, 21, 21.0, 21.0, 21.0, 21.0, 47.61904761904761, 26.3671875, 28.227306547619047], "isController": false}, {"data": ["Registrar empresa", 1, 0, 0.0, 398.0, 398, 398, 398.0, 398.0, 398.0, 398.0, 2.512562814070352, 2.034096262562814, 1.099246231155779], "isController": false}, {"data": ["Iniciar sesion", 1, 0, 0.0, 145.0, 145, 145, 145.0, 145.0, 145.0, 145.0, 6.896551724137931, 7.280441810344828, 1.7039331896551726], "isController": false}, {"data": ["GET listar roles", 1539, 0, 0.0, 2202.424951267054, 50, 8023, 2027.0, 3998.0, 4508.0, 5942.5999999999985, 22.732979807678106, 20.15775943883957, 11.965894644861816], "isController": false}, {"data": ["GET listar actividades", 2117, 0, 0.0, 7563.1880018894635, 29, 31466, 6399.0, 16035.0, 18443.699999999986, 22951.72000000001, 29.764080645614825, 2996.9790352244604, 15.95750026801029], "isController": false}, {"data": ["GET listar eventos", 1663, 0, 0.0, 2239.0715574263395, 48, 8641, 2054.0, 4281.0, 4896.799999999999, 6269.679999999991, 24.094814471377447, 16.965196517444472, 12.823900280176472], "isController": false}, {"data": ["Crear evento de envio", 1, 0, 0.0, 62.0, 62, 62, 62.0, 62.0, 62.0, 62.0, 16.129032258064516, 11.419480846774194, 10.710685483870968], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 16330, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
