package airlineflightnetwork;

public class FlightEdge {

    private final String sourceCode;
    private final String destinationCode;
    private final int distanceKm;
    private final double fare;
    private final int durationMinutes;

    public FlightEdge(
            String sourceCode,
            String destinationCode,
            int distanceKm,
            double fare,
            int durationMinutes) {

        this.sourceCode
                = sourceCode.trim().toUpperCase();

        this.destinationCode
                = destinationCode.trim().toUpperCase();

        this.distanceKm = distanceKm;
        this.fare = fare;
        this.durationMinutes = durationMinutes;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public int getDistanceKm() {
        return distanceKm;
    }

    public double getFare() {
        return fare;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    @Override
    public String toString() {
        return sourceCode + " -> " + destinationCode
                + " | " + distanceKm + " km"
                + " | RM " + String.format("%.2f", fare)
                + " | " + durationMinutes + " minutes";
    }
}