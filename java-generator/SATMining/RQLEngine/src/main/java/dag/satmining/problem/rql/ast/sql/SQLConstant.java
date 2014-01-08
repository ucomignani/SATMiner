/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.util.Map;

/**
 * 
 * @author ecoquery
 */
public class SQLConstant extends SQLValue {
	private final String _data;

	public SQLConstant(String data) {
		this._data = data;
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append(_data);
	}

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
	}

	@Override
	public String eval(Tuple[] tuples) {
		return _data;
	}
}
