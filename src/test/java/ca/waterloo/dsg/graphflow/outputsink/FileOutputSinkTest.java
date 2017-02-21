package ca.waterloo.dsg.graphflow.outputsink;

import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;
import ca.waterloo.dsg.graphflow.query.operator.FileOutputSink;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.QueryOutputUtils;

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
    // Special JUnit defined temporary folder used to test IO operations on files. Requires
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
        MatchQueryOutput matchQueryOutput = new MatchQueryOutput();
        matchQueryOutput.vertexIds = new int[]{1, 2, 3, 4, 5, 6};
        matchQueryOutput.matchQueryResultType = MatchQueryResultType.MATCHED;
        // Write the output.
        outputSink.append(matchQueryOutput);
        // Read the output from the file and test the output.
        BufferedReader br = new BufferedReader(new FileReader(location));
        Assert.assertTrue(br.readLine().equals(QueryOutputUtils.getStringMatchQueryOutput(
            matchQueryOutput.vertexIds, matchQueryOutput.matchQueryResultType)));
    }
}
