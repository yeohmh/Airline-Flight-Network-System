AIRLINE FLIGHT NETWORK SYSTEM - FULL JAVAFX INTERFACE

Tested with:
- Apache NetBeans IDE 18
- JDK 20
- JavaFX 21.0.8 libraries supplied by Practical 9

This project includes:
- Full JavaFX interface launched directly from airlineflightnetwork.Main
- Interactive directed graph based on the original graph visual design
- Right-click airport selection for departure or destination
- Clickable directed edges with distance, fare and duration details
- Airport and edge tooltips, route overlays, zoom, pan and reset controls
- JavaFX airport/flight management tables, forms, alerts and confirmations
- BFS minimum-flight-leg search
- Dijkstra shortest-distance, cheapest-fare and fastest-duration searches
- BFS/Dijkstra comparison
- Add/remove airport and directed-flight operations
- 15-airport, 70-flight initial dataset
- JavaFX algorithm analysis with best/worst time and space complexity
- Deterministic observed best/worst cases across all valid airport pairs
- BFS and Dijkstra operation counters for presentation evidence
- Clear validation feedback through JavaFX alerts and confirmation dialogs
- The earlier console methods and original GraphWindow are retained as fallback/reference
- Expanded IntegrationTest retained for verification

Open this folder as a project in NetBeans. The JavaFX library paths, native
Windows files, main class and VM arguments are already configured.

The main class is airlineflightnetwork.Main and launches the complete JavaFX system.
