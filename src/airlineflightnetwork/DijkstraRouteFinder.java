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

    public RouteResult findRoute(AirlineNetwork network, String source,
            String destination, RoutePreference preference) {
        ArrayList<String> empty = new ArrayList<>();
        if (network == null || network.isEmpty()) return failure("The flight network is empty.", empty);
        source = network.normaliseCode(source);
        destination = network.normaliseCode(destination);
        if (source.isEmpty() || destination.isEmpty()) return failure("Source and destination codes cannot be empty.", empty);
        if (!network.airportExists(source)) return failure("Source airport " + source + " does not exist.", empty);
        if (!network.airportExists(destination)) return failure("Destination airport " + destination + " does not exist.", empty);
        if (preference == null) return failure("A route preference must be selected.", empty);

        ArrayList<String> searchOrder = new ArrayList<>();
        if (source.equals(destination)) {
            ArrayList<String> path = new ArrayList<>(); path.add(source); searchOrder.add(source);
            return new RouteResult(true, path, "Source and destination are the same.", searchOrder, 0, 0, 0);
        }

        HashMap<String, Double> cost = new HashMap<>();
        HashMap<String, String> parent = new HashMap<>();
        HashSet<String> processed = new HashSet<>();
        for (Airport airport : network.getAirports()) cost.put(airport.getCode(), Double.POSITIVE_INFINITY);
        cost.put(source, 0.0); parent.put(source, null);

        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.offer(new Node(source, 0));
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (!processed.add(node.airportCode)) continue;
            searchOrder.add(node.airportCode);
            if (node.airportCode.equals(destination)) break;
            for (FlightEdge edge : network.getOutgoingFlights(node.airportCode)) {
                String neighbour = edge.getDestinationCode();
                if (processed.contains(neighbour)) continue;
                double newCost = cost.get(node.airportCode) + weight(edge, preference);
                if (newCost < cost.get(neighbour)) {
                    cost.put(neighbour, newCost);
                    parent.put(neighbour, node.airportCode);
                    queue.offer(new Node(neighbour, newCost));
                }
            }
        }
        if (!processed.contains(destination)) return failure("No route is available from " + source + " to " + destination + ".", searchOrder);
        ArrayList<String> path = reconstruct(parent, destination);
        return BFSRouteFinder.success(network, path, searchOrder,
                preference.getLabel() + " route found using Dijkstra's algorithm.");
    }

    private double weight(FlightEdge edge, RoutePreference preference) {
        switch (preference) {
            case DISTANCE: return edge.getDistanceKm();
            case FARE: return edge.getFare();
            case DURATION: return edge.getDurationMinutes();
            default: throw new IllegalArgumentException("Unsupported preference.");
        }
    }

    private RouteResult failure(String message, ArrayList<String> order) {
        return new RouteResult(false, new ArrayList<>(), message, order, 0, 0, 0);
    }

    private ArrayList<String> reconstruct(HashMap<String, String> parent, String destination) {
        ArrayList<String> path = new ArrayList<>();
        for (String current = destination; current != null; current = parent.get(current)) path.add(current);
        Collections.reverse(path);
        return path;
    }
}
