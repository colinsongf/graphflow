package ca.waterloo.dsg.graphflow.graph.serde;

import java.io.File;

/**
 * Base serialization and deserialization helper class. Contains common methods for constructing
 * file names used to write serialized data.
 */
public class SerDeUtils {

    private static final String FILE_NAME_TEMPLATE = "graphflow_%s.data";
    private static final String MAIN_FILE_TEMPLATE = "%s_main";
    private static final String BLOCK_FILE_TEMPLATE = "%s_%s_block_%s";
    private static final String METADATA_FILE_TEMPLATE = "%s_%s_metadata";

    /**
     * Constructs the full path to the main file of the given {@code className}.
     *
     * @param directoryPath The path of the directory where serialized data is stored.
     * @param className The name of the class whose objects are being serialized.
     *
     * @return The full path to the main file.
     */
    public static String getMainFilePath(String directoryPath, String className) {
        return directoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(MAIN_FILE_TEMPLATE, className));
    }

    /**
     * Constructs the full path to the metadata file of the given {@code arrayName} and
     * {@code className}.
     *
     * @param directoryPath The path of the directory where serialized data is stored.
     * @param className The name of the class whose objects are being serialized.
     * @param arrayName The name of the array being serialized in parallel.
     *
     * @return The full path to the metadata file.
     */
    public static String getMetadataFilePath(String directoryPath, String className,
        String arrayName) {
        return directoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(METADATA_FILE_TEMPLATE, className, arrayName));
    }

    /**
     * Constructs the full path to the block file of the given {@code blockIndex},
     * {@code arrayName} and {@code className}.
     *
     * @param directoryPath The path of the directory where serialized data is stored.
     * @param className The name of the class whose objects are being serialized.
     * @param arrayName The name of the array being serialized in parallel.
     * @param blockIndex The block index of the array being serialized.
     *
     * @return The full path to the block file.
     */
    public static String getArrayBlockFilePath(String directoryPath, String className,
        String arrayName, int blockIndex) {
        return directoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(BLOCK_FILE_TEMPLATE, className, arrayName, blockIndex));
    }
}
