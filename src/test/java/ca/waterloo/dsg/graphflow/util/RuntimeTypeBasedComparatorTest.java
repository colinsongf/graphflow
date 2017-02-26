package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class RuntimeTypeBasedComparatorTest {

    @Test
    public void testBooleanOperandsEquals() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Boolean(true),
            new Boolean (true), ComparisonOperator.EQUALS));
    }

    @Test
    public void testBooleanOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Boolean(true),
            new Boolean (false), ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testIntegerOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(20), new
            Integer(10), ComparisonOperator.GREATER_THAN));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(20), ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testIntegerOperandsLessThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(20), ComparisonOperator.LESS_THAN));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(20), new
            Integer(10), ComparisonOperator.LESS_THAN));
    }

    @Test
    public void testIntegerOperandsGreaterThanOrEqual() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(10), ComparisonOperator.GREATER_THAN_EQUAL));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(20), ComparisonOperator.GREATER_THAN_EQUAL));
    }

    @Test
    public void testIntegerOperandsLessThanOrEqual() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(20), ComparisonOperator.LESS_THAN_OR_EQUAL));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(20), new
            Integer(10), ComparisonOperator.LESS_THAN_OR_EQUAL));
    }

    @Test
    public void testIntegerOperandsNotEquals() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(20), new
            Integer(10), ComparisonOperator.NOT_EQUALS));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(10), new
            Integer(10), ComparisonOperator.NOT_EQUALS));
    }

    @Test
    public void testDoubleOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Double(20.5), new
            Double(10.8), ComparisonOperator.GREATER_THAN));
    }

    @Test
    public void testDoubleAndIntegerOperandsLessThan() {
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Double(20.5), new
            Integer(10), ComparisonOperator.LESS_THAN));
        Assert.assertFalse(RuntimeTypeBasedComparator.resolveTypesAndCompare(new Integer(20), new
            Double(10.8), ComparisonOperator.LESS_THAN));
    }

    @Test
    public void testStringOperandsGreaterThan() {
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new String("zxc"), new
            String("abcdzx"), ComparisonOperator.GREATER_THAN));
        Assert.assertTrue(RuntimeTypeBasedComparator.resolveTypesAndCompare(new String("abs"), new
            String("abc"), ComparisonOperator.GREATER_THAN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStringAndInteger() {
        RuntimeTypeBasedComparator.resolveTypesAndCompare(new String("zxc"), new
            Integer(10), ComparisonOperator.GREATER_THAN);
    }
}