package ca.waterloo.dsg.graphflow.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class to help with I/O operations.
 */
public class IOUtils {

    /**
     * Calculates the time difference between now and the time stored in {@code beginTime}.
     *
     * @param beginTime The start time.
     *
     * @return Time difference in milliseconds.
     */
    public static double getElapsedTimeInMillis(long beginTime) {
        return (System.nanoTime() - beginTime) / 1000000.0;
    }

    /**
     * Creates a {@link ObjectOutputStream} object from the given {@code outputFilePath}.
     *
     * @param outputFilePath The {@link String} path to the output file.
     *
     * @return An {@link ObjectOutputStream} object.
     */
    public static ObjectOutputStream constructObjectOutputStream(String outputFilePath) throws
        IOException {
        return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
            outputFilePath)));
    }

    /**
     * Creates a {@link ObjectInputStream} object from the given {@code inputFilePath}.
     *
     * @param inputFilePath The {@link String} path to the input file.
     *
     * @return An {@link ObjectInputStream} object.
     */
    public static ObjectInputStream constructObjectInputStream(String inputFilePath) throws
        IOException {
        return new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));
    }
}
