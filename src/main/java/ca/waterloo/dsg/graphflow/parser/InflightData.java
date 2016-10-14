package ca.waterloo.dsg.graphflow.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates edge data during the parse tree traversal.
 */
public class InflightData {

    public enum Operation {
        CREATE,
        MATCH,
        DELETE
    }

    private List<String[]> vertices;
    private Operation operation;

    private List<InflightData> allOperations;

    public InflightData() {
        this.vertices = new ArrayList<>();
        allOperations = new ArrayList<>();
    }

    public List<String[]> getVertices() {
        return vertices;
    }

    public void addVertex(String[] vertex) {
        this.vertices.add(vertex);
    }

    public void setVertex(int pos, String[] vertex) {
        this.vertices.add(pos, vertex);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<InflightData> getAllOperations() {
        return allOperations;
    }

    public void addToAllOperations(InflightData operation) {
        this.allOperations.add(operation);
    }

}
