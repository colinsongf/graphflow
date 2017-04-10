package ca.waterloo.dsg.graphflow.graph;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@link TypeAndPropertyKeyStore}.
 */
public class TypeAndPropertyKeyStoreTest {

    @BeforeClass
    public static void resetGraphDBState() {
        GraphDBState.reset();
    }

    @Test
    public void testMapStringTypeToShortOnEmptyAndNullKeys() {
        Assert.assertEquals(new Short(TypeAndPropertyKeyStore.ANY), TypeAndPropertyKeyStore.
            getInstance().mapStringTypeToShort(""));
        Assert.assertEquals(new Short(TypeAndPropertyKeyStore.ANY), TypeAndPropertyKeyStore.
            getInstance().mapStringTypeToShort(null));
    }

    @Test
    public void testMapStringTypeToShortOrInsertOnEmptyAndNullKeys() {
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShortOrInsert(""));
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShortOrInsert(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringPropertyKeyToShortOnEmptyKeys() {
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringPropertyKeyToShortOnNullKeys() {
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringPropertyKeyValueToShortAndDataTypeOnEmptyKeys() {
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyValueToShortAndDataType(
            "", "String", true /* insert in the store */,
            true /* assert all keys exist, not used due to insertion */);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapStringPropertyKeyValueToShortAndDataTypeOnNullKeys() {
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyValueToShortAndDataType(
            null, "String", true /* insert in the store */,
            true /* assert all keys exist, not used due to insertion */);
    }

    @Test
    public void testMapStringTypeToShortOrInsert() {
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("Friend");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("Page");

        Assert.assertEquals(new Short((short) 0), TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShort("Friend"));
        Assert.assertEquals(new Short((short) 1), TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShort("Page"));
        Assert.assertEquals("Friend", TypeAndPropertyKeyStore.getInstance().typeKeyStore.
            mapShortKeyToString((short) 0));
        Assert.assertEquals("Page", TypeAndPropertyKeyStore.getInstance().typeKeyStore.
            mapShortKeyToString((short) 1));
    }

    @Test
    public void testMapStringPropertyKeyValueToShortAndDataType() {
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyValueToShortAndDataType("Name",
            "String", true /* insert in the store */, true /* check all */);
        TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyValueToShortAndDataType(
            "Num Likes", "String", true /* insert in the store */, true /* check all */);

        Assert.assertEquals(new Short((short) 0), TypeAndPropertyKeyStore.getInstance().
            mapStringPropertyKeyToShort("Name"));
        Assert.assertEquals(new Short((short) 1), TypeAndPropertyKeyStore.getInstance().
            mapStringPropertyKeyToShort("Num Likes"));
        Assert.assertEquals("Name", TypeAndPropertyKeyStore.getInstance().propertyKeyStore.
            mapShortKeyToString((short) 0));
        Assert.assertEquals("Num Likes", TypeAndPropertyKeyStore.getInstance().propertyKeyStore.
            mapShortKeyToString((short) 1));
    }
}
