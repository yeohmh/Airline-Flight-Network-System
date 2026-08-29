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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
        details.setPrefWidth(310);

        Button zoomIn = new Button("Zoom In +");
        Button zoomOut = new Button("Zoom Out -");
        Button reset = new Button("Reset View");
        zoomIn.setOnAction(e -> graph.changeZoom(1.15));
        zoomOut.setOnAction(e -> graph.changeZoom(1 / 1.15));
        reset.setOnAction(e -> graph.resetView());

        Label modeTitle = new Label(title);
        modeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        modeTitle.setTextFill(Color.web("#12355b"));

        Label help = new Label(
                "Mouse wheel: zoom | Drag blank area: pan | Hover over a flight: view weights");
        help.setTextFill(Color.web("#475569"));

        HBox tools = new HBox(10, zoomIn, zoomOut, reset, help);
        VBox topSection = new VBox(8, modeTitle, tools);
        topSection.setPadding(new Insets(12));

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setCenter(graph);
        root.setRight(details);
        BorderPane.setMargin(details, new Insets(0, 10, 10, 10));

        Stage stage = new Stage();
        stage.setTitle(title == null ? "Airline Flight Network Graph" : title);
        stage.setScene(new Scene(root, 1280, 760));
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
                .append("Purple : shared by both routes\n")
                .append("\nHighlighted edge box:\n")
                .append("Distance | Fare | Duration\n");
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
            setPrefSize(950, 680);
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
            // Fixed layout keeps nearby airports readable and reduces line crossings.
            points.put("LGK", new Point2D(70, 80));
            points.put("PEN", new Point2D(150, 185));
            points.put("KBR", new Point2D(300, 105));
            points.put("KUL", new Point2D(260, 315));
            points.put("JHB", new Point2D(320, 445));
            points.put("KCH", new Point2D(465, 355));
            points.put("SIN", new Point2D(425, 550));
            points.put("CGK", new Point2D(535, 625));
            points.put("BKK", new Point2D(455, 125));
            points.put("BKI", new Point2D(620, 300));
            points.put("HKG", new Point2D(705, 185));
            points.put("MNL", new Point2D(740, 420));
            points.put("TPE", new Point2D(805, 105));
            points.put("ICN", new Point2D(865, 50));
            points.put("NRT", new Point2D(910, 175));
        }

        private void draw() {
            getChildren().clear();

            // Draw ordinary flights first so highlighted routes always stay visible on top.
            drawEdges(false);
            drawEdges(true);

            for (Airport airport : network.getAirports()) {
                drawAirport(airport);
            }
        }

        private void drawEdges(boolean highlightedOnly) {
            for (Airport airport : network.getAirports()) {
                Point2D from = points.get(airport.getCode());
                if (from == null) {
                    continue;
                }

                for (Edge edge : network.getOutgoingFlights(airport.getCode())) {
                    Point2D to = points.get(edge.getDestinationCode());
                    if (to == null) {
                        continue;
                    }

                    boolean first = containsEdge(primary, edge);
                    boolean second = containsEdge(secondary, edge);
                    boolean highlighted = first || second;
                    if (highlighted != highlightedOnly) {
                        continue;
                    }

                    Color color = first && second ? Color.web("#7c3aed")
                            : second ? Color.web("#16803c")
                                    : first ? Color.web("#d92d20")
                                            : Color.web("#9fb1c5");

                    double width = highlighted ? 3.2 : 1.05;
                    double opacity = highlighted ? 1.0 : hasRouteResult() ? 0.30 : 0.62;
                    drawArrow(from, to, edge, color, width, opacity, highlighted);
                }
            }
        }

        private boolean hasRouteResult() {
            return primary != null || secondary != null;
        }

        private boolean containsEdge(RouteResult result, Edge edge) {
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

        private boolean containsAirport(RouteResult result, String code) {
            return result != null && result.isRouteFound() && result.getPath().contains(code);
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

            boolean first = containsAirport(primary, airport.getCode());
            boolean second = containsAirport(secondary, airport.getCode());
            Color fill = first && second ? Color.web("#7c3aed")
                    : second ? Color.web("#16803c")
                            : first ? Color.web("#d92d20")
                                    : explored(airport.getCode()) ? Color.web("#d99a00")
                                            : Color.web("#1769aa");

            Circle circle = new Circle(p.getX(), p.getY(), 17, fill);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(2.2);

            Text code = new Text(airport.getCode());
            code.setFill(Color.WHITE);
            code.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            code.setX(p.getX() - code.getLayoutBounds().getWidth() / 2);
            code.setY(p.getY() + 4);

            Text city = new Text(airport.getCity());
            city.setFont(Font.font("Arial", 10));
            city.setFill(Color.web("#334155"));
            city.setX(p.getX() - city.getLayoutBounds().getWidth() / 2);
            city.setY(p.getY() + 34);

            Group node = new Group(circle, code, city);
            Tooltip.install(node, new Tooltip(airport.toString() + "\nOutgoing flights: "
                    + network.getOutgoingFlights(airport.getCode()).size()));
            getChildren().add(node);
        }

        private void drawArrow(Point2D from, Point2D to, Edge edge,
                Color color, double width, double opacity, boolean highlighted) {
            double dx = to.getX() - from.getX();
            double dy = to.getY() - from.getY();
            double length = Math.hypot(dx, dy);
            if (length == 0) {
                return;
            }

            double ux = dx / length;
            double uy = dy / length;
            double px = -uy;
            double py = ux;

            double x1 = from.getX() + ux * 20;
            double y1 = from.getY() + uy * 20;
            double x2 = to.getX() - ux * 21;
            double y2 = to.getY() - uy * 21;

            boolean reverseExists = network.flightExists(
                    edge.getDestinationCode(), edge.getSourceCode());

            double bend = 0;
            if (reverseExists) {
                // Separate opposite-direction flights so their arrows are both visible.
                bend = edge.getSourceCode().compareTo(edge.getDestinationCode()) < 0 ? 14 : -14;
            }

            // Small extra bend keeps LGK -> KUL from visually passing through PEN.
            if (edge.getSourceCode().equals("LGK") && edge.getDestinationCode().equals("KUL")) {
                bend = -20;
            }

            double controlX = (x1 + x2) / 2 + px * bend;
            double controlY = (y1 + y2) / 2 + py * bend;

            QuadCurve curve = new QuadCurve(x1, y1, controlX, controlY, x2, y2);
            curve.setFill(Color.TRANSPARENT);
            curve.setStroke(color);
            curve.setStrokeWidth(width);
            curve.setOpacity(opacity);

            // Arrow direction follows the curve tangent at the destination.
            double tangentX = x2 - controlX;
            double tangentY = y2 - controlY;
            double tangentLength = Math.hypot(tangentX, tangentY);
            double tx = tangentX / tangentLength;
            double ty = tangentY / tangentLength;
            double arrowPx = -ty;
            double arrowPy = tx;

            double arrowLength = highlighted ? 12 : 10;
            double arrowHalfWidth = highlighted ? 5 : 4;
            Polygon arrow = new Polygon(
                    x2, y2,
                    x2 - tx * arrowLength + arrowPx * arrowHalfWidth,
                    y2 - ty * arrowLength + arrowPy * arrowHalfWidth,
                    x2 - tx * arrowLength - arrowPx * arrowHalfWidth,
                    y2 - ty * arrowLength - arrowPy * arrowHalfWidth);
            arrow.setFill(color);
            arrow.setOpacity(opacity);

            String tooltip = edge.getSourceCode() + " -> " + edge.getDestinationCode()
                    + "\nDistance: " + edge.getDistanceKm() + " km"
                    + String.format("%nFare: RM %.2f", edge.getFare())
                    + "\nDuration: " + edge.getDurationMinutes() + " minutes";
            Tooltip.install(curve, new Tooltip(tooltip));
            Tooltip.install(arrow, new Tooltip(tooltip));
            getChildren().addAll(curve, arrow);

            if (highlighted) {
                drawWeightLabel(edge, color, x1, y1, controlX, controlY, x2, y2, px, py, bend);
            }
        }

        private void drawWeightLabel(Edge edge, Color color,
                double x1, double y1, double controlX, double controlY,
                double x2, double y2, double px, double py, double bend) {

            // Midpoint of a quadratic curve at t = 0.5.
            double midX = 0.25 * x1 + 0.5 * controlX + 0.25 * x2;
            double midY = 0.25 * y1 + 0.5 * controlY + 0.25 * y2;

            double labelOffset = bend == 0 ? 16 : Math.signum(bend) * 18;
            midX += px * labelOffset;
            midY += py * labelOffset;

            Label weightLabel = new Label(
                    edge.getSourceCode() + " → " + edge.getDestinationCode()
                    + "\n" + edge.getDistanceKm() + " km"
                    + String.format(" | RM %.2f", edge.getFare())
                    + " | " + edge.getDurationMinutes() + " min");

            weightLabel.setLayoutX(midX - 65);
            weightLabel.setLayoutY(midY - 18);
            weightLabel.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.96);"
                    + "-fx-border-color: " + toHex(color) + ";"
                    + "-fx-border-width: 1.2;"
                    + "-fx-border-radius: 5;"
                    + "-fx-background-radius: 5;"
                    + "-fx-padding: 4 6;"
                    + "-fx-font-size: 10px;"
                    + "-fx-font-weight: bold;");

            getChildren().add(weightLabel);
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
