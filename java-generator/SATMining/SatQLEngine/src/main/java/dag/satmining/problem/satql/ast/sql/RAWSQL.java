package dag.satmining.problem.satql.ast.sql;

public abstract class RAWSQL implements SQLRenderer {

    private String _expression;

    public RAWSQL(String parsed, boolean fromParsing) {
        if (fromParsing) {
            parsed = parsed.trim();
            // remove first ( '{' ) and last ( '}' ) characters
            parsed = parsed.substring(1, parsed.length() - 1);
            parsed = parsed.replace("{{", "{").replace("}}", "}");
        }
        _expression = parsed;
    }
    
    public RAWSQL(String expression) {
        this(expression,false);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_expression == null) ? 0 : _expression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RAWSQL other = (RAWSQL) obj;
        if (_expression == null) {
            if (other._expression != null)
                return false;
        } else if (!_expression.equals(other._expression))
            return false;
        return true;
    }

}
