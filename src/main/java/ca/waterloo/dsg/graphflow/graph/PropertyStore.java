package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Abstract property store class for the {@link EdgeStore} and {@link VertexPropertyStore} classes.
 */
abstract class PropertyStore {

    protected PropertyIterator propertyIterator = new PropertyIterator();

    protected byte[] serializeProperties(Map<Short, Pair<DataType, String>> properties) {
        byte[] propertiesAsBytes = new byte[0];
        if (null != properties && !properties.isEmpty()) {
            int index = 0;
            int propertiesLength = 0;
            byte[][] keyValueByteArrays = new byte[properties.size()][];
            for (Short key : properties.keySet()) {
                keyValueByteArrays[index] = DataType.serialize(properties.get(key).a, key,
                    properties.get(key).b);
                propertiesLength += keyValueByteArrays[index].length;
                index++;
            }

            propertiesAsBytes = new byte[propertiesLength];
            propertiesLength = 0;
            for (byte[] keyValueAsBytes : keyValueByteArrays) {
                System.arraycopy(keyValueAsBytes, 0, propertiesAsBytes, propertiesLength,
                    keyValueAsBytes.length);
                propertiesLength += keyValueAsBytes.length;
            }
        }
        return propertiesAsBytes;
    }

    protected Object getPropertyFromIterator(short key) {
        Pair<Short, Object> keyValue;
        while (propertyIterator.hasNext()) {
            keyValue = propertyIterator.next();
            if (key == keyValue.a) {
                return keyValue.b;
            }
        }
        return null;
    }

    /**
     * An iterator to iterate over a set of properties that are serialized as a byte array.
     * Classes that use this iterator should create an instance of this iterator and then call the
     * {@link #reset(byte[], int, int)} method to use it again without constructing.
     */
    protected static class PropertyIterator implements Iterator<Pair<Short, Object>> {

        private byte[] data;
        private int endIndex;
        private int currentIndex;

        protected PropertyIterator() {}

        /**
         * Resets the iterator.
         *
         * @param data byte array containing the properties.
         * @param startIndex start index of properties in {@link #data}.
         * @param endIndex end index of properties in {@link #data}.
         */
        protected void reset(byte[] data, int startIndex, int endIndex) {
            this.data = data;
            this.currentIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < endIndex;
        }

        @Override
        public Pair<Short, Object> next() {
            if (!hasNext()) {
                throw new NoSuchElementException("PropertyIterator has no more elements.");
            }
            short key = (short) ((short) (data[currentIndex] << 8) |
                ((short) data[currentIndex + 1]));
            DataType dataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(key);

            int length;
            int valueOffset;
            if (DataType.STRING == dataType) {
                length = (((int) data[currentIndex + 2]) << 24) |
                    (((int) data[currentIndex + 3]) << 16) |
                    (((int) data[currentIndex + 4]) << 8) |
                    (int) data[currentIndex + 5];
                // 2 bytes for short key + 4 for an int storing the length of
                // the String.
                valueOffset = 6;
            } else {
                length = DataType.getLength(dataType);
                // 2 bytes for short key. We do not store the lengths of data
                // types other than
                // Strings since they are fixed.
                valueOffset = 2;
            }

            Object value = DataType.deserialize(dataType, data, currentIndex + valueOffset, length);
            currentIndex += (valueOffset + length);
            return new Pair<>(key, value);
        }
    }
}
