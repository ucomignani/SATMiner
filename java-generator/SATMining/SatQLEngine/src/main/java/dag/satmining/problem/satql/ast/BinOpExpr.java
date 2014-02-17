/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/BinOpExpr.java

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
package dag.satmining.problem.satql.ast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.satql.ast.intermediate.And;
import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.intermediate.Or;
import dag.satmining.utils.UnreachableException;

/**
 * 
 * @author ecoquery
 */
public final class BinOpExpr extends MiningExpression {

	private final Op _op;
	private final MiningExpression _a;
	private final MiningExpression _b;

	private BinOpExpr(Op op, MiningExpression a, MiningExpression b) {
		this._op = op;
		this._a = a;
		this._b = b;
	}

	@Override
	public MiningExpression pushDown(Quantifier q, AttributeVariable av,
			SchemaVariable s) {
		if (_a.getFreeAttVariables().contains(av)) {
			if (_b.getFreeAttVariables().contains(av)) {
				return new AttributeQuantifier(q, av, s, this);
			} else {
				return new BinOpExpr(_op, _a.pushDown(q, av, s), _b);
			}
		} else {
			if (_b.getFreeAttVariables().contains(av)) {
				return new BinOpExpr(_op, _a, _b.pushDown(q, av, s));
			} else {
				// the variable does not appear -> no quantifier needed
				return this;
			}
		}
	}

	@Override
	public MiningExpression pushDown() {
		MiningExpression nA = _a.pushDown();
		MiningExpression nB = _b.pushDown();
		if (_a == nA && _b == nB) {
			return this;
		} else {
			return new BinOpExpr(_op, nA, nB);
		}
	}

	@Override
	public Set<AttributeVariable> getFreeAttVariables() {
		Set<AttributeVariable> s = _a.getFreeAttVariables();
		s.addAll(_b.getFreeAttVariables());
		return s;
	}

	@Override
	public String toString() {
		return "(" + _a.toString() + " " + _op + " " + _b.toString() + ")";
	}

	@Override
	public <E> E accept(Visitor<E> v) {
		switch (_op) {
		case AND:
			return v.and(_a, _b);
		case OR:
			return v.or(_a, _b);
		default:
			throw new IllegalArgumentException();
		}
	}

	private enum Op {

		AND, OR
	}

	public static BinOpExpr and(MiningExpression a, MiningExpression b) {
		return new BinOpExpr(Op.AND, a, b);
	}

	public static BinOpExpr or(MiningExpression a, MiningExpression b) {
		return new BinOpExpr(Op.OR, a, b);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BinOpExpr other = (BinOpExpr) obj;
		if (this._op != other._op) {
			return false;
		}
		if (this._a != other._a
				&& (this._a == null || !this._a.equals(other._a))) {
			return false;
		}
		if (this._b != other._b
				&& (this._b == null || !this._b.equals(other._b))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 83 * hash + (this._op != null ? this._op.hashCode() : 0);
		hash = 83 * hash + (this._a != null ? this._a.hashCode() : 0);
		hash = 83 * hash + (this._b != null ? this._b.hashCode() : 0);
		return hash;
	}

	@Override
	public void acceptPrefix(VoidVisitor v) {
		switch (_op) {
		case AND:
			v.and(this, _a, _b);
			break;
		case OR:
			v.or(this, _a, _b);
			break;
		}
		_a.acceptPrefix(v);
		_b.acceptPrefix(v);
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		_a.registerSQLExpressions(atts, maxAttId, binding, dict);
		_b.registerSQLExpressions(atts, maxAttId, binding, dict);
	}

	@Override
	public BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		BFormula a = _a.getIntermediateFormula(attVal, atts, fas, domain);
		BFormula b = _b.getIntermediateFormula(attVal, atts, fas, domain);
		switch (_op) {
		case AND:
			return new And(a, b);
		case OR:
			return new Or(a, b);
		default:
			throw new UnreachableException();
		}
	}
}
