package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final AirlineNetwork NETWORK = InitialData.createNetwork();
    private static final BFSRouteFinder BFS = new BFSRouteFinder();
    private static final DijkstraRouteFinder DIJKSTRA = new DijkstraRouteFinder();

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("      AIRLINE FLIGHT NETWORK SYSTEM");
        System.out.println("============================================");
        System.out.println("Loaded " + NETWORK.getAirportCount() + " airports and "
                + NETWORK.getFlightCount() + " directed flights.");

        int choice;
        do {
            displayMenu();
            choice = readInt("Enter your choice: ");
            System.out.println();
            switch (choice) {
                case 1 ->
                    NETWORK.displayAirports();
                case 2 ->
                    NETWORK.displayFlights();
                case 3 ->
                    addAirport();
                case 4 ->
                    removeAirport();
                case 5 ->
                    addFlight();
                case 6 ->
                    removeFlight();
                case 7 ->
                    runBfs();
                case 8 ->
                    runDijkstra();
                case 9 ->
                    compareAlgorithms();
                case 10 ->
                    displaySummary();
                case 11 ->
                    displayAlgorithmAnalysis();
                case 12 ->
                    GraphWindow.show(NETWORK, null, null,
                            "Complete Airline Flight Network");
                case 0 ->
                    System.out.println("Thank you for using the system.");
                default ->
                    System.out.println("Invalid choice. Please enter 0 to 12.");
            }
        } while (choice != 0);
    }

    private static void displayMenu() {
        System.out.println("\n================ MAIN MENU ================");
        System.out.println("1. Display all airports");
        System.out.println("2. Display complete flight network");
        System.out.println("3. Add airport");
        System.out.println("4. Remove airport");
        System.out.println("5. Add directed flight");
        System.out.println("6. Remove directed flight");
        System.out.println("7. BFS - minimum number of flight legs");
        System.out.println("8. Dijkstra - optimise a weighted route");
        System.out.println("9. Compare BFS and Dijkstra");
        System.out.println("10. Display network summary");
        System.out.println("11. Display algorithm complexity analysis");
        System.out.println("12. Display JavaFX airline graph");
        System.out.println("0. Exit");
        System.out.println("===========================================");
    }

    private static void addAirport() {
        String code = readRequired("Airport code: ");
        String name = readRequired("Airport name: ");
        String city = readRequired("City: ");
        String country = readRequired("Country: ");
        try {
            NETWORK.addAirport(new Airport(code, name, city, country));
        } catch (IllegalArgumentException ex) {
            System.out.println("Unable to add airport: " + ex.getMessage());
        }
    }

    private static void removeAirport() {
        displayAirportDirectory();
        NETWORK.removeAirport(readRequired("Airport code to remove: "));
    }

    private static void addFlight() {
        displayAirportDirectory();
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        int distance = readInt("Distance (km): ");
        double fare = readDouble("Fare (RM): ");
        int duration = readInt("Duration (minutes): ");
        NETWORK.addFlight(source, destination, distance, fare, duration);
    }

    private static void removeFlight() {
        displayAirportDirectory();
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        NETWORK.removeFlight(source, destination);
    }

    private static void runBfs() {
        displayAirportDirectory();
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        RouteResult result = BFS.findRoute(NETWORK, source, destination);
        displayRouteResult("BFS - MINIMUM FLIGHT LEGS", result);
        offerGraph(result, null, "BFS Traversal and Route");
    }

    private static void runDijkstra() {
        displayAirportDirectory();
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        RoutePreference preference = readPreference();
        RouteResult result = DIJKSTRA.findRoute(NETWORK, source, destination, preference);
        displayRouteResult("DIJKSTRA - " + preference.getLabel().toUpperCase(), result);
        offerGraph(result, null, "Dijkstra Weighted Route");
    }

    private static void compareAlgorithms() {
        displayAirportDirectory();
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        RoutePreference preference = readPreference();
        RouteResult bfsResult = BFS.findRoute(NETWORK, source, destination);
        RouteResult dijkstraResult = DIJKSTRA.findRoute(NETWORK, source, destination, preference);
        displayRouteResult("BFS - MINIMUM FLIGHT LEGS", bfsResult);
        displayRouteResult("DIJKSTRA - " + preference.getLabel().toUpperCase(), dijkstraResult);
        if (bfsResult.isRouteFound() && dijkstraResult.isRouteFound()) {
            System.out.println("\nComparison conclusion:");
            System.out.println("BFS minimises flight legs without using edge weights.");
            System.out.println("Dijkstra minimises " + preference.getLabel().toLowerCase()
                    + " and may select more flight legs when the total weight is lower.");
        }
        offerGraph(bfsResult, dijkstraResult, "BFS and Dijkstra Route Comparison");
    }

    private static RoutePreference readPreference() {
        while (true) {
            System.out.println("1. Shortest distance");
            System.out.println("2. Cheapest fare");
            System.out.println("3. Fastest flight time");
            int choice = readInt("Select optimisation criterion: ");
            if (choice == 1) {
                return RoutePreference.DISTANCE;
            }
            if (choice == 2) {
                return RoutePreference.FARE;
            }
            if (choice == 3) {
                return RoutePreference.DURATION;
            }
            System.out.println("Invalid criterion. Please enter 1, 2 or 3.");
        }
    }

    private static void displayRouteResult(String heading, RouteResult result) {
        System.out.println("\n============================================");
        System.out.println(heading);
        System.out.println("============================================");
        System.out.println("Status         : " + result.getMessage());
        if (heading.startsWith("BFS")) {
            System.out.println("Algorithm      : Breadth-First Search (BFS)");
            System.out.println("Optimised For  : Minimum flight legs / transfers");
        } else {
            System.out.println("Algorithm      : Dijkstra's Algorithm");
            System.out.println("Optimised For  : "
                    + heading.replace("DIJKSTRA - ", ""));
        }
        if (!result.getSearchOrder().isEmpty()) {
            System.out.println("Search Order   : " + join(result.getSearchOrder()));
        }
        if (result.isRouteFound()) {
            System.out.println("Route          : " + join(result.getPath()));
            System.out.println("Flight Legs    : " + result.getFlightLegs());
            System.out.println("Transfers      : " + result.getTransfers());
            System.out.println("Total Distance : " + result.getTotalDistanceKm() + " km");
            System.out.printf("Total Fare     : RM %.2f%n", result.getTotalFare());
            System.out.println("Flight Time    : " + result.getFormattedDuration());
        }
    }

    private static void displaySummary() {
        System.out.println("========== NETWORK SUMMARY ==========");
        System.out.println("Graph type       : Directed weighted graph");
        System.out.println("Representation   : Adjacency list");
        System.out.println("Airports         : " + NETWORK.getAirportCount());
        System.out.println("Directed flights : " + NETWORK.getFlightCount());
        System.out.println("Edge weights     : Distance, fare and duration");
        System.out.println("Algorithms       : BFS and Dijkstra");
    }

    private static void displayAlgorithmAnalysis() {
        int vertices = NETWORK.getAirportCount();
        int edges = NETWORK.getFlightCount();

        System.out.println("========== ALGORITHM COMPLEXITY ANALYSIS ==========");
        System.out.println("Current airline network: V = " + vertices
                + " airports, E = " + edges + " directed flights");
        double logV = Math.log(vertices) / Math.log(2);
        double dijkstraScale = (vertices + edges) * logV;
        System.out.println("Graph storage (adjacency list): O(V + E)");
        System.out.println("Current storage scale: V + E = " + vertices + " + " + edges
                + " = " + (vertices + edges) + " vertices/edge records");

        System.out.println("\nBFS - Minimum Number of Flight Legs");
        System.out.println("Best-case time      : Theta(1)");
        System.out.println("  Source and destination are the same, so no edge is explored.");
        System.out.println("Worst-case time     : O(V + E)");
        System.out.println("  Every reachable airport and directed flight may be examined.");
        System.out.println("  Current-data substitution: O(" + vertices + " + " + edges
                + ") = O(" + (vertices + edges) + ") graph elements examined.");
        System.out.println("Best auxiliary space: Theta(1)");
        System.out.println("Worst auxiliary space: O(V)");
        System.out.println("  Queue, visited set, parent map, search order and path store airports.");
        System.out.println("  With V = " + vertices + ", each individual structure stores at most "
                + vertices + " airport records (constant multiples are ignored by Big O).");

        System.out.println("\nDijkstra - Distance, Fare or Duration");
        System.out.println("Best-case time      : Theta(1)");
        System.out.println("  Source and destination are the same, so the method returns early.");
        System.out.println("Distinct-airport setup: Theta(V)");
        System.out.println("  The cost map is initialised for all airports before relaxation.");
        System.out.println("Worst-case time     : O((V + E) log V)");
        System.out.println("  PriorityQueue operations occur while vertices and edges are processed.");
        System.out.printf("  Current-data substitution: O((%d + %d) log2 %d) = O(%.2f).%n",
                vertices, edges, vertices, dijkstraScale);
        System.out.println("  This is a growth-scale illustration, not an exact Java statement count.");
        System.out.println("Best auxiliary space: Theta(1)");
        System.out.println("Worst auxiliary space: O(V + E)");
        System.out.println("  Maps and sets store V airports; duplicate queue entries can grow with E.");
        System.out.println("  Current-data storage scale: O(" + vertices + " + " + edges
                + ") = O(" + (vertices + edges) + ") records.");
        System.out.println("===================================================");
    }

    private static void displayAirportDirectory() {
        System.out.println("\n================ AIRPORT CODE DIRECTORY ================");
        System.out.printf("%-6s %-34s %-18s %-16s%n", "CODE", "AIRPORT", "CITY", "COUNTRY");
        System.out.println("--------------------------------------------------------------------------");
        for (Airport airport : NETWORK.getAirports()) {
            System.out.printf("%-6s %-34s %-18s %-16s%n",
                    airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry());
        }
        System.out.println("========================================================");
    }

    private static void offerGraph(RouteResult primary, RouteResult secondary, String title) {
        if (readYesNo("Display this result on the JavaFX graph? (Y/N): ")) {
            GraphWindow.show(NETWORK, primary, secondary, title);
        }
    }

    private static String join(ArrayList<String> values) {
        return String.join(" -> ", values);
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (value.equalsIgnoreCase("Y")) {
                return true;
            }
            if (value.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }
}
