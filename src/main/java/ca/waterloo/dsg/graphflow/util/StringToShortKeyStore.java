package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.graph.GraphDBState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Stores a mapping of {@code String} keys to {@code short} keys and vice versa. Each new
 * {@code String} key inserted gets a consecutively increasing short key starting from 0.
 * <p>
 * Warning: This class internally uses {@link StringToIntKeyMap} and stores the {@code short} keys
 * as integers and converts them to {@code short}s. If the callers insert more than
 * {@link Short#MAX_VALUE} keys this class will overflow its short values.
 */
public class StringToShortKeyStore extends StringToIntKeyMap {

    private static final int DEFAULT_CAPACITY = 2;
    private String[] shortToStringMap = new String[DEFAULT_CAPACITY];

    /**
     * @see StringToIntKeyMap#adjustOtherDataStructures(String, int).
     */
    protected void adjustOtherDataStructures(String newStringKey, int newShortKey) {
        shortToStringMap = (String[]) ArrayUtils.resizeIfNecessary(shortToStringMap,
            nextKeyAsInt + 1);
        shortToStringMap[nextKeyAsInt] = newStringKey;
    }

    /**
     * See {@link GraphDBState#serialize(String)}.
     */
    public void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        super.serialize(objectOutputStream);
        objectOutputStream.writeObject(shortToStringMap);
    }

    /**
     * See {@link GraphDBState#deserialize(String)}.
     */
    public void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        super.deserialize(objectInputStream);
        shortToStringMap = (String[]) objectInputStream.readObject();
    }

    /**
     * @param stringKey The {@code String} key.
     * @return The {@code short} mapping of the given {@code String} key.
     * @throws IllegalArgumentException if {@code stringKey} passed is {@code null}.
     */
    public short getKeyAsShortOrInsert(String stringKey) {
        return (short) getKeyAsIntOrInsert(stringKey);
    }

    /**
     * @param key The {@code String} key.
     * @return The {@code Short} mapping of the given {@code String} key or {@code null} if the
     * {@code key} is not in the map.
     * @throws IllegalArgumentException if {@code key} passed is {@code null}.
     */
    public Short mapStringKeyToShort(String key) {
        Integer intKey = mapStringKeyToInt(key);
        if (null == intKey) {
            return null;
        }
        return intKey.shortValue();
    }

    /**
     * @param shortKey The {@code short} key.
     * @return The {@code String} mapping of the given {@code short} key.
     * @throws NoSuchElementException if {@code key} passed is not present in the key store.
     */
    public String mapShortKeyToString(short shortKey) {
        if (shortKey < 0 || shortKey >= nextKeyAsInt) {
            throw new NoSuchElementException("The short " + shortKey + " is not present in the " +
                "key store.");
        }
        return shortToStringMap[shortKey];
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
    public static boolean isSameAs(StringToShortKeyStore a, StringToShortKeyStore b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Arrays.equals(a.shortToStringMap, b.shortToStringMap) &&
            StringToIntKeyMap.isSameAs(a, b);
    }
}
