/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

/**
 *
 * @author ecoquery
 */
public class Where implements SQLRenderer {

    private final SQLBooleanValue _expr;
    
    public Where(SQLBooleanValue expr) {
        this._expr = expr;
    }

    public SQLBooleanValue getExpr() {
        return _expr;
    }
    
    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append("WHERE ");
        _expr.buildSQLQuery(output);
    }

}
