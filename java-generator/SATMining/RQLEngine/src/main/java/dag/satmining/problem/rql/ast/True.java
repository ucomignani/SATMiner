/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.intermediate.Constant;

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
}
