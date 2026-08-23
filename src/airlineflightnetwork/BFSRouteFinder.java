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
        if (network == null || network.isEmpty()) return failure("The flight network is empty.", empty);
        source = network.normaliseCode(source);
        destination = network.normaliseCode(destination);
        if (source.isEmpty()) return failure("Source airport code cannot be empty.", empty);
        if (destination.isEmpty()) return failure("Destination airport code cannot be empty.", empty);
        if (!network.airportExists(source)) return failure("Source airport " + source + " does not exist.", empty);
        if (!network.airportExists(destination)) return failure("Destination airport " + destination + " does not exist.", empty);

        ArrayList<String> searchOrder = new ArrayList<>();
        if (source.equals(destination)) {
            ArrayList<String> path = new ArrayList<>();
            path.add(source); searchOrder.add(source);
            return new RouteResult(true, path, "Source and destination are the same.", searchOrder, 0, 0, 0);
        }

        Queue<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        HashMap<String, String> parent = new HashMap<>();
        queue.offer(source); visited.add(source); parent.put(source, null);

        boolean found = false;
        while (!queue.isEmpty()) {
            String current = queue.poll();
            searchOrder.add(current);
            if (current.equals(destination)) { found = true; break; }
            for (FlightEdge edge : network.getOutgoingFlights(current)) {
                String neighbour = edge.getDestinationCode();
                if (visited.add(neighbour)) {
                    parent.put(neighbour, current);
                    queue.offer(neighbour);
                }
            }
        }
        if (!found) return failure("No route is available from " + source + " to " + destination + ".", searchOrder);
        ArrayList<String> path = reconstruct(parent, destination);
        return success(network, path, searchOrder,
                "A route with the minimum number of flight legs was found.");
    }

    private RouteResult failure(String message, ArrayList<String> order) {
        return new RouteResult(false, new ArrayList<>(), message, order, 0, 0, 0);
    }

    static RouteResult success(AirlineNetwork network, ArrayList<String> path,
            ArrayList<String> order, String message) {
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
        return new RouteResult(true, path, message, order, distance, fare, duration);
    }

    private ArrayList<String> reconstruct(HashMap<String, String> parent, String destination) {
        ArrayList<String> path = new ArrayList<>();
        for (String current = destination; current != null; current = parent.get(current)) path.add(current);
        Collections.reverse(path);
        return path;
    }
}
