package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Projection operator for projecting MATCH query outputs onto a subset of the variables.
 */
public class Projection extends AbstractDBOperator {

    private int[] projectedVertexIds;
    private List<Integer> vertexIndicesToProject;

    /**
     * Default constructor that sets the vertex and indices to project and the next operator for
     * this operator.
     *
     * @param nextOperator next operator that the projected outputs should be appended to.
     * @param vertexIndicesToProject indices that specify which of the vertices in the vertexIds
     * array of {@link MatchQueryOutput}s should be in the output of the projection.
     */
    public Projection(AbstractDBOperator nextOperator, List<Integer> vertexIndicesToProject) {
        super(nextOperator);
        this.vertexIndicesToProject = vertexIndicesToProject != null ? vertexIndicesToProject
            : new ArrayList<>();
        projectedVertexIds = new int[vertexIndicesToProject.size()];
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        for (int i = 0; i < vertexIndicesToProject.size(); ++i) {
            projectedVertexIds[i] = matchQueryOutput.vertexIds[vertexIndicesToProject.get(i)];
        }
        matchQueryOutput.vertexIds = projectedVertexIds;
        nextOperator.append(matchQueryOutput);
    }

    @Override
    public String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("Projection:\n");
        appendListAsCommaSeparatedString(stringBuilder, vertexIndicesToProject,
            "VertexIndicesToProject");
        return stringBuilder.toString();
    }
}
