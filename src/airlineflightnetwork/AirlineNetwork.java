package airlineflightnetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AirlineNetwork {

    private final HashMap<String, Airport> airports;
    private final HashMap<String, ArrayList<FlightEdge>>
            adjacencyList;

    public AirlineNetwork() {
        airports = new LinkedHashMap<>();
        adjacencyList = new LinkedHashMap<>();
    }

    private String normaliseCode(String code) {
        if (code == null) {
            return "";
        }

        return code.trim().toUpperCase(Locale.ROOT);
    }

    public boolean addAirport(Airport airport) {
        if (airport == null) {
            System.out.println(
                    "Unable to add airport: invalid airport data.");
            return false;
        }

        String code = normaliseCode(airport.getCode());

        if (code.isEmpty()) {
            System.out.println(
                    "Unable to add airport: code cannot be empty.");
            return false;
        }

        if (airports.containsKey(code)) {
            System.out.println(
                    "Unable to add airport: "
                    + code + " already exists.");
            return false;
        }

        airports.put(code, airport);
        adjacencyList.put(code, new ArrayList<>());

        System.out.println(
                "Airport " + code + " added successfully.");
        return true;
    }

    public boolean removeAirport(String airportCode) {
        String code = normaliseCode(airportCode);

        if (!airports.containsKey(code)) {
            System.out.println(
                    "Unable to remove airport: "
                    + code + " does not exist.");
            return false;
        }

        int outgoingFlights
                = adjacencyList.get(code).size();

        int incomingFlights = 0;

        for (ArrayList<FlightEdge> flights
                : adjacencyList.values()) {

            int originalSize = flights.size();

            flights.removeIf(
                    edge -> edge.getDestinationCode()
                            .equals(code));

            incomingFlights
                    += originalSize - flights.size();
        }

        adjacencyList.remove(code);
        airports.remove(code);

        System.out.println(
                "Airport " + code + " removed successfully.");

        System.out.println(
                "Removed " + outgoingFlights
                + " outgoing and " + incomingFlights
                + " incoming flight(s).");

        return true;
    }

    public boolean addFlight(
            String sourceCode,
            String destinationCode,
            int distanceKm,
            double fare,
            int durationMinutes) {

        String source = normaliseCode(sourceCode);
        String destination
                = normaliseCode(destinationCode);

        if (!airports.containsKey(source)
                || !airports.containsKey(destination)) {

            System.out.println(
                    "Unable to add flight: "
                    + "source or destination airport "
                    + "does not exist.");

            return false;
        }

        if (source.equals(destination)) {
            System.out.println(
                    "Unable to add flight: "
                    + "source and destination "
                    + "cannot be the same.");

            return false;
        }

        if (distanceKm <= 0
                || fare <= 0
                || durationMinutes <= 0) {

            System.out.println(
                    "Unable to add flight: distance, fare "
                    + "and duration must be positive.");

            return false;
        }

        if (flightExists(source, destination)) {
            System.out.println(
                    "Unable to add flight: "
                    + source + " -> " + destination
                    + " already exists.");

            return false;
        }

        FlightEdge flight = new FlightEdge(
                source,
                destination,
                distanceKm,
                fare,
                durationMinutes);

        adjacencyList.get(source).add(flight);

        System.out.println(
                "Flight " + source + " -> "
                + destination + " added successfully.");

        return true;
    }

    public boolean removeFlight(
            String sourceCode,
            String destinationCode) {

        String source = normaliseCode(sourceCode);
        String destination
                = normaliseCode(destinationCode);

        if (!airports.containsKey(source)
                || !airports.containsKey(destination)) {

            System.out.println(
                    "Unable to remove flight: "
                    + "source or destination airport "
                    + "does not exist.");

            return false;
        }

        boolean removed = adjacencyList.get(source)
                .removeIf(
                        edge -> edge.getDestinationCode()
                                .equals(destination));

        if (!removed) {
            System.out.println(
                    "Unable to remove flight: "
                    + source + " -> " + destination
                    + " does not exist.");

            return false;
        }

        System.out.println(
                "Flight " + source + " -> "
                + destination + " removed successfully.");

        return true;
    }

    public boolean airportExists(String airportCode) {
        return airports.containsKey(
                normaliseCode(airportCode));
    }

    public boolean flightExists(
            String sourceCode,
            String destinationCode) {

        String source = normaliseCode(sourceCode);
        String destination
                = normaliseCode(destinationCode);

        if (!adjacencyList.containsKey(source)) {
            return false;
        }

        for (FlightEdge edge : adjacencyList.get(source)) {
            if (edge.getDestinationCode()
                    .equals(destination)) {

                return true;
            }
        }

        return false;
    }

    public Airport getAirport(String airportCode) {
        return airports.get(normaliseCode(airportCode));
    }

    public ArrayList<FlightEdge> getOutgoingFlights(
            String airportCode) {

        String code = normaliseCode(airportCode);

        if (!adjacencyList.containsKey(code)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(adjacencyList.get(code));
    }

    public FlightEdge getFlight(
            String sourceCode,
            String destinationCode) {

        String source = normaliseCode(sourceCode);
        String destination
                = normaliseCode(destinationCode);

        if (!adjacencyList.containsKey(source)) {
            return null;
        }

        for (FlightEdge edge : adjacencyList.get(source)) {
            if (edge.getDestinationCode()
                    .equals(destination)) {

                return edge;
            }
        }

        return null;
    }

    public int getAirportCount() {
        return airports.size();
    }

    public int getFlightCount() {
        int total = 0;

        for (ArrayList<FlightEdge> flights
                : adjacencyList.values()) {

            total += flights.size();
        }

        return total;
    }

    public boolean isEmpty() {
        return airports.isEmpty();
    }

    public void displayAirports() {
        System.out.println(
                "\n========== AIRPORTS ==========");

        if (airports.isEmpty()) {
            System.out.println("No airports available.");
            return;
        }

        for (Airport airport : airports.values()) {
            System.out.println(airport);
        }

        System.out.println(
                "Total airports: " + getAirportCount());
    }

    public void displayFlights() {
        System.out.println(
                "\n======= FLIGHT NETWORK =======");

        if (airports.isEmpty()) {
            System.out.println("The flight network is empty.");
            return;
        }

        for (Map.Entry<String, Airport> entry
                : airports.entrySet()) {

            String source = entry.getKey();

            System.out.println(
                    "\n" + source + " - "
                    + entry.getValue().getCity());

            ArrayList<FlightEdge> flights
                    = adjacencyList.get(source);

            if (flights.isEmpty()) {
                System.out.println(
                        "  No outgoing flights.");
            } else {
                for (FlightEdge flight : flights) {
                    System.out.println(
                            "  " + flight);
                }
            }
        }

        System.out.println(
                "\nTotal directed flights: "
                + getFlightCount());
    }
}