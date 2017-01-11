package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.ExistsForTesting;
import ca.waterloo.dsg.graphflow.util.IndexedKeyValueByteArrays;
import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the assigned and recycled edge IDs and edge properties of the Graph.
 */
public class EdgeStore {

    private static final int INITIAL_CAPACITY = 2;
    private final int MAX_EDGES_PER_BUCKET = 8;
    private final int MAX_BUCKETS_PER_PARTITION = 1000000;

    private long nextIDNeverYetAssigned = 0;
    private byte nextBucketOffset = 0;
    private int nextBucketId = 0;
    private int nextPartitionId = 0;

    private long[] recycledIds;
    private int recycledIdsSize = 0;

    @PackagePrivateForTesting
    byte[][][] edgePropertyData;
    @PackagePrivateForTesting
    int[][][] edgePropertyDataOffsets;

    /**
     * Creates {@link EdgeStore} with default capacity.
     */
    public EdgeStore() {
        edgePropertyData = new byte[INITIAL_CAPACITY][][];
        edgePropertyDataOffsets = new int[INITIAL_CAPACITY][][];
        recycledIds = new long[INITIAL_CAPACITY];
    }

    /**
     * Generates the next Id to assign to edge added. Sets the properties of the edge in the
     * edgestore based on the Id.
     *
     * @param properties The {@code short} and {@code String} key value pairs representing the
     * properties.
     * @return The {@code long} id assigned to the edge added.
     */
    public long addEdge(HashMap<Short, String> properties) {
        long edgeId = getNextIdToAssign();
        setProperties(edgeId, properties);
        return edgeId;
    }

    /**
     * Returns the next ID to assign to an edge. If there are recycled IDs, which is an ID
     * previously assigned to an edge that was deleted, the last added ID to the recycled IDs array
     * is returned. Else, the {@code long} ID bytes are as follows: (1) The 3 most significant
     * bytes are those of the partition ID. (2) The next 4 bytes are the bucket ID, where each
     * partition is made of a max number of buckets, and (3) the last byte presents the maximum
     * number of edges per bucket.
     */
    public long getNextIdToAssign() {
        long nextIDToAssign;
        if (recycledIdsSize > 0) {
            nextIDToAssign = recycledIds[--recycledIdsSize];
        } else {
            nextIDToAssign = nextIDNeverYetAssigned;
            incrementNextIDNeverYetAssigned();
        }
        return nextIDToAssign;
    }

    /**
     * Sets the bytes array value at {@code edgeId} with a list of {@code short} key, and {@code
     * String} value pairs representing the properties of the edge with {@code edgeId}.
     *
     * @param edgeId The id of the edge.
     * @param properties The {@code short} key and {@code String} value pair properties of the edge.
     */
    public void setProperties(long edgeId, HashMap<Short, String> properties) {
        int partitionId = (int) (edgeId >> 40);
        int bucketId = (int) (edgeId >> 8);
        byte bucketOffset = (byte) edgeId;

        resizeIfNecessary(partitionId, bucketId);

        byte[] propertiesAsBytes = new byte[0];
        if (null != properties) {
            for (Map.Entry<Short, String> property : properties.entrySet()) {
                byte[] propertyAsBytes = IndexedKeyValueByteArrays.getKeyValueAsByteArray(property.
                    getKey(), property.getValue());
                propertiesAsBytes = Arrays.copyOf(propertiesAsBytes, propertiesAsBytes.length +
                    propertyAsBytes.length);
                System.arraycopy(propertyAsBytes, 0, propertiesAsBytes, propertiesAsBytes.length -
                    propertyAsBytes.length, propertyAsBytes.length);
            }
        }

        if (null == edgePropertyData[partitionId][bucketId]) {
            edgePropertyData[partitionId][bucketId] = new byte[0];
        }

        int dataOffsetStart = edgePropertyDataOffsets[partitionId][bucketId][bucketOffset];
        int dataOffsetEnd = -1;
        if (bucketOffset < MAX_EDGES_PER_BUCKET - 1) {
            dataOffsetEnd = edgePropertyDataOffsets[partitionId][bucketId][bucketOffset + 1] - 1;
        }

        updatePropertyDataAndOffsets(propertiesAsBytes, dataOffsetStart, dataOffsetEnd,
            partitionId, bucketId, bucketOffset);
    }

    private void updatePropertyDataAndOffsets (byte[] properties, int dataOffsetStart,
        int dataOffsetEnd, int partitionId, int bucketId, int bucketOffset) {
        if (properties.length == 0) { // no properties, only set the next bucketOffset.
            if (bucketOffset < MAX_EDGES_PER_BUCKET - 1) {
                edgePropertyDataOffsets[partitionId][bucketId][bucketOffset + 1] = edgePropertyData[
                    partitionId][bucketId].length;
            }
            return;
        }
        if (dataOffsetEnd < 0) { // First time assigning properties for the edgeId or offset is max.
            edgePropertyData[partitionId][bucketId] = Arrays.copyOf(edgePropertyData[partitionId][
                bucketId], dataOffsetStart + properties.length);
            System.arraycopy(properties, 0, edgePropertyData[partitionId][bucketId],
                dataOffsetStart, properties.length);
            if (bucketOffset < MAX_EDGES_PER_BUCKET - 1) {
                edgePropertyDataOffsets[partitionId][bucketId][bucketOffset + 1] = edgePropertyData[
                    partitionId][bucketId].length;
            }
        } else { // recycled edgeId, need to insert the new properties.
            int lastIndex = edgePropertyData[partitionId][bucketId].length - 1;
            int diffInOffset = properties.length - (dataOffsetEnd - dataOffsetStart + 1);

            byte[] oldData = null;
            if (diffInOffset < 0) {
                oldData = new byte[edgePropertyData[partitionId][bucketId].length];
                System.arraycopy(edgePropertyData[partitionId][bucketId], 0, oldData, 0,
                    edgePropertyData[partitionId][bucketId].length);
            }

            // copy the properties of the edges with bigger Id and in the same bucket.
            edgePropertyData[partitionId][bucketId] = Arrays.copyOf(edgePropertyData[partitionId][
                bucketId], dataOffsetStart + properties.length + edgePropertyData[partitionId][
                    bucketId].length - dataOffsetEnd - 1);

            // copy the properties of the edges with bigger Id and in the same bucket.
            if (diffInOffset > 0) { // shifting to the right, start from the end.
                for (int i = lastIndex; i > dataOffsetEnd; i--) {
                    edgePropertyData[partitionId][bucketId][i + diffInOffset] = edgePropertyData[
                        partitionId][bucketId][i];
                }
            }
            else if (diffInOffset < 0) { // shifting to the left, start from the start.
                for (int i = dataOffsetEnd + 1; i < oldData.length; i++) {
                    edgePropertyData[partitionId][bucketId][i + diffInOffset] = oldData[i];
                }
            }

            // copy the properties of the edge.
            System.arraycopy(properties, 0, edgePropertyData[partitionId][bucketId],
                dataOffsetStart, properties.length);
            // update the offsets.
            for (int i = bucketOffset + 1; i < MAX_EDGES_PER_BUCKET; ++i) {
                if (edgePropertyDataOffsets[partitionId][bucketId][i] != 0) {
                    edgePropertyDataOffsets[partitionId][bucketId][i] += diffInOffset;
                }
            }
        }
    }

    /**
     * Adds the given ID to the recycled IDs array.
     *
     * @param id The {@code long} id to add to the recycled IDs array.
     */
    public void deleteEdge(long id) {
        recycledIds = ArrayUtils.resizeIfNecessary(recycledIds, recycledIdsSize + 1);
        recycledIds[recycledIdsSize++] = id;
    }

    private void incrementNextIDNeverYetAssigned() {
        if (nextBucketOffset < MAX_EDGES_PER_BUCKET - 1) {
            nextBucketOffset++;
        } else {
            nextBucketOffset = 0;
            if (nextBucketId < MAX_BUCKETS_PER_PARTITION - 1) {
                nextBucketId++;
            } else {
                nextBucketId = 0;
                nextPartitionId++;
            }
        }
        nextIDNeverYetAssigned = (((long) nextPartitionId) << 40) | (nextBucketId << 8) |
            nextBucketOffset;
    }

    private void resizeIfNecessary(int partitionId, int bucketId) {
        edgePropertyData = ArrayUtils.resizeIfNecessary(edgePropertyData, partitionId + 1);
        edgePropertyDataOffsets = ArrayUtils.resizeIfNecessary(edgePropertyDataOffsets,
            partitionId + 1);

        if (null == edgePropertyData[partitionId]) {
            edgePropertyData[partitionId] = new byte[0][];
        }
        if (null == edgePropertyDataOffsets[partitionId]) {
            edgePropertyDataOffsets[partitionId] = new int[0][];
        }

        edgePropertyData[partitionId] = ArrayUtils.resizeIfNecessary(edgePropertyData[partitionId],
            bucketId + 1);
        edgePropertyDataOffsets[partitionId] = ArrayUtils.resizeIfNecessary(
            edgePropertyDataOffsets[partitionId], bucketId + 1);

        if (null == edgePropertyDataOffsets[partitionId][bucketId]) {
            edgePropertyDataOffsets[partitionId][bucketId] = new int[MAX_EDGES_PER_BUCKET];
        }
    }

    @ExistsForTesting
    void setNextIDParams (int partitionID, int bucketID, byte bucketOffset) {
        this.nextPartitionId = partitionID;
        this.nextBucketId = bucketID;
        this.nextBucketOffset = bucketOffset;
    }
}
