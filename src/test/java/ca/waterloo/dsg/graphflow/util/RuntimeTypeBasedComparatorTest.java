package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the static methods of {@link RuntimeTypeBasedComparator} with each supported comparison
 * operator and operands with runtime types of {@code Integer}, {@code Double}, {@code String},
 * and {@code Boolean}.
 */
public class RuntimeTypeBasedComparatorTest {

    @Test
    public void testBooleanOperandsEquals() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(true, true,
            ComparisonOperator.EQUALS));
    }

    @Test
    public void testBooleanOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(true, false,
            ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testIntegerOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(20, 10,
            ComparisonOperator.GREATER_THAN));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 20,
            ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testIntegerOperandsLessThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 20,
            ComparisonOperator.LESS_THAN));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(20, 10,
            ComparisonOperator.LESS_THAN));
    }

    @Test
    public void testIntegerOperandsGreaterThanOrEqual() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 10,
            ComparisonOperator.GREATER_THAN_OR_EQUAL));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 20,
            ComparisonOperator.GREATER_THAN_OR_EQUAL));
    }

    @Test
    public void testIntegerOperandsLessThanOrEqual() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 20,
            ComparisonOperator.LESS_THAN_OR_EQUAL));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(20, 10,
            ComparisonOperator.LESS_THAN_OR_EQUAL));
    }

    @Test
    public void testIntegerOperandsNotEquals() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(20, 10,
            ComparisonOperator.NOT_EQUALS));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(10, 10,
            ComparisonOperator.NOT_EQUALS));
    }

    @Test
    public void testDoubleOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(20.5, 10.8,
            ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testStringOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare("zxc", "abcdzx",
            ComparisonOperator.GREATER_THAN));
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare("abs", "abc",
            ComparisonOperator.GREATER_THAN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStringAndInteger() {
        RuntimeTypeBasedComparator.resolveTypesAndCompare("zxc", 10, ComparisonOperator.
            GREATER_THAN);
    }
}