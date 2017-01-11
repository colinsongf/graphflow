package ca.waterloo.dsg.graphflow.graph;

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
import java.util.HashMap;
import java.util.Map;

public class GraphBuilder {

    /**
     * Creates a graph object from the given JSON file.
     *
     * @param file JSON file with pattern {"num-vertices": x, "edges" : [("src": 1, "dst": 2),
     * ("src": 2, "dst": 3)...]}. Vertex indices are assumed to start from 0.
     * @return The {@code Graph} object created from the JSON file.
     * @throws IOException on IO errors during file operations on {@code file}.
     */
    public static Graph createInstance(File file) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
    }

    /**
     * Used to deserialize graph from a json file with a vertex count and list of edge objects.
     */
    private static class GraphDeserializer implements JsonDeserializer<Graph> {

        private static final String EDGES = "edges";
        private static final String NUM_VERTICES = "num-vertices";
        private static final String SOURCE = "src";
        private static final String DESTINATION = "dst";
        private static final String ID = "id";
        private static final String TYPE = "type";
        private static final String PROPERTIES = "properties";

        /**
         * Gets the root of a json object and returns a graph object populated with data.
         *
         * @param json handle to the root element of the json file.
         * @param typeOfT is used when deserializing to a specific Type.
         * @return The {@code Graph} object created from the JSON input.
         * @throws JsonParseException if JSON input is not in correct format.
         */
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext
            context) throws JsonParseException {
            JsonObject rawGraph = json.getAsJsonObject();
            JsonArray edges = rawGraph.get(EDGES).getAsJsonArray();
            int numVertices = rawGraph.get(NUM_VERTICES).getAsInt();
            Graph graph = new Graph(numVertices);

            for (JsonElement edge : edges) {
                JsonObject edgeObj = edge.getAsJsonObject();

                JsonObject srcObj = edgeObj.get(SOURCE).getAsJsonObject();
                int src = srcObj.get(ID).getAsInt();
                short srcType = srcObj.get(TYPE).getAsShort();
                JsonObject srcPropObj = srcObj.get(PROPERTIES).getAsJsonObject();
                HashMap<Short,String> srcProperties = getPropertiesAsHashMap(srcPropObj);

                JsonObject dstObj = edgeObj.get(DESTINATION).getAsJsonObject();
                int dst = dstObj.get(ID).getAsInt();
                short dstType = dstObj.get(TYPE).getAsShort();
                JsonObject dstPropObj = dstObj.get(PROPERTIES).getAsJsonObject();
                HashMap<Short,String> dstProperties = getPropertiesAsHashMap(dstPropObj);

                short edgeType = edgeObj.get(TYPE).getAsShort();
                JsonObject edgePropObj = edgeObj.get(PROPERTIES).getAsJsonObject();
                HashMap<Short,String> edgeProperties = getPropertiesAsHashMap(edgePropObj);

                graph.addEdgeTemporarily(src, dst, srcType, dstType, srcProperties, dstProperties,
                    edgeType, edgeProperties);
            }
            graph.finalizeChanges();
            return graph;
        }

        private HashMap<Short,String> getPropertiesAsHashMap(JsonObject propertiesObj) {
            HashMap<Short,String> properties = new HashMap<>();
            for (Map.Entry<String,JsonElement> property : propertiesObj.entrySet()) {
                String key = property.getKey();
                properties.put(Short.parseShort(key), property.getValue().getAsString());
            }
            return properties;
        }
    }
}
