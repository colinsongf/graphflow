package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.ExistsForTesting;
import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;
import ca.waterloo.dsg.graphflow.util.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

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
            for (Short property : properties.keySet()) {
                Type type = TypeAndPropertyKeyStore.getInstance().getPropertyType(property);
                byte[] propertyAsBytes = Type.getKeyValueAsByteArray(property, type, properties.get(
                    property));
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

    /**
     * Returns the {@code Short} key, and {@code String} value properties stored for an edge with
     * the given id. If the id provided is an id of a deleted edge, the properties of the deleted
     * edge are returned.
     *
     * @param edgeId The id of the edge.
     * @return {@code Short} key, and {@code String} value properties of edge for the given {@code
     * edgeId} given the id has been previously assigned to an edge.
     * @throws NoSuchElementException if the {@code edgeId} has never been assigned before.
     */
    public HashMap<Short, Object> getEdgeProperties(long edgeId) {
        if (edgeId >= nextIDNeverYetAssigned) {
            throw new NoSuchElementException("no edge with id " + edgeId);
        }

        int partitionId = (int) (edgeId >> 40);
        int bucketId = (int) (edgeId >> 8);
        byte bucketOffset = (byte) edgeId;

        int startOffset = edgePropertyDataOffsets[partitionId][bucketId][bucketOffset];
        int endOffset = edgePropertyDataOffsets[partitionId][bucketId][bucketOffset + 1] - 1;
        if (bucketOffset == MAX_EDGES_PER_BUCKET - 1) {
            endOffset = edgePropertyData[partitionId][bucketId].length;
        }

        HashMap<Short, Object> edgeProperties = new HashMap<>();
        if (startOffset == endOffset + 1) { // no properties
            return edgeProperties;
        }

        byte[] properties = new byte[endOffset - startOffset + 1];
        System.arraycopy(edgePropertyData[partitionId][bucketId], startOffset, properties, 0,
            endOffset - startOffset + 1);

        for (int i = 0; i < properties.length; ) {
            short property = (short) ((short)(properties[i] << 8) | ((short)properties[i+1]));
            Type type = TypeAndPropertyKeyStore.getInstance().getPropertyType(property);

            int length;
            int value_offset;
            if (type == Type.STRING) {
                length = (((int) properties[i + 2]) << 24) | (((int) properties[i + 3]) << 16) |
                    (((int) properties[i + 4]) << 8) | (int) properties[i + 5];
                value_offset = 6; // 2 bytes for short key + 4 for string length
            } else {
                length = Type.getNumberOfBytes(type);
                value_offset = 2; // 2 bytes for short key
            }

            byte[] valueAsByte = new byte[length];
            System.arraycopy(edgePropertyData[partitionId][bucketId], startOffset + value_offset,
                valueAsByte, 0, length);

            Object value = Type.getValue(type, valueAsByte);
            edgeProperties.put(property, value);

            i += (value_offset + length);
        }

        return edgeProperties;
    }

    /**
     * Returns the {@code Short} key, and {@code String} value properties stored for an edge with
     * the given id.
     *
     * @param edgeId The id of the edge.
     * @param properties The properties to check against those of the given {@code edgeId} and
     * see if they match.
     * @return whether the {@code properties} passed matches those of the edge at {@code edgeId}.
     * @throws NoSuchElementException if the {@code edgeId} has never been assigned before.
     */
    public boolean edgePropertiesMatches(long edgeId, HashMap<Short, String> properties) {
        if (edgeId >= nextIDNeverYetAssigned) {
            throw new NoSuchElementException("no edge with id " + edgeId);
        }

        if (null == properties || properties.size() == 0) {
            return true;
        }

        // CHANGE TO Type.equals(String val, Object obj, Type type)
        HashMap<Short, Object> edgeProperties = getEdgeProperties(edgeId);
        for(Short property: properties.keySet()) {
            Type type = TypeAndPropertyKeyStore.getInstance().getPropertyType(property);
            if (!Type.equals(type, properties.get(property), edgeProperties.get(property))) {
                return false;
            }
        }
        return true;
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
