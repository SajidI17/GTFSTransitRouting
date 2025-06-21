package dev.sajidislam;
import java.util.*;

public class Graph<T> {
    Map<T, List<Edge<T>>> adjacencyList;

    public Graph(){
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(T node){
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    public boolean doesNodeExist(T node){
        return adjacencyList.containsKey(node);
    }

    public void addEdge(T source, T destination, long weight){
        //make sure nodes are added
        addNode(source);
        addNode(destination);

        //add edge to the source node
        adjacencyList.get(source).add(new Edge<>(destination, weight));
    }

    public boolean removeEdge(T source, Edge<T> edge){
        return adjacencyList.get(source).remove(edge);
    }

    @Override
    public String toString() {
        return "Graph{" +
                "adjacencyList=" + adjacencyList +
                '}';
    }

    public static class Edge<T>{
        T destination;
        long weight;

        public Edge(T destination, long weight){
            this.destination = destination;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "Edge{" +
                    "destination=" + destination +
                    ", weight=" + weight +
                    '}';
        }
    }
}
