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

    var getDatesAPIUrl = "http://localhost:8080/getDates"
    fetch(getDatesAPIUrl)
    .then(function(response) {
        if(!response.ok){
            throw new Error("Error in API");
        }
        return response.json();
    })
    .then(function(data) {
        for(let i = 0; i < data.length; i++){
            var selectElement = document.getElementById("dates");
            var newSelect = new Option(data[i], data[i]);
            selectElement.add(newSelect);
        }
    })
    .catch(function(error){
        throw new Error(error)
    })
}

function fetchAPIData(){
    var busStopOrigin = document.getElementById("busStopOrigin").value;
    var busStopDest = document.getElementById("busStopDest").value;
    var time = document.getElementById("time").value;
    var date = document.getElementById("dates").value

    document.getElementById("messageBox").textContent = "busStopOrigin: " + busStopOrigin + ", busStopDest:" + busStopDest + ", time:" + time + ", date:" + date;

    var debugMode = document.getElementById("debugMode").checked;
    var urlMapping;
    if(debugMode){
        urlMapping = "apitest"
    }
    else{
        urlMapping = "getRoute"
    }

    var apiUrl = "http://localhost:8080/" + urlMapping + "?busStopOrigin=" + busStopOrigin + "&busStopDest=" + busStopDest + "&time=" + time + "&date=" + date;

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
        var previousRouteId;
        var resultHTML = "";
        //go through the array backwards as the last element is the first bus in the route
        for (let i = data.length-1; i >= 0; i--){
            //set html text for routing provided by API
            if(i === (data.length - 1)){
                //initial html tag
                var index = data.length - 1
                previousRouteId = data[index].routeId;

                //set different text for walking if needed
                if(data[index].routeId === "Walking"){
                    resultHTML = "<h3>Walking</h3>";
                    resultHTML += "<p>Walk to stop: " + data[index].stopCodeId;
                }
                else{
                    resultHTML = "<h3>Route: " + data[index].routeId + "</h3>";
                    resultHTML += "<p>Board at stop: " + data[index].stopCodeId + " at " + data[index].arrivalTime;
                }
                
            }
            else if(i === 0 && data[i].routeId !== "Walking"){
                //if we reach the end of the array, then we know we have finished the route
                resultHTML += "<p>Drop off stop: " + data[i].stopCodeId + " at " + data[i].arrivalTime;
            }
            else if(data[i].routeId !== previousRouteId){
                //if a new route was found, set the drop off information and the board stop information for the new route
                previousRouteId = data[i].routeId;

                if(data[i+1].routeId !== "Walking"){
                    resultHTML += "<p>Drop off stop: " + data[i+1].stopCodeId + " at " + data[i+1].arrivalTime;
                }

                if(data[i].routeId !== "Walking"){
                    resultHTML += "<h3>Route: " + data[i].routeId + "</h3>";
                    resultHTML += "<p>Board at stop: " + data[i].stopCodeId + " at " + data[i].arrivalTime;
                }
                else{
                    resultHTML += "<h3>Walking</h3>";
                    resultHTML += "<p>Walk to stop: " + data[i].stopCodeId;
                }
                
            }

            //avoid creating edges/points on map made for walking
            if(data[i].routeId === "Walking" && (i != 0) && (i != (data.length-1))){
                continue;
            }

            //set edges and points on map
            var lat = data[i].latPoint;
            var lon = data[i].lonPoint;

            var point = L.marker([data[i].latPoint, data[i].lonPoint]).addTo(map);

            var stringToolTip = "StopCode = " + data[i].stopCodeId + ", Route = " + data[i].routeId + ", Time = " + data[i].arrivalTime
            point.bindTooltip(stringToolTip, {direction: 'top'});

            latAndLon.push([lat,lon]);
        }
        var route = L.polyline(latAndLon).addTo(map);
        document.getElementById("routeResult").innerHTML = resultHTML;
        
    })
    .catch(function(error){
        throw new Error(error)
    })
}