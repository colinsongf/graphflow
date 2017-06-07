package ca.waterloo.dsg.graphflow.query.operator.udf;

import ca.waterloo.dsg.graphflow.query.operator.UDFSink;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Given the path to the jar file and the name of the class that extends {@link UDFAction}, loads
 * the class and instantiates an object of the udf class used in the {@link UDFSink} operator.
 */
public class UDFResolver {

    /**
     * @param pathToJar The absolute path to the jar file containing the user's {@link UDFAction}
     * class.
     * @param udfClassName The qualified class name for the udf in the jar file.
     *
     * @throws IOException when the jar file cannot be read or accessed.
     * @throws ClassNotFoundException when the class specified is not found in the specified jar.
     * @throws InstantiationException When the class loaded from the jar fails to be instantiated.
     * @throws IllegalAccessException when the application has no access to the definition of the
     * specified class.
     */
    public static UDFAction getUDFObject(String pathToJar, String udfClassName) throws IOException,
        ClassNotFoundException, IllegalAccessException, InstantiationException {
        File file = new File(pathToJar);
        URL[] urls = {new URL("jar:file:" + file.getAbsolutePath() + "!/")};
        URLClassLoader classLoader = URLClassLoader.newInstance(urls);
        @SuppressWarnings("rawtypes")
        Class classLoaded = classLoader.loadClass(udfClassName);
        return (UDFAction) classLoaded.newInstance();
    }
}
