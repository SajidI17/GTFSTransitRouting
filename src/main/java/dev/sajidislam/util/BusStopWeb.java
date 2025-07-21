package dev.sajidislam.util;

public class BusStopWeb extends BusStop{
    public double latPoint;
    public double lonPoint;

    public BusStopWeb(String stopCodeId, String tripId, String routeId, String arrivalTime) {
        super(stopCodeId, tripId, routeId, arrivalTime);
    }

    public BusStopWeb(String stopCodeId, String tripId, String routeId, String arrivalTime, String previousStopId) {
        super(stopCodeId, tripId, routeId, arrivalTime, previousStopId);
    }

    public BusStopWeb(BusStop busStop, double latPoint, double lonPoint){
        super(busStop.stopCodeId, busStop.tripId, busStop.routeId, busStop.arrivalTime, busStop.previousStopId);
        this.latPoint = latPoint;
        this.lonPoint = lonPoint;
    }
}
