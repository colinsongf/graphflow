package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchTypeException;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.StringToShortKeyStore;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import org.antlr.v4.runtime.misc.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Stores a mapping from {@code String} types and property keys to {@code short} types and
 * property keys.
 */
public class TypeAndPropertyKeyStore {

    public static final short ANY = -1;
    private static TypeAndPropertyKeyStore INSTANCE = new TypeAndPropertyKeyStore();

    @VisibleForTesting
    StringToShortKeyStore typeKeyStore = new StringToShortKeyStore();
    // TypeAndPropertyKeyStore has the invariant that if a property key has a short key,
    // then the property key certainly has a DataType associated with it.
    @VisibleForTesting
    StringToShortKeyStore propertyKeyStore = new StringToShortKeyStore();

    @VisibleForTesting
    Map<Short, DataType> propertyDataTypeStore = new HashMap<>();

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private TypeAndPropertyKeyStore() {}

    /**
     * @param stringType The {@code String} type.
     * @return The {@code Short} type. If {@code stringType} is either {@code null} or an empty
     * string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     */
    public Short mapStringTypeToShort(String stringType) {
        if (isNullOrEmpty(stringType)) {
            return ANY;
        }
        return typeKeyStore.mapStringKeyToShort(stringType);
    }

    /**
     * @param stringType The {@code String} type.
     * @return The {@code short} type. If {@code stringType} is either {@code null} or an empty
     * string, {@link TypeAndPropertyKeyStore#ANY} is returned.
     */
    public short mapStringTypeToShortOrInsert(String stringType) {
        if (isNullOrEmpty(stringType)) {
            return ANY;
        }
        return typeKeyStore.getKeyAsShortOrInsert(stringType);
    }

    /**
     * Asserts the {@code Short} type has been inserted in the store previously.
     *
     * @throws NoSuchTypeException if the type is not found in the store.
     */
    public Short mapStringTypeToShortAndAssertTypeExists(String type) {
        if (null != type) {
            Short shortType = mapStringTypeToShort(type);
            if (null == shortType) {
                throw new NoSuchTypeException("The type " + type + " is not found in the store.");
            }
            return shortType;
        }
        return ANY;
    }

    /**
     * @param key The {@code short} property key.
     * @return The data type of the property with the given key.
     * @throws NoSuchPropertyKeyException if the {@code key} passed is not present in the store.
     */
    public DataType getPropertyDataType(short key) {
        DataType dataType = propertyDataTypeStore.get(key);
        if (null == dataType) {
            throw new NoSuchPropertyKeyException(key);
        }
        return dataType;
    }

    /**
     * Given some properties as Map<String (key), Pair<String (dataType), String (value)>>,
     * returns the same properties as Map<Short, Pair<DataType, String>>. If a property key K
     * does not exist in the given properties, K is inserted into the store.
     *
     * @param stringProperties The properties as {@code Map<String, Pair<String, String>>}.
     * @return The properties as {@code Map<Short, Pair<DataType, String>>}. If the {@code
     * stringProperties} passed is {@code null}, {@code null} is returned.
     * @throws IncorrectDataTypeException if any of the property keys has been previously
     * stored with a different data type from the one passed.
     */
    public Map<Short, Pair<DataType, String>> mapStringPropertiesToShortAndDataTypeOrInsert(
        Map<String, Pair<String, String>> stringProperties) {
        return mapStringPropertiesToShortAndDataType(stringProperties,
            true /* insert if key doesn't exist */,
            true /* assert all keys exist. Argument not used since a key is inserted if it doesn't
            exist */);
    }

    /**
     * Given some properties as Map<String (key), Pair<String (dataType), String (value)>>,
     * returns the same properties as Map<Short, Pair<DataType, String>>.
     *
     * @param stringProperties The properties as {@code Map<String, Pair<String, String>>}.
     * @return The properties as as {@code Map<Short, Pair<DataType, String>>}. If the {@code
     * stringProperties} passed is {@code null}, {@code null} is returned.
     * @throws IncorrectDataTypeException if any of the property keys has been previously
     * stored with a different data type from the one passed.
     * @throws NoSuchPropertyKeyException if any of the property keys is not in the store.
     */
    public Map<Short, Pair<DataType, String>> mapStringPropertiesToShortAndDataType(
        Map<String, Pair<String, String>> stringProperties) {
        return mapStringPropertiesToShortAndDataType(stringProperties,
            false /* do not insert if key doesn't exist */, true /* assert all keys exist */);
    }

    /**
     * Ensures for each property in properties that its associated data type is the same as that
     * in the store if it had previously been inserted.
     *
     * @param properties The properties to check the data types of in the store.
     * @throws IncorrectDataTypeException if the dataType of any property key is not the
     * same as that in the store if it had been previously inserted.
     */
    public void assertExistingKeyDataTypesMatchPreviousDeclarations(
        Map<String, Pair<String, String>> properties) {
        mapStringPropertiesToShortAndDataType(properties,
            false /* do not insert if key doesn't exist */,
            false /* do not assert on key existence */);
    }

    /**
     * @param stringKey String key to check.
     * @return whether the given key is defined as a property in the store.
     */
    public boolean isPropertyDefined(String stringKey) {
        return propertyKeyStore.mapStringKeyToShort(stringKey) != null;
    }

    /**
     * Ensures for each property in properties that its associated data type is the same as that
     * in the store.
     *
     * @param properties The properties to check the data types of in the store.
     * @throws NoSuchPropertyKeyException if the property key is not found in the store.
     * @throws IncorrectDataTypeException if the dataType of any property is not the same
     * as that in the store if it had been previously inserted.
     */
    public void assertAllKeyDataTypesMatchPreviousDeclarations(
        Map<String, Pair<String, String>> properties) {
        mapStringPropertiesToShortAndDataType(properties,
            false /* do not insert if key doesn't exist */, true /* assert all keys exist */);
    }

    private Map<Short, Pair<DataType, String>> mapStringPropertiesToShortAndDataType(
        Map<String, Pair<String, String>> stringProperties, boolean insertIfKeyDoesntExist,
        boolean assertAllKeysExist) {
        if (null == stringProperties) {
            return null;
        }
        Pair<Short, DataType> keyDataTypePair;
        Pair<String, String> stringDataTypeValuePair;
        Map<Short, Pair<DataType, String>> resultProperties = new HashMap<>();
        for (String stringKey : stringProperties.keySet()) {
            stringDataTypeValuePair = stringProperties.get(stringKey);
            keyDataTypePair = mapStringPropertyKeyValueToShortAndDataType(stringKey,
                stringDataTypeValuePair.a/* DataType as String */, insertIfKeyDoesntExist,
                assertAllKeysExist);
            resultProperties.put(keyDataTypePair.a /* key as short */, new Pair<>(
                keyDataTypePair.b/* DataType */, stringDataTypeValuePair.b/* value as String */));
        }
        return resultProperties;
    }

    @VisibleForTesting
    Pair<Short, DataType> mapStringPropertyKeyValueToShortAndDataType(String stringKey,
        String stringDataType, boolean insertIfKeyDoesntExist, boolean assertKeyExist) {
        if (isNullOrEmpty(stringKey)) {
            throw new IllegalArgumentException("Property keys can't be null or the empty string.");
        }
        Short key = propertyKeyStore.mapStringKeyToShort(stringKey);
        DataType dataType = DataType.mapStringToDataType(stringDataType);
        if (null != key) {
            DataType dataTypeStored = propertyDataTypeStore.get(key);
            if (dataTypeStored != dataType) {
                throw new IncorrectDataTypeException("Incorrect DataType usage - property key " +
                    stringKey + " has been declared as " + dataTypeStored + " previously but " +
                    "now it used as " + stringDataType.toUpperCase() + ".");
            }
        } else if (insertIfKeyDoesntExist) {
            key = propertyKeyStore.getKeyAsShortOrInsert(stringKey);
            propertyDataTypeStore.put(key, dataType);
        } else if (assertKeyExist) {
            throw new NoSuchPropertyKeyException(stringKey);
        }
        return new Pair<>(key, dataType);
    }

    /**
     * @param stringKey String key to map.
     * @return short mapping of the key or null if the key does not exist.
     * @throws IllegalArgumentException if the given String key is null or empty.
     */
    public Short mapStringPropertyKeyToShort(String stringKey) {
        if (isNullOrEmpty(stringKey)) {
            throw new IllegalArgumentException("property keys can't be null or the empty string.");
        }
        return propertyKeyStore.mapStringKeyToShort(stringKey);
    }

    /**
     * See {@link StringToShortKeyStore#mapStringKeyToShort(String)}
     */
    public String getTypeStringFromShort(short typeId) {
        return typeKeyStore.mapShortKeyToString(typeId);
    }

    /**
     * See {@link StringToShortKeyStore#mapStringKeyToShort(String)}
     */
    public String getPropertyStringFromShort(short typeId) {
        return propertyKeyStore.mapShortKeyToString(typeId);
    }

    /**
     * Resets the {@link TypeAndPropertyKeyStore} state by creating a new {@code INSTANCE}.
     */
    void reset() {
        INSTANCE = new TypeAndPropertyKeyStore();
    }

    private boolean isNullOrEmpty(String key) {
        return null == key || "".equals(key);
    }

    /**
     * See {@link GraphDBState#serialize(ObjectOutputStream)}.
     */
    public void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        typeKeyStore.serialize(objectOutputStream);
        propertyKeyStore.serialize(objectOutputStream);
        objectOutputStream.writeObject(propertyDataTypeStore);
    }

    /**
     * See {@link GraphDBState#deserialize(ObjectInputStream)}.
     */
    @SuppressWarnings("unchecked") // Ignore {@code HashMap<Short, DataType>} cast warnings.
    public void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        typeKeyStore.deserialize(objectInputStream);
        propertyKeyStore.deserialize(objectInputStream);
        propertyDataTypeStore = (HashMap<Short, DataType>) objectInputStream.readObject();
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link TypeAndPropertyKeyStore}.
     */
    public static TypeAndPropertyKeyStore getInstance() {
        return INSTANCE;
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
    public static boolean isSameAs(TypeAndPropertyKeyStore a, TypeAndPropertyKeyStore b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (!StringToShortKeyStore.isSameAs(a.typeKeyStore, b.typeKeyStore) ||
            !StringToShortKeyStore.isSameAs(a.propertyKeyStore, b.propertyKeyStore) ||
            !Objects.equals(a.propertyDataTypeStore, b.propertyDataTypeStore)) {
            return false;
        }
        return true;
    }
}
