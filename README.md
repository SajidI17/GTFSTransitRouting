# GTFSTransitRouting

## Introduction
GTFS (General Transit Feed Specification) is a type of data set provided by many transit agencies,
including OC Transpo, that gives an overview of a transit agency’s entire transit offerings. This
project presents a full-stack application that uses components of database, backend and frontend
development to generate an optimal transit route using GTFS data. The core focus of the project
is the Java algorithm that was developed to compute the shortest paths between two bus stops by
constructing a graph. To accomplish this, the algorithm introduces several unique constraints: each
bus stop node in the graph represents the earliest possible arrival time to that bus stop, maintaining
a topologically sorted graph, and restricting nodes to have at most one incoming edge. Upon
generation of the graph, we can then find the optimal route in O(length of route) which is at
most the size of the graph.