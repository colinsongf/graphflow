package ca.waterloo.dsg.graphflow.query.operator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringJoiner;

/**
 * Tests the {@link FileOutputSink} class.
 */
public class FileOutputSinkTest {

    private static String FILENAME = "test.out";
    // Special JUnit defined temporary folder used to test I/O operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File location;
    private FileOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        location = temporaryFolder.newFile(FILENAME);
        outputSink = new FileOutputSink(location);
    }

    /**
     * Tests writing output to a file.
     */
    @Test
    public void testAppend() throws Exception {
        Object[] expectedResultArray = new Object[]{1, 2, 3, 4, 5, 6, "MATCHED"};
        StringJoiner joiner = new StringJoiner(" ");
        for (Object element : expectedResultArray) {
            joiner.add(element.toString());
        }
        // Write the output.
        outputSink.append(joiner.toString());
        // Read the output from the file and test the output.
        BufferedReader br = new BufferedReader(new FileReader(location));
        Assert.assertTrue(br.readLine().equals(joiner.toString()));
    }
}
