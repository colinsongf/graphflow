package ca.waterloo.dsg.graphflow.query.operator.udf;

import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Subgraph;

import java.util.List;

/**
 * A UDF to execute when a new subgraph in a registered continuous subgraph trigger emerges or gets
 * deleted. An example of such query:
 * CONTINUOUSLY MATCH (a)->(b) ACTION UDF ca.uwaterloo.Count IN 'udf.jar';
 */
public abstract class UDFAction {

    /**
     * The evaluate method that gets executed on the list of {@link Subgraph}s that emerged or
     * deleted of continuous queries.
     */
    public abstract void evaluate(List<Subgraph> subgraphs);
}
