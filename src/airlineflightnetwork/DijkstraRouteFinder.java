package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class DijkstraRouteFinder {
    private static class Node implements Comparable<Node> {
        private final String airportCode;
        private final double cost;
        Node(String airportCode, double cost) { this.airportCode = airportCode; this.cost = cost; }
        @Override public int compareTo(Node other) { return Double.compare(cost, other.cost); }
    }

    // ========== 4.5 Dijkstra's Algorithm (matches proposal naming) ==========
    public RouteResult dijkstra(String fromAirport, String toAirport,
            RoutePreference preference, AirlineNetwork adjList) {
        ArrayList<String> empty = new ArrayList<>();
        AlgorithmMetrics metrics = new AlgorithmMetrics("DIJKSTRA");

        if (adjList == null || adjList.isEmpty()) return failure("The flight network is empty.", empty, metrics);
        fromAirport = adjList.normaliseCode(fromAirport);
        toAirport = adjList.normaliseCode(toAirport);
        if (fromAirport.isEmpty() || toAirport.isEmpty()) return failure("Source and destination codes cannot be empty.", empty, metrics);
        if (!adjList.airportExists(fromAirport)) return failure("Airport not found: " + fromAirport, empty, metrics);
        if (!adjList.airportExists(toAirport)) return failure("Airport not found: " + toAirport, empty, metrics);
        if (preference == null) return failure("A route preference must be selected.", empty, metrics);

        ArrayList<String> searchOrder = new ArrayList<>();
        if (fromAirport.equals(toAirport)) {
            ArrayList<String> path = new ArrayList<>(); path.add(fromAirport); searchOrder.add(fromAirport);
            return new RouteResult(true, path, "Source and destination are the same.",
                    searchOrder, 0, 0, 0, metrics);
        }

        HashMap<String, Double> distance = new HashMap<>();
        HashMap<String, String> parent = new HashMap<>();
        HashSet<String> visited = new HashSet<>();
        for (Airport airport : adjList.getAirports()) distance.put(airport.getCode(), Double.POSITIVE_INFINITY);
        distance.put(fromAirport, 0.0);
        parent.put(fromAirport, null);

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        priorityQueue.offer(new Node(fromAirport, 0));
        metrics.queueInserted();
        metrics.observeQueueSize(priorityQueue.size());

        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            metrics.queueRemoved();
            String currentAirport = current.airportCode;

            if (visited.contains(currentAirport)) continue;
            visited.add(currentAirport);
            metrics.vertexProcessed();
            searchOrder.add(currentAirport);

            if (currentAirport.equals(toAirport)) break;

            for (Edge edge : adjList.getOutgoingFlights(currentAirport)) {
                metrics.edgeExamined();
                String neighbour = edge.getDestinationCode();
                if (visited.contains(neighbour)) continue;

                double weight = weight(edge, preference);
                double newDist = distance.get(currentAirport) + weight;

                if (newDist < distance.get(neighbour)) {
                    distance.put(neighbour, newDist);
                    parent.put(neighbour, currentAirport);
                    priorityQueue.offer(new Node(neighbour, newDist));
                    metrics.relaxationSucceeded();
                    metrics.queueInserted();
                    metrics.observeQueueSize(priorityQueue.size());
                }
            }
        }

        if (distance.get(toAirport) == Double.POSITIVE_INFINITY) {
            return failure("No route found from " + fromAirport + " to " + toAirport + ".", searchOrder, metrics);
        }

        ArrayList<String> path = reconstructPath(parent, toAirport);
        return BFSRouteFinder.success(adjList, path, searchOrder,
                preference.getLabel() + " route found using Dijkstra's algorithm.", metrics);
    }

    // Backward-compatibility wrapper — keeps existing callers (GUI, tests) working unchanged
    public RouteResult findRoute(AirlineNetwork network, String source, String destination, RoutePreference preference) {
        return dijkstra(source, destination, preference, network);
    }

    private double weight(Edge edge, RoutePreference preference) {
        switch (preference) {
            case DISTANCE: return edge.getDistanceKm();
            case FARE: return edge.getFare();
            case DURATION: return edge.getDurationMinutes();
            default: throw new IllegalArgumentException("Unsupported preference.");
        }
    }

    private RouteResult failure(String message, ArrayList<String> order, AlgorithmMetrics metrics) {
        return new RouteResult(false, new ArrayList<>(), message, order, 0, 0, 0, metrics);
    }

    // ========== 4.6 Helper Function: Reconstruct Path (matches proposal naming) ==========
    private ArrayList<String> reconstructPath(HashMap<String, String> parent, String toAirport) {
        ArrayList<String> path = new ArrayList<>();
        String current = toAirport;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}