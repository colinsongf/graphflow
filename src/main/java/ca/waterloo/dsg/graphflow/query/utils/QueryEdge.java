package ca.waterloo.dsg.graphflow.query.utils;

/**
 * Represents an edge in the {@code QueryGraph}
 */
public class QueryEdge {
    public String toVariable;
    public String fromVariable;

    public QueryEdge() { }

    public QueryEdge(String fromVariable, String toVariable) {
        this.fromVariable = fromVariable;
        this.toVariable = toVariable;
    }

    @Override
    public boolean equals(Object edge) {
        QueryEdge compareObject = (QueryEdge) edge;
        return this.fromVariable == compareObject.fromVariable && this.toVariable ==
            compareObject.toVariable;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (this.fromVariable != null? this.fromVariable.hashCode(): 0);
        hash += (this.toVariable != null? this.toVariable.hashCode(): 0);
        return hash;
    }

    public String toString() {
        return this.fromVariable + "->" + this.toVariable;
    }
}
