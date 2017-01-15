package ca.waterloo.dsg.graphflow.util;

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
     * do not match.
     */
    public static void assertValueCanBeParsedAsGivenType(String type, String value) {
        type = type.toUpperCase();
        try {
            if (type.matches(BYTE.name())) {
                Byte.parseByte(value);
            } else if (type.matches(SHORT.name())) {
                Short.parseShort(value);
            } else if (type.matches(INT.name())) {
                Integer.parseInt(value);
            } else if (type.matches(LONG.name())) {
                Long.parseLong(value);
            } else if (type.matches(FLOAT.name())) {
                Float.parseFloat(value);
            } else if (type.matches(DOUBLE.name())) {
                Double.parseDouble(value);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value " + value + " can not be parsed as " +
                type);
        }
    }
}
