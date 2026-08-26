AIRLINE FLIGHT NETWORK SYSTEM - CONSOLE WITH JAVAFX GRAPH

Tested with:
- Apache NetBeans IDE 18
- JDK 20
- JavaFX 21.0.8 libraries supplied by Practical 9

This project includes:
- Console menu following the assignment sample-output style
- Airport code directory shown before operations that require airport codes
- Optional JavaFX directed graph with arrows and highlighted traversal result
- Airport and edge tooltips with route-weight details
- Zoom, pan and reset controls for the generated graph
- BFS minimum-flight-leg search
- Dijkstra shortest-distance, cheapest-fare and fastest-duration searches
- BFS/Dijkstra comparison
- Add/remove airport and directed-flight operations
- 15-airport, 70-flight initial dataset
- Console algorithm analysis with best/worst time and space complexity
- Console Main and IntegrationTest retained for verification

Open this folder as a project in NetBeans. The JavaFX library paths, native
Windows files, main class and VM arguments are already configured.

The main class is airlineflightnetwork.Main. JavaFX opens only when the user
selects the graph option or requests graph visualisation after a route search.
