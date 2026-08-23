package airlineflightnetwork;

import java.util.ArrayList;

public class RouteResult {
    private final boolean routeFound;
    private final ArrayList<String> path;
    private final String message;
    private final ArrayList<String> searchOrder;
    private final int totalDistanceKm;
    private final double totalFare;
    private final int totalDurationMinutes;

    public RouteResult(boolean routeFound, ArrayList<String> path, String message,
            ArrayList<String> searchOrder, int totalDistanceKm,
            double totalFare, int totalDurationMinutes) {
        this.routeFound = routeFound;
        this.path = new ArrayList<>(path);
        this.message = message;
        this.searchOrder = new ArrayList<>(searchOrder);
        this.totalDistanceKm = totalDistanceKm;
        this.totalFare = totalFare;
        this.totalDurationMinutes = totalDurationMinutes;
    }

    public boolean isRouteFound() { return routeFound; }
    public ArrayList<String> getPath() { return new ArrayList<>(path); }
    public String getMessage() { return message; }
    public ArrayList<String> getSearchOrder() { return new ArrayList<>(searchOrder); }
    public int getTotalDistanceKm() { return totalDistanceKm; }
    public double getTotalFare() { return totalFare; }
    public int getTotalDurationMinutes() { return totalDurationMinutes; }
    public int getFlightLegs() { return routeFound ? Math.max(0, path.size() - 1) : 0; }
    public int getTransfers() { return Math.max(0, getFlightLegs() - 1); }

    public String getFormattedDuration() {
        return (totalDurationMinutes / 60) + " hour(s) "
                + (totalDurationMinutes % 60) + " minute(s)";
    }
}
