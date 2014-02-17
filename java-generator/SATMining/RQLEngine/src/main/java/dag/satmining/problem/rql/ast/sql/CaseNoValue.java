/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/CaseNoValue.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
