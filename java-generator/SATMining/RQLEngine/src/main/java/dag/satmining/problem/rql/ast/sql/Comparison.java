/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/sql/Comparison.java

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
