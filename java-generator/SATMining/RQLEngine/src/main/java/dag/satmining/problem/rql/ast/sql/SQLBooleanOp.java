/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast.sql;

import java.util.Map;

import dag.satmining.utils.UnreachableException;

/**
 * 
 * @author ecoquery
 */
public final class SQLBooleanOp extends SQLBooleanValue {
	private final Op _op;
	private final SQLBooleanValue _a;
	private final SQLBooleanValue _b;

	private SQLBooleanOp(Op op, SQLBooleanValue a, SQLBooleanValue b) {
		this._op = op;
		this._a = a;
		this._b = b;
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append("(");
		if (_b != null) {
			_a.buildSQLQuery(output);
			output.append(_op.sqlRepr());
			_b.buildSQLQuery(output);
		} else {
			output.append(_op.sqlRepr());
			_a.buildSQLQuery(output);
		}
		output.append(")");
	}

	public enum Op {

		And, Or, Not;

		public String sqlRepr() {
			switch (this) {
			case And:
				return " AND ";
			case Or:
				return " OR ";
			default:
				return "NOT ";
			}
		}

		public boolean eval(boolean a, boolean b) {
			switch (this) {
			case And:
				return a && b;
			case Or:
				return a || b;
			case Not:
				return !a;
			default:
				throw new UnreachableException();
			}
		}
	}

	public static SQLBooleanOp and(SQLBooleanValue a, SQLBooleanValue b) {
		return new SQLBooleanOp(Op.And, a, b);
	}

	public static SQLBooleanOp or(SQLBooleanValue a, SQLBooleanValue b) {
		return new SQLBooleanOp(Op.Or, a, b);
	}

	public static SQLBooleanOp not(SQLBooleanValue a) {
		return new SQLBooleanOp(Op.Not, a, null);
	}

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
		_a.setupEval(tupleIdx, tupleSchemas);
		if (_b != null) {
			_b.setupEval(tupleIdx, tupleSchemas);
		}
	}

	@Override
	public boolean eval(Tuple[]tuples) {
		return _op.eval(_a.eval(tuples), _b == null ? false : _b.eval(tuples));
	}
}
