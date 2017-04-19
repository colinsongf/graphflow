package ca.waterloo.dsg.graphflow.query.output;

import com.google.gson.JsonObject;

/**
 * An interface for classes that can be outputted in JSON format
 */
public interface JsonOutputable {

    JsonObject toJson();
}
