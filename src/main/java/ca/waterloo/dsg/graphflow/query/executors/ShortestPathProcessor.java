package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.outputsink.ShortestPathOutputSink;
import ca.waterloo.dsg.graphflow.util.IntQueue;
import ca.waterloo.dsg.graphflow.util.SortedArrayList;
import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Processes the s-t shortest path between the given vertices using two way breadth-first-search
 * expansion.
 **/
public class ShortestPathProcessor {

    private static boolean DIRECTION_FORWARD = true;
    private static boolean DIRECTION_BACKWARD = false;
    private static final int INITIAL_QUEUE_SIZE = 25000;
    private Graph graph;
    private ShortestPathOutputSink outputSink;
    IntQueue forwardQueue = new IntQueue(INITIAL_QUEUE_SIZE);
    IntQueue backwardQueue = new IntQueue(INITIAL_QUEUE_SIZE);
    Map<Integer, Set<Integer>> results;
    int[] visitedVerticesByQueryId;



    boolean[] visitedDirections;
    short[] visitedStages;
    int queryId;

    protected ShortestPathProcessor(Graph graph, ShortestPathOutputSink outputSink) {
        this.graph = graph;
        this.outputSink = outputSink;
        visitedVerticesByQueryId = new int[graph.getVertexCount()];
        visitedDirections = new boolean[graph.getVertexCount()];
        visitedStages = new short[graph.getVertexCount()];
        queryId = 0;
    }

    /**
     * Calculates the shortest paths for the given pair of nodes using two sided BFS. Each side
     * uses a queue to store the frontier. Once the two expansions intersect, backtracking is
     * used to identify the paths and merge them to get the set of s-t paths. Paths are output to
     * the given output sink.
     *
     * @param start The start node for the shortest path calculation
     * @param target The target node for the shortest path calculation
     */
    public void evaluate(int start, int target) {
        results = new HashMap<>();
        boolean foundIntersections = false;
        short forwardStageNumber = 0;
        short backwardStageNumber = 0;
        queryId++;
        forwardQueue.put(start);
        backwardQueue.put(target);
        while (!forwardQueue.isEmpty() && !backwardQueue.isEmpty() && !foundIntersections) {
            IntQueue minQueue = (forwardQueue.size() <= backwardQueue.size())? forwardQueue:
                backwardQueue;
            boolean direction = forwardQueue.size() <= backwardQueue.size()? DIRECTION_FORWARD:
                DIRECTION_BACKWARD;
            short currentStageNumber = forwardQueue.size() <= backwardQueue.size()?
                ++forwardStageNumber: --backwardStageNumber;
            int stopVertex = -1;
            do {
                int currentNode = minQueue.get();
                // Check if this vertex has been visited during this query. If the value is less
                // than current {@code queryId} it has not been visited.
                if(!(visitedVerticesByQueryId[currentNode] < queryId)) {
                    if(visitedDirections[currentNode] == direction) {
                        // This node has been visited before in expanding from this side.
                        continue;
                    } else {
                        // It has been visited from the other expansion, so we just found an
                        // intersection.
                        foundIntersections = true;
                        backTrackIntersection(currentNode, currentStageNumber);
                        continue;
                    }
                }
                visitedVerticesByQueryId[currentNode] = queryId;
                visitedDirections[currentNode] = direction;
                visitedStages[currentNode] = currentStageNumber;
                SortedIntArrayList neighbours = graph.getAdjacencyList(currentNode, direction);
                for(int i = 0; i < neighbours.size(); i++) {
                    if (stopVertex < 0) {
                        stopVertex = neighbours.get(i);
                    }
                    minQueue.put(neighbours.get(i));
                }
            } while(!minQueue.isEmpty() && minQueue.peekNext() != stopVertex);
        }
        outputSink.setResults(results);
    }

    @VisibleForTesting
    public void backTrackIntersection(int currentNode, short currentStageNumber) {
        IntQueue backTrackQueue = new IntQueue(15);
        backTrackQueue.put(currentNode); // Put the current node to the queue such that
        // backtracking will happen in the opposite direction as well.
        int backTrackNode = currentNode;
        int backTrackStage = currentStageNumber;
        int precedingStage = backTrackStage > 0? backTrackStage -1 : backTrackStage +1;
        do {
            SortedIntArrayList adjList = graph.getAdjacencyList(backTrackNode, !(backTrackStage >
                0));
            for (int i=0; i < adjList.size();i++) {
                // If a node from the adjacency list was reached in the preceding stage, that
                // edge forms part of a shortest path.
                if(precedingStage != 0 && visitedStages[adjList.get(i)] == precedingStage) {
                    backTrackQueue.put(adjList.get(i));
                    int src = backTrackStage > 0? adjList.get(i): backTrackNode;
                    int dst = backTrackStage > 0? backTrackNode: adjList.get(i);
                    if(!results.containsKey(src)) {
                        results.put(src, new HashSet<>());
                    }
                    results.get(src).add(dst);
                }
            }
            backTrackNode = backTrackQueue.get();
            backTrackStage = visitedStages[backTrackNode];
            precedingStage = backTrackStage > 0? backTrackStage -1 : backTrackStage +1;
        } while (!backTrackQueue.isEmpty());
    }

    public void setVisitedStages(short[] visitedStages) {
        this.visitedStages = visitedStages;
    }

    @VisibleForTesting
    public void setResults(Map<Integer, Set<Integer>> results) {
        this.results = results;
    }
}
