package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class BFSRouteFinder {
    public RouteResult findRoute(AirlineNetwork network, String source, String destination) {
        ArrayList<String> empty = new ArrayList<>();
        AlgorithmMetrics metrics = new AlgorithmMetrics("BFS");
        if (network == null || network.isEmpty()) return failure("The flight network is empty.", empty, metrics);
        source = network.normaliseCode(source);
        destination = network.normaliseCode(destination);
        if (source.isEmpty()) return failure("Source airport code cannot be empty.", empty, metrics);
        if (destination.isEmpty()) return failure("Destination airport code cannot be empty.", empty, metrics);
        if (!network.airportExists(source)) return failure("Source airport " + source + " does not exist.", empty, metrics);
        if (!network.airportExists(destination)) return failure("Destination airport " + destination + " does not exist.", empty, metrics);

        ArrayList<String> searchOrder = new ArrayList<>();
        if (source.equals(destination)) {
            ArrayList<String> path = new ArrayList<>();
            path.add(source); searchOrder.add(source);
            return new RouteResult(true, path, "Source and destination are the same.",
                    searchOrder, 0, 0, 0, metrics);
        }

        Queue<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        HashMap<String, String> parent = new HashMap<>();
        queue.offer(source); metrics.queueInserted(); metrics.observeQueueSize(queue.size());
        visited.add(source); parent.put(source, null);

        boolean found = false;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            metrics.queueRemoved();
            metrics.vertexProcessed();
            searchOrder.add(current);
            if (current.equals(destination)) { found = true; break; }
            for (FlightEdge edge : network.getOutgoingFlights(current)) {
                metrics.edgeExamined();
                String neighbour = edge.getDestinationCode();
                if (visited.add(neighbour)) {
                    parent.put(neighbour, current);
                    queue.offer(neighbour);
                    metrics.queueInserted();
                    metrics.observeQueueSize(queue.size());
                }
            }
        }
        if (!found) return failure("No route is available from " + source + " to " + destination + ".", searchOrder, metrics);
        ArrayList<String> path = reconstruct(parent, destination);
        return success(network, path, searchOrder,
                "A route with the minimum number of flight legs was found.", metrics);
    }

    private RouteResult failure(String message, ArrayList<String> order, AlgorithmMetrics metrics) {
        return new RouteResult(false, new ArrayList<>(), message, order, 0, 0, 0, metrics);
    }

    static RouteResult success(AirlineNetwork network, ArrayList<String> path,
            ArrayList<String> order, String message, AlgorithmMetrics metrics) {
        int distance = 0, duration = 0;
        double fare = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            FlightEdge edge = network.getFlight(path.get(i), path.get(i + 1));
            if (edge != null) {
                distance += edge.getDistanceKm();
                fare += edge.getFare();
                duration += edge.getDurationMinutes();
            }
        }
        return new RouteResult(true, path, message, order, distance, fare, duration, metrics);
    }

    private ArrayList<String> reconstruct(HashMap<String, String> parent, String destination) {
        ArrayList<String> path = new ArrayList<>();
        for (String current = destination; current != null; current = parent.get(current)) path.add(current);
        Collections.reverse(path);
        return path;
    }
}
