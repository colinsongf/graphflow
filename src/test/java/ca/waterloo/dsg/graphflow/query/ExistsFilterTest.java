package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.operator.FileOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.junit.Test;

/**
 * Tests the {@link FileOutputSink} class.
 */
public class ExistsFilterTest {

    /**
     * Tests writing output to a file.
     */
    @Test
    public void testFilter() {
        GraphDBState.reset();
        String createQuery = "CREATE " +
            "(1:P)-[:T]->(2:P)," +
            "(2:P)-[:M]->(3:P)," +
            "(1:P)-[:X]->(3:P)," +
            "(2:P)-[:T]->(4:P)," +
            "(4:P)-[:M]->(5:P);";
        TestUtils.initializeGraphPermanentlyWithProperties(createQuery);

        String matchQuery = "MATCH (a)->(b),(b)->(c) where not exists((a)-[:X]->(c));";
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(matchQuery);
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery, outputSink).
            plan()).execute(Graph.getInstance());
        System.out.println(outputSink.toString());
    }
}
