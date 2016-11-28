package ca.waterloo.dsg.graphflow.outputsink;

import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Tests the {@code FileOutputSink} class.
 */
public class FileOutputSinkTest {

    // Special JUnit defined temporary folder used to test IO operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File location;
    private FileOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        String FILENAME = "test.out";
        location = temporaryFolder.newFile(FILENAME);
        outputSink = new FileOutputSink(location);
    }

    /**
     * Tests writing output to a file.
     */
    @Test
    public void testAppend() throws Exception {
        String output = Arrays.toString(new int[]{1, 2, 3, 4, 5, 6}) + ", " +
            MatchQueryResultType.MATCHED;
        // Write the output.
        outputSink.append(output);
        // Read the output from the file and test the output.
        BufferedReader br = new BufferedReader(new FileReader(location));
        Assert.assertTrue(br.readLine().equals(output));
    }
}
