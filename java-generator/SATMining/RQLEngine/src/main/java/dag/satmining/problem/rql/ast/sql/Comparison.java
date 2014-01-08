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
public final class Comparison extends SQLBooleanValue {

	private final Op _op;
	private final SQLValue _a, _b;

	private Comparison(SQLValue a, Op op, SQLValue b) {
		this._a = a;
		this._b = b;
		this._op = op;
	}

	public static Comparison eq(SQLValue a, SQLValue b) {
		return new Comparison(a, Op.Eq, b);
	}
	
	public static Comparison lt(SQLValue a, SQLValue b) {
		return  new Comparison(a, Op.Lt, b);
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append("(");
		_a.buildSQLQuery(output);
		output.append(" ");
		output.append(_op.infixRepr());
		output.append(" ");
		_b.buildSQLQuery(output);
		output.append(")");
	}

	public enum Op {
		Eq, Lt;
		public String infixRepr() {
			switch (this) {
			case Lt:
				return "<";
			default:
				return "=";
			}
		}

		private static boolean le(String a, String b) {
			try {
				double a1 = Double.parseDouble(a);
				double b1 = Double.parseDouble(b);
				return a1 < b1;
			} catch (NumberFormatException e) {
				return a.compareTo(b) < 0;
			}
		}
		
		public boolean eval(String a, String b) {
			switch (this) {
			case Eq:
				return a != null && a.equals(b);
			case Lt:
				return a != null && b != null && le(a,b);
			default:
				return false;
			}
		}
	}

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
		_a.setupEval(tupleIdx, tupleSchemas);
		_b.setupEval(tupleIdx, tupleSchemas);
	}

	@Override
	public boolean eval(Tuple[] tuples) {
		return _op.eval(_a.eval(tuples), _b.eval(tuples));
	}
}
