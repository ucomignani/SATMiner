/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/True.java

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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.satql.ast.intermediate.BFormula;
import dag.satmining.problem.satql.ast.intermediate.Constant;

/**
 *
 * @author ecoquery
 */
public final class True extends AtomicMiningExpression {

	private static final True INSTANCE = new True();

    private True() {
    }

	public static True getInstance() {
        return INSTANCE;
    }

    @Override
    public MiningExpression pushDown(Quantifier q, AttributeVariable av, SchemaVariable s) {
        return this;
    }
    
    @Override
    public MiningExpression pushDown() {
    	return this;
    }

    @Override
    public Set<AttributeVariable> getFreeAttVariables() {
        return new HashSet<AttributeVariable>();
    }

    @Override
    public String toString() {
        return "TRUE";
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.trueV();
    }

	@Override
	public void acceptPrefix(VoidVisitor v) {
		v.trueV(this);
	}

	@Override
	void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, SQLBinding binding, ASTDictionnary dict) {
		// nothing to do
	}

	@Override
	public BFormula buildIntermediateFormula(AttributeValuation attVal,
			Collection<AttributeConstant> atts, ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		return Constant.TRUE;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	@Override
	public int hashCode() {
		return 0;
	}

    @Override
    protected boolean isDataIndependant() {
        return true;
    } 
}
