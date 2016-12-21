package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.IntArrayList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests {@link SortedAdjacencyList}.
 */
public class SortedAdjacencyListTest {

    private SortedAdjacencyList getPopulatedAdjacencyList(int[] neighbourIds,
        short[] neighbourTypes) {
        SortedAdjacencyList adjacencyList = new SortedAdjacencyList();
        for (int i = 0; i < neighbourIds.length; i++) {
            if (neighbourTypes != null && neighbourTypes.length == neighbourIds.length) {
                adjacencyList.add(neighbourIds[i], neighbourTypes[i]);
            }
        }
        return adjacencyList;
    }

    private void testSort(int[] inputNeighbourIds, short[] inputNeighbourTypes,
        int[] sortedNeighbourIds, short[] sortedNeighbourTypes) {
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(inputNeighbourIds,
            inputNeighbourTypes);
        int expectedSize = inputNeighbourIds.length;
        Assert.assertEquals(expectedSize, adjacencyList.getSize());
        Assert.assertTrue(expectedSize <= adjacencyList.neighbourIds.length); // Check capacity.
        Assert.assertArrayEquals(sortedNeighbourIds, Arrays.copyOf(adjacencyList.neighbourIds,
            adjacencyList.getSize()));
        Assert.assertArrayEquals(sortedNeighbourTypes, Arrays.copyOf(adjacencyList.edgeTypes,
            adjacencyList.getSize()));
    }

    private void testSearch(int[] inputNeighbourIds, short[] inputNeighbourTypes,
        int neighbourIdForSearch, int edgeTypeForSearch, int expectedIndex) {
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(inputNeighbourIds,
            inputNeighbourTypes);
        int resultIndex = adjacencyList.search(neighbourIdForSearch, edgeTypeForSearch);
        Assert.assertEquals(expectedIndex, resultIndex);
    }

    @Test
    public void testCreationAndSortWithTypes() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 12, 89, 0};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 0, 10, 5};
        int[] sortedNeighboursIds = {0, 1, 12, 32, 34, 34, 54, 89};
        short[] sortedNeighbourTypes = {5, 4, 0, 3, 1, 9, 3, 10};
        testSort(neighbourIds, neighbourTypes, sortedNeighboursIds, sortedNeighbourTypes);
    }

    @Test
    public void testSortWithMultipleTypesForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        int[] sortedNeighboursIds = {0, 1, 7, 12, 14, 32, 34, 34, 34, 54, 89};
        short[] sortedNeighbourTypes = {5, 4, 0, 0, 3, 3, 1, 4, 9, 3, 10};
        testSort(neighbourIds, neighbourTypes, sortedNeighboursIds, sortedNeighbourTypes);
    }

    @Test
    public void testSearchWithMultipleTypesForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        int neighbourIdForSearch = 34;
        int edgeTypeForSearch = 4;
        int expectedIndex = 7;
        testSearch(neighbourIds, neighbourTypes, neighbourIdForSearch, edgeTypeForSearch,
            expectedIndex);
    }

    @Test
    public void testSearchWithSingleTypeForSingleEdge() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        int neighbourIdForSearch = 7;
        int edgeTypeForSearch = TypeStore.ANY_TYPE;
        int expectedIndex = 2;
        testSearch(neighbourIds, neighbourTypes, neighbourIdForSearch, edgeTypeForSearch,
            expectedIndex);
    }

    @Test
    public void testSearchWithNonExistentNeighbourType() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        int neighbourIdForSearch = 7;
        int edgeTypeForSearch = 10;
        int expectedIndex = -1;
        testSearch(neighbourIds, neighbourTypes, neighbourIdForSearch, edgeTypeForSearch,
            expectedIndex);
    }

    @Test
    public void testSearchWithNonExistentNeighbour() throws Exception {
        int[] neighbourIds = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes = {4, 3, 3, 1, 9, 4, 0, 10, 5, 3, 0};
        int neighbourIdForSearch = 70;
        int edgeTypeForSearch = 10;
        int expectedIndex = -1;
        testSearch(neighbourIds, neighbourTypes, neighbourIdForSearch, edgeTypeForSearch,
            expectedIndex);
    }

    @Test
    public void testSearchWithSmallSizeArrays() throws Exception {
        int[] neighbourIds = {1, 3};
        short[] neighbourTypes = {1, 3};
        int neighbourIdForSearch = 1;
        int edgeTypeForSearch = 1;
        int expectedIndex = 0;
        testSearch(neighbourIds, neighbourTypes, neighbourIdForSearch, edgeTypeForSearch,
            expectedIndex);
    }

    @Test
    public void testRemoveNeighbourWithShortNeighbourAndTypeArrays() throws Exception {
        int[] neighbourIds = {1, 3};
        short[] neighbourTypes = {1, 3};
        SortedAdjacencyList adjacencyList = getPopulatedAdjacencyList(neighbourIds, neighbourTypes);
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
        SortedAdjacencyList adjacencyList1 = getPopulatedAdjacencyList(neighbourIds1,
            neighbourTypes1);
        int[] neighbourIds2 = {1, 9, 14, 23, 34, 54, 89};
        short[] neighbourTypes2 = {4, 14, 3, 13, 3, 3, 23};
        SortedAdjacencyList adjacencyList2 = getPopulatedAdjacencyList(neighbourIds2,
            neighbourTypes2);
        short intersectionFilterEdgeType = 3;
        IntArrayList listToIntersect = new IntArrayList();
        listToIntersect.addAll(Arrays.copyOf(adjacencyList2.neighbourIds, adjacencyList2.
            getSize()));
        IntArrayList intersections = adjacencyList1.getIntersection(listToIntersect,
            intersectionFilterEdgeType);
        int[] expectedNeighbours = {14, 34, 54};
        Assert.assertArrayEquals(expectedNeighbours, intersections.toArray());
    }

    @Test
    public void testIntersectionWithNoEdgeType() {
        int[] neighbourIds1 = {1, 32, 54, 34, 34, 34, 12, 89, 0, 14, 7};
        short[] neighbourTypes1 = {4, 3, 3, 1, 9, 3, 0, 10, 5, 3, 0};
        SortedAdjacencyList adjacencyList1 = getPopulatedAdjacencyList(neighbourIds1,
            neighbourTypes1);
        int[] neighbourIds2 = {1, 9, 14, 23, 34, 54, 89};
        short[] neighbourTypes2 = {4, 14, 3, 13, 3, 3, 23};
        SortedAdjacencyList adjacencyList2 = getPopulatedAdjacencyList(neighbourIds2,
            neighbourTypes2);
        IntArrayList listToIntersect = new IntArrayList();
        listToIntersect.addAll(Arrays.copyOf(adjacencyList2.neighbourIds, adjacencyList2.
            getSize()));
        IntArrayList intersections = adjacencyList1.getIntersection(listToIntersect, (short) -1);
        int[] expectedNeighbours = {1, 14, 34, 54, 89};
        Assert.assertArrayEquals(expectedNeighbours, intersections.toArray());
    }
}
