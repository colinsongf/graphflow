package ca.waterloodsg.activeg.models;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * Created by chathura on 10/2/16.
 */
public class GraphTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getInstance() throws Exception {
        String testFile = "src/test/java/ca/waterloodsg/activeg/models/Fixtures/graph.json";
        File file = new File(testFile);

        Graph g = Graph.getInstance(file);
        assertEquals(g.getVertices().size(),6);
        assertEquals(g.getEdges().size(),6);

    }

}