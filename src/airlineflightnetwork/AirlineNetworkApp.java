package airlineflightnetwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class AirlineNetworkApp extends Application {
    private final AirlineNetwork network = InitialData.createNetwork();
    private final BFSRouteFinder bfs = new BFSRouteFinder();
    private final DijkstraRouteFinder dijkstra = new DijkstraRouteFinder();
    private final ComboBox<Airport> sourceBox = new ComboBox<>();
    private final ComboBox<Airport> destinationBox = new ComboBox<>();
    private final ComboBox<Airport> flightSourceBox = new ComboBox<>();
    private final ComboBox<Airport> flightDestinationBox = new ComboBox<>();
    private final ComboBox<Airport> removeAirportBox = new ComboBox<>();
    private final TextArea resultArea = new TextArea();
    private final TextArea networkArea = new TextArea();
    private final Label summaryLabel = new Label();
    private final GraphPane graphPane = new GraphPane();
    private final GraphPane networkMapPane = new GraphPane();
    private RouteResult lastResult;
    private RouteResult comparisonResult;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setTop(createHeader());
        root.setCenter(createTabs());
        root.setStyle("-fx-background-color: #f4f7fb;");
        refreshAllControls();

        Scene scene = new Scene(root, 1240, 760);
        stage.setTitle("Airline Flight Network System");
        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createHeader() {
        Label title = new Label("AIRLINE FLIGHT NETWORK SYSTEM");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 25));
        Label subtitle = new Label("Directed Weighted Graph • BFS • Dijkstra's Algorithm");
        subtitle.setTextFill(Color.web("#dbeafe"));
        summaryLabel.setTextFill(Color.WHITE);
        summaryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        VBox box = new VBox(5, title, subtitle, summaryLabel);
        box.setPadding(new Insets(18, 25, 18, 25));
        box.setStyle("-fx-background-color: linear-gradient(to right, #12355b, #1f6aa5);");
        return box;
    }

    private TabPane createTabs() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(tab("Route Planner", createRoutePlanner()));
        tabs.getTabs().add(tab("Network Map", createMapView()));
        tabs.getTabs().add(tab("Manage Network", createManagementView()));
        tabs.getTabs().add(tab("Network Data", createNetworkDataView()));
        return tabs;
    }

    private Tab tab(String name, javafx.scene.Node content) {
        Tab tab = new Tab(name, content);
        tab.setClosable(false);
        return tab;
    }

    private BorderPane createRoutePlanner() {
        Label heading = sectionTitle("Find the Best Route");
        Label hint = new Label("Select complete airport names below—no airport code memorisation is required.");
        sourceBox.setPromptText("Select departure airport");
        destinationBox.setPromptText("Select destination airport");
        sourceBox.setMaxWidth(Double.MAX_VALUE);
        destinationBox.setMaxWidth(Double.MAX_VALUE);
        sourceBox.setOnAction(e -> redrawMaps());
        destinationBox.setOnAction(e -> redrawMaps());

        ToggleGroup algorithms = new ToggleGroup();
        RadioButton minimumLegs = radio("BFS: Minimum Flight Legs", algorithms, true);
        RadioButton shortest = radio("Dijkstra: Shortest Distance", algorithms, false);
        RadioButton cheapest = radio("Dijkstra: Cheapest Fare", algorithms, false);
        RadioButton fastest = radio("Dijkstra: Fastest Flight Time", algorithms, false);
        VBox algorithmBox = new VBox(8, minimumLegs, shortest, cheapest, fastest);
        algorithmBox.setStyle(cardStyle());
        algorithmBox.setPadding(new Insets(15));

        Button findButton = primaryButton("Find Route");
        Button compareButton = secondaryButton("Compare BFS with Selected Dijkstra");
        Button clearButton = secondaryButton("Clear");
        HBox buttons = new HBox(10, findButton, compareButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.add(new Label("Departure:"), 0, 0); form.add(sourceBox, 1, 0);
        form.add(new Label("Destination:"), 0, 1); form.add(destinationBox, 1, 1);
        GridPane.setHgrow(sourceBox, Priority.ALWAYS);
        GridPane.setHgrow(destinationBox, Priority.ALWAYS);

        resultArea.setEditable(false);
        resultArea.setWrapText(false);
        resultArea.setFont(Font.font("Consolas", 13));
        resultArea.setPromptText("The selected route, search order and totals will appear here.");
        VBox left = new VBox(12, heading, hint, form, new Label("Optimisation Method:"), algorithmBox,
                buttons, new Label("Route Result:"), resultArea);
        left.setPadding(new Insets(20));
        left.setPrefWidth(540);
        VBox.setVgrow(resultArea, Priority.ALWAYS);

        graphPane.setPrefSize(660, 600);
        BorderPane pane = new BorderPane();
        pane.setLeft(left);
        pane.setCenter(createMapContainer(graphPane));
        BorderPane.setMargin(pane.getCenter(), new Insets(20, 20, 20, 0));

        findButton.setOnAction(e -> {
            if (!validRouteSelection()) return;
            RouteResult result;
            String headingText;
            if (minimumLegs.isSelected()) {
                result = bfs.findRoute(network, selectedCode(sourceBox), selectedCode(destinationBox));
                headingText = "BFS - MINIMUM FLIGHT LEGS";
            } else {
                RoutePreference preference = shortest.isSelected() ? RoutePreference.DISTANCE
                        : cheapest.isSelected() ? RoutePreference.FARE : RoutePreference.DURATION;
                result = dijkstra.findRoute(network, selectedCode(sourceBox),
                        selectedCode(destinationBox), preference);
                headingText = "DIJKSTRA - " + preference.getLabel().toUpperCase();
            }
            showResult(headingText, result);
        });

        compareButton.setOnAction(e -> {
            if (!validRouteSelection()) return;
            RoutePreference preference = shortest.isSelected() ? RoutePreference.DISTANCE
                    : fastest.isSelected() ? RoutePreference.DURATION : RoutePreference.FARE;
            RouteResult first = bfs.findRoute(network, selectedCode(sourceBox), selectedCode(destinationBox));
            RouteResult second = dijkstra.findRoute(network, selectedCode(sourceBox),
                    selectedCode(destinationBox), preference);
            resultArea.setText(formatResult("BFS - MINIMUM FLIGHT LEGS", first)
                    + "\n\n" + formatResult("DIJKSTRA - " + preference.getLabel().toUpperCase(), second)
                    + comparison(first, second, preference));
            lastResult = first;
            comparisonResult = second;
            redrawMaps();
        });

        clearButton.setOnAction(e -> {
            sourceBox.getSelectionModel().clearSelection();
            destinationBox.getSelectionModel().clearSelection();
            resultArea.clear(); lastResult = null; comparisonResult = null; redrawMaps();
        });
        return pane;
    }

    private BorderPane createMapView() {
        networkMapPane.setPrefSize(1150, 620);
        return createMapContainer(networkMapPane);
    }

    private BorderPane createMapContainer(GraphPane map) {
        Button zoomIn = secondaryButton("Zoom In +");
        Button zoomOut = secondaryButton("Zoom Out −");
        Button reset = secondaryButton("Reset View");
        Label help = new Label("Mouse wheel: zoom  •  Drag blank map: pan  •  Click airport/selected route: interact");
        help.setTextFill(Color.web("#52606d"));
        zoomIn.setOnAction(e -> map.changeZoom(1.15));
        zoomOut.setOnAction(e -> map.changeZoom(1 / 1.15));
        reset.setOnAction(e -> map.resetView());
        HBox tools = new HBox(9, zoomIn, zoomOut, reset, help);
        tools.setAlignment(Pos.CENTER_LEFT);
        tools.setPadding(new Insets(8));
        BorderPane wrapper = new BorderPane(map);
        wrapper.setTop(tools);
        return wrapper;
    }

    private BorderPane createManagementView() {
        VBox airportCard = createAirportManagement();
        VBox flightCard = createFlightManagement();
        HBox cards = new HBox(18, airportCard, flightCard);
        cards.setPadding(new Insets(22));
        HBox.setHgrow(airportCard, Priority.ALWAYS);
        HBox.setHgrow(flightCard, Priority.ALWAYS);
        BorderPane pane = new BorderPane(cards);
        return pane;
    }

    private VBox createAirportManagement() {
        TextField code = new TextField();
        TextField name = new TextField();
        TextField city = new TextField();
        TextField country = new TextField();
        code.setPromptText("e.g. KUL"); name.setPromptText("Full airport name");
        city.setPromptText("City"); country.setPromptText("Country");
        GridPane form = formGrid();
        addRow(form, 0, "Airport Code", code); addRow(form, 1, "Airport Name", name);
        addRow(form, 2, "City", city); addRow(form, 3, "Country", country);
        Button add = primaryButton("Add Airport");
        add.setOnAction(e -> {
            try {
                if (network.addAirport(new Airport(code.getText(), name.getText(), city.getText(), country.getText()))) {
                    alert(Alert.AlertType.INFORMATION, "Airport Added", "The airport vertex was added successfully.");
                    code.clear(); name.clear(); city.clear(); country.clear(); refreshAllControls();
                } else alert(Alert.AlertType.WARNING, "Unable to Add", "The airport code already exists.");
            } catch (IllegalArgumentException ex) { alert(Alert.AlertType.ERROR, "Invalid Airport", ex.getMessage()); }
        });
        removeAirportBox.setMaxWidth(Double.MAX_VALUE);
        Button remove = dangerButton("Remove Airport and Related Flights");
        remove.setOnAction(e -> {
            Airport selected = removeAirportBox.getValue();
            if (selected == null) { alert(Alert.AlertType.WARNING, "Selection Required", "Select an airport first."); return; }
            if (network.removeAirport(selected.getCode())) {
                alert(Alert.AlertType.INFORMATION, "Airport Removed", "The vertex and all incoming/outgoing edges were removed.");
                refreshAllControls();
            }
        });
        VBox box = new VBox(13, sectionTitle("Airport (Vertex) Operations"), form, add,
                separatorLabel("REMOVE AIRPORT"), removeAirportBox, remove);
        box.setPadding(new Insets(20)); box.setStyle(cardStyle()); box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox createFlightManagement() {
        TextField distance = new TextField();
        TextField fare = new TextField();
        TextField duration = new TextField();
        distance.setPromptText("Positive integer"); fare.setPromptText("Positive number");
        duration.setPromptText("Minutes");
        GridPane form = formGrid();
        addRow(form, 0, "Source", flightSourceBox); addRow(form, 1, "Destination", flightDestinationBox);
        addRow(form, 2, "Distance (km)", distance); addRow(form, 3, "Fare (RM)", fare);
        addRow(form, 4, "Duration (min)", duration);
        flightSourceBox.setMaxWidth(Double.MAX_VALUE); flightDestinationBox.setMaxWidth(Double.MAX_VALUE);
        Button add = primaryButton("Add Directed Flight");
        Button remove = dangerButton("Remove Directed Flight");
        add.setOnAction(e -> {
            if (flightSourceBox.getValue() == null || flightDestinationBox.getValue() == null) {
                alert(Alert.AlertType.WARNING, "Selection Required", "Select both source and destination airports."); return;
            }
            try {
                boolean added = network.addFlight(selectedCode(flightSourceBox), selectedCode(flightDestinationBox),
                        Integer.parseInt(distance.getText().trim()), Double.parseDouble(fare.getText().trim()),
                        Integer.parseInt(duration.getText().trim()));
                if (added) { alert(Alert.AlertType.INFORMATION, "Flight Added", "The directed weighted edge was added."); refreshAllControls(); }
                else alert(Alert.AlertType.WARNING, "Unable to Add", "Check for duplicate route, same airport or invalid weights.");
            } catch (NumberFormatException ex) { alert(Alert.AlertType.ERROR, "Invalid Weights", "Distance and duration must be whole numbers; fare must be numeric."); }
        });
        remove.setOnAction(e -> {
            if (flightSourceBox.getValue() == null || flightDestinationBox.getValue() == null) {
                alert(Alert.AlertType.WARNING, "Selection Required", "Select both source and destination airports."); return;
            }
            if (network.removeFlight(selectedCode(flightSourceBox), selectedCode(flightDestinationBox))) {
                alert(Alert.AlertType.INFORMATION, "Flight Removed", "The directed edge was removed."); refreshAllControls();
            } else alert(Alert.AlertType.WARNING, "Route Not Found", "That directed flight does not exist.");
        });
        VBox box = new VBox(13, sectionTitle("Flight (Edge) Operations"), form, new HBox(10, add, remove));
        box.setPadding(new Insets(20)); box.setStyle(cardStyle()); box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private VBox createNetworkDataView() {
        networkArea.setEditable(false); networkArea.setFont(Font.font("Consolas", 12));
        Button refresh = secondaryButton("Refresh Network Data");
        refresh.setOnAction(e -> refreshNetworkText());
        VBox box = new VBox(12, sectionTitle("Adjacency List and Weighted Edges"), refresh, networkArea);
        box.setPadding(new Insets(20)); VBox.setVgrow(networkArea, Priority.ALWAYS);
        return box;
    }

    private void refreshAllControls() {
        ArrayList<Airport> airports = network.getAirports();
        sourceBox.setItems(FXCollections.observableArrayList(airports));
        destinationBox.setItems(FXCollections.observableArrayList(airports));
        flightSourceBox.setItems(FXCollections.observableArrayList(airports));
        flightDestinationBox.setItems(FXCollections.observableArrayList(airports));
        removeAirportBox.setItems(FXCollections.observableArrayList(airports));
        summaryLabel.setText(network.getAirportCount() + " airports  •  " + network.getFlightCount() + " directed flights");
        redrawMaps();
        refreshNetworkText();
    }

    private void refreshNetworkText() {
        StringBuilder text = new StringBuilder();
        text.append("AIRPORTS: ").append(network.getAirportCount()).append("    DIRECTED FLIGHTS: ")
                .append(network.getFlightCount()).append("\n\n");
        for (Airport airport : network.getAirports()) {
            text.append(airport).append("\n");
            ArrayList<FlightEdge> edges = network.getOutgoingFlights(airport.getCode());
            if (edges.isEmpty()) text.append("  No outgoing flights.\n");
            else for (FlightEdge edge : edges) text.append("  ").append(edge).append("\n");
            text.append("\n");
        }
        networkArea.setText(text.toString());
    }

    private boolean validRouteSelection() {
        if (sourceBox.getValue() == null || destinationBox.getValue() == null) {
            alert(Alert.AlertType.WARNING, "Airport Selection Required", "Select both departure and destination airports.");
            return false;
        }
        return true;
    }

    private void showResult(String heading, RouteResult result) {
        lastResult = result;
        comparisonResult = null;
        resultArea.setText(formatResult(heading, result));
        redrawMaps();
    }

    private void redrawMaps() {
        ArrayList<String> primary = lastResult != null && lastResult.isRouteFound()
                ? lastResult.getPath() : new ArrayList<>();
        ArrayList<String> secondary = comparisonResult != null && comparisonResult.isRouteFound()
                ? comparisonResult.getPath() : new ArrayList<>();
        ArrayList<String> visited = new ArrayList<>();
        if (lastResult != null) visited.addAll(lastResult.getSearchOrder());
        if (comparisonResult != null) {
            for (String code : comparisonResult.getSearchOrder()) if (!visited.contains(code)) visited.add(code);
        }
        graphPane.drawNetwork(primary, secondary, visited);
        networkMapPane.drawNetwork(primary, secondary, visited);
    }

    private String formatResult(String heading, RouteResult result) {
        StringBuilder out = new StringBuilder(heading).append("\n").append("=".repeat(44)).append("\n");
        out.append("Status         : ").append(result.getMessage()).append("\n");
        if (!result.getSearchOrder().isEmpty()) out.append("Search Order   : ").append(String.join(" -> ", result.getSearchOrder())).append("\n");
        if (result.isRouteFound()) {
            out.append("Route          : ").append(String.join(" -> ", result.getPath())).append("\n");
            out.append("Flight Legs    : ").append(result.getFlightLegs()).append("\n");
            out.append("Transfers      : ").append(result.getTransfers()).append("\n");
            out.append("Total Distance : ").append(result.getTotalDistanceKm()).append(" km\n");
            out.append(String.format("Total Fare     : RM %.2f%n", result.getTotalFare()));
            out.append("Flight Time    : ").append(result.getFormattedDuration()).append("\n");
        }
        return out.toString();
    }

    private String comparison(RouteResult bfsResult, RouteResult weighted, RoutePreference preference) {
        if (!bfsResult.isRouteFound() || !weighted.isRouteFound()) return "";
        return "\nCOMPARISON\n" + "=".repeat(44) + "\n"
                + "BFS minimises flight legs without using edge weights.\n"
                + "Dijkstra minimises " + preference.getLabel().toLowerCase()
                + " and may use more legs for a lower total weight.\n";
    }

    private String selectedCode(ComboBox<Airport> box) { return box.getValue().getCode(); }
    private RadioButton radio(String text, ToggleGroup group, boolean selected) {
        RadioButton button = new RadioButton(text); button.setToggleGroup(group); button.setSelected(selected); return button;
    }
    private Label sectionTitle(String text) { Label label = new Label(text); label.setFont(Font.font("Arial", FontWeight.BOLD, 19)); return label; }
    private Label separatorLabel(String text) { Label label = new Label(text); label.setTextFill(Color.web("#64748b")); label.setFont(Font.font("Arial", FontWeight.BOLD, 11)); return label; }
    private Button primaryButton(String text) { Button b = new Button(text); b.setStyle("-fx-background-color:#1769aa;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:9 14;"); return b; }
    private Button secondaryButton(String text) { Button b = new Button(text); b.setStyle("-fx-background-color:#e2e8f0;-fx-text-fill:#17324d;-fx-padding:9 14;"); return b; }
    private Button dangerButton(String text) { Button b = new Button(text); b.setStyle("-fx-background-color:#b42318;-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:9 14;"); return b; }
    private String cardStyle() { return "-fx-background-color:white;-fx-background-radius:8;-fx-border-color:#d8e0e8;-fx-border-radius:8;"; }
    private GridPane formGrid() { GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); return grid; }
    private void addRow(GridPane grid, int row, String label, javafx.scene.control.Control control) {
        grid.add(new Label(label + ":"), 0, row); grid.add(control, 1, row); GridPane.setHgrow(control, Priority.ALWAYS);
    }
    private void alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void showAirportMenu(Airport airport, javafx.scene.Node owner,
            double screenX, double screenY) {
        MenuItem departure = new MenuItem("Set " + airport.getCode() + " as Departure");
        MenuItem destination = new MenuItem("Set " + airport.getCode() + " as Destination");
        MenuItem details = new MenuItem("View Airport Details");
        departure.setOnAction(e -> { sourceBox.setValue(airport); redrawMaps(); });
        destination.setOnAction(e -> { destinationBox.setValue(airport); redrawMaps(); });
        details.setOnAction(e -> alert(Alert.AlertType.INFORMATION, "Airport Vertex Details",
                "Airport Code: " + airport.getCode()
                + "\nAirport Name: " + airport.getName()
                + "\nCity: " + airport.getCity()
                + "\nCountry: " + airport.getCountry()
                + "\nOutgoing Flights: " + network.getOutgoingFlights(airport.getCode()).size()));
        ContextMenu menu = new ContextMenu(departure, destination, details);
        menu.show(owner, screenX, screenY);
    }

    private void showFlightDetails(FlightEdge edge) {
        int hours = edge.getDurationMinutes() / 60;
        int minutes = edge.getDurationMinutes() % 60;
        alert(Alert.AlertType.INFORMATION, "Selected Route Edge",
                "Directed Flight: " + edge.getSourceCode() + " → " + edge.getDestinationCode()
                + "\nDistance: " + edge.getDistanceKm() + " km"
                + String.format("%nFare: RM %.2f", edge.getFare())
                + "\nDuration: " + hours + " hour(s) " + minutes + " minute(s)"
                + "\n\nThis edge is part of the reconstructed route.");
    }

    private class GraphPane extends Pane {
        private final Map<String, Point2D> fixedPoints = new LinkedHashMap<>();
        private double zoom = 1.0;
        private double dragX;
        private double dragY;
        private double originalTranslateX;
        private double originalTranslateY;
        private boolean panning;

        GraphPane() {
            setStyle("-fx-background-color:white;-fx-border-color:#d8e0e8;-fx-border-radius:8;-fx-background-radius:8;");
            setClip(null);
            fixedPoints.put("LGK", new Point2D(80,80)); fixedPoints.put("PEN", new Point2D(150,145));
            fixedPoints.put("KBR", new Point2D(290,100)); fixedPoints.put("KUL", new Point2D(230,250));
            fixedPoints.put("JHB", new Point2D(285,360)); fixedPoints.put("KCH", new Point2D(420,285));
            fixedPoints.put("BKI", new Point2D(550,245)); fixedPoints.put("SIN", new Point2D(340,430));
            fixedPoints.put("BKK", new Point2D(400,110)); fixedPoints.put("CGK", new Point2D(450,500));
            fixedPoints.put("MNL", new Point2D(620,310)); fixedPoints.put("HKG", new Point2D(585,140));
            fixedPoints.put("TPE", new Point2D(690,90)); fixedPoints.put("NRT", new Point2D(820,110));
            fixedPoints.put("ICN", new Point2D(760,45));
            widthProperty().addListener((o,a,b) -> redrawMaps());
            heightProperty().addListener((o,a,b) -> redrawMaps());
            setOnScroll(e -> { changeZoom(e.getDeltaY() > 0 ? 1.12 : 1 / 1.12); e.consume(); });
            setOnMousePressed(e -> {
                if (e.getTarget() != this) return;
                dragX = e.getSceneX(); dragY = e.getSceneY();
                originalTranslateX = getTranslateX(); originalTranslateY = getTranslateY();
                panning = true;
                setCursor(Cursor.MOVE);
            });
            setOnMouseDragged(e -> {
                if (!panning) return;
                setTranslateX(originalTranslateX + e.getSceneX() - dragX);
                setTranslateY(originalTranslateY + e.getSceneY() - dragY);
            });
            setOnMouseReleased(e -> { panning = false; setCursor(Cursor.DEFAULT); });
        }

        void changeZoom(double factor) {
            zoom = Math.max(0.65, Math.min(2.40, zoom * factor));
            setScaleX(zoom); setScaleY(zoom);
        }

        void resetView() {
            zoom = 1.0; setScaleX(1); setScaleY(1); setTranslateX(0); setTranslateY(0);
        }

        void drawNetwork(ArrayList<String> route) {
            drawNetwork(route, new ArrayList<>(), new ArrayList<>());
        }

        void drawNetwork(ArrayList<String> primaryRoute, ArrayList<String> secondaryRoute,
                ArrayList<String> visited) {
            getChildren().clear();
            String legendText = secondaryRoute.isEmpty()
                    ? "Blue: airport   Yellow: explored   Grey: flight   Red: selected route"
                    : "Yellow: explored   Red: BFS route   Green: Dijkstra route   Purple: shared edge";
            Label legend = new Label(legendText);
            legend.setLayoutX(15); legend.setLayoutY(12); legend.setTextFill(Color.web("#52606d"));
            legend.setStyle("-fx-background-color:rgba(255,255,255,0.90);-fx-padding:4 7;-fx-font-weight:bold;");
            getChildren().add(legend);
            Map<String, Point2D> points = scaledPoints();
            for (Airport airport : network.getAirports()) {
                Point2D from = points.get(airport.getCode()); if (from == null) continue;
                for (FlightEdge edge : network.getOutgoingFlights(airport.getCode())) {
                    Point2D to = points.get(edge.getDestinationCode()); if (to == null) continue;
                    boolean primary = routeEdge(primaryRoute, airport.getCode(), edge.getDestinationCode());
                    boolean secondary = routeEdge(secondaryRoute, airport.getCode(), edge.getDestinationCode());
                    Color color = primary && secondary ? Color.web("#7c3aed")
                            : secondary ? Color.web("#16803c")
                            : primary ? Color.web("#d92d20") : Color.web("#cbd5e1");
                    drawArrow(from, to, color, primary || secondary ? 3.4 : 1.0, edge, primary || secondary);
                }
            }
            for (Airport airport : network.getAirports()) {
                Point2D p = points.get(airport.getCode()); if (p == null) continue;
                String airportCode = airport.getCode();
                boolean primary = primaryRoute.contains(airportCode);
                boolean secondary = secondaryRoute.contains(airportCode);
                Color fill = primary && secondary ? Color.web("#7c3aed")
                        : secondary ? Color.web("#16803c")
                        : primary ? Color.web("#d92d20")
                        : visited.contains(airportCode) ? Color.web("#d99a00") : Color.web("#1769aa");
                String source = sourceBox.getValue() == null ? "" : sourceBox.getValue().getCode();
                String destination = destinationBox.getValue() == null ? "" : destinationBox.getValue().getCode();
                if (airportCode.equals(source) || airportCode.equals(destination)) {
                    Circle ring = new Circle(p.getX(), p.getY(), 19, Color.TRANSPARENT);
                    ring.setStroke(airportCode.equals(source) ? Color.web("#00a878") : Color.web("#9333ea"));
                    ring.setStrokeWidth(4); getChildren().add(ring);
                }
                Circle circle = new Circle(p.getX(), p.getY(), primary || secondary ? 16 : 13, fill);
                circle.setStroke(Color.WHITE); circle.setStrokeWidth(2);
                Text code = new Text(p.getX() - 13, p.getY() + 4, airportCode);
                code.setFill(Color.WHITE); code.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                Text city = new Text(p.getX() - 24, p.getY() + 29, airport.getCity());
                city.setFill(Color.web("#334155")); city.setFont(Font.font("Arial", 10));
                Group airportNode = new Group(circle, code, city);
                airportNode.setCursor(Cursor.HAND);
                Tooltip.install(airportNode, new Tooltip(airport.toString() + "\nClick to select or view details."));
                airportNode.setOnMouseClicked(e -> {
                    showAirportMenu(airport, airportNode, e.getScreenX(), e.getScreenY());
                    e.consume();
                });
                getChildren().add(airportNode);
            }
        }
        private Map<String, Point2D> scaledPoints() {
            double sx = Math.max(0.75, getWidth() / 900.0); double sy = Math.max(0.75, getHeight() / 590.0);
            Map<String, Point2D> points = new LinkedHashMap<>(); int extra = 0;
            for (Airport airport : network.getAirports()) {
                Point2D p = fixedPoints.get(airport.getCode());
                if (p == null) { p = new Point2D(80 + (extra % 6) * 130, 545); extra++; }
                points.put(airport.getCode(), new Point2D(p.getX() * sx, p.getY() * sy));
            }
            return points;
        }
        private boolean routeEdge(ArrayList<String> route, String from, String to) {
            for (int i = 0; i < route.size() - 1; i++) if (route.get(i).equals(from) && route.get(i + 1).equals(to)) return true;
            return false;
        }
        private void drawArrow(Point2D from, Point2D to, Color color, double width,
                FlightEdge edge, boolean interactive) {
            double dx = to.getX() - from.getX(), dy = to.getY() - from.getY();
            double length = Math.hypot(dx, dy); if (length == 0) return;
            double ux = dx / length, uy = dy / length;
            double x1 = from.getX() + ux * 16, y1 = from.getY() + uy * 16;
            double x2 = to.getX() - ux * 18, y2 = to.getY() - uy * 18;
            Line line = new Line(x1, y1, x2, y2); line.setStroke(color); line.setStrokeWidth(width);
            double px = -uy, py = ux;
            Polygon arrow = new Polygon(x2, y2, x2 - ux * 10 + px * 4, y2 - uy * 10 + py * 4,
                    x2 - ux * 10 - px * 4, y2 - uy * 10 - py * 4);
            arrow.setFill(color);
            if (interactive) {
                Line hitArea = new Line(x1, y1, x2, y2);
                hitArea.setStroke(Color.TRANSPARENT); hitArea.setStrokeWidth(14);
                hitArea.setCursor(Cursor.HAND);
                hitArea.setOnMouseClicked(e -> { showFlightDetails(edge); e.consume(); });
                line.setCursor(Cursor.HAND); arrow.setCursor(Cursor.HAND);
                line.setOnMouseClicked(e -> { showFlightDetails(edge); e.consume(); });
                arrow.setOnMouseClicked(e -> { showFlightDetails(edge); e.consume(); });
                getChildren().addAll(hitArea, line, arrow);
            } else getChildren().addAll(line, arrow);
        }
    }

    public static void main(String[] args) { launch(args); }
}
