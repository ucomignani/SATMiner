/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.BFormula;

/**
 * 
 * @author ecoquery
 */
public abstract class MiningExpression {
	
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

		E eq(MiningValue a, MiningValue b);

		E trueV();

		E and(MiningExpression a, MiningExpression b);

		E or(MiningExpression a, MiningExpression b);

		E neg(MiningExpression a);
		
		E attCmp(AttributeVariable a, AttributeVariable b);
		
		E attCmp(AttributeVariable a, AttributeConstant b);
	}

	public abstract <E> E accept(Visitor<E> v);

	public interface VoidVisitor {
		void and(MiningExpression e, MiningExpression a, MiningExpression b);

		void eq(MiningExpression e, MiningValue a, MiningValue b);

		void exists(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a);

		void forall(MiningExpression e, AttributeVariable av,
				SchemaVariable sv, MiningExpression a);

		void neg(MiningExpression e, MiningExpression a);

		void or(MiningExpression e, MiningExpression a, MiningExpression b);

		void trueV(MiningExpression e);
		
		void attCmp(MiningExpression e, AttributeVariable a, AttributeVariable b);
		
		void attCmp(MiningExpression e, AttributeVariable a, AttributeConstant b);
	}

	public abstract void acceptPrefix(VoidVisitor v);
}
