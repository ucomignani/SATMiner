/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author ecoquery
 */
public class NamedFromExpression extends FromExpression {

    private final String _name;
    private final FromExpression _expr;
    
    public NamedFromExpression(String name, FromExpression expr) {
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

	@Override
	public TupleFetcher getTupleFetcher(Connection c) throws SQLException, IOException {
		return _expr.getTupleFetcher(c);
	}
}
