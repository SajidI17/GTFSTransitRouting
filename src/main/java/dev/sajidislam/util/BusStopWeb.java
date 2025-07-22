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

    public BusStopWeb(String stopCodeId, String tripId, String routeId, String arrivalTime, String previousStopId, double latPoint, double lonPoint){
        super(stopCodeId, tripId, routeId, arrivalTime, previousStopId);
        this.latPoint = latPoint;
        this.lonPoint = lonPoint;
    }



    @Override
    public String toString() {
        if(previousStopId == null){
            return "BusStopWeb{" +
                    "stopCodeId='" + stopCodeId + '\'' +
                    ", tripId='" + tripId + '\'' +
                    ", routeId='" + routeId + '\'' +
                    ", arrivalTime='" + arrivalTime + '\'' +
                    ", latPoint='" + latPoint + '\'' +
                    ", lonPoint='" + lonPoint + '\'' +
                    '}';
        }
        return "new BusStopWeb(\"" + stopCodeId +  "\"," +
                "\"" + tripId +  "\"," +
                "\"" + routeId +  "\"," +
                "\"" + arrivalTime +  "\"," +
                "\"" + previousStopId +  "\"," +
                latPoint +  "," +
                lonPoint +  "),";

    }

//    @Override
//    public String toString() {
//        if(previousStopId == null){
//            return "BusStopWeb{" +
//                    "stopCodeId='" + stopCodeId + '\'' +
//                    ", tripId='" + tripId + '\'' +
//                    ", routeId='" + routeId + '\'' +
//                    ", arrivalTime='" + arrivalTime + '\'' +
//                    ", latPoint='" + latPoint + '\'' +
//                    ", lonPoint='" + lonPoint + '\'' +
//                    '}';
//        }
//        return "BusStopWeb{" +
//                "stopCodeId='" + stopCodeId + '\'' +
//                ", tripId='" + tripId + '\'' +
//                ", routeId='" + routeId + '\'' +
//                ", arrivalTime='" + arrivalTime + '\'' +
//                ", previousStopId='" + previousStopId + '\'' +
//                ", latPoint='" + latPoint + '\'' +
//                ", lonPoint='" + lonPoint + '\'' +
//                '}';
//
//    }
}
