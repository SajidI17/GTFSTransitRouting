package dev.sajidislam;

import java.util.Objects;

public class BusStop {
    String stopCodeId;
    String tripId;
    String routeId;
    String arrivalTime;
    String previousStopId;

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
        return "BusStop{" +
                "stopCodeId='" + stopCodeId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", arrivalTime='" + arrivalTime + '\'' +
                '}';
    }
}
