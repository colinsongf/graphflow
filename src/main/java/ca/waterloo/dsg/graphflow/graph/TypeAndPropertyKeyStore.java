package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;
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
    @PackagePrivateForTesting
    static Map<Short, Type> propertyTypeStore = new HashMap<>();

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
     * @param properties The {@code Short} key, and {@code String} value properties to get a
     * mapping of as {@code short} keys and {@code String} values or to add to the property store.
     * @return The properties key value pairs stored as a {@code short} key and {@code String}. If
     * the {@code properties} passed is {@code null}, {@code null} is returned.
     * @throws IllegalArgumentException if any of the property types has been previously stored
     * with a type value different from the one passed.
     */
    public HashMap<Short, String> getPropertiesAsShortStringKeyValuesOrInsertIfDoesNotExist(
        HashMap<String, String[]> properties) {
        HashMap<Short, String> resultProperties = null;
        if (null != properties) {
            resultProperties = new HashMap<>();
            for (String property : properties.keySet()) {
                String type = properties.get(property)[0];
                String value = properties.get(property)[1];
                short propertyAsShort = getPropertyAsShortOrInsertIfDoesNotExist(property, type);
                resultProperties.put(propertyAsShort, value);
            }
        }
        return resultProperties;
    }


    @PackagePrivateForTesting
    short getPropertyAsShortOrInsertIfDoesNotExist(String property, String type) {
        if (isNullOrEmpty(property)) {
            return ANY;
        }
        short key;
        try {
            key = propertyKeyStore.getKeyAsShort(property);
            String typeStored = propertyTypeStore.get(key).name();
            if (!typeStored.equals(type.toUpperCase())) {
                throw new IllegalArgumentException("Type mismatch: property " + property +
                    " has been declared as " + typeStored + " in a previous query but used " +
                    "instead as " + type.toUpperCase());
            }
        } catch (NoSuchElementException e) {
            key = propertyKeyStore.getKeyAsShortOrInsertIfDoesNotExist(property);
            propertyTypeStore.put(key, Type.convert(type));
        }
        return key;
    }

    /**
     * @param edgeProperties The {@code String} key, and {@code String} value properties to get a
     * mapping of as {@code short} keys and {@code String} values.
     * @return The properties key value pairs stored as a {@code short} key and {@code String}. If
     * the {@code edgeProperties} passed is {@code null}, {@code null} is returned.
     * @throws NoSuchElementException if {@code edgeProperties} is not null and if a given
     * property, that is not {@code null}, and not an empty string, is not present in the key store.
     */
    public HashMap<Short, String> getPropertiesAsShortStringKeyValues(
        HashMap<String, String[]> edgeProperties) {
        if (null == edgeProperties) {
            return null;
        }

        HashMap<Short, String> properties = new HashMap<>();
        for (String key: edgeProperties.keySet()) {
            short keyAsShort = getPropertyAsShortOrAnyIfNullOrEmpty(key);
            properties.put(keyAsShort, edgeProperties.get(key)[1]);
        }
        return properties;
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

    /**
     * @param property The {@code short} property to get a mapping of as a String.
     * @return The property type stored as a {@code Type}.
     * @throws NoSuchElementException if the {@code property} passed is not present in the property
     * store.
     */
    public Type getPropertyType(short property) {
        Type type = propertyTypeStore.get(property);
        if (null == type) {
            throw new NoSuchElementException("property " + property + " is not found in store.");
        }

        return type;
    }

    /**
     * @param properties The {@code Short}, and {@code String} value properties to get their types.
     * @return The properties type stored as a {@code Short}, and {@code Type} key values.
     * @throws NoSuchElementException if the {@code property} passed is not present in the property
     * store.
     */
    public HashMap<Short, Type> getPropertyTypes(HashMap<Short, String> properties) {
        HashMap<Short, Type> propertyTypes = new HashMap<>();
        if (null != properties) {
            for (Short property: properties.keySet()) {
                propertyTypeStore.put(property, getPropertyType(property));
            }
        }

        return propertyTypes;
    }

    /**
     * Ensures that for a given edge, each property type in the edge as well as the from and to
     * vertices is declared correctly.
     * The type has to be the same in the from and to vertices and edge. The type has to also be
     * the same as that in the store if it had previously been inserted.
     *
     * @param fromProperties The properties of the from vertex.
     * @param toProperties The properties of the to vertex.
     * @param edgeProperties The properties of the edge.
     * @throws IllegalArgumentException if the type is not the same in the from, to vertices
     * or edge. Also if the type of any property is not the same as that in the store if it had
     * been previously inserted.
     */
    public void assertEachPropertyTypeCorrectness(
        HashMap<String, String[]> fromProperties, HashMap<String, String[]> toProperties,
        HashMap<String, String[]> edgeProperties) {

        // For a given type of a property in fromVertex properties, ensure the type used is the
        // same for the same property in edge and toVertex properties.
        compareTypesOfTwoPropertyCollection(fromProperties, toProperties);
        compareTypesOfTwoPropertyCollection(edgeProperties, fromProperties);

        // For a given type of a property in edge properties, ensure the type used is the
        // same for the same property toVertex properties.
        compareTypesOfTwoPropertyCollection(edgeProperties, toProperties);

        assertEachPropertyTypeMatchesPreviousDeclatationInTheStore(fromProperties);
        assertEachPropertyTypeMatchesPreviousDeclatationInTheStore(toProperties);
        assertEachPropertyTypeMatchesPreviousDeclatationInTheStore(edgeProperties);
    }

    /**
     * Ensures that for a given set of properties, the type of each is declared correctly.
     * The type has to be the same as that in the store if it had previously been inserted.
     *
     * @param properties The properties to check the types of in the store.
     * @throws IllegalArgumentException if the type of any property is not the same as that in the
     * store if it had been previously inserted.
     */
    public void assertEachPropertyTypeMatchesPreviousDeclatationInTheStore(
        HashMap<String, String[]> properties) {
        if (null != properties) {
            for (String property : properties.keySet()) {
                String type = properties.get(property)[0].toUpperCase();
                try {
                    String typeStored = propertyTypeStore.get(propertyKeyStore.getKeyAsShort(
                        property)).name();
                    if (!typeStored.equals(type)) {
                        throw new IllegalArgumentException("Type mismatch: property " + property +
                            " has been declared as " + typeStored + " in a previous query" +
                            " but used instead as " + type);
                    }
                } catch (NoSuchElementException e) {
                    // Escape. The key doesn't exist. Any type is fine.
                }
            }
        }
    }

    private void compareTypesOfTwoPropertyCollection(
        HashMap<String, String[]> thisPropertiesCollection,
        HashMap<String, String[]> thatPropertiesCollection) {
        if (null == thisPropertiesCollection || null == thatPropertiesCollection) {
            return;
        }

        for (String property: thisPropertiesCollection.keySet()) {
            String thisPropertyType = thisPropertiesCollection.get(property)[0].toUpperCase();
            String thatPropertyType = thatPropertiesCollection.get(property)[0].toUpperCase();
            if (null != thatPropertyType && !thisPropertyType.equals(thatPropertyType)) {
                throw new IllegalArgumentException("Type mismatch: property " + property +
                    " is used with two different types: " + thisPropertyType + " and " +
                    thatPropertyType);
            }
        }
    }

    @ExistsForTesting
    void resetStore() {
        typeKeyStore.reset();
        propertyKeyStore.reset();
        propertyTypeStore.clear();
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
