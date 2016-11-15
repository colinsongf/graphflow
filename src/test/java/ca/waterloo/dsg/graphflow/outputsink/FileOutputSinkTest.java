package ca.waterloo.dsg.graphflow.outputsink;

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
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private FileOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        outputSink = new FileOutputSink(tempFolder.getRoot(), FILENAME);
    }

    @Test
    public void testAppend() throws Exception {
        int[][] test = new int[1][];
        int[] testArray = {1, 2, 3, 4, 5, 6};
        test[0] = testArray;
        outputSink.append(test);
        BufferedReader br = new BufferedReader(
            new FileReader(tempFolder.getRoot().getAbsolutePath() + File.separator + FILENAME));
        Assert.assertTrue(br.readLine().equals(Arrays.toString(testArray)));
    }
}
