package ca.waterloo.dsg.graphflow.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public enum Type {
    BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING;

    /**
     * Converts the {@code String} type value to one of the Enum Type values: {BYTE, SHORT, INT,
     * LONG, FLOAT, DOUBLE, BOOLEAN, STRING}.
     *
     * @param type The {@code String} type value to be converted to an enum value.
     * @return The type as one of the enum values of Type.
     * @throws IllegalArgumentException if {@code type} is, ignoring case, not in {"BYTE", "SHORT",
     * "INT", "LONG", "FLOAT", "DOUBLE", "BOOLEAN", "STRING"}.
     */
    public static Type convert(String type) {
        type = type.toUpperCase();
        if (type.matches(BYTE.name())) {
            return BYTE;
        } else if (type.matches(SHORT.name())) {
            return SHORT;
        } else if (type.matches(INT.name())) {
            return INT;
        } else if (type.matches(LONG.name())) {
            return LONG;
        } else if (type.matches(FLOAT.name())) {
            return FLOAT;
        } else if (type.matches(DOUBLE.name())) {
            return DOUBLE;
        } else if (type.matches(BOOLEAN.name())) {
            return BOOLEAN;
        } else if (type.matches(STRING.name())) {
            return STRING;
        }

        throw new IllegalArgumentException("The type " + type + " is not supported.");
    }

    /**
     * The functions checks if the {@code String} value matches the assigned {@code type} if casted.
     *
     * @param type The type assigned to the value.
     * @param value The value as a {@code String} presentation.
     * @throws IllegalArgumentException if the check fails and the {@code type} and {@code value}
     * do not match, or if the {@code type} is not one of the supported ones.
     */
    public static void assertValueCanBeParsedAsGivenType(String type, String value) {
        Type valueType = convert(type);
        try {
            if (valueType == BYTE) {
                Byte.parseByte(value);
            } else if (valueType == SHORT) {
                Short.parseShort(value);
            } else if (valueType == INT) {
                Integer.parseInt(value);
            } else if (valueType == LONG) {
                Long.parseLong(value);
            } else if (valueType == FLOAT) {
                Float.parseFloat(value);
            } else if (valueType == DOUBLE) {
                Double.parseDouble(value);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value " + value + " can not be parsed as " +
                type);
        }
    }

    /**
     * @param type The type of the passed values.
     * @param value The {@code String} value which gets converted to {@code type} when comparing.
     * @param otherValue The value to compare to which gets converted to {@code type} when
     * comparing.
     * @throws NumberFormatException if parsing of {@code value} or {@code otherValue} fails.
     * @throws NullPointerException if the {@code value} or {@code otherValue} is {@code null}.
     * @throws IllegalArgumentException if the type is not one of the Enum Type values: {BYTE,
     * SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN, STRING}.
     */
    public static boolean equals(Type type, String value, Object otherValue) {
        if (type == BYTE) {
            return Byte.parseByte(value) == (byte) otherValue;
        } else if (type == BOOLEAN) {
            return Boolean.parseBoolean(value) == (boolean) otherValue;
        } else if  (type == SHORT) {
            return Short.parseShort(value) == (short) otherValue;
        } else if  (type == INT) {
            return Integer.parseInt(value) == (int) otherValue;
        } else if  (type == LONG) {
            return Long.parseLong(value) == (long) otherValue;
        } else if  (type == FLOAT) {
            return Float.parseFloat(value) == (float) otherValue;
        } else if  (type == DOUBLE) {
            return Double.parseDouble(value) == (double) otherValue;
        } else if (type == STRING) {
            return value.equals(otherValue);
        }

        throw new IllegalArgumentException("The type " + type + " is not supported");
    }

    /**
     * @param type One of the Enum Type values: {BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN,
     * STRING}.
     * @return The number of bytes required to store the {@code type}.
     * @throws IllegalArgumentException if the type passed is not in {BYTE, SHORT, INT, LONG,
     * FLOAT, DOUBLE, BOOLEAN}
     */
    public static int getNumberOfBytes(Type type) {
        if (type == BOOLEAN || type == BYTE) {
            return 1;
        } else if (type == SHORT) {
            return 2;
        } else if (type == INT || type == FLOAT) {
            return 4;
        } else if (type == LONG || type == DOUBLE) {
            return 8;
        }

        throw new IllegalArgumentException("The number of bytes for String is not fixed");
    }

    /**
     * @return the {@code byte[]} value passed as an Object after casting it deserializing it
     * based on the proper {@code Type}.
     *
     * @param type the type of the passed value.
     * @param value the value to deserialize and return as byte array.
     * @return The value once converted as an Object
     * @throws IllegalArgumentException if the type passed is not in {BYTE, SHORT, INT, LONG,
     * FLOAT, DOUBLE, BOOLEAN}
     */
    public static Object getValue(Type type, byte[] value) {
        if (type == BYTE || type == SHORT || type == INT || type == LONG) {
            return getIntegerNumber(type, value);
        } else if (type == FLOAT || type == DOUBLE) {
            return getFloatPointNumber(type, value);
        } else if (type == BOOLEAN) {
            return getBoolean(value);
        } else if (type == STRING) {
            return getString(value);
        }

        throw new IllegalArgumentException("The type " + type + " is not supported");
    }

    /**
     * @return the {@code short} key, and {@code String} value passed as a byte array after
     * casting the value to the proper {@code Type}.
     *
     * @param key the {@code short} key of the key-value pair.
     * @param type the {@code Type} of the value.
     * @param value the {@code String} value of the key-value pair.
     * @throws NumberFormatException if parsing of {@code value} fails.
     * @throws NullPointerException if {@code value} is {@code null}.
     * @throws IllegalArgumentException if the type passed is not in {BYTE, SHORT, INT, LONG,
     * FLOAT, DOUBLE, BOOLEAN}
     */
    public static byte[] getKeyValueAsByteArray(short key, Type type, String value) {
        byte[] keyValue = allocate(type, value);
        // set the key and value in byte[] keyValue.
        keyValue[0] = (byte) (key >> 8);
        keyValue[1] = (byte) key;
        setValue(keyValue, type, value);

        return keyValue;
    }

    private static byte[] allocate(Type type, String value) {
        if (type == BYTE || type == BOOLEAN) {
            return new byte[3];
        } else if (type == SHORT) {
            return new byte[4];
        } else if (type == INT || type == FLOAT) {
            return new byte[6];
        } else if (type == LONG || type == DOUBLE) {
            return new byte[10];
        } else if (type == STRING) {
            byte[] valueAsBytes = value.getBytes(StandardCharsets.UTF_8);
            return new byte[6 + valueAsBytes.length];
        }

        throw new IllegalArgumentException("The type " + type + " is not supported");
    }

    private static void setValue(byte[] keyValue, Type type, String value) {
        if (type == BYTE || type == SHORT || type == INT || type == LONG) {
            setIntegerNumber(keyValue, type, value);
        } else if (type == FLOAT || type == DOUBLE) {
            setFloatPointNumber(keyValue, type, value);
        } else if (type == BOOLEAN) {
            setBoolean(keyValue, value);
        } else if (type == STRING) {
            setString(keyValue, value);
        }
    }

    private static Object getIntegerNumber(Type type, byte[] value) {
        long valueL = 0;
        switch (type) {
            case LONG:
                valueL |= ((long) value[7]) << 56 | ((long) value[6]) << 48 |
                    ((long) value[5]) << 40 | (long) value[4] << 32;
            case INT:
                valueL |= ((long) value[3]) << 24 | ((long) value[2]) << 16;
            case SHORT:
                valueL |= ((long) value[1]) << 8;
            case BYTE:
                valueL |= (long) value[0];
        }
        return valueL;
    }

    private static void setIntegerNumber(byte[] keyValue, Type type, String value) {
        long valueL = Long.parseLong(value);
        switch (type) {
            case LONG:
                keyValue[9] = (byte) (valueL >> 56);
                keyValue[8] = (byte) (valueL >> 48);
                keyValue[7] = (byte) (valueL >> 40);
                keyValue[6] = (byte) (valueL >> 32);
            case INT:
                keyValue[5] = (byte) (valueL >> 24);
                keyValue[4] = (byte) (valueL >> 16);
            case SHORT:
                keyValue[3] = (byte) (valueL >> 8);
            case BYTE:
                keyValue[2] = (byte) valueL;
        }
    }

    private static Object getFloatPointNumber(Type type, byte[] value) {
        if (type == FLOAT) {
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        } else { // (type == DOUBLE)
            return ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        }
    }

    private static void setFloatPointNumber(byte[] keyValue, Type type, String value) {
        byte[] valueF;
        int numBytes;
        if (type == FLOAT) {
            numBytes = 4;
            valueF = ByteBuffer.allocate(numBytes).putFloat(Float.parseFloat(value)).order(
                ByteOrder.LITTLE_ENDIAN).array();
        } else { // (type == DOUBLE)
            numBytes = 8;
            valueF = ByteBuffer.allocate(numBytes).putDouble(Double.parseDouble(value)).order
                (ByteOrder.LITTLE_ENDIAN).array();
        }

        System.arraycopy(valueF, 0, keyValue, 2, numBytes);
    }

    private static boolean getBoolean(byte[] value) {
        return value[0] == 1;
    }

    private static void setBoolean(byte[] keyValue, String value) {
        if (Boolean.parseBoolean(value)) {
            keyValue[2] = 1; // true
        } else {
            keyValue[2] = 0; // false
        }
    }

    private static String getString(byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }

    private static void setString(byte[] keyValue, String value) {
        byte[] valueAsBytes = value.getBytes(StandardCharsets.UTF_8);
        keyValue[2] = (byte) (valueAsBytes.length >> 24);
        keyValue[3] = (byte) (valueAsBytes.length >> 16);
        keyValue[4] = (byte) (valueAsBytes.length >> 8);
        keyValue[5] = (byte) valueAsBytes.length;
        System.arraycopy(valueAsBytes, 0, keyValue, 6, valueAsBytes.length);
    }
}
