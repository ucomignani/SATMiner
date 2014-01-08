/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author ecoquery
 */
public class BoundVariables implements MiningExpression.Visitor<Set<AttributeVariable>> {

    @Override
    public Set<AttributeVariable> forall(AttributeVariable av, SchemaVariable sv, MiningExpression e) {
        Set<AttributeVariable> s = e.accept(this);
        s.add(av);
        return s;
    }

    @Override
    public Set<AttributeVariable> exists(AttributeVariable av, SchemaVariable sv, MiningExpression e) {
        return forall(av, sv, e); // same as forall
    }

    @Override
    public Set<AttributeVariable> eq(MiningValue a, MiningValue b) {
        return new HashSet<AttributeVariable>();
    }

    @Override
    public Set<AttributeVariable> trueV() {
        return new HashSet<AttributeVariable>();
    }

    @Override
    public Set<AttributeVariable> and(MiningExpression a, MiningExpression b) {
        HashSet<AttributeVariable> s = new HashSet<AttributeVariable>(a.accept(this));
        s.addAll(b.accept(this));
        return s;
    }

    @Override
    public Set<AttributeVariable> or(MiningExpression a, MiningExpression b) {
        return and(a,b); // same as and
    }

    @Override
    public Set<AttributeVariable> neg(MiningExpression a) {
        return a.accept(this);
    }
    
    public static Set<AttributeVariable> getFrom(MiningExpression e) {
        return e.accept(new BoundVariables());
    }

	@Override
	public Set<AttributeVariable> attCmp(AttributeVariable a,
			AttributeVariable b) {
		return new HashSet<AttributeVariable>();
	}

	@Override
	public Set<AttributeVariable> attCmp(AttributeVariable a,
			AttributeConstant b) {
		return new HashSet<AttributeVariable>();
	}
}
