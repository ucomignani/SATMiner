package dag.satmining.problem.rql.ast.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaseNoValue extends SQLValue {

	private List<When> _whens = new ArrayList<CaseNoValue.When>();
	private SQLValue _else;

	private class When {
		private SQLBooleanValue _condition;
		private SQLValue _value;

		public When(SQLBooleanValue condition, SQLValue value) {
			_condition = condition;
			_value = value;
		}
	}

	public void addWhen(SQLBooleanValue condition, SQLValue value) {
		_whens.add(new When(condition, value));
	}

	public void setElse(SQLValue value) {
		_else = value;
	}

	@Override
	public void buildSQLQuery(StringBuilder output) {
		output.append("(CASE");
		for (When when : _whens) {
			output.append(" WHEN ");
			when._condition.buildSQLQuery(output);
			output.append(" THEN ");
			when._value.buildSQLQuery(output);
		}
		if (_else != null) {
			output.append(" ELSE ");
			_else.buildSQLQuery(output);
		}
		output.append(" END)");
	}

	@Override
	public void setupEval(Map<String, Integer> tupleIdx,
			Map<String, Integer>[] tupleSchemas) {
		for (When when : _whens) {
			when._condition.setupEval(tupleIdx, tupleSchemas);
			when._value.setupEval(tupleIdx, tupleSchemas);
		}
		if (_else != null) {
			_else.setupEval(tupleIdx, tupleSchemas);
		}
	}

	@Override
	public String eval(Tuple[] tuples) {
		for (When when : _whens) {
			if (when._condition.eval(tuples)) {
				return when._value.eval(tuples);
			}
		}
		if (_else != null) {
			return _else.eval(tuples);
		}
		return null;
	}
}
