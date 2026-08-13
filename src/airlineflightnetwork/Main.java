package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        HashMap<String, ArrayList<String>> adjacencyList
                = createTestGraph();

        BFSRouteFinder bfs = new BFSRouteFinder();

        runTest(
                "TEST 1 - Route requiring transfers",
                bfs.findRoute(adjacencyList, "KUL", "NRT"));

        runTest(
                "TEST 2 - Direct flight",
                bfs.findRoute(adjacencyList, "KUL", "PEN"));

        runTest(
                "TEST 3 - Same source and destination",
                bfs.findRoute(adjacencyList, "KUL", "KUL"));

        runTest(
                "TEST 4 - Invalid source airport",
                bfs.findRoute(adjacencyList, "XXX", "NRT"));

        runTest(
                "TEST 5 - Unavailable directed route",
                bfs.findRoute(adjacencyList, "NRT", "KUL"));

        runTest(
                "TEST 6 - Lowercase and spaced input",
                bfs.findRoute(adjacencyList, " kul ", " nrt "));
    }

    private static HashMap<String, ArrayList<String>>
            createTestGraph() {

        HashMap<String, ArrayList<String>> adjacencyList
                = new HashMap<>();

        adjacencyList.put(
                "KUL",
                new ArrayList<>(
                        Arrays.asList("PEN", "JHB", "BKK")));

        adjacencyList.put(
                "PEN",
                new ArrayList<>(
                        Arrays.asList("LGK", "BKK", "KCH")));

        adjacencyList.put(
                "JHB",
                new ArrayList<>(
                        Arrays.asList("KCH", "BKI")));

        adjacencyList.put(
                "LGK",
                new ArrayList<>(
                        Arrays.asList("PEN")));

        adjacencyList.put(
                "KCH",
                new ArrayList<>(
                        Arrays.asList("BKI", "SIN")));

        adjacencyList.put(
                "BKI",
                new ArrayList<>(
                        Arrays.asList("MNL", "NRT")));

        adjacencyList.put(
                "BKK",
                new ArrayList<>(
                        Arrays.asList("SIN", "HKG")));

        adjacencyList.put(
                "SIN",
                new ArrayList<>(
                        Arrays.asList("HKG", "NRT")));

        adjacencyList.put(
                "MNL",
                new ArrayList<>(
                        Arrays.asList("NRT")));

        adjacencyList.put(
                "HKG",
                new ArrayList<>(
                        Arrays.asList("NRT")));

        adjacencyList.put(
                "NRT",
                new ArrayList<>());

        return adjacencyList;
    }

    private static void runTest(
            String testTitle,
            RouteResult result) {

        System.out.println(
                "\n========================================");
        System.out.println(testTitle);
        System.out.println(
                "========================================");

        displayResult(result);
    }

    private static void displayResult(RouteResult result) {

        System.out.println(
                "Status       : " + result.getMessage());

        if (!result.getSearchOrder().isEmpty()) {
            System.out.println(
                    "Search Order : "
                    + String.join(
                            " -> ",
                            result.getSearchOrder()));
        }

        if (result.isRouteFound()) {
            System.out.println(
                    "Route        : "
                    + String.join(
                            " -> ",
                            result.getPath()));

            System.out.println(
                    "Flight Legs  : "
                    + result.getFlightLegs());

            System.out.println(
                    "Transfers    : "
                    + result.getTransfers());
        }
    }
}