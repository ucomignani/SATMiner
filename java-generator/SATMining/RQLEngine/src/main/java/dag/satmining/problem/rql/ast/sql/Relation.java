/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 
 * @author ecoquery
 */
public class Relation extends FromExpression {

//	private static final Logger LOG = LoggerFactory.getLogger(Relation.class);

	private final String _name;

	public Relation(String name) {
		this._name = name;
	}

	public String getName() {
		return _name;
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append(_name);
	}

	@Override
	public TupleFetcher getTupleFetcher(Connection c) throws SQLException, IOException {
		return new PreparedStatementWrapper(c, "SELECT * FROM "+_name);
	}
}
