package airlineflightnetwork;

public class Main {

    public static void main(String[] args) {

        AirlineNetwork network = new AirlineNetwork();

        System.out.println(
                "========== ADD AIRPORT TEST ==========");

        network.addAirport(new Airport(
                "KUL",
                "Kuala Lumpur International Airport",
                "Kuala Lumpur",
                "Malaysia"));

        network.addAirport(new Airport(
                "PEN",
                "Penang International Airport",
                "Penang",
                "Malaysia"));

        network.addAirport(new Airport(
                "BKK",
                "Suvarnabhumi Airport",
                "Bangkok",
                "Thailand"));

        network.addAirport(new Airport(
                "NRT",
                "Narita International Airport",
                "Tokyo",
                "Japan"));

        network.addAirport(new Airport(
                "KUL",
                "Duplicate Airport",
                "Kuala Lumpur",
                "Malaysia"));

        System.out.println(
                "\n========== ADD FLIGHT TEST ==========");

        network.addFlight(
                "KUL", "PEN",
                325, 120.00, 60);

        network.addFlight(
                "KUL", "BKK",
                1215, 320.00, 130);

        network.addFlight(
                "PEN", "BKK",
                945, 210.00, 105);

        network.addFlight(
                "BKK", "NRT",
                4650, 780.00, 365);

        network.addFlight(
                "KUL", "PEN",
                325, 150.00, 65);

        network.addFlight(
                "KUL", "XXX",
                500, 100.00, 60);

        network.addFlight(
                "KUL", "NRT",
                -1, 900.00, 420);

        network.displayAirports();
        network.displayFlights();

        System.out.println(
                "\n========== REMOVE TEST ==========");

        network.removeFlight("PEN", "BKK");
        network.removeFlight("PEN", "BKK");

        network.removeAirport("BKK");
        network.removeAirport("XXX");

        network.displayFlights();
    }
}