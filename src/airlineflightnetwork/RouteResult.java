package airlineflightnetwork;

import java.util.ArrayList;

public class RouteResult {

    private boolean routeFound;
    private ArrayList<String> path;
    private String message;

    public RouteResult(boolean routeFound, ArrayList<String> path, String message) {
        this.routeFound = routeFound;
        this.path = path;
        this.message = message;
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

    public int getFlightLegs() {
        if (!routeFound || path.size() < 2) {
            return 0;
        }

        return path.size() - 1;
    }

    public int getTransfers() {
        int flightLegs = getFlightLegs();

        if (flightLegs == 0) {
            return 0;
        }

        return flightLegs - 1;
    }

}
