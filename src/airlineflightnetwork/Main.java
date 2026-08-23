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
                case 1 -> NETWORK.displayAirports();
                case 2 -> NETWORK.displayFlights();
                case 3 -> addAirport();
                case 4 -> removeAirport();
                case 5 -> addFlight();
                case 6 -> removeFlight();
                case 7 -> runBfs();
                case 8 -> runDijkstra();
                case 9 -> compareAlgorithms();
                case 10 -> displaySummary();
                case 0 -> System.out.println("Thank you for using the system.");
                default -> System.out.println("Invalid choice. Please enter 0 to 10.");
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
        NETWORK.removeAirport(readRequired("Airport code to remove: "));
    }

    private static void addFlight() {
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        int distance = readInt("Distance (km): ");
        double fare = readDouble("Fare (RM): ");
        int duration = readInt("Duration (minutes): ");
        NETWORK.addFlight(source, destination, distance, fare, duration);
    }

    private static void removeFlight() {
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        NETWORK.removeFlight(source, destination);
    }

    private static void runBfs() {
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        RouteResult result = BFS.findRoute(NETWORK, source, destination);
        displayRouteResult("BFS - MINIMUM FLIGHT LEGS", result);
    }

    private static void runDijkstra() {
        String source = readRequired("Source airport code: ");
        String destination = readRequired("Destination airport code: ");
        RoutePreference preference = readPreference();
        RouteResult result = DIJKSTRA.findRoute(NETWORK, source, destination, preference);
        displayRouteResult("DIJKSTRA - " + preference.getLabel().toUpperCase(), result);
    }

    private static void compareAlgorithms() {
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
    }

    private static RoutePreference readPreference() {
        while (true) {
            System.out.println("1. Shortest distance");
            System.out.println("2. Cheapest fare");
            System.out.println("3. Fastest flight time");
            int choice = readInt("Select optimisation criterion: ");
            if (choice == 1) return RoutePreference.DISTANCE;
            if (choice == 2) return RoutePreference.FARE;
            if (choice == 3) return RoutePreference.DURATION;
            System.out.println("Invalid criterion. Please enter 1, 2 or 3.");
        }
    }

    private static void displayRouteResult(String heading, RouteResult result) {
        System.out.println("\n============================================");
        System.out.println(heading);
        System.out.println("============================================");
        System.out.println("Status         : " + result.getMessage());
        if (!result.getSearchOrder().isEmpty())
            System.out.println("Search Order   : " + join(result.getSearchOrder()));
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

    private static String join(ArrayList<String> values) {
        return String.join(" -> ", values);
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            try { return Integer.parseInt(value); }
            catch (NumberFormatException ex) { System.out.println("Please enter a valid whole number."); }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            try { return Double.parseDouble(value); }
            catch (NumberFormatException ex) { System.out.println("Please enter a valid number."); }
        }
    }
}
