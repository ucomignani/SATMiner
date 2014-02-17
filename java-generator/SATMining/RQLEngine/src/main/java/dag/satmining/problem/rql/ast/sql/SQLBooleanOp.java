/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/SQLBooleanOp.java

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
