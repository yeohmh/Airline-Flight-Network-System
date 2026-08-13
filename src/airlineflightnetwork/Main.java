package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        HashMap<String, ArrayList<String>> adjacencyList
                = createTestGraph();

        BFSRouteFinder bfs = new BFSRouteFinder();

        System.out.println("TEST 1: Route requiring transfers");
        displayResult(
                bfs.findRoute(adjacencyList, "KUL", "NRT"));

        System.out.println("\nTEST 2: Same source and destination");
        displayResult(
                bfs.findRoute(adjacencyList, "KUL", "KUL"));

        System.out.println("\nTEST 3: Invalid airport");
        displayResult(
                bfs.findRoute(adjacencyList, "XXX", "NRT"));

        System.out.println("\nTEST 4: Unavailable route");
        displayResult(
                bfs.findRoute(adjacencyList, "NRT", "KUL"));
    }

    private static HashMap<String, ArrayList<String>>
            createTestGraph() {

        HashMap<String, ArrayList<String>> adjacencyList
                = new HashMap<>();

        adjacencyList.put(
                "KUL",
                new ArrayList<>(Arrays.asList("PEN", "JHB")));

        adjacencyList.put(
                "PEN",
                new ArrayList<>(Arrays.asList("LGK", "BKK")));

        adjacencyList.put(
                "JHB",
                new ArrayList<>(Arrays.asList("BKI")));

        adjacencyList.put(
                "LGK",
                new ArrayList<>());

        adjacencyList.put(
                "BKK",
                new ArrayList<>(Arrays.asList("NRT")));

        adjacencyList.put(
                "BKI",
                new ArrayList<>(Arrays.asList("NRT")));

        adjacencyList.put(
                "NRT",
                new ArrayList<>());

        return adjacencyList;
    }

    private static void displayResult(RouteResult result) {

        System.out.println(result.getMessage());

        if (result.isRouteFound()) {
            System.out.println(
                    "Route: "
                    + String.join(" -> ", result.getPath()));

            System.out.println(
                    "Flight legs: " + result.getFlightLegs());

            System.out.println(
                    "Transfers: " + result.getTransfers());
        }
    }
}