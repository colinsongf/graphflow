package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.graph.GraphDBState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Stores a mapping of {@code String} keys to {@code int} keys. Each new {@code String} key
 * inserted gets a consecutively increasing integer key starting from 0.
 */
public class StringToIntKeyMap {

    protected Map<String, Integer> stringToIntMap = new HashMap<>();
    protected int nextKeyAsInt = 0;

    /**
     * @param key The {@code String} key.
     * @return The {@code Integer} mapping of the given {@code String} key or {@code null} if the
     * {@code key} is not in the map.
     * @throws IllegalArgumentException if {@code key} passed is {@code null}.
     */
    public Integer mapStringKeyToInt(String key) {
        if (null == key) {
            throw new IllegalArgumentException("The key parameter passed is null.");
        }
        return stringToIntMap.get(key);
    }

    /**
     * @param stringKey The {@code String} key.
     * @return The {@code int} mapping of the given {@code String} key.
     * @throws IllegalArgumentException if {@code stringKey} passed is {@code null}.
     */
    public int getKeyAsIntOrInsert(String stringKey) {
        if (null == stringKey) {
            throw new IllegalArgumentException("The stringKey parameter passed is null.");
        }
        Integer intKey = stringToIntMap.get(stringKey);
        if (null == intKey) {
            adjustOtherDataStructures(stringKey, nextKeyAsInt);
            stringToIntMap.put(stringKey, nextKeyAsInt);
            return nextKeyAsInt++;
        }
        return intKey.intValue();
    }

    /**
     * Adjusts other data structures that classes extending {@link StringToIntKeyMap} might have.
     * This is called when a new key is being inserted to the map.
     * <p>
     * Note: Should be implemented by classes extending {@link StringToIntKeyMap}.
     *
     * @param newStringKey new String key being inserted.
     * @param newIntKey the new integer key that corresponds to the newStringKey.
     */
    protected void adjustOtherDataStructures(String newStringKey, int newIntKey) { }

    @UsedOnlyByTests
    int getStringToIntMapSize() {
        return stringToIntMap.size();
    }

    /**
     * @return the set of String key and int value entries in the map.
     */
    public Set<Entry<String, Integer>> entrySet() {
        return stringToIntMap.entrySet();
    }

    /**
     * See {@link GraphDBState#serialize(String)}
     */
    public void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeInt(nextKeyAsInt);
        objectOutputStream.writeObject(stringToIntMap);
    }

    /**
     * See {@link GraphDBState#deserialize(String)}
     */
    @SuppressWarnings("unchecked") // Ignore {@code HashMap<String, Integer>} cast warnings.
    public void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        nextKeyAsInt = objectInputStream.readInt();
        stringToIntMap = (HashMap<String, Integer>) objectInputStream.readObject();
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the {@code b} object
     * values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean isSameAs(StringToIntKeyMap a, StringToIntKeyMap b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.nextKeyAsInt == b.nextKeyAsInt &&
            Objects.equals(a.stringToIntMap, b.stringToIntMap);
    }
}
