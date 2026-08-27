package airlineflightnetwork;

import java.util.ArrayList;

/** Finds reproducible observed cases among all valid ordered airport pairs. */
public class AlgorithmCaseAnalyzer {
    private final BFSRouteFinder bfs = new BFSRouteFinder();
    private final DijkstraRouteFinder dijkstra = new DijkstraRouteFinder();

    public AnalysisCase findBfsBest(AirlineNetwork network) {
        return analyseBfs(network, true);
    }

    public AnalysisCase findBfsWorst(AirlineNetwork network) {
        return analyseBfs(network, false);
    }

    public AnalysisCase findDijkstraBest(AirlineNetwork network, RoutePreference preference) {
        return analyseDijkstra(network, preference, true);
    }

    public AnalysisCase findDijkstraWorst(AirlineNetwork network, RoutePreference preference) {
        return analyseDijkstra(network, preference, false);
    }

    private AnalysisCase analyseBfs(AirlineNetwork network, boolean best) {
        ArrayList<Airport> airports = network.getAirports();
        AnalysisCase selected = null;
        int pairs = 0;
        for (Airport source : airports) {
            for (Airport destination : airports) {
                if (source.getCode().equals(destination.getCode())) continue;
                pairs++;
                RouteResult result = bfs.findRoute(network, source.getCode(), destination.getCode());
                if (best && !result.isRouteFound()) continue;
                AnalysisCase candidate = new AnalysisCase(source.getCode(), destination.getCode(),
                        result, null, pairs);
                if (prefer(candidate, selected, best)) selected = candidate;
            }
        }
        return selected == null ? null : selected.withPairsTested(pairs);
    }

    private AnalysisCase analyseDijkstra(AirlineNetwork network,
            RoutePreference preference, boolean best) {
        ArrayList<Airport> airports = network.getAirports();
        AnalysisCase selected = null;
        int pairs = 0;
        for (Airport source : airports) {
            for (Airport destination : airports) {
                if (source.getCode().equals(destination.getCode())) continue;
                pairs++;
                RouteResult result = dijkstra.findRoute(network, source.getCode(),
                        destination.getCode(), preference);
                if (best && !result.isRouteFound()) continue;
                AnalysisCase candidate = new AnalysisCase(source.getCode(), destination.getCode(),
                        result, preference, pairs);
                if (prefer(candidate, selected, best)) selected = candidate;
            }
        }
        return selected == null ? null : selected.withPairsTested(pairs);
    }

    private boolean prefer(AnalysisCase candidate, AnalysisCase selected, boolean best) {
        if (selected == null) return true;
        int candidateWork = candidate.getResult().getMetrics().getObservedWorkUnits();
        int selectedWork = selected.getResult().getMetrics().getObservedWorkUnits();
        if (candidateWork != selectedWork) return best
                ? candidateWork < selectedWork : candidateWork > selectedWork;
        String candidateKey = candidate.getSource() + candidate.getDestination();
        String selectedKey = selected.getSource() + selected.getDestination();
        return candidateKey.compareTo(selectedKey) < 0;
    }

    public static class AnalysisCase {
        private final String source;
        private final String destination;
        private final RouteResult result;
        private final RoutePreference preference;
        private final int pairsTested;

        AnalysisCase(String source, String destination, RouteResult result,
                RoutePreference preference, int pairsTested) {
            this.source = source;
            this.destination = destination;
            this.result = result;
            this.preference = preference;
            this.pairsTested = pairsTested;
        }

        AnalysisCase withPairsTested(int count) {
            return new AnalysisCase(source, destination, result, preference, count);
        }

        public String getSource() { return source; }
        public String getDestination() { return destination; }
        public RouteResult getResult() { return result; }
        public RoutePreference getPreference() { return preference; }
        public int getPairsTested() { return pairsTested; }
    }
}
