/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import dag.satmining.problem.rql.ast.intermediate.BFormula;
import dag.satmining.problem.rql.ast.intermediate.BNeg;

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
    
}
