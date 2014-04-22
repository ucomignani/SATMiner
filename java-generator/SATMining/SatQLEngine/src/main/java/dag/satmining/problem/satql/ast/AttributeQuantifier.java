/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/AttributeQuantifier.java

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.satql.ast.intermediate.And;
import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.intermediate.BNeg;
import dag.satmining.problem.satql.ast.intermediate.LiteralHolder;
import dag.satmining.problem.satql.ast.intermediate.Or;

/**
 * 
 * @author ecoquery
 */
public final class AttributeQuantifier extends
		MiningExpression {

	private final Quantifier _quantifier;
	private final AttributeVariable _attributeVar;
	private final SchemaVariable _schemaVar;
	private final MiningExpression _expr;

	AttributeQuantifier(Quantifier q, AttributeVariable v, SchemaVariable s,
			MiningExpression e) {
		this._quantifier = q;
		this._attributeVar = v;
		this._schemaVar = s;
		this._expr = e;
	}

	public AttributeVariable getAttributeVar() {
		return _attributeVar;
	}

	public MiningExpression getExpr() {
		return _expr;
	}

	public Quantifier getQuantifier() {
		return _quantifier;
	}

	@Override
	public MiningExpression pushDown(Quantifier q, AttributeVariable av,
			SchemaVariable s) {
		if (_quantifier.equals(q)) {
			return new AttributeQuantifier(_quantifier, _attributeVar,
					_schemaVar, _expr.pushDown(q, av, s));
		} else {
			return new AttributeQuantifier(q, av, s, this);
		}
	}

	@Override
	public MiningExpression pushDown() {
		MiningExpression me = _expr.pushDown();
		me = me.pushDown(_quantifier, _attributeVar, _schemaVar);
		if (this.equals(me)) {
			return this;
		} else {
			return me;
		}
	}

	@Override
	public Set<AttributeVariable> getFreeAttVariables() {
		Set<AttributeVariable> s = _expr.getFreeAttVariables();
		s.remove(_attributeVar);
		return s;
	}

	@Override
	public String toString() {
		return "( " + _quantifier.syntax() + " $" + _attributeVar.getName()
				+ " IN " + _schemaVar.getName() + ": " + _expr.toString()
				+ " )";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AttributeQuantifier other = (AttributeQuantifier) obj;
		if (this._quantifier != other._quantifier) {
			return false;
		}
		if (this._attributeVar != other._attributeVar
				&& (this._attributeVar == null || !this._attributeVar
						.equals(other._attributeVar))) {
			return false;
		}
		if (this._schemaVar != other._schemaVar
				&& (this._schemaVar == null || !this._schemaVar
						.equals(other._schemaVar))) {
			return false;
		}
		if (this._expr != other._expr
				&& (this._expr == null || !this._expr.equals(other._expr))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 89 * hash
				+ (this._quantifier != null ? this._quantifier.hashCode() : 0);
		hash = 89
				* hash
				+ (this._attributeVar != null ? this._attributeVar.hashCode()
						: 0);
		hash = 89 * hash
				+ (this._schemaVar != null ? this._schemaVar.hashCode() : 0);
		hash = 89 * hash + (this._expr != null ? this._expr.hashCode() : 0);
		return hash;
	}

	@Override
	public <E> E accept(Visitor<E> v) {
		switch (_quantifier) {
		case Exists:
			return v.exists(_attributeVar, _schemaVar, _expr);
		default:
			return v.forall(_attributeVar, _schemaVar, _expr);
		}
	}

	@Override
	public void acceptPrefix(VoidVisitor v) {
		switch (_quantifier) {
		case Exists:
			v.exists(this, _attributeVar, _schemaVar, _expr);
			break;
		default:
			v.forall(this, _attributeVar, _schemaVar, _expr);
			break;
		}
		_expr.acceptPrefix(v);
	}

	@Override
	public BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		Collection<BFormula> subs = new ArrayList<BFormula>(atts.size());
		for (AttributeConstant att : atts) {
			attVal.set(_attributeVar, att);
			if (att.equals(fas.getAttribute())
					&& _schemaVar.equals(fas.getSchemaVariable())) {
				if (fas.isInSet()) {
					subs.add(_expr.getIntermediateFormula(attVal, atts, fas,
							domain));
				} // else do nothing
			} else {
				BFormula subBFormula = _expr.getIntermediateFormula(attVal,
						atts, fas, domain);
				LiteralHolder litH = new LiteralHolder(domain.get(
						_schemaVar).get(att));
				if (_quantifier == Quantifier.Exists) {
					subs.add(new And(litH, subBFormula));
				} else {
					subs.add(new Or(new BNeg(litH), subBFormula));
				}
			}
		}
		BFormula inter;
		if (_quantifier == Quantifier.Exists) {
			inter = new Or(subs);
		} else {
			inter = new And(subs);
		}
		inter.setCached(_doCache);
		return inter;
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		_expr.registerSQLExpressions(atts, maxAttId, binding, dict);
	}

    @Override
    protected boolean isDataIndependant() {
        return _expr.isDataIndependant();
    }

}
