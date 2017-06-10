package ca.waterloo.dsg.graphflow.query.structuredquery;

public class ExistsPredicate {

    private boolean isExists;
    private QueryRelation queryRelation;

    public ExistsPredicate(boolean isExists, QueryRelation queryRelation) {
        this.isExists = isExists;
        this.queryRelation = queryRelation;
    }

    public boolean isExists() {
        return isExists;
    }

    public QueryRelation getQueryRelation() {
        return queryRelation;
    }
}
