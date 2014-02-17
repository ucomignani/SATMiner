/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/AttributeVarComparison.java

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

package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.intermediate.Constant;

public class AttributeVarComparison extends MiningExpression {
	private final AttributeVariable _a;
	private final AttributeVariable _b;

	AttributeVarComparison(AttributeVariable a, AttributeVariable b) {
		this._a = a;
		this._b = b;
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		// do nothing
	}

	@Override
	BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		return attVal.getAtt(_a).equals(attVal.getAtt(_b)) ? Constant.TRUE
				: Constant.FALSE;
	}

	@Override
	protected MiningExpression pushDown(Quantifier q, AttributeVariable av,
			SchemaVariable s) {
		return new AttributeQuantifier(q, av, s, this);
	}

	@Override
	public MiningExpression pushDown() {
		return this;
	}

	@Override
	public Set<AttributeVariable> getFreeAttVariables() {
		Set<AttributeVariable> s = new HashSet<AttributeVariable>();
		s.add(_a);
		s.add(_b);
		return s;
	}

	@Override
	public String toString() {
		return _a.getName()+" = "+_b.getName();
	}

	@Override
	public <E> E accept(Visitor<E> v) {
		return v.attCmp(_a, _b);
	}

	@Override
	public void acceptPrefix(VoidVisitor v) {
		v.attCmp(this, _a, _b);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_a == null) ? 0 : _a.hashCode());
		result = prime * result + ((_b == null) ? 0 : _b.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeVarComparison other = (AttributeVarComparison) obj;
		if (_a == null) {
			if (other._a != null)
				return false;
		} else if (!_a.equals(other._a))
			return false;
		if (_b == null) {
			if (other._b != null)
				return false;
		} else if (!_b.equals(other._b))
			return false;
		return true;
	}
}
