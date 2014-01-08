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
public final class SQLTrue extends SQLBooleanValue {

    private static final SQLTrue INSTANCE = new SQLTrue();
    
    private SQLTrue(){}
    
    @Override
    public void buildSQLQuery(StringBuilder output) {
        output.append("0=0");
    }
    
    public static SQLTrue instance() {
        return INSTANCE;
    }

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {	
	}

	@Override
	public boolean eval(Tuple[] tuples) {
		return true;
	}
    
}
