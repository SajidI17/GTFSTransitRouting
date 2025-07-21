package dev.sajidislam.util;

import java.util.Objects;

public class BusStop {
    public String stopCodeId;
    public String tripId;
    public String routeId;
    public String arrivalTime;
    public String previousStopId;

    public BusStop(String stopCodeId, String tripId, String routeId, String arrivalTime) {
        this.stopCodeId = stopCodeId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.arrivalTime = arrivalTime;
        this.previousStopId = null;
    }

    public BusStop(String stopCodeId, String tripId, String routeId, String arrivalTime, String previousStopId) {
        this.stopCodeId = stopCodeId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.arrivalTime = arrivalTime;
        this.previousStopId = previousStopId;
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BusStop busStop = (BusStop) o;
        return Objects.equals(stopCodeId, busStop.stopCodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stopCodeId);
    }

    @Override
    public String toString() {
        if(previousStopId == null){
            return "BusStop{" +
                    "stopCodeId='" + stopCodeId + '\'' +
                    ", tripId='" + tripId + '\'' +
                    ", routeId='" + routeId + '\'' +
                    ", arrivalTime='" + arrivalTime + '\'' +
                    '}';
        }
        return "BusStop{" +
                "stopCodeId='" + stopCodeId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", previousStopId='" + previousStopId + '\'' +
                '}';

    }
}
