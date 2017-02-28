package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@code SortedIntArrayList}.
 */
public class StringToShortKeyStoreTest {

    private StringToShortKeyStore keyStore = new StringToShortKeyStore();
    private StringToShortKeyStore anotherKeyStore = new StringToShortKeyStore();

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerExceptionForGetKeyAsString() {
        keyStore.mapStringKeyToShort(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerExceptionForGetKeyAsShortOrAddIfDoesNotExist() {
        keyStore.getKeyAsShortOrInsert(null);
    }

    @Test
    public void testGetOrInsertIfDoesNotExistAndResetStoreMethods() {
        short BulbasaurIdx = keyStore.getKeyAsShortOrInsert("Bulbasaur");
        short IvysaurIdx = keyStore.getKeyAsShortOrInsert("Ivysaur");
        short VenusaurIdx = anotherKeyStore.getKeyAsShortOrInsert("Venusaur");
        short CharmanderIdx = anotherKeyStore.getKeyAsShortOrInsert("Charmander");

        Assert.assertEquals(0, BulbasaurIdx);
        Assert.assertEquals(1, IvysaurIdx);
        Assert.assertEquals(0, VenusaurIdx);
        Assert.assertEquals(1, CharmanderIdx);

        Assert.assertEquals(new Short((short) 0), keyStore.mapStringKeyToShort("Bulbasaur"));
        Assert.assertEquals("Bulbasaur", keyStore.mapShortKeyToString((short) 0));

        Assert.assertEquals(new Short((short) 1), keyStore.mapStringKeyToShort("Ivysaur"));
        Assert.assertEquals("Ivysaur", keyStore.mapShortKeyToString((short) 1));

        Assert.assertEquals(new Short((short) 0), anotherKeyStore.mapStringKeyToShort("Venusaur"));
        Assert.assertEquals("Venusaur", anotherKeyStore.mapShortKeyToString((short) 0));

        Assert.assertEquals(new Short((short) 1), anotherKeyStore.mapStringKeyToShort(
            "Charmander"));
        Assert.assertEquals("Charmander", anotherKeyStore.mapShortKeyToString((short) 1));

        Assert.assertEquals(null, anotherKeyStore.mapStringKeyToShort("Zapdos"));

        Assert.assertEquals(2, keyStore.getStringToIntMapSize());
        Assert.assertEquals(2, anotherKeyStore.getStringToIntMapSize());
    }
}
