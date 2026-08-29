package airlineflightnetwork;

import java.util.ArrayList;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/** Full JavaFX interface backed by the existing graph and algorithm classes. */
public class AirlineSystemGUI extends Application {

    private static final String BLUE = "#12355b";
    private static final String ACCENT = "#1769aa";
    private static final String PANEL = "-fx-background-color: white; -fx-background-radius: 12;"
            + "-fx-border-color: #d8e2ef; -fx-border-radius: 12; -fx-padding: 14;";

    private final AirlineNetwork network = InitialData.createNetwork();
    private final BFSRouteFinder bfs = new BFSRouteFinder();
    private final DijkstraRouteFinder dijkstra = new DijkstraRouteFinder();
    private final AlgorithmCaseAnalyzer analyzer = new AlgorithmCaseAnalyzer();

    private final ObservableList<Airport> airports = FXCollections.observableArrayList();
    private final ObservableList<Edge> flights = FXCollections.observableArrayList();
    private final ComboBox<Airport> departureBox = airportCombo();
    private final ComboBox<Airport> destinationBox = airportCombo();
    private final ComboBox<String> routeMode = new ComboBox<>();
    private final TextArea routeDetails = readOnlyArea();
    private InteractiveGraphView graphView;
    private Label networkCount;
    private TableView<Airport> airportTable;
    private TableView<Edge> flightTable;
    private ComboBox<Airport> flightSourceBox;
    private ComboBox<Airport> flightDestinationBox;

    @Override
    public void start(Stage stage) {
        graphView = new InteractiveGraphView(network, new InteractiveGraphView.Listener() {
            @Override public void showAirport(Airport airport) { showAirportDetails(airport); }
            @Override public void setDeparture(Airport airport) {
                departureBox.setValue(airport); updateEndpointHighlights();
            }
            @Override public void setDestination(Airport airport) {
                destinationBox.setValue(airport); updateEndpointHighlights();
            }
            @Override public void showFlight(Edge edge) { showFlightDetails(edge); }
        });

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #edf3f9;");
        root.setTop(createHeader());

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tab("Interactive Network", createNetworkWorkspace()),
                tab("Graph Management", createManagementWorkspace()),
                tab("Algorithm Analysis", createAnalysisWorkspace()));
        root.setCenter(tabs);

        refreshData();
        routeMode.getItems().setAll(
                "Minimum Flight Legs / Transfers - BFS",
                "Shortest Total Distance - Dijkstra",
                "Cheapest Total Fare - Dijkstra",
                "Fastest Total Flight Duration - Dijkstra");
        routeMode.getSelectionModel().selectFirst();
        departureBox.valueProperty().addListener((o, a, b) -> updateEndpointHighlights());
        destinationBox.valueProperty().addListener((o, a, b) -> updateEndpointHighlights());

        stage.setTitle("Airline Flight Network System");
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double initialWidth = Math.min(1280, screen.getWidth() * 0.90);
        double initialHeight = Math.min(800, screen.getHeight() * 0.90);
        stage.setScene(new Scene(root, initialWidth, initialHeight));
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.show();
        stage.centerOnScreen();
    }

    private Node createHeader() {
        Label title = new Label("AIRLINE FLIGHT NETWORK SYSTEM");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Directed weighted graph • Adjacency list • BFS • Dijkstra");
        subtitle.setTextFill(Color.web("#dbeafe"));
        VBox titles = new VBox(3, title, subtitle);
        networkCount = new Label();
        networkCount.setStyle("-fx-background-color: rgba(255,255,255,0.16);"
                + "-fx-text-fill: white; -fx-padding: 8 14; -fx-background-radius: 18;"
                + "-fx-font-weight: bold;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(16, titles, spacer, networkCount);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 22, 16, 22));
        header.setStyle("-fx-background-color: linear-gradient(to right, #12355b, #1769aa);");
        return header;
    }

    private Node createNetworkWorkspace() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(14));
        ScrollPane routeScroll = new ScrollPane(createRouteControls());
        routeScroll.setFitToWidth(true);
        routeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        routeScroll.setPrefWidth(300);
        routeScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        pane.setLeft(routeScroll);
        StackPane graphHolder = new StackPane(graphView);
        graphHolder.setPadding(new Insets(0, 12, 0, 12));
        pane.setCenter(graphHolder);
        pane.setRight(createDetailsPanel());
        return pane;
    }

    private Node createRouteControls() {
        Label heading = sectionTitle("Route Search");
        Label hint = new Label("Right-click an airport on the graph\nor use the lists below.");
        hint.setStyle("-fx-text-fill: #52657a; -fx-font-size: 12px;");
        hint.setWrapText(true);
        departureBox.setMaxWidth(Double.MAX_VALUE);
        destinationBox.setMaxWidth(Double.MAX_VALUE);
        routeMode.setMaxWidth(Double.MAX_VALUE);
        routeMode.setPrefWidth(265);

        Button search = primaryButton("Find Route");
        search.setMaxWidth(Double.MAX_VALUE);
        search.setOnAction(e -> findRoute());
        Button compare = secondaryButton("Compare BFS + Dijkstra");
        compare.setMaxWidth(Double.MAX_VALUE);
        compare.setOnAction(e -> compareRoutes());
        Button clear = secondaryButton("Clear Route");
        clear.setMaxWidth(Double.MAX_VALUE);
        clear.setOnAction(e -> {
            departureBox.setValue(null); destinationBox.setValue(null);
            routeDetails.clear(); graphView.clearSelection();
        });
        Separator sep = new Separator();
        Label graphTools = sectionTitle("Graph Tools");
        Button plus = secondaryButton("Zoom In +");
        Button minus = secondaryButton("Zoom Out −");
        Button reset = secondaryButton("Reset View");
        plus.setOnAction(e -> graphView.changeZoom(1.15));
        minus.setOnAction(e -> graphView.changeZoom(1 / 1.15));
        reset.setOnAction(e -> graphView.resetView());
        HBox zoom = new HBox(7, plus, minus);

        VBox box = new VBox(9,
                heading, hint,
                formLabel("Departure"), departureBox,
                formLabel("Destination"), destinationBox,
                formLabel("Optimisation"), routeMode,
                search, compare, clear, sep, graphTools, zoom, reset,
                legend());
        box.setPrefWidth(285);
        box.setStyle(PANEL);
        return box;
    }

    private Node createDetailsPanel() {
        Label heading = sectionTitle("Selection Details");
        Label help = new Label("Click an airport or directed edge to inspect it.\n"
                + "Highlighted edge labels show distance, fare and duration.");
        help.setWrapText(true);
        help.setTextFill(Color.web("#64748b"));
        routeDetails.setPrefWidth(330);
        routeDetails.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px;");
        VBox box = new VBox(10, heading, help, routeDetails);
        VBox.setVgrow(routeDetails, Priority.ALWAYS);
        box.setPrefWidth(350);
        box.setStyle(PANEL);
        return box;
    }

    private Node createManagementWorkspace() {
        airportTable = createAirportTable();
        flightTable = createFlightTable();
        SplitPane split = new SplitPane(createAirportPanel(), createFlightPanel());
        split.setDividerPositions(.48);
        BorderPane pane = new BorderPane(split);
        pane.setPadding(new Insets(14));
        return pane;
    }

    private Node createAirportPanel() {
        TextField code = new TextField();
        TextField name = new TextField();
        TextField city = new TextField();
        TextField country = new TextField();
        code.setPromptText("e.g. AOR");
        name.setPromptText("Airport name");
        city.setPromptText("City");
        country.setPromptText("Country");
        GridPane form = formGrid();
        addFormRow(form, 0, "Airport Code", code);
        addFormRow(form, 1, "Airport Name", name);
        addFormRow(form, 2, "City", city);
        addFormRow(form, 3, "Country", country);
        Button add = primaryButton("Add Airport");
        Button remove = dangerButton("Remove Selected Airport");
        add.setOnAction(e -> {
            try {
                Airport airport = new Airport(code.getText(), name.getText(), city.getText(), country.getText());
                if (!airport.getCode().matches("[A-Z]{3}")) {
                    error("Airport code must contain exactly three letters."); return;
                }
                if (network.airportExists(airport.getCode())) {
                    error("Airport " + airport.getCode() + " already exists."); return;
                }
                if (network.addAirport(airport)) {
                    refreshData(); code.clear(); name.clear(); city.clear(); country.clear();
                    info("Airport Added", "Airport " + airport.getCode() + " was added successfully.");
                }
            } catch (IllegalArgumentException ex) { error(ex.getMessage()); }
        });
        remove.setOnAction(e -> {
            Airport selected = airportTable.getSelectionModel().getSelectedItem();
            if (selected == null) { error("Select an airport from the table first."); return; }
            if (confirm("Remove Airport", "Remove " + selected.getCode()
                    + " and all of its incoming and outgoing flights?")) {
                network.removeAirport(selected.getCode()); refreshData();
            }
        });
        HBox buttons = new HBox(10, add, remove);
        VBox box = new VBox(12, sectionTitle("Airport Vertices"), form, buttons, airportTable);
        VBox.setVgrow(airportTable, Priority.ALWAYS);
        box.setPadding(new Insets(14));
        box.setStyle(PANEL);
        return box;
    }

    private Node createFlightPanel() {
        flightSourceBox = airportCombo();
        flightDestinationBox = airportCombo();
        TextField distance = new TextField();
        TextField fare = new TextField();
        TextField duration = new TextField();
        distance.setPromptText("km"); fare.setPromptText("RM"); duration.setPromptText("minutes");
        GridPane form = formGrid();
        addFormRow(form, 0, "Source", flightSourceBox);
        addFormRow(form, 1, "Destination", flightDestinationBox);
        addFormRow(form, 2, "Distance", distance);
        addFormRow(form, 3, "Fare", fare);
        addFormRow(form, 4, "Duration", duration);
        Button add = primaryButton("Add Directed Flight");
        Button remove = dangerButton("Remove Selected Flight");
        add.setOnAction(e -> {
            Airport source = flightSourceBox.getValue();
            Airport destination = flightDestinationBox.getValue();
            if (source == null || destination == null) { error("Select both source and destination airports."); return; }
            if (source.equals(destination)) { error("Source and destination airports must be different."); return; }
            if (network.flightExists(source.getCode(), destination.getCode())) {
                error("Flight " + source.getCode() + " → " + destination.getCode() + " already exists."); return;
            }
            try {
                int d = Integer.parseInt(distance.getText().trim());
                double f = Double.parseDouble(fare.getText().trim());
                int t = Integer.parseInt(duration.getText().trim());
                if (d <= 0 || f <= 0 || t <= 0) {
                    error("Distance, fare and duration must be greater than zero."); return;
                }
                if (network.addFlight(source.getCode(), destination.getCode(), d, f, t)) {
                    refreshData(); distance.clear(); fare.clear(); duration.clear();
                    info("Flight Added", "Flight " + source.getCode() + " → "
                            + destination.getCode() + " was added successfully.");
                }
            } catch (NumberFormatException ex) { error("Distance and duration must be whole numbers, and fare must be numeric."); }
            catch (IllegalArgumentException ex) { error(ex.getMessage()); }
        });
        remove.setOnAction(e -> {
            Edge selected = flightTable.getSelectionModel().getSelectedItem();
            if (selected == null) { error("Select a directed flight from the table first."); return; }
            if (confirm("Remove Flight", "Remove " + selected.getSourceCode()
                    + " → " + selected.getDestinationCode() + "?")) {
                network.removeFlight(selected.getSourceCode(), selected.getDestinationCode()); refreshData();
            }
        });
        HBox buttons = new HBox(10, add, remove);
        VBox box = new VBox(12, sectionTitle("Directed Flight Edges"), form, buttons, flightTable);
        VBox.setVgrow(flightTable, Priority.ALWAYS);
        box.setPadding(new Insets(14));
        box.setStyle(PANEL);
        return box;
    }

    private Node createAnalysisWorkspace() {
        TextArea output = readOnlyArea();
        output.setStyle("-fx-font-family: Consolas; -fx-font-size: 13px;");
        ComboBox<RoutePreference> preference = new ComboBox<>();
        preference.getItems().setAll(RoutePreference.values());
        preference.getSelectionModel().select(RoutePreference.FARE);
        Button bfsBest = secondaryButton("BFS Observed Best");
        Button bfsWorst = secondaryButton("BFS Observed Worst");
        Button dijBest = secondaryButton("Dijkstra Observed Best");
        Button dijWorst = secondaryButton("Dijkstra Observed Worst");
        Button theory = primaryButton("Theoretical Complexity");
        bfsBest.setOnAction(e -> output.setText(formatCase(analyzer.findBfsBest(network), true)));
        bfsWorst.setOnAction(e -> output.setText(formatCase(analyzer.findBfsWorst(network), false)));
        dijBest.setOnAction(e -> output.setText(formatCase(
                analyzer.findDijkstraBest(network, preference.getValue()), true)));
        dijWorst.setOnAction(e -> output.setText(formatCase(
                analyzer.findDijkstraWorst(network, preference.getValue()), false)));
        theory.setOnAction(e -> output.setText(theoreticalComplexity()));
        HBox controls = new HBox(9, bfsBest, bfsWorst, preference, dijBest, dijWorst, theory);
        controls.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(12, sectionTitle("Algorithm Best/Worst Cases and Complexity"), controls, output);
        VBox.setVgrow(output, Priority.ALWAYS);
        box.setPadding(new Insets(18));
        box.setStyle(PANEL);
        BorderPane pane = new BorderPane(box);
        pane.setPadding(new Insets(14));
        return pane;
    }

    private void findRoute() {
        Airport source = departureBox.getValue(), destination = destinationBox.getValue();
        if (!validPair(source, destination)) return;
        RouteResult result;
        int mode = routeMode.getSelectionModel().getSelectedIndex();
        if (mode == 0) result = bfs.findRoute(network, source.getCode(), destination.getCode());
        else result = dijkstra.findRoute(network, source.getCode(), destination.getCode(),
                mode == 1 ? RoutePreference.DISTANCE
                        : mode == 2 ? RoutePreference.FARE : RoutePreference.DURATION);
        graphView.setRoutes(result, null);
        routeDetails.setText(formatRoute(routeMode.getValue(), result));
    }

    private void compareRoutes() {
        Airport source = departureBox.getValue(), destination = destinationBox.getValue();
        if (!validPair(source, destination)) return;
        int mode = routeMode.getSelectionModel().getSelectedIndex();
        RoutePreference preference = mode == 1 ? RoutePreference.DISTANCE
                : mode == 3 ? RoutePreference.DURATION : RoutePreference.FARE;
        RouteResult first = bfs.findRoute(network, source.getCode(), destination.getCode());
        RouteResult second = dijkstra.findRoute(network, source.getCode(), destination.getCode(), preference);
        graphView.setRoutes(first, second);
        routeDetails.setText(formatRoute("PRIMARY - BFS", first)
                + "\n\n" + formatRoute("SECONDARY - " + preference.getLabel(), second)
                + "\n\nCOMPARISON\nBFS minimises flight legs. Dijkstra minimises "
                + preference.getLabel().toLowerCase() + ".");
    }

    private boolean validPair(Airport source, Airport destination) {
        if (source == null || destination == null) { error("Select both departure and destination airports."); return false; }
        if (source.equals(destination)) { error("Departure and destination airports must be different."); return false; }
        return true;
    }

    private void updateEndpointHighlights() {
        if (graphView == null) return;
        graphView.setSelectedAirports(codeOf(departureBox.getValue()), codeOf(destinationBox.getValue()));
    }

    private void showAirportDetails(Airport airport) {
        routeDetails.setText("AIRPORT VERTEX\n\n"
                + "Code     : " + airport.getCode() + "\n"
                + "Airport  : " + airport.getName() + "\n"
                + "City     : " + airport.getCity() + "\n"
                + "Country  : " + airport.getCountry() + "\n"
                + "Outdegree: " + network.getOutgoingFlights(airport.getCode()).size()
                + " outgoing flight(s)\n\n"
                + "Right-click this vertex to set it as a route endpoint.");
    }

    private void showFlightDetails(Edge edge) {
        routeDetails.setText("DIRECTED FLIGHT EDGE\n\n"
                + "Source      : " + edge.getSourceCode() + "\n"
                + "Destination : " + edge.getDestinationCode() + "\n"
                + "Distance    : " + edge.getDistanceKm() + " km\n"
                + String.format("Fare        : RM %.2f%n", edge.getFare())
                + "Duration    : " + edge.getDurationMinutes() + " minutes\n\n"
                + "This edge is stored only in the source airport's adjacency list.");
    }

    private String formatRoute(String title, RouteResult result) {
        StringBuilder out = new StringBuilder(title).append("\n")
                .append("Status          : ").append(result.getMessage()).append("\n");
        if (!result.getSearchOrder().isEmpty()) out.append("Search Order    : ")
                .append(String.join(" → ", result.getSearchOrder())).append("\n");
        if (result.isRouteFound()) out.append("Route           : ")
                .append(String.join(" → ", result.getPath())).append("\n")
                .append("Flight Legs     : ").append(result.getFlightLegs()).append("\n")
                .append("Transfers       : ").append(result.getTransfers()).append("\n")
                .append("Total Distance  : ").append(result.getTotalDistanceKm()).append(" km\n")
                .append(String.format("Total Fare      : RM %.2f%n", result.getTotalFare()))
                .append("Flight Duration : ").append(result.getFormattedDuration()).append("\n");
        return out.append("\n").append(formatMetrics(result.getMetrics())).toString();
    }

    private String formatMetrics(AlgorithmMetrics m) {
        StringBuilder out = new StringBuilder("OPERATION COUNTERS\n")
                .append("Vertices Processed  : ").append(m.getVerticesProcessed()).append("\n")
                .append("Edges Examined      : ").append(m.getEdgesExamined()).append("\n")
                .append("Queue Insertions    : ").append(m.getQueueInsertions()).append("\n")
                .append("Queue Removals      : ").append(m.getQueueRemovals()).append("\n");
        if (m.getAlgorithm().equalsIgnoreCase("DIJKSTRA")) out.append("Successful Relaxation: ")
                .append(m.getSuccessfulRelaxations()).append("\n");
        return out.append("Maximum Queue Size  : ").append(m.getMaximumQueueSize()).append("\n")
                .append("Observed Work Units : ").append(m.getObservedWorkUnits()).toString();
    }

    private String formatCase(AlgorithmCaseAnalyzer.AnalysisCase c, boolean best) {
        if (c == null) return "No valid case is available.";
        RouteResult r = c.getResult();
        String algorithm = c.getPreference() == null ? "BFS" : "DIJKSTRA";
        return algorithm + " OBSERVED " + (best ? "BEST" : "WORST") + " CASE\n\n"
                + "Classification     : Current dataset, not theoretical Big O\n"
                + "Valid Pairs Tested : " + c.getPairsTested() + "\n"
                + "Source             : " + c.getSource() + "\n"
                + "Destination        : " + c.getDestination() + "\n"
                + (c.getPreference() == null ? "" : "Optimisation       : " + c.getPreference().getLabel() + "\n")
                + "Route              : " + (r.isRouteFound() ? String.join(" → ", r.getPath()) : "Unavailable") + "\n\n"
                + formatMetrics(r.getMetrics()) + "\n\n"
                + "This is the " + (best ? "minimum" : "maximum")
                + " observed work count among valid source ≠ destination pairs.";
    }

    private String theoreticalComplexity() {
        int v = network.getAirportCount(), e = network.getFlightCount();
        return "THEORETICAL TIME AND SPACE COMPLEXITY\n\n"
                + "Current input: V = " + v + " airports, E = " + e + " directed flights\n"
                + "Graph storage: O(V + E)\n\n"
                + "BFS\n"
                + "Best defensive case : Θ(1) time, Θ(1) space\n"
                + "Worst-case time      : O(V + E)\n"
                + "Worst auxiliary space: O(V)\n"
                + "Current scale        : " + v + " + " + e + " = " + (v + e) + " graph records\n\n"
                + "DIJKSTRA WITH PRIORITYQUEUE\n"
                + "Best defensive case : Θ(1) time, Θ(1) space\n"
                + "Different-airport setup: Θ(V)\n"
                + "Worst-case time      : O((V + E) log V)\n"
                + "Worst auxiliary space: O(V + E)\n\n"
                + "Substituted values illustrate current input scale, not exact runtime.";
    }

    private void refreshData() {
        airports.setAll(network.getAirports());
        ArrayList<Edge> all = new ArrayList<>();
        for (Airport airport : network.getAirports()) all.addAll(network.getOutgoingFlights(airport.getCode()));
        flights.setAll(all);
        setComboItems(departureBox, airports);
        setComboItems(destinationBox, airports);
        if (flightSourceBox != null) setComboItems(flightSourceBox, airports);
        if (flightDestinationBox != null) setComboItems(flightDestinationBox, airports);
        if (networkCount != null) networkCount.setText(network.getAirportCount()
                + " Airports  •  " + network.getFlightCount() + " Directed Flights");
        if (graphView != null) graphView.refreshGraph();
    }

    private void setComboItems(ComboBox<Airport> box, ObservableList<Airport> values) {
        Airport selected = box.getValue();
        box.setItems(FXCollections.observableArrayList(values));
        if (selected != null) box.getItems().stream()
                .filter(a -> a.getCode().equals(selected.getCode())).findFirst().ifPresent(box::setValue);
    }

    private TableView<Airport> createAirportTable() {
        TableView<Airport> table = new TableView<>(airports);
        table.getColumns().add(column("Code", "code", 65));
        table.getColumns().add(column("Airport", "name", 210));
        table.getColumns().add(column("City", "city", 120));
        table.getColumns().add(column("Country", "country", 110));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private TableView<Edge> createFlightTable() {
        TableView<Edge> table = new TableView<>(flights);
        TableColumn<Edge, String> source = new TableColumn<>("From");
        source.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSourceCode()));
        TableColumn<Edge, String> destination = new TableColumn<>("To");
        destination.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDestinationCode()));
        TableColumn<Edge, Number> distance = new TableColumn<>("Distance");
        distance.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDistanceKm()));
        TableColumn<Edge, Number> fare = new TableColumn<>("Fare");
        fare.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getFare()));
        TableColumn<Edge, Number> duration = new TableColumn<>("Minutes");
        duration.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDurationMinutes()));
        table.getColumns().addAll(source, destination, distance, fare, duration);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private <T> TableColumn<T, String> column(String title, String property, double width) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private static ComboBox<Airport> airportCombo() {
        ComboBox<Airport> box = new ComboBox<>();
        box.setConverter(new StringConverter<>() {
            @Override public String toString(Airport airport) {
                return airport == null ? "" : airport.getCode() + " — " + airport.getCity();
            }
            @Override public Airport fromString(String value) { return null; }
        });
        return box;
    }

    private static TextArea readOnlyArea() {
        TextArea area = new TextArea();
        area.setEditable(false); area.setWrapText(true);
        return area;
    }

    private static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(9); grid.setVgap(9);
        return grid;
    }

    private static void addFormRow(GridPane grid, int row, String label, Node input) {
        Label l = formLabel(label);
        grid.add(l, 0, row); grid.add(input, 1, row);
        GridPane.setHgrow(input, Priority.ALWAYS);
        if (input instanceof Region) ((Region) input).setMaxWidth(Double.MAX_VALUE);
    }

    private static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-family: Arial; -fx-font-size: 17px;"
                + "-fx-font-weight: bold; -fx-text-fill: #12355b;");
        return label;
    }

    private static Label formLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        return label;
    }

    private static Button primaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 8 12;");
        return button;
    }

    private static Button secondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #e7eef7; -fx-text-fill: #12355b;"
                + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 8 11;");
        return button;
    }

    private static Button dangerButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #b42318; -fx-text-fill: white;"
                + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 8 11;");
        return button;
    }

    private static Node legend() {
        Label text = new Label("GRAPH INTERACTION\n"
                + "Left-click vertex: details\n"
                + "Right-click vertex: set endpoint\n"
                + "Click edge: weights + highlight\n"
                + "Mouse wheel: zoom\n"
                + "Drag blank area: pan");
        text.setStyle("-fx-background-color: #e8f0f8; -fx-padding: 10;"
                + "-fx-background-radius: 8; -fx-text-fill: #334155;"
                + "-fx-font-size: 12px; -fx-font-weight: normal;");
        text.setMaxWidth(Double.MAX_VALUE);
        return text;
    }

    private static Tab tab(String title, Node content) {
        Tab tab = new Tab(title, content);
        return tab;
    }

    private static String codeOf(Airport airport) { return airport == null ? null : airport.getCode(); }

    private static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Invalid Operation"); alert.setHeaderText("Please check the entered information"); alert.showAndWait();
    }

    private static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }

    private static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title); alert.setHeaderText("Confirm operation");
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static void main(String[] args) { launch(args); }
}
