package airlineflightnetwork;

import java.util.Locale;

public class FlightEdge {
    private final String sourceCode;
    private final String destinationCode;
    private final int distanceKm;
    private final double fare;
    private final int durationMinutes;

    public FlightEdge(String sourceCode, String destinationCode,
            int distanceKm, double fare, int durationMinutes) {
        this.sourceCode = normalise(sourceCode);
        this.destinationCode = normalise(destinationCode);
        if (distanceKm <= 0 || fare <= 0 || durationMinutes <= 0) {
            throw new IllegalArgumentException("Distance, fare and duration must be positive.");
        }
        this.distanceKm = distanceKm;
        this.fare = fare;
        this.durationMinutes = durationMinutes;
    }

    private static String normalise(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Airport code cannot be empty.");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    public String getSourceCode() { return sourceCode; }
    public String getDestinationCode() { return destinationCode; }
    public int getDistanceKm() { return distanceKm; }
    public double getFare() { return fare; }
    public int getDurationMinutes() { return durationMinutes; }

    @Override
    public String toString() {
        return String.format("%s -> %s | %,d km | RM %,.2f | %d minutes",
                sourceCode, destinationCode, distanceKm, fare, durationMinutes);
    }
}
