package airlineflightnetwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

/**
 * JavaFX is used only for graph and traversal-result visualisation.
 */
public final class GraphWindow {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private GraphWindow() {
    }

    public static void show(AirlineNetwork network, RouteResult primary,
            RouteResult secondary, String title) {
        CountDownLatch closed = new CountDownLatch(1);
        Runnable open = () -> createStage(network, primary, secondary, title, closed);
        if (STARTED.compareAndSet(false, true)) {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                open.run();
            });
        } else {
            Platform.runLater(open);
        }
        try {
            closed.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void createStage(AirlineNetwork network, RouteResult primary,
            RouteResult secondary, String title, CountDownLatch closed) {
        GraphPane graph = new GraphPane(network, primary, secondary);
        TextArea details = new TextArea(
                "COMPUTATION MODE\n"
                + title
                + "\n\n"
                + buildDetails(network, primary, secondary));
        details.setEditable(false);
        details.setWrapText(true);
        details.setFont(Font.font("Consolas", 12));
        details.setPrefWidth(330);

        Button zoomIn = new Button("Zoom In +");
        Button zoomOut = new Button("Zoom Out -");
        Button reset = new Button("Reset View");
        zoomIn.setOnAction(e -> graph.changeZoom(1.15));
        zoomOut.setOnAction(e -> graph.changeZoom(1 / 1.15));
        reset.setOnAction(e -> graph.resetView());
        Label modeTitle = new Label(title);
        modeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        modeTitle.setTextFill(Color.web("#12355b"));

        HBox tools = new HBox(10, zoomIn, zoomOut, reset,
                new Label("Mouse wheel: zoom | Drag blank area: pan | Hover over an edge: view weights"));

        VBox topSection = new VBox(8, modeTitle, tools);
        topSection.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(graph);
        root.setRight(details);
        BorderPane.setMargin(details, new Insets(0, 10, 10, 10));
        Stage stage = new Stage();
        stage.setTitle(title == null ? "Airline Flight Network Graph" : title);
        stage.setScene(new Scene(root, 1240, 720));
        stage.setOnHidden(e -> closed.countDown());
        stage.show();
    }

    private static String buildDetails(AirlineNetwork network, RouteResult first, RouteResult second) {
        StringBuilder out = new StringBuilder("DIRECTED WEIGHTED GRAPH\n")
                .append("Representation : Adjacency list\n")
                .append("Airports       : ").append(network.getAirportCount()).append("\n")
                .append("Flights        : ").append(network.getFlightCount()).append("\n")
                .append("Weights        : Distance, fare, duration\n\n");
        appendResult(out, "PRIMARY ROUTE", first);
        appendResult(out, "SECONDARY ROUTE", second);
        out.append("\nLEGEND\n")
                .append("Grey   : directed flight\n")
                .append("Blue   : airport vertex\n")
                .append("Yellow : explored airport\n")
                .append("Red    : primary route\n")
                .append("Green  : secondary route\n")
                .append("Purple : edge shared by both routes\n");
        return out.toString();
    }

    private static void appendResult(StringBuilder out, String label, RouteResult result) {
        if (result == null) {
            return;
        }
        out.append(label).append("\n").append(result.getMessage()).append("\n");
        if (!result.getSearchOrder().isEmpty()) {
            out.append("Search: ").append(String.join(" -> ", result.getSearchOrder())).append("\n");
        }
        if (result.isRouteFound()) {
            out.append("Route : ").append(String.join(" -> ", result.getPath())).append("\n")
                    .append("Legs  : ").append(result.getFlightLegs()).append("\n")
                    .append("Fare  : RM ").append(String.format("%.2f", result.getTotalFare())).append("\n")
                    .append("Distance: ").append(result.getTotalDistanceKm()).append(" km\n")
                    .append("Time  : ").append(result.getFormattedDuration()).append("\n");
        }
        out.append("\n");
    }

    private static final class GraphPane extends Pane {

        private final AirlineNetwork network;
        private final RouteResult primary;
        private final RouteResult secondary;
        private final Map<String, Point2D> points = new LinkedHashMap<>();
        private double zoom = 1;
        private double pressX;
        private double pressY;
        private double startX;
        private double startY;

        GraphPane(AirlineNetwork network, RouteResult primary, RouteResult secondary) {
            this.network = network;
            this.primary = primary;
            this.secondary = secondary;
            setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1;");
            setPrefSize(880, 660);
            initialisePoints();
            widthProperty().addListener((o, a, b) -> draw());
            heightProperty().addListener((o, a, b) -> draw());
            setOnScroll(e -> {
                changeZoom(e.getDeltaY() > 0 ? 1.12 : 1 / 1.12);
                e.consume();
            });
            setOnMousePressed(e -> {
                if (e.getTarget() != this) {
                    return;
                }
                pressX = e.getSceneX();
                pressY = e.getSceneY();
                startX = getTranslateX();
                startY = getTranslateY();
            });
            setOnMouseDragged(e -> {
                if (e.getTarget() != this) {
                    return;
                }
                setTranslateX(startX + e.getSceneX() - pressX);
                setTranslateY(startY + e.getSceneY() - pressY);
            });
            draw();
        }

        void changeZoom(double factor) {
            zoom = Math.max(0.65, Math.min(2.4, zoom * factor));
            setScaleX(zoom);
            setScaleY(zoom);
        }

        void resetView() {
            zoom = 1;
            setScaleX(1);
            setScaleY(1);
            setTranslateX(0);
            setTranslateY(0);
        }

        private void initialisePoints() {
            points.put("LGK", new Point2D(75, 85));
            points.put("PEN", new Point2D(145, 160));
            points.put("KBR", new Point2D(285, 105));
            points.put("KUL", new Point2D(230, 275));
            points.put("JHB", new Point2D(285, 390));
            points.put("KCH", new Point2D(425, 310));
            points.put("BKI", new Point2D(555, 260));
            points.put("SIN", new Point2D(350, 465));
            points.put("BKK", new Point2D(405, 120));
            points.put("CGK", new Point2D(470, 535));
            points.put("MNL", new Point2D(635, 340));
            points.put("HKG", new Point2D(600, 155));
            points.put("TPE", new Point2D(700, 100));
            points.put("NRT", new Point2D(825, 125));
            points.put("ICN", new Point2D(765, 50));
        }

        private void draw() {
            getChildren().clear();
            for (Airport airport : network.getAirports()) {
                Point2D from = points.get(airport.getCode());
                if (from == null) {
                    continue;
                }
                for (FlightEdge edge : network.getOutgoingFlights(airport.getCode())) {
                    Point2D to = points.get(edge.getDestinationCode());
                    if (to == null) {
                        continue;
                    }
                    boolean first = containsEdge(primary, edge);
                    boolean second = containsEdge(secondary, edge);
                    Color color = first && second ? Color.web("#7c3aed")
                            : second ? Color.web("#16803c") : first ? Color.web("#d92d20")
                                    : Color.web("#cbd5e1");
                    drawArrow(from, to, edge, color, first || second ? 3.5 : 1.0);
                }
            }
            for (Airport airport : network.getAirports()) {
                drawAirport(airport);
            }
        }

        private boolean containsEdge(RouteResult result, FlightEdge edge) {
            if (result == null || !result.isRouteFound()) {
                return false;
            }
            ArrayList<String> path = result.getPath();
            for (int i = 0; i < path.size() - 1; i++) {
                if (path.get(i).equals(edge.getSourceCode())
                        && path.get(i + 1).equals(edge.getDestinationCode())) {
                    return true;
                }
            }
            return false;
        }

        private boolean explored(String code) {
            return primary != null && primary.getSearchOrder().contains(code)
                    || secondary != null && secondary.getSearchOrder().contains(code);
        }

        private void drawAirport(Airport airport) {
            Point2D p = points.get(airport.getCode());
            if (p == null) {
                return;
            }
            Color fill = explored(airport.getCode()) ? Color.web("#d99a00") : Color.web("#1769aa");
            Circle circle = new Circle(p.getX(), p.getY(), 15, fill);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2);
            Text code = new Text(p.getX() - 13, p.getY() + 4, airport.getCode());
            code.setFill(Color.WHITE);
            code.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            Text city = new Text(p.getX() - 25, p.getY() + 31, airport.getCity());
            city.setFont(Font.font("Arial", 10));
            city.setFill(Color.web("#334155"));
            Group node = new Group(circle, code, city);
            Tooltip.install(node, new Tooltip(airport.toString() + "\nOutgoing flights: "
                    + network.getOutgoingFlights(airport.getCode()).size()));
            getChildren().add(node);
        }

        private void drawArrow(Point2D from, Point2D to, FlightEdge edge, Color color, double width) {
            double dx = to.getX() - from.getX();
            double dy = to.getY() - from.getY();
            double length = Math.hypot(dx, dy);
            if (length == 0) {
                return;
            }
            double ux = dx / length, uy = dy / length;
            double x1 = from.getX() + ux * 17, y1 = from.getY() + uy * 17;
            double x2 = to.getX() - ux * 19, y2 = to.getY() - uy * 19;
            Line line = new Line(x1, y1, x2, y2);
            line.setStroke(color);
            line.setStrokeWidth(width);
            double px = -uy, py = ux;
            Polygon arrow = new Polygon(x2, y2,
                    x2 - ux * 10 + px * 4, y2 - uy * 10 + py * 4,
                    x2 - ux * 10 - px * 4, y2 - uy * 10 - py * 4);
            arrow.setFill(color);
            String tooltip = edge.getSourceCode() + " -> " + edge.getDestinationCode()
                    + "\nDistance: " + edge.getDistanceKm() + " km"
                    + String.format("%nFare: RM %.2f", edge.getFare())
                    + "\nDuration: " + edge.getDurationMinutes() + " minutes";
            Tooltip.install(line, new Tooltip(tooltip));
            Tooltip.install(arrow, new Tooltip(tooltip));
            getChildren().addAll(line, arrow);
            if (width > 1.0) {
                Label weightLabel = new Label(
                        edge.getSourceCode() + " → " + edge.getDestinationCode()
                        + "\n" + edge.getDistanceKm() + " km"
                        + String.format(" | RM %.2f", edge.getFare())
                        + " | " + edge.getDurationMinutes() + " min");

                weightLabel.setLayoutX((x1 + x2) / 2);
                weightLabel.setLayoutY((y1 + y2) / 2);
                weightLabel.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.92);"
                        + "-fx-border-color: " + toHex(color) + ";"
                        + "-fx-border-radius: 4;"
                        + "-fx-background-radius: 4;"
                        + "-fx-padding: 3 5;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;");

                getChildren().add(weightLabel);
            }
        }

        private String toHex(Color color) {
            return String.format(
                    "#%02X%02X%02X",
                    (int) Math.round(color.getRed() * 255),
                    (int) Math.round(color.getGreen() * 255),
                    (int) Math.round(color.getBlue() * 255));
        }
    }
}
