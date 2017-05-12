package ca.waterloo.dsg.graphflow.udfexamples;

import ca.waterloo.dsg.graphflow.query.operator.udf.UDFAction;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Edge;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Subgraph;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Vertex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * "The {@link UDFAction} that can be used for writing continuously matched (a)->(b)->(c) patterns
 * to a file. Assuming the user creates a jar write-to-file.jar that contains this class, this
 * class can be used in a continuous match query as follows:
 * <p>
 * CONTINUOUSLY MATCH (a)->(b), (b)->(c)
 * ACTION UDF ca.waterloo.dsg.graphflow.udfexamples.WriteToFile in UDFExamples.jar
 */
public class WriteToFileUDFAction extends UDFAction {

    private String absoluteFilePath = "/tmp/Graphflow-FileSinkUDF-Graphflow.txt";

    // The subgraph matched is: (a)->(b), (b)->(c);
    @Override
    public void evaluate(List<Subgraph> subgraphs) {
        BufferedWriter bufferedWriter;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(absoluteFilePath, true));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String[] vertexVariables = {"a", "b", "c"};
        String[][] edges = {{"a", "b"}, {"b", "c"}};

        try {
            for (Subgraph subgraph : subgraphs) {
                for (String vertexVariable : vertexVariables) {
                    Vertex vertex = subgraph.getVertex(vertexVariable);
                    bufferedWriter.write("vertex " + vertexVariable + " : " + vertex.getId() + "-" +
                        vertex.getType() + "\n");
                    for (String property : vertex.getProperties().keySet()) {
                        bufferedWriter.write(property + " : " + vertex.getProperties().get(
                            property) + "\n");
                    }
                }

                for (String[] edgeVariables : edges) {
                    Edge edge = subgraph.getEdge(edgeVariables[0], edgeVariables[1]);
                    bufferedWriter.write("Edge properties (" + edge.getFromVertexId() + ")->(" +
                        edge.getToVertexId() + "):\n");
                    for (String property : edge.getProperties().keySet()) {
                        bufferedWriter.write(property + " : " + edge.getProperties().get(property) +
                            "\n");
                    }
                }
            }
            bufferedWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setFileToWriteTo(String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
    }
}
