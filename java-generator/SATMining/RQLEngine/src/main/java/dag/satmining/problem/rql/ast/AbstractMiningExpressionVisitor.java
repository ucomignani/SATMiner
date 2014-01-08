package dag.satmining.problem.rql.ast;

import dag.satmining.problem.rql.ast.MiningExpression.VoidVisitor;

public abstract class AbstractMiningExpressionVisitor implements VoidVisitor {

	@Override
	public void and(MiningExpression e, MiningExpression a, MiningExpression b) {
		defaultHandling(e);
	}

	@Override
	public void eq(MiningExpression e, MiningValue a, MiningValue b) {
		defaultHandling(e);
	}

	@Override
	public void exists(MiningExpression e, AttributeVariable av,
			SchemaVariable sv, MiningExpression a) {
		defaultHandling(e);
	}

	@Override
	public void forall(MiningExpression e, AttributeVariable av,
			SchemaVariable sv, MiningExpression a) {
		defaultHandling(e);
	}

	@Override
	public void neg(MiningExpression e, MiningExpression a) {
		defaultHandling(e);
	}

	@Override
	public void or(MiningExpression e, MiningExpression a, MiningExpression b) {
		defaultHandling(e);
	}

	@Override
	public void trueV(MiningExpression e) {
		defaultHandling(e);
	}
	
	
	
	@Override
	public void attCmp(MiningExpression e, AttributeVariable a,
			AttributeVariable b) {
		defaultHandling(e);
	}

	@Override
	public void attCmp(MiningExpression e, AttributeVariable a,
			AttributeConstant b) {
		defaultHandling(e);
	}

	public void defaultHandling(MiningExpression e) {
	}

}
