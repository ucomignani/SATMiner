/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.HashSet;
import java.util.Set;

import dag.satmining.problem.rql.ast.sql.SQLConstant;
import dag.satmining.problem.rql.ast.sql.SQLValue;

/**
 *
 * @author ecoquery
 */
public class Constant extends MiningValue {

    private final String _value;

    public Constant(String v) {
        this._value = v;
    }

    @Override
    public SQLValue generateSQLExpression(AttributeValuation attVarValues) {
        return new SQLConstant(_value);
    }

    @Override
    public Set<AttributeVariable> getAttributeVariables() {
        return new HashSet<AttributeVariable>();
    }

    @Override
    public String toString() {
        return _value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Constant other = (Constant) obj;
        if ((this._value == null) ? (other._value != null) : !this._value.equals(other._value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this._value != null ? this._value.hashCode() : 0);
        return hash;
    }

    @Override
    public <E> E accept(Visitor<E> visitor) {
        return visitor.constant(_value);
    }
}
