package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;

/**
 * Utility methods for resize operations on Arrays.
 */
public class ArrayUtils {

    private static double RESIZE_MULTIPLIER = 1.2;

    /**
     * Checks if the capacity of {@code array} is at least {@code minCapacity} and increases the
     * capacity by a factor of {@code RESIZE_MULTIPLIER} if it isn't.
     *
     * @param minCapacity The minimum required size of the array.
     */
    public static Object[] resizeIfNecessary(Object[] array, int minCapacity) {
        return (minCapacity > array.length) ? Arrays.copyOf(array, ArrayUtils.getNewCapacity(
            array.length, minCapacity)) : array;
    }

    /**
     * See {@link #resizeIfNecessary(Object[], int)}
     */
    public static int[] resizeIfNecessary(int[] array, int minCapacity) {
        return (minCapacity > array.length) ? Arrays.copyOf(array, ArrayUtils.getNewCapacity(
            array.length, minCapacity)) : array;
    }

    private static int getNewCapacity(int oldCapacity, int minCapacity) {
        int newCapacity = (int) (oldCapacity * RESIZE_MULTIPLIER) + 1;
        // Check if {@code newCapacity} > {@code minCapacity} before returning.
        return (newCapacity > minCapacity) ? newCapacity : minCapacity;
    }
}
