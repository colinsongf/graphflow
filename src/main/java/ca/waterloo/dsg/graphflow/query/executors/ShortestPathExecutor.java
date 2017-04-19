package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.exceptions.NoSuchVertexIDException;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.SortedAdjacencyList;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.util.IntQueue;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Finds the s-t shortest path between a given source s and destination t using bi-directional BFS.
 * In bi-directional BFS, instead of doing a BFS from s until the search finds t, we take one full
 * step (evaluate step to completion) from s and another full step from t until the two searches
 * intersect.
 **/
public class ShortestPathExecutor {

    private static final ShortestPathExecutor INSTANCE = new ShortestPathExecutor();

    private static final Logger logger = LogManager.getLogger(ShortestPathExecutor.class);
    private static final int INITIAL_QUEUE_SIZE = 25000;

    // The forward and backward queues store the unvisited neighbours of visited vertices for
    // BFS in the forward and backward directions.
    private IntQueue forwardQueue;
    private IntQueue backwardQueue;
    // Stores the {@link #queryId} when a vertex is visited during BFS from either direction.
    // By storing {@link #queryId} we signify that the vertex was visited during the query
    // identified by {@link #queryId}.
    private int[] visitedVerticesByQueryId;
    // Stores the BFS direction from which each vertex u was visited as boolean values. If a vertex
    // u has been visited in the FORWARD direction we store true for u. If u was visited in the
    // BACKWARD direction we store false for u.
    private boolean[] visitedDirections;
    // Stores the expansion level at which a vertex was evaluated during BFS. Levels in the
    // forward direction are denoted by 1, 2, 3... and levels in the backward direction by -1,
    // -2, -3...
    private short[] visitedLevels;
    // We give each query a local query ID. We keep a global latest query ID and increment this
    // value for each new query. We use this query ID in {@link #visitedVerticesByQueryId} to
    // identify visited vertices during query evaluation. This avoids having to
    // reallocate the {@link visitedVerticesByQueryId}, {@link visitedLevels}, and
    // {@link visitedDirections} arrays that are used during the bi-directional BFS search.
    private int queryId;

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private ShortestPathExecutor() {
        forwardQueue = new IntQueue(INITIAL_QUEUE_SIZE);
        backwardQueue = new IntQueue(INITIAL_QUEUE_SIZE);
        initArrays();
    }

    /**
     * Used to set executor state for testing purposes.
     */
    @UsedOnlyByTests
    ShortestPathExecutor(short[] visitedLevels, int[] visitedVerticesByQueryId, int queryId) {
        this.visitedLevels = visitedLevels;
        this.visitedVerticesByQueryId = visitedVerticesByQueryId;
        this.queryId = queryId;
    }

    private void initArrays() {
        // Initialize {@code visitedVerticesByQueryId} on the first query and on overflows.
        Graph graph = Graph.getInstance();
        int extraArraySize = (int) Double.min(graph.getVertexCount() * 0.01, 1000);
        visitedVerticesByQueryId = new int[graph.getVertexCount() + extraArraySize];
        visitedDirections = new boolean[graph.getVertexCount() + extraArraySize];
        visitedLevels = new short[graph.getVertexCount() + extraArraySize];
    }

    /**
     * Initializes the {@link ShortestPathExecutor} for a new query by resetting the data structures
     * used.
     */
    private void initQuery() {
        queryId++;
        if (Integer.MIN_VALUE == queryId) {
            // An overflow has occurred in {@code queryId}. We reset it to 1 and reinitialize
            // visitedVerticesByQueryId, visitedDirections and visitedLevels arrays to avoid
            // conflicts with data stored when queryId had value 1 previously.
            queryId = 1;
            logger.info("Overflow in ShortestPathExecutor#queryId.");
            initArrays();
        } else if (Graph.getInstance().getVertexCount() > visitedVerticesByQueryId.length) {
            initArrays();
        }
        forwardQueue.reset();
        backwardQueue.reset();
    }

    /**
     * Calculates the shortest paths for the given pair of vertices using bi-directional BFS.
     * Vertices are traversed starting from s in the forward direction and t in the backward
     * direction. Each side uses a queue to store its frontier. In each iteration, the algorithm
     * traverses in the direction that has the smaller size queue. Once the two traversals
     * intersect, we backtrack from the intersecting vertices to s and t to identify all of the
     * edges that are on at least one shortest path. The subgraph formed by the identified edges
     * are output to the given {@code outputSink}. If no paths are found, an empty result set is
     * output to the {@code outputSink}.
     *
     * @param source The source vertex of the shortest path query.
     * @param target The target vertex for the shortest path query.
     * @throws NoSuchVertexIDException Throws exception if the specified {@code source} and
     * {@code target} vertex IDs don't exist.
     */
    public void execute(int source, int target, AbstractDBOperator outputSink)
        throws NoSuchVertexIDException {
        assertVertexIDExists(source);
        assertVertexIDExists(target);
        initQuery();
        Set<Integer> intersectionSet = new HashSet<>();
        boolean foundIntersections = false;
        short forwardLevelNumber = 1;
        short backwardLevelNumber = -1;
        forwardQueue.enqueue(source);
        visitedVerticesByQueryId[source] = queryId;
        visitedDirections[source] = Direction.FORWARD.getBooleanValue();
        visitedLevels[source] = 1;
        backwardQueue.enqueue(target);
        visitedVerticesByQueryId[target] = queryId;
        visitedDirections[target] = Direction.BACKWARD.getBooleanValue();
        visitedLevels[target] = -1;
        // Pre declare the variables used in the loop.
        Direction currentDirection;
        short currentLevelNumber;
        // Holds the first vertex added to the current queue for the next level. We find out if
        // we have finished visiting all of the vertices in this level by checking if the next
        // queue item is the {@code stopVertex}.
        int stopVertex;
        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty() && !foundIntersections) {
            IntQueue minQueue = (forwardQueue.getSize() <= backwardQueue.getSize()) ? forwardQueue :
                backwardQueue;
            currentDirection = (forwardQueue.getSize() <= backwardQueue.getSize()) ?
                Direction.FORWARD : Direction.BACKWARD;
            currentLevelNumber = (forwardQueue.getSize() <= backwardQueue.getSize()) ?
                ++forwardLevelNumber : --backwardLevelNumber;
            stopVertex = -1;
            while (!minQueue.isEmpty() && minQueue.peekNext() != stopVertex) {
                int currentVertex = minQueue.dequeue();
                SortedAdjacencyList neighbours = Graph.getInstance().getSortedAdjacencyList(
                    currentVertex, currentDirection, GraphVersion.PERMANENT);
                if (null == neighbours || neighbours.getSize() == 0) {
                    continue;
                }
                for (int i = 0; i < neighbours.getSize(); i++) {
                    int neighbourVertex = neighbours.getNeighbourId(i);
                    // Check if this vertex has been visited during this query. If the value equals
                    // the current {@link #queryId} it has been visited.
                    if (visitedVerticesByQueryId[neighbourVertex] == queryId) {
                        if (visitedDirections[neighbourVertex] == currentDirection.
                            getBooleanValue()) {
                            // This vertex has been visited before while traversing in the current
                            // direction.
                            continue;
                        } else {
                            // This vertex has been visited before from the opposite direction, so
                            // we just found an intersection.
                            foundIntersections = true;
                            intersectionSet.add(neighbourVertex);
                        }
                    } else {
                        // This vertex has not been visited before, so we mark it as visited and add
                        // its neighbours to the queue for later evaluation.
                        visitedVerticesByQueryId[neighbourVertex] = queryId;
                        visitedDirections[neighbourVertex] = currentDirection.getBooleanValue();
                        visitedLevels[neighbourVertex] = currentLevelNumber;
                        if (stopVertex == -1) {
                            stopVertex = neighbourVertex;
                        }
                        minQueue.enqueue(neighbourVertex);
                    }
                }
            }
        }
        Map<Integer, Set<Integer>> results = new HashMap<>();
        if (!intersectionSet.isEmpty()) {
            // Backtrack with the {@code intersectionSet} found in the last iteration. Also pass in
            // the direction of the last expansion as this was not recorded in the
            // {@link #visitedStages} array.
            backTrackIntersection(intersectionSet, Direction.BACKWARD, forwardLevelNumber, results);
            backTrackIntersection(intersectionSet, Direction.FORWARD, backwardLevelNumber, results);
        }
        // Set the results from the backtracking or an empty result set to the
        // {@link ShortestPathOutputSink}.
        outputSink.append(getStringOutput(results));
    }

    /**
     * Backtracks in the given direction from the given set of intersection vertices and populates
     * {@code results} with all edges belonging to at least one shortest path. An edge (u, v)
     * belongs to at least one shortest path if one of two conditions are met depending on whether
     * we are backtracking in the forward or backward directions. (1) If we are backtracking in the
     * backward direction, then (u, v) is in at least one shortest path if v is in the current
     * intersectionSet, has a level k, and u has a level k-1. (2) If we are backtracking in the
     * forward direction, then (u, v) is in at least one shortest path if u is in the current
     * intersection set, and v has a level k + 1. Backtracking iteratively evaluates the adjacency
     * lists of the current intersection set to identify such (u, v) edges adds u or v (depending on
     * the direction of the backtracking) to the next intersection set, until we reach the source
     * or the target vertex (again depending on the direction of the backtracking).
     *
     * @param intersectionSet The set of vertices where the bi-directional breadth first search
     * intersected.
     * @param directionToBacktrack The direction in which backtracking should happen, backward
     * towards the source or forward towards the destination.
     * @param startingLevel The level at which the BFS in the given direction found intersections.
     * @param results The data structure for storing the set of edges in at least one (source,
     * target) shortest path.
     */
    @VisibleForTesting
    void backTrackIntersection(Set<Integer> intersectionSet, Direction directionToBacktrack,
        int startingLevel, Map<Integer, Set<Integer>> results) {
        IntQueue nextLevelVertices = new IntQueue(intersectionSet.size());
        for (int intersectionVertex : intersectionSet) {
            nextLevelVertices.enqueue(intersectionVertex);
        }
        IntQueue currentLevelVertices = new IntQueue();
        int currentLevel = startingLevel;
        int precedingLevel = currentLevel > 0 ? currentLevel - 1 : currentLevel + 1;
        while (!nextLevelVertices.isEmpty()) {
            // At this point in code currentLevelVertices is an empty queue.
            IntQueue temp = currentLevelVertices;
            currentLevelVertices = nextLevelVertices;
            nextLevelVertices = temp; // Assign empty queue to nextLevelVertices.
            while (!currentLevelVertices.isEmpty()) {
                int currentVertex = currentLevelVertices.dequeue();
                SortedAdjacencyList adjList = Graph.getInstance().getSortedAdjacencyList(
                    currentVertex, directionToBacktrack, GraphVersion.PERMANENT);
                if (null == adjList || adjList.getSize() == 0) {
                    continue;
                }
                for (int i = 0; i < adjList.getSize(); i++) {
                    int neighbourVertex = adjList.getNeighbourId(i);
                    // If a vertex from the adjacency list was reached in the preceding stage, that
                    // edge forms part of a shortest path.
                    if (precedingLevel != 0 && (visitedVerticesByQueryId[neighbourVertex] ==
                        queryId) && visitedLevels[neighbourVertex] == precedingLevel) {
                        nextLevelVertices.enqueue(adjList.getNeighbourId(i));
                        // Add edge (u, v).
                        int u = (Direction.FORWARD == directionToBacktrack) ? currentVertex :
                            neighbourVertex;
                        int v = (Direction.FORWARD == directionToBacktrack) ? neighbourVertex :
                            currentVertex;
                        if (!results.containsKey(u)) {
                            results.put(u, new HashSet<>());
                        }
                        results.get(u).add(v);
                    }
                }
            }
            currentLevel = precedingLevel;
            precedingLevel = (Direction.FORWARD == directionToBacktrack) ? currentLevel + 1 :
                currentLevel - 1;
        }
    }

    /**
     * Checks whether the given vertexID is stored in the graph and throws an error if it is not
     * present.
     *
     * @param vertexId The vertexID to be checked.
     * @throws NoSuchVertexIDException
     */
    private void assertVertexIDExists(int vertexId) throws NoSuchVertexIDException {
        if (vertexId >= Graph.getInstance().getVertexCount()) {
            throw new NoSuchVertexIDException("The specified vertexID " + vertexId + " does not " +
                "exist.");
        }
    }

    @VisibleForTesting
    static String getStringOutput(Map<Integer, Set<Integer>> results) {
        StringJoiner stringJoiner = new StringJoiner(", ");
        for (Map.Entry<Integer, Set<Integer>> entry : results.entrySet()) {
            stringJoiner.add(entry.getKey() + ": " + Arrays.toString(entry.getValue().toArray()));
        }
        return stringJoiner.toString();
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link ShortestPathExecutor}.
     */
    public static ShortestPathExecutor getInstance() {
        return INSTANCE;
    }
}
