package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests the FileOutputSink class
 */
public class FileOutputSinkTest {

    FileOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        String outputDir = "src/test/Fixtures/generated";
        String name = "test.out";
        outputSink = new FileOutputSink(new File(outputDir), name);
    }

    @Test
    public void testAppend() throws Exception {
        int[][] test = new int[1][];
        int[] testArray = {1, 2, 3, 4, 5, 6};
        test[0] = testArray;
        outputSink.append(test);
    }
}
