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

    private static String FILENAME = "test.out";
    // Temporary folder to do write and read operations on files. This is automatically
    // created and deleted by JUnit.
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private FileOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        outputSink = new FileOutputSink(tempFolder.getRoot(), FILENAME);
    }

    /**
     * Tests writing output to a file.
     */
    @Test
    public void testAppend() throws Exception {
        int[][] test = new int[1][];
        int[] testArray = {1, 2, 3, 4, 5, 6};
        test[0] = testArray;
        // Write the output.
        outputSink.append(MatchQueryResultType.MATCHED, test);
        // Read the output from the file and test the output.
        BufferedReader br = new BufferedReader(new FileReader(tempFolder.getRoot()
            .getAbsolutePath() + File.separator + FILENAME));
        Assert.assertTrue(br.readLine().equals("Output type: " + MatchQueryResultType.MATCHED));
        Assert.assertTrue(br.readLine().equals(Arrays.toString(testArray)));
    }
}
