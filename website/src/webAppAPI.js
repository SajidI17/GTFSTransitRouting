var map;

window.onload = function(){
    pageLoad();
}

function pageLoad(){    
    document.getElementById("routeForm").addEventListener("submit", function (e) {
        e.preventDefault();
        fetchAPIData();
        
    });
    map = L.map('map').setView([45.414, -75.715], 13);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {maxZoom: 19, attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'}).addTo(map);
}

function fetchAPIData(){
    var busStopOrigin = document.getElementById("busStopOrigin").value;
    var busStopDest = document.getElementById("busStopDest").value;
    var time = document.getElementById("time").value;
    var date = document.getElementById("date").value;
    var weekDayType = document.getElementById("weekDayType").value;

    document.getElementById("messageBox").textContent = "busStopOrigin: " + busStopOrigin + ", busStopDest:" + busStopDest + ", time:" + time + ", date:" + date + ", weekDayType:" + weekDayType;

    var debugMode = document.getElementById("debugMode").checked;
    var urlMapping;
    if(debugMode){
        urlMapping = "apitest"
    }
    else{
        urlMapping = "getRoute"
    }

    var apiUrl = "http://localhost:8080/" + urlMapping + "?busStopOrigin=" + busStopOrigin + "&busStopDest=" + busStopDest + "&time=" + time + "&date=" + date + "&weekDayType=" + weekDayType;

    fetch(apiUrl)
    .then(function(response){
        if(!response.ok){
            throw new Error("Error in API");
        }
        return response.json();
    })
    .then(function(data){
        if((data==null) || data.length === 0){
            throw new Error("No data was returned");
        }
        document.getElementById("messageBox").textContent = JSON.stringify(data);
        
        var latAndLon = [];
        for (let i = 0; i < data.length; i++){
            var lat = data[i].latPoint;
            var lon = data[i].lonPoint;

            var point = L.marker([data[i].latPoint, data[i].lonPoint]).addTo(map);
            
            // var point = L.circle([lat, lon], {
            //     color: 'red',
            //     fillColor: 'red',
            //     fillOpacity: 1,
            //     radius: 40
            // }).addTo(map);

            var stringToolTip = "StopCode = " + data[i].stopCodeId + ", Route = " + data[i].routeId + ", Time = " + data[i].arrivalTime
            point.bindTooltip(stringToolTip, {direction: 'top'});

            latAndLon.push([lat,lon]);
        }
        var route = L.polyline(latAndLon).addTo(map)
        
    })
    .catch(function(error){
        throw new Error(error)
    })
}