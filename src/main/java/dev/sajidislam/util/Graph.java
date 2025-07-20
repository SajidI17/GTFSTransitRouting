package dev.sajidislam;
import java.util.*;

public class Graph {
    Map<String, List<Edge>> adjacencyList;
    Map<String, BusStop> allBusStops;

    public Graph(){
        this.adjacencyList = new HashMap<>();
        this.allBusStops = new HashMap<>();
    }

    public void addNode(BusStop busStop){
        adjacencyList.putIfAbsent(busStop.stopCodeId, new ArrayList<>());
        allBusStops.putIfAbsent(busStop.stopCodeId, busStop);
    }

    public boolean doesNodeExist(String stopId){
        return adjacencyList.containsKey(stopId);
    }

    public BusStop getBusStop(String stopId){
        return allBusStops.getOrDefault(stopId, null);
    }

    public void addEdge(BusStop source, BusStop destination, long weight){
        //make sure nodes are added
        addNode(source);
        addNode(destination);

        //add edge to the source node
        adjacencyList.get(source.stopCodeId).add(new Edge(destination.stopCodeId, weight));
    }

    /// removes the edge with destinationId under node stopId
    public Edge removeEdge(String stopId, String destinationId){
       List<Edge> edgeList = adjacencyList.get(stopId);
       for(int i = 0; i < edgeList.size(); i++){
           if(edgeList.get(i).destinationId.equals(destinationId)){
               return edgeList.remove(i);
           }
       }
       return null;
    }

    public boolean removeEdge(String sourceId, Edge edge){
        return adjacencyList.get(sourceId).remove(edge);
    }

    @Override
    public String toString() {
        return "Graph{" +
                "adjacencyList=" + adjacencyList +
                '}';
    }

    public static class Edge{
        String destinationId;
        long weight;

        public Edge(String destination, long weight){
            this.destinationId = destination;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Edge{" +
                    "destination=" + destinationId +
                    ", weight=" + weight +
                    '}';
        }
    }
}
