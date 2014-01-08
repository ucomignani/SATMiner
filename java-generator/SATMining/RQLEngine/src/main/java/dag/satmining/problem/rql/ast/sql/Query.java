/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.sql.Connection;
import java.sql.SQLException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 
 * @author ecoquery
 */
public class Query extends FromExpression {

//	private final static Logger LOG = LoggerFactory
//			.getLogger(FromExpression.class);

	private Select _select;
	private From _from;
	private Where _where;

	public Query(Select select, From from, Where where) {
		this._select = select;
		this._from = from;
		this._where = where;
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append("(");
		buildSQL(output);
		output.append(")");
	}

	private void buildSQL(StringBuilder output) {
		_select.buildSQLQuery(output);
		output.append(" ");
		_from.buildSQLQuery(output);
		output.append(" ");
		_where.buildSQLQuery(output);
	}

	public String asSQL() {
		StringBuilder sb = new StringBuilder();
		buildSQL(sb);
		return sb.toString();
	}

	@Override
	public TupleFetcher getTupleFetcher(Connection c) throws SQLException {
		return new PreparedStatementWrapper(c, asSQL());
	}

}
