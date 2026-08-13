package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class BFSRouteFinder {

    public RouteResult findRoute(
            HashMap<String, ArrayList<String>> adjacencyList,
            String source,
            String destination) {

        source = source.toUpperCase();
        destination = destination.toUpperCase();

        if (!adjacencyList.containsKey(source)) {
            return new RouteResult(
                    false,
                    new ArrayList<>(),
                    "Source airport does not exist.");
        }

        if (!adjacencyList.containsKey(destination)) {
            return new RouteResult(
                    false,
                    new ArrayList<>(),
                    "Destination airport does not exist.");
        }

        if (source.equals(destination)) {
            ArrayList<String> path = new ArrayList<>();
            path.add(source);

            return new RouteResult(
                    true,
                    path,
                    "Source and destination are the same.");
        }

        Queue<String> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        HashMap<String, String> parent = new HashMap<>();

        queue.offer(source);
        visited.add(source);
        parent.put(source, null);

        boolean destinationFound = false;

        while (!queue.isEmpty() && !destinationFound) {
            String currentAirport = queue.poll();

            for (String neighbour : adjacencyList.get(currentAirport)) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    parent.put(neighbour, currentAirport);

                    if (neighbour.equals(destination)) {
                        destinationFound = true;
                        break;
                    }

                    queue.offer(neighbour);
                }
            }
        }

        if (!destinationFound) {
            return new RouteResult(
                    false,
                    new ArrayList<>(),
                    "No route available.");
        }

        ArrayList<String> path
                = reconstructPath(parent, destination);

        return new RouteResult(
                true,
                path,
                "Route found.");
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