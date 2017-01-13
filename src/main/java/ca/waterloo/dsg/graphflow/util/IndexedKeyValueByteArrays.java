package ca.waterloo.dsg.graphflow.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An array of an array of {@code short} key, and {@code String} value pairs. Each {@code short}
 * and {at code String} key-value pair is stored in a byte array that contains the {@code short}
 * key as the first two bytes and the {at code String} value as a set of bytes starting from index
 * 2. {@code String} values are serialized to and deserialized from bytes using UTF-8 char set.
 */
public class IndexedKeyValueByteArrays {

    private static final int INITIAL_CAPACITY = 2;
    private byte[][] data;

    /**
     * Creates {@link IndexedKeyValueByteArrays} with default capacity.
     */
    public IndexedKeyValueByteArrays() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Creates {@link IndexedKeyValueByteArrays} with the given {@code capacity}.
     *
     * @param capacity The initial capacity of the array underlying the {@link
     * IndexedKeyValueByteArrays}.
     */
    public IndexedKeyValueByteArrays(int capacity) {
        data = new byte[capacity][0];
    }

    /**
     * Overwrites all the key-value pairs in the list at the given {@code index} with the given
     * map. It does nothing if the map provided is {@code null} or empty.
     *
     * @param index The index of the list at which the key-value pairs are overridden.
     * @param keyValues The map containing the key-value pairs to override those of the list at
     * the given {@code index}.
     * @throws ArrayIndexOutOfBoundsException Exception thrown when {@code index} is larger than
     * the size of the collection.
     */
    public void set(int index, HashMap<Short,String> keyValues) {
        resizeIfNecessary(index);
        if (null == keyValues) {
            return;
        }
        data[index] = new byte[0];
        for (Map.Entry<Short, String> keyValue: keyValues.entrySet()) {
            byte[] keyValueAsBytes = getKeyValueAsByteArray(keyValue.getKey(), keyValue.getValue());
            data[index] = Arrays.copyOf(data[index], data[index].length + keyValueAsBytes.length);
            System.arraycopy(keyValueAsBytes, 0, data[index], data[index].length - keyValueAsBytes.
                length, keyValueAsBytes.length);
        }
    }

    public static byte[] getKeyValueAsByteArray(short key, String value) {
        byte[] valueAsBytes = value.getBytes(StandardCharsets.UTF_8);
        // 2 bytes (key) + 4 bytes (length of the value) + (length of value in bytes.
        byte[] keyValue = new byte[6 + valueAsBytes.length];
        keyValue[0] = (byte) (key >> 8);
        keyValue[1] = (byte) key;
        keyValue[2] = (byte) (valueAsBytes.length >> 24);
        keyValue[3] = (byte) (valueAsBytes.length >> 16);
        keyValue[4] = (byte) (valueAsBytes.length >> 8);
        keyValue[5] = (byte) valueAsBytes.length;
        System.arraycopy(valueAsBytes, 0, keyValue, 6, valueAsBytes.length);
        return keyValue;
    }

    private void resizeIfNecessary(int nextIndexToInsertAt) {
        data = ArrayUtils.resizeIfNecessary(data, nextIndexToInsertAt + 1);
    }

    @ExistsForTesting
    int getSize(int index) {
        return data[index].length;
    }
}
