package ca.waterloo.dsg.graphflow.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Util {

    public static double getElapsedTimeInMicro(long beginTime) {
        return (System.nanoTime() - beginTime) / 1000000.0;
    }

    public static ObjectOutputStream constructObjectOutputStream(String outputFilePath) throws
        IOException {
        return new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
            outputFilePath)));
    }

    public static ObjectInputStream constructObjectInputStream(String inputFilePath) throws
        IOException {
        return new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFilePath)));
    }
}
