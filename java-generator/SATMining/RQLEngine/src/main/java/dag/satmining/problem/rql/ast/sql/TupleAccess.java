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
public class TupleAccess extends SQLValue {
    private final String _tupleName;
    private final String _attributeName;
    private int _tupleIdx;
    private int _attIdx;

    public TupleAccess(String tupleName, String attributeName) {
        this._tupleName = tupleName;
        this._attributeName = attributeName;
    }

    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append(_tupleName);
        output.append(".");
        output.append(_attributeName);
    }

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
		_tupleIdx = tupleIdx.get(_tupleName.toUpperCase());
		_attIdx = tupleSchemas[_tupleIdx].get(_attributeName.toUpperCase());
	}

	@Override
	public String eval(Tuple[]tuples) {
		return tuples[_tupleIdx].getValue(_attIdx);
	}
    
}
