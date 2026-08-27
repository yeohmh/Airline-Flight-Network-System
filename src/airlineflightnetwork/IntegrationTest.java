package airlineflightnetwork;

public class IntegrationTest {
    private static int passed;

    public static void main(String[] args) {
        AirlineNetwork network = InitialData.createNetwork();
        network.setOperationMessagesEnabled(false);
        check(network.getAirportCount() == 15, "15-airport initial dataset");
        check(network.getFlightCount() == 70, "70-flight directed weighted dataset");
        check(network.flightExists("KUL", "NRT"), "directed KUL to NRT flight exists");
        check(network.getFlight("KUL", "NRT").getFare() == 1800.0, "flight weights are stored correctly");

        BFSRouteFinder bfs = new BFSRouteFinder();
        RouteResult bfsResult = bfs.findRoute(network, " kul ", "nrt");
        check(bfsResult.isRouteFound(), "BFS finds a route");
        check(bfsResult.getFlightLegs() == 1, "BFS selects minimum flight legs");
        check(!bfs.findRoute(network, "XXX", "NRT").isRouteFound(), "BFS rejects invalid airport");
        check(bfs.findRoute(network, "KUL", "KUL").getFlightLegs() == 0, "BFS handles same airport");
        check(bfsResult.getSearchOrder().get(0).equals("KUL"), "BFS records traversal order");
        check(bfsResult.getTransfers() == 0, "BFS calculates transfers");
        check(bfsResult.getMetrics().getVerticesProcessed() > 0, "BFS records processed vertices");
        check(bfsResult.getMetrics().getEdgesExamined() > 0, "BFS records examined edges");
        check(bfsResult.getMetrics().getMaximumQueueSize() > 0, "BFS records maximum queue size");

        DijkstraRouteFinder dijkstra = new DijkstraRouteFinder();
        RouteResult cheapest = dijkstra.findRoute(network, "KUL", "NRT", RoutePreference.FARE);
        check(cheapest.isRouteFound(), "Dijkstra finds a route");
        check(cheapest.getTotalFare() < bfsResult.getTotalFare(), "Dijkstra finds a cheaper multi-leg route");
        RouteResult shortest = dijkstra.findRoute(network, "KUL", "NRT", RoutePreference.DISTANCE);
        check(shortest.isRouteFound(), "Dijkstra shortest-distance mode");
        check(shortest.getPath().equals(new java.util.ArrayList<>(
                java.util.Arrays.asList("KUL", "NRT"))), "Dijkstra reconstructs shortest-distance route");
        check(shortest.getTotalDistanceKm() == 5400, "Dijkstra shortest-distance total");
        RouteResult fastest = dijkstra.findRoute(network, "KUL", "NRT", RoutePreference.DURATION);
        check(fastest.isRouteFound(), "Dijkstra fastest-duration mode");
        check(fastest.getPath().equals(new java.util.ArrayList<>(
                java.util.Arrays.asList("KUL", "NRT"))), "Dijkstra reconstructs fastest route");
        check(fastest.getTotalDurationMinutes() == 420, "Dijkstra fastest-duration total");
        check(cheapest.getPath().equals(new java.util.ArrayList<>(
                java.util.Arrays.asList("KUL", "BKK", "NRT"))), "Dijkstra reconstructs cheapest route");
        check(cheapest.getMetrics().getSuccessfulRelaxations() > 0,
                "Dijkstra records successful relaxations");
        check(cheapest.getMetrics().getMaximumQueueSize() > 0,
                "Dijkstra records maximum priority-queue size");

        AlgorithmCaseAnalyzer analyzer = new AlgorithmCaseAnalyzer();
        AlgorithmCaseAnalyzer.AnalysisCase bfsBest = analyzer.findBfsBest(network);
        AlgorithmCaseAnalyzer.AnalysisCase bfsWorst = analyzer.findBfsWorst(network);
        check(bfsBest.getPairsTested() == 210, "BFS analysis checks all 210 valid ordered pairs");
        check(!bfsBest.getSource().equals(bfsBest.getDestination()),
                "BFS observed best excludes same-airport input");
        check(bfsBest.getResult().getMetrics().getObservedWorkUnits()
                <= bfsWorst.getResult().getMetrics().getObservedWorkUnits(),
                "BFS observed best work does not exceed observed worst work");

        AlgorithmCaseAnalyzer.AnalysisCase fareBest
                = analyzer.findDijkstraBest(network, RoutePreference.FARE);
        AlgorithmCaseAnalyzer.AnalysisCase fareWorst
                = analyzer.findDijkstraWorst(network, RoutePreference.FARE);
        check(fareBest.getPairsTested() == 210,
                "Dijkstra analysis checks all 210 valid ordered pairs");
        check(!fareBest.getSource().equals(fareBest.getDestination()),
                "Dijkstra observed best excludes same-airport input");
        check(fareBest.getResult().getMetrics().getObservedWorkUnits()
                <= fareWorst.getResult().getMetrics().getObservedWorkUnits(),
                "Dijkstra observed best work does not exceed observed worst work");
        check(network.getAirportCount() == 15 && network.getFlightCount() == 70,
                "case analysis does not modify the airline graph");

        check(network.addAirport(new Airport("TST", "Test Airport", "Test City", "Malaysia")), "add airport");
        check(!network.addAirport(new Airport("TST", "Duplicate", "Test City", "Malaysia")), "reject duplicate airport");
        check(network.addFlight("TST", "KUL", 100, 50, 30), "add flight");
        check(!network.addFlight("TST", "KUL", 100, 50, 30), "reject duplicate flight");
        check(!network.addFlight("TST", "XXX", 100, 50, 30), "reject nonexistent destination");
        check(!network.addFlight("TST", "KUL", -1, 50, 30), "reject invalid edge weight");
        check(network.removeFlight("TST", "KUL"), "remove flight");
        check(!network.removeFlight("TST", "KUL"), "reject removal of nonexistent flight");
        check(network.addFlight("TST", "KUL", 100, 50, 30), "restore flight for vertex-removal test");
        check(network.removeAirport("TST"), "remove airport and related flight");
        check(!network.flightExists("TST", "KUL"), "related edge was removed");
        check(!network.removeAirport("TST"), "reject removal of nonexistent airport");

        AirlineNetwork disconnected = new AirlineNetwork();
        disconnected.setOperationMessagesEnabled(false);
        disconnected.addAirport(new Airport("AAA", "Airport A", "City A", "Malaysia"));
        disconnected.addAirport(new Airport("BBB", "Airport B", "City B", "Malaysia"));
        check(!bfs.findRoute(disconnected, "AAA", "BBB").isRouteFound(), "BFS handles unavailable route");
        check(!dijkstra.findRoute(disconnected, "AAA", "BBB", RoutePreference.FARE).isRouteFound(),
                "Dijkstra handles unavailable route");

        System.out.println("ALL " + passed + " INTEGRATION TESTS PASSED.");
        System.out.println("BFS route      : " + String.join(" -> ", bfsResult.getPath()));
        System.out.printf("BFS fare       : RM %.2f%n", bfsResult.getTotalFare());
        System.out.println("Cheapest route : " + String.join(" -> ", cheapest.getPath()));
        System.out.printf("Cheapest fare  : RM %.2f%n", cheapest.getTotalFare());
    }

    private static void check(boolean condition, String description) {
        if (!condition) throw new AssertionError("FAILED: " + description);
        passed++;
        System.out.println("PASS: " + description);
    }
}
