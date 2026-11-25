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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.7761712192888753, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.8498483316481295, 500, 1500, "GET listar procesos"], "isController": false}, {"data": [0.8554542384329619, 500, 1500, "GET detalle proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear gateway"], "isController": false}, {"data": [1.0, 500, 1500, "Crear lane"], "isController": false}, {"data": [1.0, 500, 1500, "Crear actividad inicial"], "isController": false}, {"data": [0.816933240611961, 500, 1500, "POST crear actividad"], "isController": false}, {"data": [1.0, 500, 1500, "Crear arco"], "isController": false}, {"data": [0.8633229920716994, 500, 1500, "GET listar arcos"], "isController": false}, {"data": [0.8361562391980643, 500, 1500, "GET listar gateways"], "isController": false}, {"data": [0.8532269257460097, 500, 1500, "GET listar lanes"], "isController": false}, {"data": [1.0, 500, 1500, "Crear proceso"], "isController": false}, {"data": [1.0, 500, 1500, "Crear rol"], "isController": false}, {"data": [0.5, 500, 1500, "Registrar empresa"], "isController": false}, {"data": [1.0, 500, 1500, "Iniciar sesion"], "isController": false}, {"data": [0.8482111844390413, 500, 1500, "GET listar roles"], "isController": false}, {"data": [0.22844098748731823, 500, 1500, "GET listar actividades"], "isController": false}, {"data": [0.8425701420159335, 500, 1500, "GET listar eventos"], "isController": false}, {"data": [1.0, 500, 1500, "Crear evento de envio"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 26212, 0, 0.0, 649.9625362429426, 14, 12440, 379.0, 1646.9000000000015, 2741.9500000000007, 5336.920000000013, 138.20666673696866, 3720.5521284941187, 75.36556653126945], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET listar procesos", 2967, 0, 0.0, 423.3137849679811, 16, 3947, 298.0, 908.0, 1235.6, 1873.2800000000047, 16.329743028074827, 14.56388123613733, 8.515705836906209], "isController": false}, {"data": ["GET detalle proceso", 2961, 0, 0.0, 414.8905775075979, 22, 3250, 295.0, 911.0, 1237.9, 1897.9800000000023, 16.363274644384763, 12.251624549609293, 8.581131332065056], "isController": false}, {"data": ["Crear gateway", 1, 0, 0.0, 95.0, 95, 95, 95.0, 95.0, 95.0, 95.0, 10.526315789473683, 6.990131578947368, 6.589226973684211], "isController": false}, {"data": ["Crear lane", 1, 0, 0.0, 110.0, 110, 110, 110.0, 110.0, 110.0, 110.0, 9.09090909090909, 4.731889204545454, 5.211292613636363], "isController": false}, {"data": ["Crear actividad inicial", 1, 0, 0.0, 126.0, 126, 126, 126.0, 126.0, 126.0, 126.0, 7.936507936507936, 4.611545138888889, 5.138578869047619], "isController": false}, {"data": ["POST crear actividad", 2876, 0, 0.0, 482.89777468706484, 42, 3715, 374.0, 969.0, 1264.9000000000005, 1847.9500000000012, 15.885026870881687, 9.919851442066049, 10.587583488216028], "isController": false}, {"data": ["Crear arco", 1, 0, 0.0, 189.0, 189, 189, 189.0, 189.0, 189.0, 189.0, 5.291005291005291, 2.800512566137566, 3.0536954365079363], "isController": false}, {"data": ["GET listar arcos", 2901, 0, 0.0, 402.21371940710077, 21, 3267, 289.0, 843.6000000000004, 1139.9, 1985.96, 15.992370409980209, 8.36975741321617, 8.48032923107349], "isController": false}, {"data": ["GET listar gateways", 2893, 0, 0.0, 433.756308330453, 21, 3915, 322.0, 927.3999999999996, 1149.5999999999995, 1721.0, 16.007835151944402, 10.567359014989709, 8.535427727501606], "isController": false}, {"data": ["GET listar lanes", 2882, 0, 0.0, 419.2012491325467, 14, 3570, 298.5, 902.0, 1187.5499999999997, 1771.3400000000001, 16.026157892688133, 8.122632760063615, 8.639100739027198], "isController": false}, {"data": ["Crear proceso", 1, 0, 0.0, 207.0, 207, 207, 207.0, 207.0, 207.0, 207.0, 4.830917874396135, 3.6420591787439616, 3.0853713768115942], "isController": false}, {"data": ["Crear rol", 1, 0, 0.0, 77.0, 77, 77, 77.0, 77.0, 77.0, 77.0, 12.987012987012989, 7.191051136363637, 7.6983563311688314], "isController": false}, {"data": ["Registrar empresa", 1, 0, 0.0, 683.0, 683, 683, 683.0, 683.0, 683.0, 683.0, 1.4641288433382138, 1.1853152452415812, 0.6405563689604685], "isController": false}, {"data": ["Iniciar sesion", 1, 0, 0.0, 356.0, 356, 356, 356.0, 356.0, 356.0, 356.0, 2.8089887640449436, 2.9653484901685396, 0.6940177317415731], "isController": false}, {"data": ["GET listar roles", 2879, 0, 0.0, 422.61479680444626, 20, 3366, 303.0, 925.0, 1217.0, 1906.199999999999, 15.95075709307286, 14.143835391123202, 8.395955149576437], "isController": false}, {"data": ["GET listar actividades", 2957, 0, 0.0, 2395.1258031789016, 28, 12440, 2026.0, 4740.000000000002, 5797.699999999999, 8030.520000000004, 16.223452407731514, 3782.887959139271, 8.697925167816994], "isController": false}, {"data": ["GET listar eventos", 2887, 0, 0.0, 426.938344302043, 21, 3960, 310.0, 923.2000000000003, 1142.3999999999996, 1720.2399999999998, 15.984364445724031, 11.239006250899708, 8.507303342694918], "isController": false}, {"data": ["Crear evento de envio", 1, 0, 0.0, 215.0, 215, 215, 215.0, 215.0, 215.0, 215.0, 4.651162790697675, 3.2839752906976747, 3.0886627906976747], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 26212, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
