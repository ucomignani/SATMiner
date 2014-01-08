package dag.satmining.problem.rql.ast.sql;

import java.util.Map;

import dag.satmining.utils.UnreachableException;

public class SQLBinOpValue extends SQLValue {

	private final SQLValue _a, _b;
	private final Op _op;

	public SQLBinOpValue(Op op, SQLValue a, SQLValue b) {
		this._a = a;
		this._b = b;
		this._op = op;
	}

	public enum Op {
		Concat;

		public String opRepr() {
			switch (this) {
			case Concat:
				return "||";
			}
			throw new UnreachableException();
		}

		public String eval(String a, String b) {
			switch (this) {
			case Concat:
				return a + b;
			default:
				return null;
			}
		}
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append('(');
		_a.buildSQLQuery(output);
		output.append(' ');
		output.append(_op.opRepr());
		output.append(' ');
		_b.buildSQLQuery(output);
		output.append(')');
	}

	public static SQLBinOpValue concat(SQLValue a, SQLValue b) {
		return new SQLBinOpValue(Op.Concat, a, b);
	}

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
		_a.setupEval(tupleIdx, tupleSchemas);
		_b.setupEval(tupleIdx, tupleSchemas);
	}

	@Override
	public String eval(Tuple[] tuples) {
		return _op.eval(_a.eval(tuples), _b.eval(tuples));
	}

}
