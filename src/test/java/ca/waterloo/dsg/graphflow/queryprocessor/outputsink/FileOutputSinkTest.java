package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

import ca.waterloo.dsg.graphflow.util.IntArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

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

  @After
  public void tearDown() throws Exception {

  }


  @Test
  public void append() throws Exception {
    IntArrayList[] test = new IntArrayList[1];
    IntArrayList testArrayList = new IntArrayList();
    int[] testArray = {1, 2, 3, 4, 5, 6};
    testArrayList.addAll(testArray);
    test[0] = testArrayList;
    outputSink.append(test);
  }

}
