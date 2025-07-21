package dev.sajidislam.util;

public class BusRecord {
    String arrivalTime;
    String stopId;
    String tripId;
    String routeId;
    int stopSequence;
    String headSign;
    String serviceId;

    public BusRecord(String arrivalTime, String stopId, String tripId, String routeId, int stopSequence, String headSign, String serviceId) {
        this.arrivalTime = arrivalTime;
        this.stopId = stopId;
        this.tripId = tripId;
        this.routeId = routeId;
        this.stopSequence = stopSequence;
        this.headSign = headSign;
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "BusRecord{" +
                "arrivalTime='" + arrivalTime + '\'' +
                ", stopId='" + stopId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", stopSequence=" + stopSequence +
                ", headSign='" + headSign + '\'' +
                ", serviceId='" + serviceId + '\'' +
                '}';
    }
}
