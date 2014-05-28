package dag.satmining.problem.satql.ast.sql;

public class NamedFilterExpression  implements FromExpression {

    private final String _name;
    private final FromExpression _expr;
    
    public NamedFilterExpression(String name, FromExpression expr) {
        this._name = name;
        this._expr = expr;
    }

    public FromExpression getExpr() {
        return _expr;
    }
    
    public String getName() {
        return _name;
    }

    @Override
    public void buildSQLQueryNoName(StringBuilder output) {
        _expr.buildSQLQueryNoName(output);
    }
    
    @Override
    public void buildSQLQuery(StringBuilder output) {
        _expr.buildSQLQueryNoName(output);
        if (_name != null) {
            output.append(" ");
            output.append(_name);
        }
    }
}

