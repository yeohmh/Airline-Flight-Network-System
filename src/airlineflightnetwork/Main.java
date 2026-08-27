package airlineflightnetwork;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final AirlineNetwork NETWORK = InitialData.createNetwork();
    private static final BFSRouteFinder BFS = new BFSRouteFinder();
    private static final DijkstraRouteFinder DIJKSTRA = new DijkstraRouteFinder();
    private static final AlgorithmCaseAnalyzer ANALYZER = new AlgorithmCaseAnalyzer();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            clearScreen();
            displayHeader();
            System.out.println("MAIN MENU"); line();
            System.out.println("[1] Graph Management");
            System.out.println("[2] Route Search");
            System.out.println("[3] Network Display");
            System.out.println("[4] Algorithm Analysis");
            System.out.println("[5] Exit"); line();
            switch (readChoice("Enter selection [1-5]: ", 1, 5)) {
                case 1 -> graphManagementMenu();
                case 2 -> routeSearchMenu();
                case 3 -> networkDisplayMenu();
                case 4 -> algorithmAnalysisMenu();
                case 5 -> running = false;
                default -> { }
            }
        }
        clearScreen();
        heading("SYSTEM CLOSED");
        System.out.println("Thank you for using the Airline Flight Network System.");
        System.exit(0);
    }

    private static void graphManagementMenu() {
        boolean stay = true;
        while (stay) {
            clearScreen(); heading("GRAPH MANAGEMENT");
            System.out.println("[1] Add Airport Vertex");
            System.out.println("[2] Remove Airport Vertex");
            System.out.println("[3] Add Directed Flight Edge");
            System.out.println("[4] Remove Directed Flight Edge");
            System.out.println("[0] Return to Main Menu"); line();
            int choice = readChoice("Enter selection [0-4]: ", 0, 4);
            clearScreen();
            switch (choice) {
                case 1 -> runCancellable(Main::addAirport);
                case 2 -> runCancellable(Main::removeAirport);
                case 3 -> runCancellable(Main::addFlight);
                case 4 -> runCancellable(Main::removeFlight);
                case 0 -> stay = false;
                default -> { }
            }
            if (choice != 0) pause("Press Enter to return to Graph Management...");
        }
    }

    private static void routeSearchMenu() {
        boolean stay = true;
        while (stay) {
            clearScreen(); heading("ROUTE SEARCH");
            System.out.println("[1] Minimum Flight Legs / Transfers - BFS");
            System.out.println("[2] Shortest Total Distance          - Dijkstra");
            System.out.println("[3] Cheapest Total Fare              - Dijkstra");
            System.out.println("[4] Fastest Total Flight Duration    - Dijkstra");
            System.out.println("[5] Compare BFS and Dijkstra");
            System.out.println("[0] Return to Main Menu"); line();
            int choice = readChoice("Enter selection [0-5]: ", 0, 5);
            clearScreen();
            switch (choice) {
                case 1 -> runCancellable(Main::runBfs);
                case 2 -> runCancellable(() -> runDijkstra(RoutePreference.DISTANCE));
                case 3 -> runCancellable(() -> runDijkstra(RoutePreference.FARE));
                case 4 -> runCancellable(() -> runDijkstra(RoutePreference.DURATION));
                case 5 -> runCancellable(Main::compareAlgorithms);
                case 0 -> stay = false;
                default -> { }
            }
            if (choice != 0) pause("Press Enter to return to Route Search...");
        }
    }

    private static void networkDisplayMenu() {
        boolean stay = true;
        while (stay) {
            clearScreen(); heading("NETWORK DISPLAY");
            System.out.println("[1] View Airport Code Directory");
            System.out.println("[2] View Complete Adjacency-List Network");
            System.out.println("[3] View Network Summary");
            System.out.println("[4] Display JavaFX Airline Graph");
            System.out.println("[0] Return to Main Menu"); line();
            int choice = readChoice("Enter selection [0-4]: ", 0, 4);
            clearScreen();
            switch (choice) {
                case 1 -> displayAirportDirectory();
                case 2 -> NETWORK.displayFlights();
                case 3 -> displaySummary();
                case 4 -> GraphWindow.show(NETWORK, null, null, "Complete Airline Flight Network");
                case 0 -> stay = false;
                default -> { }
            }
            if (choice != 0) pause("Press Enter to return to Network Display...");
        }
    }

    private static void algorithmAnalysisMenu() {
        boolean stay = true;
        while (stay) {
            clearScreen(); heading("ALGORITHM ANALYSIS");
            System.out.println("[1] Find BFS Observed Best Case");
            System.out.println("[2] Find BFS Observed Worst Case");
            System.out.println("[3] Find Dijkstra Observed Best Case");
            System.out.println("[4] Find Dijkstra Observed Worst Case");
            System.out.println("[5] Display Theoretical Time and Space Complexity");
            System.out.println("[0] Return to Main Menu"); line();
            int choice = readChoice("Enter selection [0-5]: ", 0, 5);
            clearScreen();
            switch (choice) {
                case 1 -> displayAnalysisCase(ANALYZER.findBfsBest(NETWORK), true, true);
                case 2 -> displayAnalysisCase(ANALYZER.findBfsWorst(NETWORK), false, true);
                case 3 -> runCancellable(() -> displayAnalysisCase(
                        ANALYZER.findDijkstraBest(NETWORK, readPreference()), true, false));
                case 4 -> runCancellable(() -> displayAnalysisCase(
                        ANALYZER.findDijkstraWorst(NETWORK, readPreference()), false, false));
                case 5 -> displayTheoreticalComplexity();
                case 0 -> stay = false;
                default -> { }
            }
            if (choice != 0) pause("Press Enter to return to Algorithm Analysis...");
        }
    }

    private static void addAirport() {
        heading("ADD AIRPORT VERTEX");
        cancellationHint();
        String code;
        while (true) {
            code = readRequired("Airport code (3 letters): ").toUpperCase();
            if (!code.matches("[A-Z]{3}")) printError("Airport code must contain exactly 3 letters.");
            else if (NETWORK.airportExists(code)) printError("Airport " + code + " already exists.");
            else break;
        }
        String name = readRequired("Airport name: ");
        String city = readRequired("City: ");
        String country = readRequired("Country: ");
        if (!readYesNo("Confirm add airport " + code + "? (Y/N): ")) {
            printInfo("Operation cancelled."); return;
        }
        try { NETWORK.addAirport(new Airport(code, name, city, country)); }
        catch (IllegalArgumentException ex) { printError(ex.getMessage()); }
    }

    private static void removeAirport() {
        heading("REMOVE AIRPORT VERTEX"); cancellationHint(); displayAirportDirectory();
        String code = readExistingAirportCode("Airport code to remove: ");
        System.out.println("Removing this vertex also removes all incoming and outgoing edges.");
        if (readYesNo("Confirm remove " + code + "? (Y/N): ")) NETWORK.removeAirport(code);
        else printInfo("Operation cancelled.");
    }

    private static void addFlight() {
        heading("ADD DIRECTED FLIGHT EDGE"); cancellationHint(); displayAirportDirectory();
        String source = readExistingAirportCode("Source airport code: ");
        String destination = readDifferentAirportCode("Destination airport code: ", source);
        if (NETWORK.flightExists(source, destination)) {
            printError("Flight " + source + " -> " + destination + " already exists."); return;
        }
        int distance = readPositiveInt("Distance (km): ");
        double fare = readPositiveDouble("Fare (RM): ");
        int duration = readPositiveInt("Duration (minutes): ");
        if (readYesNo("Confirm add flight " + source + " -> " + destination + "? (Y/N): "))
            NETWORK.addFlight(source, destination, distance, fare, duration);
        else printInfo("Operation cancelled.");
    }

    private static void removeFlight() {
        heading("REMOVE DIRECTED FLIGHT EDGE"); cancellationHint(); displayAirportDirectory();
        String source = readExistingAirportCode("Source airport code: ");
        String destination = readDifferentAirportCode("Destination airport code: ", source);
        if (!NETWORK.flightExists(source, destination)) {
            printError("Flight " + source + " -> " + destination + " does not exist."); return;
        }
        if (readYesNo("Confirm remove flight " + source + " -> " + destination + "? (Y/N): "))
            NETWORK.removeFlight(source, destination);
        else printInfo("Operation cancelled.");
    }

    private static String[] readValidRoutePair() {
        displayAirportDirectory();
        String source = readExistingAirportCode("Departure airport code: ");
        String destination = readDifferentAirportCode("Destination airport code: ", source);
        return new String[]{source, destination};
    }

    private static void runBfs() {
        heading("BFS - MINIMUM FLIGHT LEGS / TRANSFERS");
        cancellationHint();
        String[] pair = readValidRoutePair();
        RouteResult result = BFS.findRoute(NETWORK, pair[0], pair[1]);
        clearScreen();
        displayRouteResult("BFS - MINIMUM FLIGHT LEGS", "Breadth-First Search (BFS)",
                "Minimum flight legs / transfers", result);
        offerGraph(result, null, "BFS - Minimum Flight Legs");
    }

    private static void runDijkstra(RoutePreference preference) {
        heading("DIJKSTRA - " + preference.getLabel().toUpperCase());
        cancellationHint();
        String[] pair = readValidRoutePair();
        RouteResult result = DIJKSTRA.findRoute(NETWORK, pair[0], pair[1], preference);
        clearScreen();
        displayRouteResult("DIJKSTRA - " + preference.getLabel().toUpperCase(),
                "Dijkstra's Algorithm", preference.getLabel(), result);
        offerGraph(result, null, "Dijkstra - " + preference.getLabel());
    }

    private static void compareAlgorithms() {
        heading("BFS AND DIJKSTRA COMPARISON");
        cancellationHint();
        String[] pair = readValidRoutePair();
        RoutePreference preference = readPreference();
        RouteResult bfs = BFS.findRoute(NETWORK, pair[0], pair[1]);
        RouteResult weighted = DIJKSTRA.findRoute(NETWORK, pair[0], pair[1], preference);
        clearScreen();
        displayRouteResult("BFS RESULT", "Breadth-First Search (BFS)",
                "Minimum flight legs / transfers", bfs);
        System.out.println();
        displayRouteResult("DIJKSTRA RESULT", "Dijkstra's Algorithm", preference.getLabel(), weighted);
        if (bfs.isRouteFound() && weighted.isRouteFound()) {
            System.out.println("\nCOMPARISON CONCLUSION"); line();
            System.out.println("BFS minimises flight legs without using edge weights.");
            System.out.println("Dijkstra minimises " + preference.getLabel().toLowerCase()
                    + " and may use more legs when its total selected weight is lower.");
        }
        offerGraph(bfs, weighted, "BFS and Dijkstra Route Comparison");
    }

    private static void displayRouteResult(String title, String algorithm,
            String optimisedFor, RouteResult result) {
        heading(title);
        field("Algorithm", algorithm); field("Optimised For", optimisedFor);
        field("Status", result.getMessage());
        if (!result.getSearchOrder().isEmpty()) field("Search Order", join(result.getSearchOrder()));
        if (result.isRouteFound()) {
            line(); field("Route", join(result.getPath()));
            field("Flight Legs", result.getFlightLegs()); field("Transfers", result.getTransfers());
            field("Total Distance", result.getTotalDistanceKm() + " km");
            field("Total Fare", String.format("RM %.2f", result.getTotalFare()));
            field("Flight Duration", result.getFormattedDuration());
        }
        displayMetrics(result.getMetrics());
    }

    private static void displayAnalysisCase(AlgorithmCaseAnalyzer.AnalysisCase analysis,
            boolean best, boolean bfs) {
        if (analysis == null) { printError("No valid successful route is available."); return; }
        RouteResult result = analysis.getResult();
        heading((bfs ? "BFS" : "DIJKSTRA") + " OBSERVED " + (best ? "BEST" : "WORST")
                + " CASE - CURRENT NETWORK");
        field("Classification", "Observed case within the current dataset");
        field("Valid Pairs Tested", analysis.getPairsTested());
        field("Source", analysis.getSource()); field("Destination", analysis.getDestination());
        if (analysis.getPreference() != null) field("Optimisation", analysis.getPreference().getLabel());
        field("Route Found", result.isRouteFound() ? "Yes" : "No");
        if (result.isRouteFound()) {
            field("Route", join(result.getPath())); field("Flight Legs", result.getFlightLegs());
            field("Transfers", result.getTransfers());
        }
        displayMetrics(result.getMetrics());
        System.out.println("\nINTERPRETATION"); line();
        System.out.println("This has the " + (best ? "minimum" : "maximum")
                + " observed work count among valid source != destination pairs.");
        System.out.println("It is deterministic for the current dataset, not a random case.");
        System.out.println("It does not replace the theoretical asymptotic bound in the report.");
        if (bfs) {
            field("Theoretical Worst Time", "O(V + E)");
            field("Theoretical Worst Space", "O(V)");
        } else {
            field("Valid-Input Best Time", "Theta(V) for current eager initialisation");
            field("Theoretical Worst Time", "O((V + E) log V)");
            field("Theoretical Worst Space", "O(V + E)");
        }
    }

    private static void displayMetrics(AlgorithmMetrics metrics) {
        if (metrics.getAlgorithm().equals("N/A")) return;
        System.out.println("\nOPERATION COUNTERS"); line();
        field("Vertices Processed", metrics.getVerticesProcessed());
        field("Edges Examined", metrics.getEdgesExamined());
        field("Queue Insertions", metrics.getQueueInsertions());
        field("Queue Removals", metrics.getQueueRemovals());
        if (metrics.getAlgorithm().equalsIgnoreCase("DIJKSTRA"))
            field("Successful Relaxations", metrics.getSuccessfulRelaxations());
        field("Maximum Queue Size", metrics.getMaximumQueueSize());
        field("Observed Work Units", metrics.getObservedWorkUnits());
    }

    private static void displayTheoreticalComplexity() {
        int vertices = NETWORK.getAirportCount(), edges = NETWORK.getFlightCount();
        double scale = (vertices + edges) * (Math.log(vertices) / Math.log(2));
        heading("THEORETICAL TIME AND SPACE COMPLEXITY");
        field("Input Size", "V = " + vertices + " airports, E = " + edges + " flights");
        field("Graph Storage", "O(V + E)");
        System.out.println("\nBFS"); line();
        field("Defensive Same-Source", "Theta(1) time and Theta(1) space");
        field("Worst-Case Time", "O(V + E)"); field("Worst-Case Space", "O(V)");
        field("Current Scale", vertices + " + " + edges + " = " + (vertices + edges) + " graph elements");
        System.out.println("\nDIJKSTRA"); line();
        field("Defensive Same-Source", "Theta(1) time and Theta(1) space");
        field("Valid-Input Best", "Theta(V) time and Theta(V) space");
        field("Worst-Case Time", "O((V + E) log V)"); field("Worst-Case Space", "O(V + E)");
        field("Current Expression", "(" + vertices + " + " + edges + ") x log2(" + vertices + ")");
        field("Illustrative Scale", String.format("%.2f priority-queue growth units", scale));
        System.out.println("\nSubstitutions illustrate current input scale, not exact runtime.");
        System.out.println("Complete derivations and airline examples belong in the report.");
    }

    private static RoutePreference readPreference() {
        System.out.println("\nDIJKSTRA OPTIMISATION"); line();
        System.out.println("Enter E to cancel this operation.");
        System.out.println("[1] Shortest Distance"); System.out.println("[2] Cheapest Fare");
        System.out.println("[3] Fastest Flight Duration");
        return switch (readOperationChoice("Enter selection [1-3]: ", 1, 3)) {
            case 1 -> RoutePreference.DISTANCE; case 2 -> RoutePreference.FARE;
            default -> RoutePreference.DURATION;
        };
    }

    private static void displayAirportDirectory() {
        System.out.println("\nAIRPORT CODE DIRECTORY"); line();
        System.out.printf("%-6s %-34s %-18s %-16s%n", "CODE", "AIRPORT", "CITY", "COUNTRY");
        System.out.println("--------------------------------------------------------------------------");
        for (Airport airport : NETWORK.getAirports())
            System.out.printf("%-6s %-34s %-18s %-16s%n", airport.getCode(),
                    airport.getName(), airport.getCity(), airport.getCountry());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("Total Airports: " + NETWORK.getAirportCount());
    }

    private static void displaySummary() {
        heading("NETWORK SUMMARY");
        field("Graph Type", "Directed Weighted Graph"); field("Representation", "Adjacency Edge List");
        field("Airport Vertices", NETWORK.getAirportCount()); field("Directed Edges", NETWORK.getFlightCount());
        field("Edge Weights", "Distance, fare and duration");
        field("Traversal", "Breadth-First Search (BFS)"); field("Weighted Algorithm", "Dijkstra's Algorithm");
    }

    private static void offerGraph(RouteResult primary, RouteResult secondary, String title) {
        System.out.println();
        if (readYesNo("Display this result on the JavaFX graph? (Y/N): ")) {
            System.out.println("Close the graph window to return to the Console.");
            GraphWindow.show(NETWORK, primary, secondary, title);
        }
    }

    private static String readExistingAirportCode(String prompt) {
        while (true) {
            String code = readRequired(prompt).toUpperCase();
            if (NETWORK.airportExists(code)) return code;
            printError("Airport " + code + " does not exist. Refer to the directory above.");
        }
    }

    private static String readDifferentAirportCode(String prompt, String source) {
        while (true) {
            String code = readExistingAirportCode(prompt);
            if (!code.equals(source)) return code;
            printError("Departure/source and destination airports must be different.");
        }
    }

    private static int readChoice(String prompt, int minimum, int maximum) {
        while (true) {
            int value = readMenuInt(prompt);
            if (value >= minimum && value <= maximum) return value;
            printError("Enter a number from " + minimum + " to " + maximum + ".");
        }
    }

    private static int readOperationChoice(String prompt, int minimum, int maximum) {
        while (true) {
            int value = readInt(prompt);
            if (value >= minimum && value <= maximum) return value;
            printError("Enter a number from " + minimum + " to " + maximum + ", or E to cancel.");
        }
    }

    private static int readMenuInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(INPUT.nextLine().trim()); }
            catch (NumberFormatException ex) { printError("Enter a valid whole number."); }
        }
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt); String value = INPUT.nextLine().trim();
            if (value.equalsIgnoreCase("E")) throw new OperationCancelledException();
            if (!value.isEmpty()) return value; printError("Input cannot be empty.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(readRequired(prompt)); }
            catch (NumberFormatException ex) { printError("Enter a valid whole number."); }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) { int value = readInt(prompt); if (value > 0) return value;
            printError("Value must be greater than zero."); }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            try { double value = Double.parseDouble(readRequired(prompt));
                if (value > 0) return value; printError("Value must be greater than zero."); }
            catch (NumberFormatException ex) { printError("Enter a valid numeric value."); }
        }
    }

    private static boolean readYesNo(String prompt) {
        while (true) {
            String value = readRequired(prompt);
            if (value.equalsIgnoreCase("Y")) return true;
            if (value.equalsIgnoreCase("N")) return false;
            printError("Enter Y for Yes or N for No.");
        }
    }

    private static void displayHeader() {
        System.out.println("============================================================");
        System.out.println("              AIRLINE FLIGHT NETWORK SYSTEM");
        System.out.println("============================================================");
        field("Graph Model", "Directed Weighted Graph"); field("Representation", "Adjacency Edge List");
        field("Current Network", NETWORK.getAirportCount() + " Airports | " + NETWORK.getFlightCount() + " Directed Flights");
        System.out.println("============================================================\n");
    }

    private static void heading(String title) {
        System.out.println("============================================================");
        System.out.println(" " + title);
        System.out.println("============================================================");
    }

    private static void line() { System.out.println("------------------------------------------------------------"); }
    private static void field(String label, Object value) { System.out.printf("%-23s: %s%n", label, value); }
    private static void printError(String message) { System.out.println("[ERROR] " + message); }
    private static void printInfo(String message) { System.out.println("[INFO] " + message); }
    private static void cancellationHint() {
        System.out.println("Enter E at any prompt to cancel this operation.\n");
    }
    private static void runCancellable(Runnable operation) {
        try { operation.run(); }
        catch (OperationCancelledException ex) { printInfo("Operation cancelled. No changes were made."); }
    }
    private static void pause(String message) { System.out.print("\n" + message); INPUT.nextLine(); }
    private static void clearScreen() { for (int i = 0; i < 32; i++) System.out.println(); }
    private static String join(ArrayList<String> values) { return String.join(" -> ", values); }

    private static class OperationCancelledException extends RuntimeException { }
}
