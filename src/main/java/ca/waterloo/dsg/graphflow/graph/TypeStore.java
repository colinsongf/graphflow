package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores a mapping between {@code String} types and their corresponding {@code short} IDs.
 */
public class TypeStore {

    public static final short EMPTY_TYPE = -1;
    public static final short ANY_TYPE = -2;
    private static final TypeStore INSTANCE = new TypeStore();
    private Map<String, Short> stringToShortMap = new HashMap<>();
    private String[] shortToStringMap = new String[2];
    private short nextShortTypeId = 0;

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private TypeStore() {}

    /**
     * @param type The type {@code String}.
     * @return The {@code short} ID of {@code type}.
     */
    public short getShortTypeId(String type) {
        if (!stringToShortMap.containsKey(type)) {
            throw new NoSuchElementException("The type '" + type +
                "' is not present in the store.");
        }
        return stringToShortMap.get(type);
    }

    /**
     * Adds the {@code type} to the type store if it does not exist already, and returns the
     * {@code short} ID of {@code type}.
     *
     * @param type The type {@code String}.
     * @return The {@code short} ID of {@code type}.
     */
    public short addNewTypeIfDoesNotExist(String type) {
        if (stringToShortMap.containsKey(type)) {
            return stringToShortMap.get(type);
        }
        shortToStringMap = (String[]) ArrayUtils.resizeIfNecessary(shortToStringMap,
            nextShortTypeId + 1);
        shortToStringMap[nextShortTypeId] = type;
        stringToShortMap.put(type, nextShortTypeId);
        nextShortTypeId++;
        return (short) (nextShortTypeId - 1);
    }

    /**
     * @param id The {@code short} ID.
     * @return The {@code String} type mapped to {@code id}.
     * @throws NoSuchElementException if there is no {@code String} type mapping to {@code id}.
     */
    public String getStringForShort(short id) {
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
