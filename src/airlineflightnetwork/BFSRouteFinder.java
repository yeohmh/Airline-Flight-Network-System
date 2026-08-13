package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class BFSRouteFinder {

    public RouteResult findRoute(
            HashMap<String, ArrayList<String>> adjacencyList,
            String source,
            String destination) {

        ArrayList<String> emptyPath = new ArrayList<>();
        ArrayList<String> emptySearchOrder = new ArrayList<>();

        if (adjacencyList == null || adjacencyList.isEmpty()) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "The flight network is empty.",
                    emptySearchOrder);
        }

        if (source == null || source.trim().isEmpty()) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "Source airport code cannot be empty.",
                    emptySearchOrder);
        }

        if (destination == null || destination.trim().isEmpty()) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "Destination airport code cannot be empty.",
                    emptySearchOrder);
        }

        source = source.trim().toUpperCase(Locale.ROOT);
        destination
                = destination.trim().toUpperCase(Locale.ROOT);

        if (!adjacencyList.containsKey(source)) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "Source airport " + source
                    + " does not exist.",
                    emptySearchOrder);
        }

        if (!adjacencyList.containsKey(destination)) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "Destination airport " + destination
                    + " does not exist.",
                    emptySearchOrder);
        }

        ArrayList<String> searchOrder = new ArrayList<>();

        if (source.equals(destination)) {
            ArrayList<String> path = new ArrayList<>();
            path.add(source);
            searchOrder.add(source);

            return new RouteResult(
                    true,
                    path,
                    "Source and destination are the same.",
                    searchOrder);
        }

        Queue<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        HashMap<String, String> parent = new HashMap<>();

        queue.offer(source);
        visited.add(source);
        parent.put(source, null);

        boolean destinationFound = false;

        while (!queue.isEmpty()) {
            String currentAirport = queue.poll();
            searchOrder.add(currentAirport);

            if (currentAirport.equals(destination)) {
                destinationFound = true;
                break;
            }

            ArrayList<String> neighbours
                    = adjacencyList.get(currentAirport);

            if (neighbours == null) {
                continue;
            }

            for (String neighbour : neighbours) {
                if (neighbour == null) {
                    continue;
                }

                neighbour = neighbour
                        .trim()
                        .toUpperCase(Locale.ROOT);

                if (!adjacencyList.containsKey(neighbour)) {
                    continue;
                }

                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    parent.put(neighbour, currentAirport);
                    queue.offer(neighbour);
                }
            }
        }

        if (!destinationFound) {
            return new RouteResult(
                    false,
                    emptyPath,
                    "No route is available from "
                    + source + " to " + destination + ".",
                    searchOrder);
        }

        ArrayList<String> path
                = reconstructPath(parent, destination);

        return new RouteResult(
                true,
                path,
                "A route with the minimum number "
                + "of flight legs was found.",
                searchOrder);
    }

    private ArrayList<String> reconstructPath(
            HashMap<String, String> parent,
            String destination) {

        ArrayList<String> path = new ArrayList<>();
        String currentAirport = destination;

        while (currentAirport != null) {
            path.add(currentAirport);
            currentAirport = parent.get(currentAirport);
        }

        Collections.reverse(path);
        return path;
    }
}