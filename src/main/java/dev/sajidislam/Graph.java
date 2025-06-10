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

    public void addEdge(T source, T destination, float weight){
        //make sure nodes are added
        addNode(source);
        addNode(destination);

        //add edge to the source node
        adjacencyList.get(source).add(new Edge<>(source, destination, weight));
    }

    public boolean removeEdge(T source, Edge<T> edge){
        return adjacencyList.get(source).remove(edge);
    }

    public static class Edge<T>{
        T source;
        T destination;
        float weight;

        public Edge(T source, T destination, float weight){
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }
    }
}
