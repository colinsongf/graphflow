package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;
import ca.waterloo.dsg.graphflow.util.StringToShortKeyStore;
import ca.waterloo.dsg.graphflow.util.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores a mapping for types and properties between their {@code String} values as encountered
 * in queries and their internal {@code short} value representation.
*/
public class TypeAndPropertyKeyStore {

    public static final short ANY = -1;
    private static final TypeAndPropertyKeyStore INSTANCE = new TypeAndPropertyKeyStore();
    private static StringToShortKeyStore typeKeyStore = new StringToShortKeyStore();
    private static StringToShortKeyStore propertyKeyStore = new StringToShortKeyStore();
    private Map<Short, Type> propertyTypeMap = new HashMap<>();

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private TypeAndPropertyKeyStore() { }

    /**
     * @param type The {@code String} type to get a mapping of as short.
     * @return The type as a {@code short}. If the {@code type} passed is either {@code null} or an
     * empty string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     * @throws NoSuchElementException if {@code type} passed is not {@code null}, not an empty
     * string, and is not present in the key store.
     */
    public short getTypeAsShortOrAnyIfNullOrEmpty(String type) {
        if (isNullOrEmpty(type)) {
            return ANY;
        }
        return typeKeyStore.getKeyAsShort(type);
    }

    /**
     * @param property The {@code String} property to get a mapping of as short.
     * @return The property as a {@code short}. If the {@code property} passed is either {@code
     * null} or an empty string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     * @throws NoSuchElementException if {@code property} passed is not {@code null}, not an empty
     * string, and is not present in the key store.
     */
    public short getPropertyAsShortOrAnyIfNullOrEmpty(String property) {
        if (isNullOrEmpty(property)) {
            return ANY;
        }
        return propertyKeyStore.getKeyAsShort(property);
    }

    /**
     * @param type The {@code String} type to get a mapping of as short or to add to the type store.
     * @return The type value stored as a {@code short}. If the {@code type} passed is either {@code
     * null} or an empty string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     */
    public short getTypeAsShortOrInsertIfDoesNotExist(String type) {
        if (isNullOrEmpty(type)) {
            return ANY;
        }
        return typeKeyStore.getKeyAsShortOrInsertIfDoesNotExist(type);
    }

    /**
     * @param property The {@code String} property to get a mapping of as short or to add to the
     * property store.
     * @return The property value stored as a {@code short}. If the {@code property} passed is
     * either {@code null} or an empty string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     */
    public short getPropertyAsShortOrInsertIfDoesNotExist(String property, String type) {
        if (isNullOrEmpty(property)) {
            return ANY;
        }
        short key;
        try {
            key = propertyKeyStore.getKeyAsShort(property);
        } catch (NoSuchElementException e) {
            key = propertyKeyStore.getKeyAsShortOrInsertIfDoesNotExist(property);
            propertyTypeMap.put(key, Type.convert(type));
        }
        return key;
    }

    /**
     * @param properties The {@code Short} key, and {@code String} value properties to get a
     * mapping of as {@code short} keys and {@code String} values or to add to the property store.
     * @return The properties key value pairs stored as a {@code short} key and {@code String}. If
     * the {@code properties} passed is {@code null}, {@code null} is returned.
     */
    public HashMap<Short, String> getPropertiesAsShortStringKeyValuesOrInsertIfDoesNotExist(
        HashMap<String, String[]> properties) {
        HashMap<Short, String> resultProperties = null;
        if (null != properties) {
            resultProperties = new HashMap<>();
            for (String key : properties.keySet()) {
                String type = properties.get(key)[0];
                String value = properties.get(key)[1];
                short keyAsShort;
                try {
                    keyAsShort = propertyKeyStore.getKeyAsShort(value);
                    //TODO(amine): if the key exists, enforce the old type.
                } catch (NoSuchElementException e) {
                    keyAsShort = propertyKeyStore.getKeyAsShortOrInsertIfDoesNotExist(value);
                    propertyTypeMap.put(keyAsShort, Type.convert(type));
                }
                resultProperties.put(keyAsShort, value);
            }
        }
        return resultProperties;
    }

    /**
     * @param type The {@code short} type to get a mapping of as a String.
     * @return The type value stored as a {@code short}.
     * @throws NoSuchElementException if the {@code type} passed is not present in the type store.
     */
    public String getTypeAsString(short type) {
        return typeKeyStore.getKeyAsString(type);
    }

    /**
     * @param property The {@code short} property to get a mapping of as a String.
     * @return The property value stored as a {@code short}.
     * @throws NoSuchElementException if the {@code property} passed is not present in the property
     * store.
     */
    public String getPropertyAsString(short property) {
        return propertyKeyStore.getKeyAsString(property);
    }

    @PackagePrivateForTesting
    void resetStore() {
        typeKeyStore.reset();
        propertyKeyStore.reset();
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link TypeAndPropertyKeyStore}.
     */
    public static TypeAndPropertyKeyStore getInstance() {
        return INSTANCE;
    }

    private boolean isNullOrEmpty(String key) {
        return null == key || "".equals(key);
    }
}
