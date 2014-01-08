/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.Set;

import dag.satmining.problem.rql.ast.sql.SQLValue;

/**
 *
 * @author ecoquery
 */
public abstract class MiningValue {

    public abstract SQLValue generateSQLExpression(AttributeValuation attVarValues);

    public abstract Set<AttributeVariable> getAttributeVariables();

    @Override
    public abstract String toString();

    public static MiningValue cst(String data) {
        return new Constant(data);
    }

    public interface Visitor<E> {

        E constant(String value);

        E tupleVariableAttribute(TupleVariable t, AttributeVariable a);

        E tupleConstantAttribute(TupleVariable t, AttributeConstant a);
    }
    
    public abstract <E> E accept(Visitor<E> v);
}
