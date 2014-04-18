package dag.satmining.problem.satql.ast.sql;

public final class SQLDelegatedValue extends SQLValue {
    
    private String _expression;
    
    public SQLDelegatedValue(String expression) {
        _expression = expression;
    }
    
    public String getExpression() {
        return _expression;
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append('(');
        output.append(getExpression());
        output.append(')');
    }

}
