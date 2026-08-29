package airlineflightnetwork;

import java.util.Locale;

public class Edge {
    private final String sourceCode;
    private final String destination;
    private final int distance;
    private final double price;
    private final int time;

    public Edge(String sourceCode, String destination, int distance, double price, int time) {
        this.sourceCode = normalise(sourceCode);
        this.destination = normalise(destination);
        if (distance <= 0 || price <= 0 || time <= 0) {
            throw new IllegalArgumentException("Distance, fare and duration must be positive.");
        }
        this.distance = distance;
        this.price = price;
        this.time = time;
    }

    private static String normalise(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Airport code cannot be empty.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    // Direct proposal-matching field access
    public String getDestination() { return destination; }
    public int getDistance() { return distance; }
    public double getPrice() { return price; }
    public int getTime() { return time; }

    // Backward-compatibility wrappers — keeps AirlineNetwork/GraphView/BFSRouteFinder/DijkstraRouteFinder unchanged
    public String getSourceCode() { return sourceCode; }
    public String getDestinationCode() { return destination; }
    public int getDistanceKm() { return distance; }
    public double getFare() { return price; }
    public int getDurationMinutes() { return time; }

    @Override
    public String toString() {
        return String.format("%s -> %s | %,d km | RM %,.2f | %d minutes",
                sourceCode, destination, distance, price, time);
    }
}