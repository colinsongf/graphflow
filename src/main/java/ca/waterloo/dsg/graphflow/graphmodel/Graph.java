package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;

/**
 * Encapsulates the Graph representation and provides utility methods.
 */
public class Graph {

    public enum GraphVersion {
        OLD,
        DIFF,
        LATEST
    }

    public enum EdgeDirection {
        FORWARD,
        REVERSE
    }

    private static final int DEFAULT_GRAPH_SIZE = 10;
    public static Graph graph;

    private SortedIntArrayList vertices;
    private SortedIntArrayList[] outgoingAdjLists;
    private SortedIntArrayList[] incomingAdjLists;

    public Graph() {
        outgoingAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
        incomingAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
    }

    public Graph(int vertexLength) {
        // TODO(chathura): Store vertices in separate array and ensure none of the adj lists are
        // empty.
        outgoingAdjLists = new SortedIntArrayList[vertexLength];
        incomingAdjLists = new SortedIntArrayList[vertexLength];
        // Initialize the adjacency lists for each vertex with empty lists.
        for (int i = 0; i < vertexLength; i++) {
            outgoingAdjLists[i] = new SortedIntArrayList();
            incomingAdjLists[i] = new SortedIntArrayList();
        }
    }

    /**
     * Sets the outgoing or incoming adjacency list of the vertex with given ID
     * to the given adjacency list.
     *
     * @param vertexIndex
     * @param adjList
     * @param isForward
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setAdjacencyList(int vertexIndex, SortedIntArrayList adjList, boolean isForward)
        throws ArrayIndexOutOfBoundsException {
        if (isForward) {
            outgoingAdjLists[vertexIndex] = adjList;
        } else {
            incomingAdjLists[vertexIndex] = adjList;
        }
    }

    /**
     * Returns an array of outgoing or incoming adjacency lists for the given vertex.
     *
     * @param vertexIndex
     * @param edgeDirection
     * @return SortedIntArrayList
     * @throws ArrayIndexOutOfBoundsException
     */
    public SortedIntArrayList getAdjacencyList(int vertexIndex, EdgeDirection edgeDirection)
        throws ArrayIndexOutOfBoundsException {
        SortedIntArrayList result = null;
        if (edgeDirection == EdgeDirection.FORWARD) {
            if (outgoingAdjLists[vertexIndex] == null) {
                outgoingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = outgoingAdjLists[vertexIndex];
        } else {
            if (incomingAdjLists[vertexIndex] == null) {
                incomingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = incomingAdjLists[vertexIndex];
        }
        return result;
    }

    /**
     * Returns the size of the adjacency list for the given vertex in the given direction.
     *
     * @param vertexIndex
     * @param edgeDirection
     * @return
     */
    public int getAdjacencyListSize(int vertexIndex, EdgeDirection edgeDirection) {
        int result;
        if (edgeDirection == EdgeDirection.FORWARD) {
            if (outgoingAdjLists[vertexIndex] == null) {
                outgoingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = outgoingAdjLists[vertexIndex].getSize();
        } else {
            if (incomingAdjLists[vertexIndex] == null) {
                incomingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = incomingAdjLists[vertexIndex].getSize();
        }
        return result;
    }

    /**
     * Returns the number of vertices in the graph.
     *
     * @return int
     */
    public int getVertexCount() {
        return outgoingAdjLists.length;
    }

    /**
     * Returns all the vertex indices in the graph as a list.
     *
     * @return SortedIntArrayList
     */
    public SortedIntArrayList getVertices() {
        if (this.vertices == null) {
            vertices = new SortedIntArrayList(getVertexCount());
            for (int i = 0; i < this.getVertexCount(); i++) {
                vertices.add(i);
            }
        }
        return vertices;
    }

    /**
     * Convert the graph to a string.
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder graph = new StringBuilder();
        graph.append(this.convertAdjListsToString(true));
        graph.append(this.convertAdjListsToString(false));
        return graph.toString();
    }

    /**
     * Converts the set of adjacency lists in the given direction to a {@code string}
     *
     * @param isForward
     * @return
     */
    private String convertAdjListsToString(boolean isForward) {
        SortedIntArrayList[] adjLists = isForward ? this.outgoingAdjLists : this.incomingAdjLists;
        StringBuilder adjString = new StringBuilder();
        int index = 0;
        for (SortedIntArrayList adjList : adjLists) {
            adjString.append(index + " :");
            adjString.append(adjList.toString());
            adjString.append("\n");
            index++;
        }
        return adjString.toString();
    }
}
