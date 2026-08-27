package airlineflightnetwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AirlineNetwork {
    private final LinkedHashMap<String, Airport> airports = new LinkedHashMap<>();
    private final LinkedHashMap<String, ArrayList<FlightEdge>> adjacencyList = new LinkedHashMap<>();
    private boolean operationMessagesEnabled = true;

    public void setOperationMessagesEnabled(boolean enabled) {
        operationMessagesEnabled = enabled;
    }

    private void message(String text) {
        if (operationMessagesEnabled) System.out.println(text);
    }

    public String normaliseCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    public boolean addAirport(Airport airport) {
    if (airport == null) { 
        message("Unable to add airport: invalid data."); 
        return false; 
    }

    String code = airport.getCode();

    // Airport code must contain exactly 3 letters
    if (!code.matches("[A-Z]{3}")) {
        message("Unable to add airport: airport code must contain exactly 3 letters.");
        return false;
    }

    if (airports.containsKey(code)) { 
        message("Unable to add airport: " + code + " already exists."); 
        return false; 
    }

    airports.put(code, airport);
    adjacencyList.put(code, new ArrayList<>());
    message("Airport " + code + " added successfully.");
    return true;
}

    public boolean removeAirport(String airportCode) {
        String code = normaliseCode(airportCode);
        if (!airports.containsKey(code)) { message("Unable to remove airport: " + code + " does not exist."); return false; }
        int outgoing = adjacencyList.get(code).size();
        int incoming = 0;
        for (ArrayList<FlightEdge> flights : adjacencyList.values()) {
            int before = flights.size();
            flights.removeIf(edge -> edge.getDestinationCode().equals(code));
            incoming += before - flights.size();
        }
        adjacencyList.remove(code);
        airports.remove(code);
        message("Airport " + code + " removed successfully.");
        message("Removed " + outgoing + " outgoing and " + incoming + " incoming flight(s).");
        return true;
    }

    public boolean addFlight(String sourceCode, String destinationCode,
            int distanceKm, double fare, int durationMinutes) {
        String source = normaliseCode(sourceCode);
        String destination = normaliseCode(destinationCode);
        if (!airports.containsKey(source) || !airports.containsKey(destination)) {
            message("Unable to add flight: source or destination airport does not exist."); return false;
        }
        if (source.equals(destination)) { message("Unable to add flight: source and destination cannot be the same."); return false; }
        if (distanceKm <= 0 || fare <= 0 || durationMinutes <= 0) {
            message("Unable to add flight: distance, fare and duration must be positive."); return false;
        }
        if (flightExists(source, destination)) {
            message("Unable to add flight: " + source + " -> " + destination + " already exists."); return false;
        }
        adjacencyList.get(source).add(new FlightEdge(source, destination, distanceKm, fare, durationMinutes));
        message("Flight " + source + " -> " + destination + " added successfully.");
        return true;
    }

    public boolean removeFlight(String sourceCode, String destinationCode) {
        String source = normaliseCode(sourceCode);
        String destination = normaliseCode(destinationCode);
        if (!airports.containsKey(source) || !airports.containsKey(destination)) {
            message("Unable to remove flight: source or destination airport does not exist."); return false;
        }
        boolean removed = adjacencyList.get(source)
                .removeIf(edge -> edge.getDestinationCode().equals(destination));
        if (!removed) { message("Unable to remove flight: " + source + " -> " + destination + " does not exist."); return false; }
        message("Flight " + source + " -> " + destination + " removed successfully.");
        return true;
    }

    public boolean airportExists(String code) { return airports.containsKey(normaliseCode(code)); }

    public boolean flightExists(String sourceCode, String destinationCode) {
        return getFlight(sourceCode, destinationCode) != null;
    }

    public Airport getAirport(String code) { return airports.get(normaliseCode(code)); }

    public ArrayList<Airport> getAirports() { return new ArrayList<>(airports.values()); }

    public ArrayList<FlightEdge> getOutgoingFlights(String code) {
        ArrayList<FlightEdge> flights = adjacencyList.get(normaliseCode(code));
        return flights == null ? new ArrayList<>() : new ArrayList<>(flights);
    }

    public FlightEdge getFlight(String sourceCode, String destinationCode) {
        String source = normaliseCode(sourceCode);
        String destination = normaliseCode(destinationCode);
        if (!adjacencyList.containsKey(source)) return null;
        for (FlightEdge edge : adjacencyList.get(source)) {
            if (edge.getDestinationCode().equals(destination)) return edge;
        }
        return null;
    }

    public int getAirportCount() { return airports.size(); }

    public int getFlightCount() {
        int total = 0;
        for (ArrayList<FlightEdge> flights : adjacencyList.values()) total += flights.size();
        return total;
    }

    public boolean isEmpty() { return airports.isEmpty(); }

    public void displayAirports() {
        System.out.println("\n========== AIRPORTS ==========");
        if (airports.isEmpty()) { System.out.println("No airports available."); return; }
        for (Airport airport : airports.values()) System.out.println(airport);
        System.out.println("Total airports: " + getAirportCount());
    }

    public void displayFlights() {
        System.out.println("\n========== FLIGHT NETWORK ==========");
        if (airports.isEmpty()) { System.out.println("The flight network is empty."); return; }
        for (Map.Entry<String, Airport> entry : airports.entrySet()) {
            String source = entry.getKey();
            System.out.println("\n" + source + " - " + entry.getValue().getCity());
            ArrayList<FlightEdge> flights = adjacencyList.get(source);
            if (flights.isEmpty()) System.out.println("  No outgoing flights.");
            else for (FlightEdge flight : flights) System.out.println("  " + flight);
        }
        System.out.println("\nTotal directed flights: " + getFlightCount());
    }
}
