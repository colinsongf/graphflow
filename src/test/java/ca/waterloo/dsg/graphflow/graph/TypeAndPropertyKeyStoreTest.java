package ca.waterloo.dsg.graphflow.graph;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * Tests {@link TypeAndPropertyKeyStore}.
 */
public class TypeAndPropertyKeyStoreTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        TypeAndPropertyKeyStore.getInstance().resetStore();
    }

    @Test
    public void testGetMethodsReturnAnyOnEmptyAndNullKeys() throws Exception {
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrAnyIfNullOrEmpty(""));
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrAnyIfNullOrEmpty(""));

        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrInsertIfDoesNotExist(""));
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrInsertIfDoesNotExist(""));

        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrAnyIfNullOrEmpty(null));
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrAnyIfNullOrEmpty(null));

        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrInsertIfDoesNotExist(null));
        Assert.assertEquals(TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrInsertIfDoesNotExist(null));
    }

    @Test
    public void testGetAndInsertMethods() throws Exception {
        TypeAndPropertyKeyStore.getInstance().getTypeAsShortOrInsertIfDoesNotExist("Friend");
        TypeAndPropertyKeyStore.getInstance().getTypeAsShortOrInsertIfDoesNotExist("Page");
        TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrInsertIfDoesNotExist("Name");
        TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrInsertIfDoesNotExist("Num Likes");

        Assert.assertEquals((short) 1, TypeAndPropertyKeyStore.getInstance().
            getPropertyAsShortOrAnyIfNullOrEmpty("Num Likes"));
        Assert.assertEquals("Name", TypeAndPropertyKeyStore.getInstance().
            getPropertyAsString((short) 0));
        Assert.assertEquals((short) 0, TypeAndPropertyKeyStore.getInstance().
           getTypeAsShortOrAnyIfNullOrEmpty("Friend"));
        Assert.assertEquals("Page", TypeAndPropertyKeyStore.getInstance().
           getTypeAsString((short) 1));
    }

    @Test(expected=NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetTypeAsString() {
        TypeAndPropertyKeyStore.getInstance().getTypeAsShortOrAnyIfNullOrEmpty("Gibberish");
    }

    @Test(expected=NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetTypeAsShort() {
        TypeAndPropertyKeyStore.getInstance().getTypeAsString((short) 32767);
    }

    @Test(expected=NoSuchElementException.class)
    public void testNoSuchElementExceptionForGetPropertyAsString() {
        TypeAndPropertyKeyStore.getInstance().getPropertyAsShortOrAnyIfNullOrEmpty(
            "Gibberish");
    }

    @Test(expected=NoSuchElementException.class)
    public void testNoSuchElementExceptionForPropertyAsShort() {
        TypeAndPropertyKeyStore.getInstance().getPropertyAsString((short) 32767);
    }
}
