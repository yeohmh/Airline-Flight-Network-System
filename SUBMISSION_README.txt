AMCS2034 AIRLINE FLIGHT NETWORK SYSTEM - CONSOLE FINAL

Environment
- Apache NetBeans IDE 18
- JDK 20
- Main class: airlineflightnetwork.Main

Graph implementation
- Directed weighted graph
- Adjacency edge list
- 15 airport vertices
- 70 directed flight edges
- Edge weights: distance, fare and duration

Required operations
- Add/remove airport vertex
- Add/remove directed flight edge
- Display airport vertices and adjacency-list graph
- BFS minimum-flight-leg traversal and route reconstruction
- Dijkstra distance/fare/duration route and reconstruction
- BFS/Dijkstra comparison
- Best/worst time and auxiliary-space complexity display

Verification
- JDK 20 compilation passed
- 45 integration tests passed
- Observed best/worst case analysis excludes same-airport route requests
- Current 15-airport dataset evaluates 210 valid ordered airport pairs

Before submission
1. Open in NetBeans IDE 18.
2. Run Clean and Build.
3. Run the project and verify the initial counts remain 15 and 70.
4. ZIP the complete NetBeans project.
