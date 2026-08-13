package airlineflightnetwork;

import java.util.ArrayList;

public class RouteResult {

    private final boolean routeFound;
    private final ArrayList<String> path;
    private final String message;
    private final ArrayList<String> searchOrder;

    public RouteResult(
            boolean routeFound,
            ArrayList<String> path,
            String message,
            ArrayList<String> searchOrder) {

        this.routeFound = routeFound;
        this.path = path;
        this.message = message;
        this.searchOrder = searchOrder;
    }

    public boolean isRouteFound() {
        return routeFound;
    }

    public ArrayList<String> getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<String> getSearchOrder() {
        return searchOrder;
    }

    public int getFlightLegs() {
        if (!routeFound || path.size() < 2) {
            return 0;
        }

        return path.size() - 1;
    }

    public int getTransfers() {
        return Math.max(0, getFlightLegs() - 1);
    }
}