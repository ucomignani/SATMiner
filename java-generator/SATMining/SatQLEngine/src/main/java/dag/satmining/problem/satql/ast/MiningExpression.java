/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/MiningExpression.java

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
import dag.satmining.problem.satql.ast.sql.RAWSQLAtom;

/**
 * 
 * @author ecoquery
 */
public abstract class MiningExpression {
	
    // TODO: check correct construction of queries, mining formulas should be attribute-closed
    
	protected boolean _doCache = false;

	public void enableCache(boolean enable) {
		_doCache = enable;
	}
	
	public BFormula getIntermediateFormula(
			AttributeValuation attVal, Collection<AttributeConstant> atts,
			ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain) {
		BFormula f = buildIntermediateFormula(attVal, atts, fas, domain);
		f.setCached(_doCache);
		return f;
	}
	
	abstract void registerSQLExpressions(Collection<AttributeConstant> atts,
			int maxAttId, 
			SQLBinding binding, ASTDictionnary dict);

	abstract BFormula buildIntermediateFormula(
			AttributeValuation attVal, Collection<AttributeConstant> atts,
			ForceAttributeSchema fas,
			Map<SchemaVariable, Map<AttributeConstant, Integer>> domain);

	protected abstract MiningExpression pushDown(Quantifier q,
			AttributeVariable av, SchemaVariable s);

	public abstract MiningExpression pushDown();

	public abstract Set<AttributeVariable> getFreeAttVariables();

	public Set<AttributeVariable> getBoundAttVariables() {
		return BoundVariables.getFrom(this);
	}

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	public interface Visitor<E> {
	    
		E forall(AttributeVariable av, SchemaVariable sv, MiningExpression e);

		E exists(AttributeVariable av, SchemaVariable sv, MiningExpression e);

		E trueV();

		E and(MiningExpression a, MiningExpression b);

		E or(MiningExpression a, MiningExpression b);

		E neg(MiningExpression a);
		
		E attCmp(AttributeEntity a, AttributeEntity b);
		
		E sqlAtom(SQLDelegateAtom s);
	}

	public abstract <E> E accept(Visitor<E> v);

	public interface VoidVisitor {
	    
		void and(MiningExpression e, MiningExpression a, MiningExpression b);

		void exists(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a);

		void forall(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a);

		void neg(MiningExpression e, MiningExpression a);

		void or(MiningExpression e, MiningExpression a, MiningExpression b);

		void trueV(MiningExpression e);
		
		void attCmp(MiningExpression e, AttributeEntity a, AttributeEntity b);
		
		void sqlAtom(MiningExpression e, RAWSQLAtom a);
		
	}

	public abstract void acceptPrefix(VoidVisitor v);
	
	protected abstract boolean isDataIndependant();
}
