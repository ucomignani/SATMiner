/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/SQLBinOpValue.java

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
