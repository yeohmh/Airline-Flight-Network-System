package airlineflightnetwork;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;

/** Interactive version of the existing airline graph used by the full GUI. */
public class InteractiveGraphView extends Pane {

    private static final double CANVAS_WIDTH = 980;
    private static final double CANVAS_HEIGHT = 690;

    public interface Listener {
        void showAirport(Airport airport);
        void setDeparture(Airport airport);
        void setDestination(Airport airport);
        void showFlight(FlightEdge edge);
    }

    private final AirlineNetwork network;
    private final Listener listener;
    private final Map<String, Point2D> points = new LinkedHashMap<>();
    private final Pane canvas = new Pane();
    private RouteResult primary;
    private RouteResult secondary;
    private String departureCode;
    private String destinationCode;
    private FlightEdge selectedEdge;
    private double zoom = 1;
    private double panX;
    private double panY;
    private double pressX;
    private double pressY;
    private double startX;
    private double startY;

    public InteractiveGraphView(AirlineNetwork network, Listener listener) {
        this.network = network;
        this.listener = listener;
        setStyle("-fx-background-color: linear-gradient(to bottom right, #f8fbff, #eef4fb);"
                + "-fx-border-color: #cad7e6; -fx-border-radius: 12;"
                + "-fx-background-radius: 12;");
        setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setMinSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setPickOnBounds(false);
        getChildren().add(canvas);

        Rectangle viewportClip = new Rectangle();
        viewportClip.widthProperty().bind(widthProperty());
        viewportClip.heightProperty().bind(heightProperty());
        viewportClip.setArcWidth(22);
        viewportClip.setArcHeight(22);
        setClip(viewportClip);
        initialisePoints();

        widthProperty().addListener((o, a, b) -> applyInitialViewWhenReady());
        heightProperty().addListener((o, a, b) -> applyInitialViewWhenReady());
        setOnScroll(e -> {
            changeZoom(e.getDeltaY() > 0 ? 1.10 : 1 / 1.10);
            e.consume();
        });
        setOnMousePressed(e -> {
            if (e.getTarget() == this && e.getButton() == MouseButton.PRIMARY) {
                pressX = e.getSceneX();
                pressY = e.getSceneY();
                startX = panX;
                startY = panY;
            }
        });
        setOnMouseDragged(e -> {
            if (e.getTarget() == this && e.getButton() == MouseButton.PRIMARY) {
                panX = startX + e.getSceneX() - pressX;
                panY = startY + e.getSceneY() - pressY;
                applyCanvasTransform();
            }
        });
        draw();
    }

    public void setSelectedAirports(String departure, String destination) {
        departureCode = departure;
        destinationCode = destination;
        draw();
    }

    public void setRoutes(RouteResult first, RouteResult second) {
        primary = first;
        secondary = second;
        selectedEdge = null;
        draw();
    }

    public void refreshGraph() {
        ensureEveryAirportHasPoint();
        draw();
    }

    public void clearSelection() {
        primary = null;
        secondary = null;
        selectedEdge = null;
        departureCode = null;
        destinationCode = null;
        draw();
    }

    public void changeZoom(double factor) {
        double newZoom = Math.max(0.35, Math.min(2.35, zoom * factor));
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        double worldX = (centerX - panX) / zoom;
        double worldY = (centerY - panY) / zoom;
        panX = centerX - worldX * newZoom;
        panY = centerY - worldY * newZoom;
        zoom = newZoom;
        applyCanvasTransform();
    }

    public void resetView() {
        double availableWidth = Math.max(100, getWidth() - 24);
        double availableHeight = Math.max(100, getHeight() - 24);
        zoom = Math.min(1.0, Math.min(
                availableWidth / CANVAS_WIDTH,
                availableHeight / CANVAS_HEIGHT));
        panX = (getWidth() - CANVAS_WIDTH * zoom) / 2;
        panY = (getHeight() - CANVAS_HEIGHT * zoom) / 2;
        applyCanvasTransform();
    }

    private void applyInitialViewWhenReady() {
        if (getWidth() > 100 && getHeight() > 100) resetView();
    }

    private void applyCanvasTransform() {
        canvas.getTransforms().setAll(
                new Affine(zoom, 0, panX, 0, zoom, panY));
    }

    private void initialisePoints() {
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
        ensureEveryAirportHasPoint();
    }

    private void ensureEveryAirportHasPoint() {
        ArrayList<Airport> missing = new ArrayList<>();
        for (Airport airport : network.getAirports()) {
            if (!points.containsKey(airport.getCode())) missing.add(airport);
        }
        int total = Math.max(1, missing.size());
        for (int i = 0; i < missing.size(); i++) {
            double angle = (2 * Math.PI * i / total) - Math.PI / 2;
            points.put(missing.get(i).getCode(), new Point2D(
                    500 + 390 * Math.cos(angle), 340 + 270 * Math.sin(angle)));
        }
    }

    private void draw() {
        canvas.getChildren().clear();
        drawEdges(false);
        drawEdges(true);
        for (Airport airport : network.getAirports()) drawAirport(airport);
    }

    private void drawEdges(boolean emphasizedOnly) {
        for (Airport airport : network.getAirports()) {
            Point2D from = points.get(airport.getCode());
            if (from == null) continue;
            for (FlightEdge edge : network.getOutgoingFlights(airport.getCode())) {
                Point2D to = points.get(edge.getDestinationCode());
                if (to == null) continue;
                boolean first = containsEdge(primary, edge);
                boolean second = containsEdge(secondary, edge);
                boolean selected = sameEdge(selectedEdge, edge);
                boolean emphasized = first || second || selected;
                if (emphasized != emphasizedOnly) continue;

                Color color = selected ? Color.web("#0284c7")
                        : first && second ? Color.web("#7c3aed")
                        : second ? Color.web("#16803c")
                        : first ? Color.web("#d92d20") : Color.web("#9fb1c5");
                drawArrow(from, to, edge, color,
                        emphasized ? 3.4 : 1.05,
                        emphasized ? 1.0 : hasRouteResult() ? 0.24 : 0.58,
                        emphasized);
            }
        }
    }

    private void drawAirport(Airport airport) {
        Point2D p = points.get(airport.getCode());
        if (p == null) return;
        String codeValue = airport.getCode();
        boolean departure = codeValue.equals(departureCode);
        boolean destination = codeValue.equals(destinationCode);
        boolean first = containsAirport(primary, codeValue);
        boolean second = containsAirport(secondary, codeValue);

        Color fill = departure ? Color.web("#0f766e")
                : destination ? Color.web("#ea580c")
                : first && second ? Color.web("#7c3aed")
                : second ? Color.web("#16803c")
                : first ? Color.web("#d92d20")
                : explored(codeValue) ? Color.web("#d99a00") : Color.web("#1769aa");

        Circle halo = new Circle(p.getX(), p.getY(), 23, Color.TRANSPARENT);
        halo.setStroke(departure ? Color.web("#5eead4")
                : destination ? Color.web("#fdba74") : Color.TRANSPARENT);
        halo.setStrokeWidth(4);
        Circle circle = new Circle(p.getX(), p.getY(), 17, fill);
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2.2);

        Text code = new Text(codeValue);
        code.setFill(Color.WHITE);
        code.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        code.setX(p.getX() - code.getLayoutBounds().getWidth() / 2);
        code.setY(p.getY() + 4);
        Text city = new Text(airport.getCity());
        city.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        city.setFill(Color.web("#334155"));
        city.setX(p.getX() - city.getLayoutBounds().getWidth() / 2);
        city.setY(p.getY() + 35);

        Group node = new Group(halo, circle, code, city);
        node.setCursor(javafx.scene.Cursor.HAND);
        Tooltip.install(node, new Tooltip(airport.toString()
                + "\nOutgoing flights: " + network.getOutgoingFlights(codeValue).size()
                + "\nLeft-click: details | Right-click: choose route endpoint"));
        node.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && listener != null) {
                listener.showAirport(airport);
                e.consume();
            }
        });

        MenuItem asDeparture = new MenuItem("Set as Departure");
        MenuItem asDestination = new MenuItem("Set as Destination");
        asDeparture.setOnAction(e -> { if (listener != null) listener.setDeparture(airport); });
        asDestination.setOnAction(e -> { if (listener != null) listener.setDestination(airport); });
        ContextMenu menu = new ContextMenu(asDeparture, asDestination);
        node.setOnContextMenuRequested(e -> menu.show(node, e.getScreenX(), e.getScreenY()));
        canvas.getChildren().add(node);
    }

    private void drawArrow(Point2D from, Point2D to, FlightEdge edge,
            Color color, double width, double opacity, boolean emphasized) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double length = Math.hypot(dx, dy);
        if (length == 0) return;
        double ux = dx / length, uy = dy / length, px = -uy, py = ux;
        double x1 = from.getX() + ux * 20, y1 = from.getY() + uy * 20;
        double x2 = to.getX() - ux * 21, y2 = to.getY() - uy * 21;
        boolean reverse = network.flightExists(edge.getDestinationCode(), edge.getSourceCode());
        double bend = reverse
                ? (edge.getSourceCode().compareTo(edge.getDestinationCode()) < 0 ? 14 : -14) : 0;
        if (edge.getSourceCode().equals("LGK") && edge.getDestinationCode().equals("KUL")) bend = -20;
        double cx = (x1 + x2) / 2 + px * bend;
        double cy = (y1 + y2) / 2 + py * bend;

        QuadCurve curve = new QuadCurve(x1, y1, cx, cy, x2, y2);
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(color);
        curve.setStrokeWidth(width);
        curve.setOpacity(opacity);

        double tangentX = x2 - cx, tangentY = y2 - cy;
        double tangentLength = Math.hypot(tangentX, tangentY);
        double tx = tangentX / tangentLength, ty = tangentY / tangentLength;
        double apx = -ty, apy = tx;
        double arrowLength = emphasized ? 12 : 10;
        double half = emphasized ? 5 : 4;
        Polygon arrow = new Polygon(x2, y2,
                x2 - tx * arrowLength + apx * half, y2 - ty * arrowLength + apy * half,
                x2 - tx * arrowLength - apx * half, y2 - ty * arrowLength - apy * half);
        arrow.setFill(color);
        arrow.setOpacity(opacity);

        String info = edge.getSourceCode() + " -> " + edge.getDestinationCode()
                + "\nDistance: " + edge.getDistanceKm() + " km"
                + String.format("%nFare: RM %.2f", edge.getFare())
                + "\nDuration: " + edge.getDurationMinutes() + " minutes";
        Tooltip.install(curve, new Tooltip(info));
        Tooltip.install(arrow, new Tooltip(info));
        curve.setCursor(javafx.scene.Cursor.HAND);
        arrow.setCursor(javafx.scene.Cursor.HAND);
        curve.setOnMouseClicked(e -> selectEdge(edge));
        arrow.setOnMouseClicked(e -> selectEdge(edge));
        canvas.getChildren().addAll(curve, arrow);
        if (emphasized) drawWeightLabel(edge, color, x1, y1, cx, cy, x2, y2, px, py, bend);
    }

    private void selectEdge(FlightEdge edge) {
        selectedEdge = edge;
        if (listener != null) listener.showFlight(edge);
        draw();
    }

    private void drawWeightLabel(FlightEdge edge, Color color,
            double x1, double y1, double cx, double cy,
            double x2, double y2, double px, double py, double bend) {
        double midX = .25 * x1 + .5 * cx + .25 * x2;
        double midY = .25 * y1 + .5 * cy + .25 * y2;
        double offset = bend == 0 ? 16 : Math.signum(bend) * 18;
        midX += px * offset;
        midY += py * offset;
        Label label = new Label(edge.getSourceCode() + " → " + edge.getDestinationCode()
                + "\n" + edge.getDistanceKm() + " km"
                + String.format(" | RM %.2f", edge.getFare())
                + " | " + edge.getDurationMinutes() + " min");
        label.setLayoutX(midX - 65);
        label.setLayoutY(midY - 18);
        label.setMouseTransparent(true);
        label.setStyle("-fx-background-color: rgba(255,255,255,0.97);"
                + "-fx-border-color: " + toHex(color) + "; -fx-border-width: 1.2;"
                + "-fx-border-radius: 5; -fx-background-radius: 5;"
                + "-fx-padding: 4 6; -fx-font-size: 10px; -fx-font-weight: bold;");
        canvas.getChildren().add(label);
    }

    private boolean hasRouteResult() { return primary != null || secondary != null; }

    private boolean containsEdge(RouteResult result, FlightEdge edge) {
        if (result == null || !result.isRouteFound()) return false;
        ArrayList<String> path = result.getPath();
        for (int i = 0; i < path.size() - 1; i++) {
            if (path.get(i).equals(edge.getSourceCode())
                    && path.get(i + 1).equals(edge.getDestinationCode())) return true;
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

    private boolean sameEdge(FlightEdge a, FlightEdge b) {
        return a != null && b != null
                && a.getSourceCode().equals(b.getSourceCode())
                && a.getDestinationCode().equals(b.getDestinationCode());
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255));
    }
}
