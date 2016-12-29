package ca.waterloo.dsg.graphflow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores a mapping of {@code String} keys to {@code short} values. Each new {@code String} key
 * inserted gets a consecutively increasing short value starting from 0.
 */
public class StringToShortKeyStore {

    private static final int DEFAULT_CAPACITY = 2;
    private Map<String, Short> stringToShortMap = new HashMap<>();
    private String[] shortToStringMap = new String[DEFAULT_CAPACITY];
    private short nextKeyAsShort = 0;

    /**
     * @param key The {@code String} value to get a mapping of as short.
     * @return The {@code short} key.
     * @throws IllegalArgumentException if {@code key} passed is {@code null}.
     * @throws NoSuchElementException if {@code key} passed is not {@code null}, not an empty
     * string, and is not present in the key store.
     */
    public short getKeyAsShort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("The key parameter passed is null");
        }
        if (!stringToShortMap.containsKey(key)) {
            throw new NoSuchElementException("The String '" + key + "' does not exist in the key " +
                "store.");
        }
        return stringToShortMap.get(key);
    }

    /**
     * @param key The {@code String} value to get a mapping of as short or to add to the key store.
     * @return The {@code short} key.
     * @throws IllegalArgumentException if {@code key} passed is {@code null}.
     */
    public short getKeyAsShortOrInsertIfDoesNotExist(String key) {
        if (null == key) {
            throw new IllegalArgumentException("The key parameter passed is null");
        }
        if (stringToShortMap.containsKey(key)) {
            return stringToShortMap.get(key);
        }

        shortToStringMap = (String[]) ArrayUtils.resizeIfNecessary(shortToStringMap,
            nextKeyAsShort + 1);
        shortToStringMap[nextKeyAsShort] = key;
        stringToShortMap.put(key, nextKeyAsShort);
        return nextKeyAsShort++;
    }

    /**
     * @param key The {@code short} value to get a mapping of as a String.
     * @return The {@code short} key.
     * @throws NoSuchElementException if {@code key} passed is not present in the key store.
     */
    public String getKeyAsString(short key) {
        if (key < 0 || key >= nextKeyAsShort) {
            throw new NoSuchElementException("The short '" + key + "' is not present in the key " +
                "store.");
        }
        return shortToStringMap[key];
    }

    /**
     * Clears the keys in the store.
     */
    public void reset() {
        stringToShortMap = new HashMap<>();
        shortToStringMap = new String[DEFAULT_CAPACITY];
        nextKeyAsShort = 0;
    }
}
