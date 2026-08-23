package airlineflightnetwork;

public enum RoutePreference {
    DISTANCE("Shortest Distance"),
    FARE("Cheapest Fare"),
    DURATION("Fastest Flight Time");

    private final String label;

    RoutePreference(String label) { this.label = label; }
    public String getLabel() { return label; }
}
