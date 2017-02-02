package ca.waterloo.dsg.graphflow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores a mapping of {@code String} keys to {@code short} keys. Each new {@code String} key
 * inserted gets a consecutively increasing short key starting from 0.
 */
public class StringToShortKeyStore {

    private static final int DEFAULT_CAPACITY = 2;
    private Map<String, Short> stringToShortMap = new HashMap<>();
    private String[] shortToStringMap = new String[DEFAULT_CAPACITY];
    private short nextKeyAsShort = 0;

    /**
     * @param key The {@code String} key.
     * @return The {@code Short} mapping of the given {@code String} key or {@code null} if the
     * {@code key} is not in the store.
     * @throws IllegalArgumentException if {@code key} passed is {@code null}.
     */
    public Short mapStringKeyToShort(String key) {
        if (null == key) {
            throw new IllegalArgumentException("The key parameter passed is null.");
        }
        return stringToShortMap.get(key);
    }

    /**
     * @param stringKey The {@code String} key.
     * @return The {@code short} mapping of the given {@code String} key.
     * @throws IllegalArgumentException if {@code stringKey} passed is {@code null}.
     */
    public short getKeyAsShortOrInsert(String stringKey) {
        if (null == stringKey) {
            throw new IllegalArgumentException("The stringKey parameter passed is null.");
        }
        Short shortKey = stringToShortMap.get(stringKey);
        if (null == shortKey) {
            shortToStringMap = (String[]) ArrayUtils.resizeIfNecessary(shortToStringMap,
                nextKeyAsShort + 1);
            shortToStringMap[nextKeyAsShort] = stringKey;
            stringToShortMap.put(stringKey, nextKeyAsShort);
            return nextKeyAsShort++;
        }
        return shortKey;
    }

    /**
     * @param shortKey The {@code short} key.
     * @return The {@code String} mapping of the given {@code short} key.
     * @throws NoSuchElementException if {@code key} passed is not present in the key store.
     */
    public String mapShortKeyToString(short shortKey) {
        if (shortKey < 0 || shortKey >= nextKeyAsShort) {
            throw new NoSuchElementException("The short " + shortKey + " is not present in the " +
                "key store.");
        }
        return shortToStringMap[shortKey];
    }

    @UsedOnlyByTests
    int getStringToShortMapSize() {
        return stringToShortMap.size();
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
