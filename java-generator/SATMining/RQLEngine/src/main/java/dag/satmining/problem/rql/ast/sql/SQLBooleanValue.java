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
public abstract class SQLBooleanValue implements SQLRenderer {

	public abstract void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas);

	public abstract boolean eval(Tuple[] tuples);
	
}
