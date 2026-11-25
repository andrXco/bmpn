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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.928316032295271, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.9675126903553299, 500, 1500, "GET listar procesos"], "isController": false}, {"data": [0.9547764227642277, 500, 1500, "GET detalle proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear gateway"], "isController": false}, {"data": [1.0, 500, 1500, "Crear lane"], "isController": false}, {"data": [1.0, 500, 1500, "Crear actividad inicial"], "isController": false}, {"data": [0.925, 500, 1500, "POST crear actividad"], "isController": false}, {"data": [1.0, 500, 1500, "Crear arco"], "isController": false}, {"data": [0.9610591900311527, 500, 1500, "GET listar arcos"], "isController": false}, {"data": [0.9598121085594989, 500, 1500, "GET listar gateways"], "isController": false}, {"data": [0.9610115911485775, 500, 1500, "GET listar lanes"], "isController": false}, {"data": [1.0, 500, 1500, "Crear proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear rol"], "isController": false}, {"data": [1.0, 500, 1500, "Registrar empresa"], "isController": false}, {"data": [1.0, 500, 1500, "Iniciar sesion"], "isController": false}, {"data": [0.9582010582010582, 500, 1500, "GET listar roles"], "isController": false}, {"data": [0.7026476578411406, 500, 1500, "GET listar actividades"], "isController": false}, {"data": [0.9685863874345549, 500, 1500, "GET listar eventos"], "isController": false}, {"data": [1.0, 500, 1500, "Crear evento de envio"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 8670, 0, 0.0, 316.0967704728947, 31, 3075, 233.0, 591.0, 877.4499999999989, 1551.8699999999972, 131.15101275205348, 1200.0368336324066, 71.48498953401304], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET listar procesos", 985, 0, 0.0, 254.34111675126914, 50, 1809, 214.0, 434.4, 545.0999999999998, 959.0799999999997, 16.307677025214815, 14.53243220310922, 8.504198761196006], "isController": false}, {"data": ["GET detalle proceso", 984, 0, 0.0, 258.7398373983735, 34, 1817, 203.0, 465.5, 695.0, 1074.6499999999992, 16.23708788488829, 12.160090116662817, 8.5149572208838], "isController": false}, {"data": ["Crear gateway", 1, 0, 0.0, 129.0, 129, 129, 129.0, 129.0, 129.0, 129.0, 7.751937984496124, 5.132630813953488, 4.852531492248062], "isController": false}, {"data": ["Crear lane", 1, 0, 0.0, 144.0, 144, 144, 144.0, 144.0, 144.0, 144.0, 6.944444444444444, 3.607855902777778, 3.974066840277778], "isController": false}, {"data": ["Crear actividad inicial", 1, 0, 0.0, 81.0, 81, 81, 81.0, 81.0, 81.0, 81.0, 12.345679012345679, 7.149402006172839, 7.993344907407407], "isController": false}, {"data": ["POST crear actividad", 940, 0, 0.0, 362.4999999999993, 96, 1873, 303.0, 554.6999999999999, 787.4499999999992, 1639.3600000000001, 15.75621448566017, 9.794532782145192, 10.489007054677417], "isController": false}, {"data": ["Crear arco", 1, 0, 0.0, 99.0, 99, 99, 99.0, 99.0, 99.0, 99.0, 10.101010101010102, 5.326704545454545, 5.810053661616162], "isController": false}, {"data": ["GET listar arcos", 963, 0, 0.0, 255.6749740394599, 54, 1669, 208.0, 428.6, 612.5999999999999, 1310.08, 16.0251610004493, 8.35687106859368, 8.497717210199191], "isController": false}, {"data": ["GET listar gateways", 958, 0, 0.0, 260.18371607515616, 40, 1336, 216.0, 460.0, 575.0999999999999, 959.41, 15.962410023993602, 10.52209645136297, 8.511206907324715], "isController": false}, {"data": ["GET listar lanes", 949, 0, 0.0, 258.8609062170706, 40, 1617, 211.0, 442.0, 593.5, 1062.0, 15.849951564953068, 8.01784659242743, 8.544114515482512], "isController": false}, {"data": ["Crear proceso", 1, 0, 0.0, 383.0, 383, 383, 383.0, 383.0, 383.0, 383.0, 2.6109660574412534, 1.9684236292428199, 1.6675505874673628], "isController": false}, {"data": ["Crear rol", 1, 0, 0.0, 152.0, 152, 152, 152.0, 152.0, 152.0, 152.0, 6.578947368421052, 3.629985608552632, 3.8998252467105265], "isController": false}, {"data": ["Registrar empresa", 1, 0, 0.0, 365.0, 365, 365, 365.0, 365.0, 365.0, 365.0, 2.73972602739726, 2.218000856164384, 1.1986301369863015], "isController": false}, {"data": ["Iniciar sesion", 1, 0, 0.0, 424.0, 424, 424, 424.0, 424.0, 424.0, 424.0, 2.3584905660377355, 2.489773732311321, 0.5827130011792453], "isController": false}, {"data": ["GET listar roles", 945, 0, 0.0, 270.27619047619015, 31, 1500, 216.0, 455.5999999999999, 680.0999999999998, 1261.3799999999928, 15.712290502793296, 13.91703856057961, 8.270434161138269], "isController": false}, {"data": ["GET listar actividades", 982, 0, 0.0, 676.0651731160892, 58, 3075, 531.0, 1367.7, 1651.6999999999998, 2317.249999999999, 16.16620571579086, 1218.2984057581818, 8.66723333786053], "isController": false}, {"data": ["GET listar eventos", 955, 0, 0.0, 243.1947643979059, 43, 1493, 202.0, 393.4, 532.1999999999999, 984.1599999999992, 15.895208135683493, 11.160795556207452, 8.45985198627686], "isController": false}, {"data": ["Crear evento de envio", 1, 0, 0.0, 218.0, 218, 218, 218.0, 218.0, 218.0, 218.0, 4.587155963302752, 3.2298236811926606, 3.046158256880734], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 8670, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
