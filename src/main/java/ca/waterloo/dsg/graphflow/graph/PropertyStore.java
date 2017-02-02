package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;

/**
 * Abstract property store class for the {@link EdgeStore} and {@link VertexPropertyStore} classes.
 */
abstract class PropertyStore {

    protected byte[] serializeProperties(Map<Short, Pair<DataType, String>> properties) {
        byte[] propertiesAsBytes = new byte[0];
        if (null != properties && !properties.isEmpty()) {
            int index = 0;
            int propertiesLength = 0;
            byte[][] keyValueByteArrays = new byte[properties.size()][];
            for (Short key : properties.keySet()) {
                keyValueByteArrays[index] = DataType.serialize(properties.get(key).a, key,
                    properties.get(key).b);
                propertiesLength += keyValueByteArrays[index].length;
                index++;
            }

            propertiesAsBytes = new byte[propertiesLength];
            propertiesLength = 0;
            for (byte[] keyValueAsBytes : keyValueByteArrays) {
                System.arraycopy(keyValueAsBytes, 0, propertiesAsBytes, propertiesLength,
                    keyValueAsBytes.length);
                propertiesLength += keyValueAsBytes.length;
            }
        }
        return propertiesAsBytes;
    }
}
