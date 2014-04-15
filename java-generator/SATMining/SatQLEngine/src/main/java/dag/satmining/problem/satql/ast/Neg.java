/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/Neg.java

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

import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.intermediate.BNeg;

/**
 *
 * @author ecoquery
 */
public class Neg extends MiningExpression {
    private final MiningExpression _a;
    
    public Neg(MiningExpression a) {
        this._a = a;
    }

    @Override
    public MiningExpression pushDown(Quantifier q, AttributeVariable av, SchemaVariable s) {
        return new Neg(_a.pushDown(q.opp(),av,s));
    }
    
    @Override
    public MiningExpression pushDown() {
    	MiningExpression nA = _a.pushDown();
    	if (nA == _a) {
    		return this;
    	} else {
    		return new Neg(nA);
    	}
    }

    @Override
    public Set<AttributeVariable> getFreeAttVariables() {
        return _a.getFreeAttVariables();
    }
    
    @Override
    public String toString() {
        return "(NOT "+_a.toString()+")";
    }

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Neg other = (Neg) obj;
        if (this._a != other._a && (this._a == null || !this._a.equals(other._a))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this._a != null ? this._a.hashCode() : 0);
        return hash;
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.neg(_a);
    }

	@Override
	public void acceptPrefix(VoidVisitor v) {
		v.neg(this, _a);
		_a.acceptPrefix(v);
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		_a.registerSQLExpressions(atts, maxAttId, binding, dict);
	}

	@Override
	public BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		return new BNeg(_a.getIntermediateFormula(attVal, atts, fas, domain));
	}

    @Override
    protected boolean isDataIndependant() {
        return _a.isDataIndependant();
    }
    
}
