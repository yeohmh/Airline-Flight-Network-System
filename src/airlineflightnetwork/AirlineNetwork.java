package airlineflightnetwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AirlineNetwork {
    private final LinkedHashMap<String, Airport> airports = new LinkedHashMap<>();
    private final LinkedHashMap<String, ArrayList<Edge>> adjList = new LinkedHashMap<>();
    private boolean operationMessagesEnabled = true;
    private int numOfVertices = 0;
    private int numOfEdges = 0;

    public void setOperationMessagesEnabled(boolean enabled) {
        operationMessagesEnabled = enabled;
    }

    private void message(String text) {
        if (operationMessagesEnabled) System.out.println(text);
    }

    public String normaliseCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    // ========== 4.1 Add Vertex ==========
    public boolean addAirport(Airport airport) {
        if (airport == null) { message("Unable to add airport: invalid data."); return false; }
        String airportCode = airport.getCode();

        if (!airportCode.matches("[A-Z]{3}")) {
            message("Unable to add airport: airport code must contain exactly 3 letters.");
            return false;
        }

        if (adjList.containsKey(airportCode)) {
            message(airportCode + " already exists");
            return false;
        } else {
            adjList.put(airportCode, new ArrayList<Edge>());
            airports.put(airportCode, airport);
            numOfVertices++;
            message("Airport " + airportCode + " added successfully.");
            return true;
        }
    }

    // ========== 4.2 Remove Vertex ==========
    public boolean removeAirport(String airport) {
        String airportCode = normaliseCode(airport);

        if (!adjList.containsKey(airportCode)) {
            message(airportCode + " does not exist");
            return false;
        } else {
            int outgoing = adjList.get(airportCode).size();
            int incoming = 0;
            for (String otherAirport : adjList.keySet()) {
                if (!otherAirport.equals(airportCode)) {
                    ArrayList<Edge> flights = adjList.get(otherAirport);
                    int before = flights.size();
                    flights.removeIf(edge -> edge.getDestinationCode().equals(airportCode));
                    incoming += before - flights.size();
                }
            }
            adjList.remove(airportCode);
            airports.remove(airportCode);
            numOfVertices--;
            numOfEdges -= (outgoing + incoming);
            message("Airport " + airportCode + " removed successfully.");
            message("Removed " + outgoing + " outgoing and " + incoming + " incoming flight(s).");
            return true;
        }
    }

    // ========== 4.3 Add Edge ==========
    public boolean addEdge(String fromAirport, String toAirport, int distance, double price, int time) {
        String from = normaliseCode(fromAirport);
        String to = normaliseCode(toAirport);

        if (!adjList.containsKey(from) || !adjList.containsKey(to)) {
            message("Airport does not exist");
            return false;
        }

        if (from.equals(to)) {
            message("Unable to add flight: source and destination cannot be the same.");
            return false;
        }

        if (distance <= 0 || price <= 0 || time <= 0) {
            message("Unable to add flight: distance, fare and duration must be positive.");
            return false;
        }

        for (Edge edge : adjList.get(from)) {
            if (edge.getDestinationCode().equals(to)) {
                message("Edge already exists");
                return false;
            }
        }

        Edge newEdge = new Edge(from, to, distance, price, time);
        adjList.get(from).add(newEdge);
        numOfEdges++;
        message("Flight " + from + " -> " + to + " added successfully.");
        return true;
    }

    // ========== 4.4 Remove Edge ==========
    public boolean removeEdge(String fromAirport, String toAirport) {
        String from = normaliseCode(fromAirport);
        String to = normaliseCode(toAirport);

        if (!adjList.containsKey(from) || !adjList.containsKey(to)) {
            message("Airport does not exist");
            return false;
        }

        boolean removed = false;
        for (Edge edge : adjList.get(from)) {
            if (edge.getDestinationCode().equals(to)) {
                adjList.get(from).remove(edge);
                removed = true;
                break;
            }
        }

        if (!removed) {
            message("Edge does not exist");
            return false;
        }

        numOfEdges--;
        message("Flight " + from + " -> " + to + " removed successfully.");
        return true;
    }

    // ========== Backward-compatibility wrappers ==========
    public boolean addFlight(String sourceCode, String destinationCode,
            int distanceKm, double fare, int durationMinutes) {
        return addEdge(sourceCode, destinationCode, distanceKm, fare, durationMinutes);
    }

    public boolean removeFlight(String sourceCode, String destinationCode) {
        return removeEdge(sourceCode, destinationCode);
    }

    // ========== Unchanged helper/query methods ==========
    public boolean airportExists(String code) { return airports.containsKey(normaliseCode(code)); }

    public boolean flightExists(String sourceCode, String destinationCode) {
        return getFlight(sourceCode, destinationCode) != null;
    }

    public Airport getAirport(String code) { return airports.get(normaliseCode(code)); }

    public ArrayList<Airport> getAirports() { return new ArrayList<>(airports.values()); }

    public ArrayList<Edge> getOutgoingFlights(String code) {
        ArrayList<Edge> flights = adjList.get(normaliseCode(code));
        return flights == null ? new ArrayList<>() : new ArrayList<>(flights);
    }

    public Edge getFlight(String sourceCode, String destinationCode) {
        String source = normaliseCode(sourceCode);
        String destination = normaliseCode(destinationCode);
        if (!adjList.containsKey(source)) return null;
        for (Edge edge : adjList.get(source)) {
            if (edge.getDestinationCode().equals(destination)) return edge;
        }
        return null;
    }

    public int getAirportCount() { return numOfVertices; }
    public int getFlightCount() { return numOfEdges; }

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
            ArrayList<Edge> flights = adjList.get(source);
            if (flights.isEmpty()) System.out.println("  No outgoing flights.");
            else for (Edge flight : flights) System.out.println("  " + flight);
        }
        System.out.println("\nTotal directed flights: " + getFlightCount());
    }
}