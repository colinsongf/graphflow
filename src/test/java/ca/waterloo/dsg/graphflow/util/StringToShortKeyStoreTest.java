package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * Tests {@code SortedIntArrayList}.
 */
public class StringToShortKeyStoreTest {

    static StringToShortKeyStore keyStore = new StringToShortKeyStore();
    static StringToShortKeyStore anotherKeyStore = new StringToShortKeyStore();

    @Test(expected = NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetKeyAsString() {
        keyStore.getKeyAsShort("Gibberish");
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetKeyAsShort1() {
        keyStore.getKeyAsString((short) 32767);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetKeyAsShort2() {
        keyStore.getKeyAsString((short) -2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerExceptionForGetKeyAsString() {
        keyStore.getKeyAsShort(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPointerExceptionForGetKeyAsShortOrAddIfDoesNotExist() {
        keyStore.getKeyAsShortOrInsertIfDoesNotExist(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetOrInsertIfDoesNotExistAndResetStoreMethods() {
        short BulbasaurIdx = keyStore.getKeyAsShortOrInsertIfDoesNotExist("Bulbasaur");
        short IvysaurIdx = keyStore.getKeyAsShortOrInsertIfDoesNotExist("Ivysaur");
        short VenusaurIdx = anotherKeyStore.getKeyAsShortOrInsertIfDoesNotExist("Venusaur");
        short CharmanderIdx = anotherKeyStore.getKeyAsShortOrInsertIfDoesNotExist("Charmander");

        Assert.assertEquals(0, BulbasaurIdx);
        Assert.assertEquals(1, IvysaurIdx);
        Assert.assertEquals(0, VenusaurIdx);
        Assert.assertEquals(1, CharmanderIdx);

        Assert.assertEquals(0, keyStore.getKeyAsShort("Bulbasaur"));
        Assert.assertEquals("Bulbasaur", keyStore.getKeyAsString((short) 0));

        Assert.assertEquals(1, keyStore.getKeyAsShort("Ivysaur"));
        Assert.assertEquals("Ivysaur", keyStore.getKeyAsString((short) 1));

        Assert.assertEquals(0, anotherKeyStore.getKeyAsShort("Venusaur"));
        Assert.assertEquals("Venusaur", anotherKeyStore.getKeyAsString((short) 0));

        Assert.assertEquals(1, anotherKeyStore.getKeyAsShort("Charmander"));
        Assert.assertEquals("Charmander", anotherKeyStore.getKeyAsString((short) 1));

        // Empty the store. Assert an exception is thrown as the previously added key was removed.
        keyStore.reset();
        keyStore.getKeyAsShort("Bulbasaur");
    }
}
