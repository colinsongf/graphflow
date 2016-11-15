package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.graphmodel.Graph.EdgeDirection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

import static ca.waterloo.dsg.graphflow.graphmodel.Graph.graph;

public class GraphBuilder {

    /**
     * Creates a graph object from the given file.
     *
     * @param file JSON file with pattern {"num-vertices": x, "edges" : [("src": 1, "dst": 2),
     * ("src": 2, "dst": 3)...]}. Vertex indices are assumed to start from 0.
     * @return Graph
     * @throws IOException
     */
    public static Graph createInstance(File file) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        Gson gson = gsonBuilder.create();
        graph = gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
        return graph;
    }

    /**
     * Used to deserialize graph from a json file with a vertex count and list of edge objects.
     */
    private static class GraphDeserializer implements JsonDeserializer<Graph> {

        private static final String EDGES = "edges";
        private static final String NUM_VERTICES = "num-vertices";
        private static final String SOURCE = "src";
        private static final String DESTINATION = "dst";

        /**
         * Gets the root of a json object and returns a graph object populated with data.
         *
         * @param json handle to the root element of the json file.
         * @param typeOfT is used when deserializing to a specific Type.
         * @param context
         * @return Graph
         * @throws JsonParseException
         */
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            JsonObject rawGraph = json.getAsJsonObject();
            JsonArray edges = rawGraph.get(GraphDeserializer.EDGES).getAsJsonArray();
            int numVertices = rawGraph.get(GraphDeserializer.NUM_VERTICES).getAsInt();
            Graph graph = new Graph(numVertices);

            for (JsonElement edge : edges) {
                JsonObject edgeObj = edge.getAsJsonObject();
                int src = edgeObj.get(GraphDeserializer.SOURCE).getAsInt();
                int dst = edgeObj.get(GraphDeserializer.DESTINATION).getAsInt();
                graph.getAdjacencyList(src, EdgeDirection.FORWARD).add(dst);
                graph.getAdjacencyList(dst, EdgeDirection.REVERSE).add(src);
            }
            return graph;
        }
        //TODO: serialize function to write human readable graph
    }
}
