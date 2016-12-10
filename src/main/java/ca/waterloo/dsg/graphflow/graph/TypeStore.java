package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores a mapping between {@code String} types and their corresponding {@code short} IDs.
 */
public class TypeStore {

    public static final short ANY_TYPE = -1;
    private static final int DEFAULT_CAPACITY = 2;
    private static final TypeStore INSTANCE = new TypeStore();
    private Map<String, Short> stringToShortMap = new HashMap<>();
    private String[] shortToStringMap = new String[DEFAULT_CAPACITY];
    private short nextShortTypeId = 0;

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private TypeStore() {}

    /**
     * @param stringType The type {@code String}.
     * @return The {@code short} ID of {@code stringType}. If {@code stringType} is either
     * {@code null} or an empty string, {@link TypeStore#ANY_TYPE} is returned.
     * @throws NoSuchElementException if {@code stringType} is not {@code null} or an empty
     * string, and is not present in the type store.
     */
    public short getShortIdOrAnyTypeIfNull(String stringType) {
        if (null == stringType || "".equals(stringType)) {
            return ANY_TYPE;
        }
        if (!stringToShortMap.containsKey(stringType)) {
            throw new NoSuchElementException("The string type '" + stringType + "' does not exist" +
                ".");
        }
        return stringToShortMap.get(stringType);
    }

    /**
     * Adds {@code stringType} to the type store if it does not exist already, and returns the
     * {@code short} ID of {@code stringType}. If {@code stringType} is either {@code null} or an
     * empty string, {@link TypeStore#ANY_TYPE} is returned.
     *
     * @param stringType The type {@code String}.
     * @return The {@code short} ID of {@code stringType}.
     */
    public short addNewTypeIfDoesNotExist(String stringType) {
        if (null == stringType || "".equals(stringType)) {
            return ANY_TYPE;
        }
        if (stringToShortMap.containsKey(stringType)) {
            return stringToShortMap.get(stringType);
        }
        shortToStringMap = (String[]) ArrayUtils.resizeIfNecessary(shortToStringMap,
            nextShortTypeId + 1);
        shortToStringMap[nextShortTypeId] = stringType;
        stringToShortMap.put(stringType, nextShortTypeId);
        nextShortTypeId++;
        return (short) (nextShortTypeId - 1);
    }

    /**
     * @param id The {@code short} ID.
     * @return The {@code String} type mapped to {@code id}.
     * @throws NoSuchElementException if there is no {@code String} type mapping to {@code id}.
     */
    public String getStringType(short id) {
        if (id < 0 && id > nextShortTypeId) {
            throw new NoSuchElementException("The short '" + id + "' is not present in the store.");
        }
        return shortToStringMap[id];
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link TypeStore}.
     */
    public static TypeStore getInstance() {
        return INSTANCE;
    }
}
