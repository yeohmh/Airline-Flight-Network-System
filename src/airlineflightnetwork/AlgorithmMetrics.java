package airlineflightnetwork;

/** Operation counters used for deterministic algorithm case demonstrations. */
public class AlgorithmMetrics {
    private final String algorithm;
    private int verticesProcessed;
    private int edgesExamined;
    private int queueInsertions;
    private int queueRemovals;
    private int successfulRelaxations;
    private int maximumQueueSize;

    public AlgorithmMetrics(String algorithm) {
        this.algorithm = algorithm == null ? "N/A" : algorithm;
    }

    public AlgorithmMetrics(AlgorithmMetrics other) {
        this.algorithm = other.algorithm;
        this.verticesProcessed = other.verticesProcessed;
        this.edgesExamined = other.edgesExamined;
        this.queueInsertions = other.queueInsertions;
        this.queueRemovals = other.queueRemovals;
        this.successfulRelaxations = other.successfulRelaxations;
        this.maximumQueueSize = other.maximumQueueSize;
    }

    void vertexProcessed() { verticesProcessed++; }
    void edgeExamined() { edgesExamined++; }
    void queueInserted() { queueInsertions++; }
    void queueRemoved() { queueRemovals++; }
    void relaxationSucceeded() { successfulRelaxations++; }
    void observeQueueSize(int size) { maximumQueueSize = Math.max(maximumQueueSize, size); }

    public String getAlgorithm() { return algorithm; }
    public int getVerticesProcessed() { return verticesProcessed; }
    public int getEdgesExamined() { return edgesExamined; }
    public int getQueueInsertions() { return queueInsertions; }
    public int getQueueRemovals() { return queueRemovals; }
    public int getSuccessfulRelaxations() { return successfulRelaxations; }
    public int getMaximumQueueSize() { return maximumQueueSize; }

    public int getObservedWorkUnits() {
        if (algorithm.equalsIgnoreCase("DIJKSTRA")) {
            return edgesExamined + queueRemovals + successfulRelaxations;
        }
        return verticesProcessed + edgesExamined;
    }
}
