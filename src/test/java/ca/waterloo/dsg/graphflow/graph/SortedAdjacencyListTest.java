package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.IntArrayList;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@link SortedAdjacencyList}.
 */
public class SortedAdjacencyListTest {

    @Before
    public void setUp() {
        TypeAndPropertyKeyStore.getInstance().reset();
        EdgeStore.getInstance().reset();
    }

    private SortedAdjacencyList getPopulatedAdjacencyList(int[] neighbourIds,
        short[] neighbourTypes, long[] neighbourEdgeIds) {
        SortedAdjacencyList adjacencyList = new SortedAdjacencyList();
        for (int i = 0; i < neighbourIds.length; i++) {
            if (neighbourTypes != null && neighbourTypes.length == neighbourIds.length) {
                adjacencyList.add(neighbourIds[i], neighbourTypes[i], neighbourEdgeIds[i]);
            }
        }
        return adjacencyList;
    }

    private void testSort(int[] inputNeighbourIds, short[] inputNeighbourTypes,
        long[] inputNeighbourEdgeIds, int[] sortedNeighbourIds, short[] sortedNeighbourTypes,
        long[] sortedNeighbourEdgeIds) {
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(inputNeighbourIds,
            inputNeighbourTypes, inputNeighbourEdgeIds);
        int expectedSize = inputNeighbourIds.length;
        Assert.assertEquals(expectedSize, adjacencyList.getSize());
        Assert.assertTrue(expectedSize <= adjacencyList.neighbourIds.length); // Check capacity.
        Assert.assertArrayEquals(sortedNeighbourIds, Arrays.copyOf(adjacencyList.neighbourIds,
            adjacencyList.getSize()));
        Assert.assertArrayEquals(sortedNeighbourTypes, Arrays.copyOf(adjacencyList.edgeTypes,
            adjacencyList.getSize()));
        Assert.assertArrayEquals(sortedNeighbourEdgeIds, Arrays.copyOf(adjacencyList.edgeIds,
            adjacencyList.getSize()));
    }

    private void testSearch(int[] inputNeighbourIds, short[] inputNeighbourTypes,
        long[] inputNeighbourEdgeIds, int neighbourIdForSearch, short edgeTypeForSearch,
        int expectedIndex) {
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(inputNeighbourIds,
            inputNeighbourTypes, inputNeighbourEdgeIds);
        int resultIndex = adjacencyList.search(neighbourIdForSearch, edgeTypeForSearch);
        Assert.assertEquals(expectedIndex, resultIndex);
    }

    @Test
    public void testCreationAndSortWithTypes() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 12, 89, 0};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 0, 10, 5};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7};
        int[] sortedNeighboursIds = {0, 1, 12, 32, 34, 34, 54, 89};
        short[] sortedNeighbourTypes = {5, 4, 0, 3, 1, 9, 3, 10};
        long[] sortedNeighbourEdgeIds = {7, 0, 5, 1, 3, 4, 2, 6};
        testSort(neighbourIds, neighbourTypes, neighbourEdgeIds, sortedNeighboursIds,
            sortedNeighbourTypes, sortedNeighbourEdgeIds);
    }

    @Test
    public void testSortWithMultipleTypesForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] sortedNeighboursIds = {0, 1, 7, 12, 14, 32, 34, 34, 34, 54, 89};
        short[] sortedNeighbourTypes = {5, 4, 0, 0, 3, 3, 1, 4, 9, 3, 10};
        long[] sortedNeighbourEdgeIds = {8, 0, 10, 6, 9, 1, 3, 5, 4, 2, 7};
        testSort(neighbourIds, neighbourTypes, neighbourEdgeIds, sortedNeighboursIds,
            sortedNeighbourTypes, sortedNeighbourEdgeIds);
    }

    @Test
    public void testSearchWithMultipleTypesForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int neighbourIdForSearch = 34;
        short edgeTypeForSearch = 4;
        int expectedIndex = 7;
        testSearch(neighbourIds, neighbourTypes, neighbourEdgeIds, neighbourIdForSearch,
            edgeTypeForSearch, expectedIndex);
    }

    @Test
    public void testSearchWithSingleTypeForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int neighbourIdForSearch = 7;
        short edgeTypeForSearch = TypeAndPropertyKeyStore.ANY;
        int expectedIndex = 2;
        testSearch(neighbourIds, neighbourTypes, neighbourEdgeIds, neighbourIdForSearch,
            edgeTypeForSearch, expectedIndex);
    }

    @Test
    public void testSearchWithNonExistentNeighbourType() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int neighbourIdForSearch = 7;
        short edgeTypeForSearch = 10;
        int expectedIndex = -1;
        testSearch(neighbourIds, neighbourTypes, neighbourEdgeIds, neighbourIdForSearch,
            edgeTypeForSearch, expectedIndex);
    }

    @Test
    public void testSearchWithNonExistentNeighbour() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int neighbourIdForSearch = 70;
        short edgeTypeForSearch = 10;
        int expectedIndex = -1;
        testSearch(neighbourIds, neighbourTypes, neighbourEdgeIds, neighbourIdForSearch,
            edgeTypeForSearch, expectedIndex);
    }

    @Test
    public void testSearchWithSmallSizeArrays() throws Exception {
        int[] neighbourIds = {1, 3};
        short[] neighbourTypes = {1, 3};
        long[] neighbourEdgeIds = {0, 1};
        int neighbourIdForSearch = 1;
        short edgeTypeForSearch = 1;
        int expectedIndex = 0;
        testSearch(neighbourIds, neighbourTypes, neighbourEdgeIds, neighbourIdForSearch,
            edgeTypeForSearch, expectedIndex);
    }

    @Test
    public void testRemoveNeighbourWithShortNeighbourAndTypeArrays() throws Exception {
        int[] neighbourIds = {1, 3};
        short[] neighbourTypes = {1, 3};
        long[] neighbourEdgeIds = {0, 1};
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(neighbourIds, neighbourTypes,
            neighbourEdgeIds);
        int neighbourIdForRemove = 1;
        short edgeTypeForRemove = 1;
        adjacencyList.removeNeighbour(neighbourIdForRemove, edgeTypeForRemove);
        int expectedIndex = -1;
        Assert.assertEquals(expectedIndex, adjacencyList.search(neighbourIdForRemove,
            edgeTypeForRemove));
        Assert.assertEquals(1, adjacencyList.getSize());
        int[] expectedNeighbours = {3};
        Assert.assertArrayEquals(expectedNeighbours, Arrays.copyOf(adjacencyList.neighbourIds,
            adjacencyList.getSize()));
    }

    @Test
    public void testIntersectionWithEdgeType() {
        int[] neighbourIds1 = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes1 = {4, 3, 3, 1, 9, 3, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds1 = {0, 1, 2, 3, 4, 5, 6, 7,  8, 9, 10};
        SortedAdjacencyList adjacencyList1 = getPopulatedAdjacencyList(neighbourIds1,
            neighbourTypes1, neighbourEdgeIds1);
        int[] neighbourIds2 = {1, 9, 14, 23, 34, 54, 89};
        short[] neighbourTypes2 = {4, 14, 3, 13, 3, 3, 23};
        long[] neighbourEdgeIds2 = {0, 1, 2, 3, 4, 5, 6};
        SortedAdjacencyList adjacencyList2 = getPopulatedAdjacencyList(neighbourIds2,
            neighbourTypes2, neighbourEdgeIds2);
        short intersectionFilterEdgeType = 3;
        IntArrayList listToIntersect = new IntArrayList();
        listToIntersect.addAll(Arrays.copyOf(adjacencyList2.neighbourIds, adjacencyList2.
            getSize()));
        IntArrayList intersections = adjacencyList1.getIntersection(listToIntersect,
            intersectionFilterEdgeType, null /* no property equality filters */);
        int[] expectedNeighbours = {14, 34, 54};
        Assert.assertArrayEquals(expectedNeighbours, intersections.toArray());
    }

    @Test
    public void testIntersectionWithNoEdgeType() {
        int[] neighbourIds1 = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes1 = {4, 3, 3, 1, 9, 3, 0, 10, 5, 3, 0};
        long[] neighbourEdgeIds1 = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        SortedAdjacencyList adjacencyList1 = getPopulatedAdjacencyList(neighbourIds1,
            neighbourTypes1, neighbourEdgeIds1);
        int[] neighbourIds2 = {1, 9, 14, 23, 34, 54, 89};
        short[] neighbourTypes2 = {4, 14, 3, 13, 3, 3, 23};
        long[] neighbourEdgeIds2 = {0, 1, 2, 3, 4, 5, 6};
        SortedAdjacencyList adjacencyList2 = getPopulatedAdjacencyList(neighbourIds2,
            neighbourTypes2, neighbourEdgeIds2);
        IntArrayList listToIntersect = new IntArrayList();
        listToIntersect.addAll(Arrays.copyOf(adjacencyList2.neighbourIds, adjacencyList2.
            getSize()));
        IntArrayList intersections = adjacencyList1.getIntersection(listToIntersect,
            TypeAndPropertyKeyStore.ANY, null /* no property equality filters */);
        int[] expectedNeighbours = {1, 14, 34, 54, 89};
        Assert.assertArrayEquals(expectedNeighbours, intersections.toArray());
    }

    @Test
    public void testGetFilteredNeighbourIds() {
        int[] neighbourIds = {1, 9, 14, 23, 34, 54, 89};
        short[] edgeTypes = {1, 1, 2, 3, 2, 1, 1};
        long[] edgeIds = {0, 1, 2, 3, 4, 5, 6};
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(neighbourIds, edgeTypes,
            edgeIds);

        IntArrayList filteredNeighbourIds = adjacencyList.getFilteredNeighbourIds((short) 1,
            null /* no property equality filters */);
        int[] expectedFilteredNeighbourIds = {1, 9, 54, 89};
        Assert.assertArrayEquals(expectedFilteredNeighbourIds, filteredNeighbourIds.toArray());

        filteredNeighbourIds = adjacencyList.getFilteredNeighbourIds(TypeAndPropertyKeyStore.ANY,
            null /* no property equality filters */);
        Assert.assertArrayEquals(neighbourIds, filteredNeighbourIds.toArray());
    }

    @Test
    public void testGetFilteredNeighbourIdsWithPropertyEqualityFilters() {
        int[] neighbourIds = {1, 9, 14, 23, 34, 54, 89};
        short[] edgeTypes = {1, 1, 2, 3, 2, 1, 1};
        long[] edgeIds = {0, 1, 2, 3, 4, 5, 6};
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(neighbourIds, edgeTypes,
            edgeIds);

        // add 7 edges with Ids i: 0 to 6 each with properties of size 1 with key: 0, String
        // DataType and value "value-" + i
        Map<Short, Pair<DataType, String>> properties = new HashMap<>();
        for (int i = 0; i < 7; ++i) {
            TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyValueToShortAndDataType(
                Integer.toString(i), "String", true /* insert in the store */, true /* checkAll */);
            properties.put((short) 0, new Pair<>(DataType.STRING, "value-" + i));
            EdgeStore.getInstance().addEdge(properties);
        }

        Map<Short, Pair<DataType, String>> propertyEqualityFilters = new HashMap<>();
        propertyEqualityFilters.put((short) 0, new Pair<>(DataType.STRING, "value-4"));

        // This filtering would get the neighbour with edgeId 4, neighbour Id 34, and edgeType 2.
        IntArrayList filteredNeighbourIds = adjacencyList.getFilteredNeighbourIds(
            TypeAndPropertyKeyStore.ANY, propertyEqualityFilters);
        Assert.assertEquals(1, filteredNeighbourIds.getSize());
        Assert.assertEquals(34, filteredNeighbourIds.get(0));
    }
}
