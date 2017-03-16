package ca.waterloo.dsg.graphflow.graph.serde;

import java.io.File;

/**
 * Base serialization and deserialization helper class used for constructing file names.
 */
public class BaseSerDeHelper {

    private static final String FILE_NAME_TEMPLATE = "graphflow_%s.data";
    private static final String MAIN_FILE_TEMPLATE = "%s_main";
    private static final String BLOCK_FILE_TEMPLATE = "%s_%s_block_%s";
    private static final String METADATA_FILE_TEMPLATE = "%s_%s_metadata";
    String ioDirectoryPath;

    /**
     * Constructs the full path to the main file of the given {@code className}.
     *
     * @param className The class name prefix.
     * @return The full path to the main file.
     */
    public String getMainFilePath(String className) {
        return ioDirectoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(MAIN_FILE_TEMPLATE, className));
    }

    /**
     * Constructs the full path to the metadata file of the given {@code objectName} and
     * {@code className}.
     *
     * @param className The class name prefix.
     * @param objectName The objectName prefix.
     * @return The full path to the metadata file.
     */
    public String getMetadataFilePath(String className, String objectName) {
        return ioDirectoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(METADATA_FILE_TEMPLATE, className, objectName));
    }

    /**
     * Constructs the full path to the block file of the given {@code blockIndex},
     * {@code objectName} and {@code className}.
     *
     * @param className The class name prefix.
     * @param objectName The objectName prefix.
     * @param blockIndex The block index
     * @return The full path to the block file.
     */
    public String getBlockFilePath(String className, String objectName, int blockIndex) {
        return ioDirectoryPath + File.separator + String.format(FILE_NAME_TEMPLATE,
            String.format(BLOCK_FILE_TEMPLATE, className, objectName, blockIndex));
    }
}
