/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.rql.ast;

import java.util.HashSet;
import java.util.Set;

import dag.satmining.problem.rql.ast.sql.SQLValue;
import dag.satmining.problem.rql.ast.sql.TupleAccess;

/**
 *
 * @author ecoquery
 */
public final class TupleConstantAttribute extends MiningValue {

    private final TupleVariable _t;
    private final AttributeConstant _att;

    public TupleConstantAttribute(TupleVariable t, AttributeConstant att) {
        this._t = t;
        this._att = att;
    }

    @Override
    public SQLValue generateSQLExpression(AttributeValuation attVal) {
        return new TupleAccess(_t.getName(), _att.getName());
    }

    @Override
    public Set<AttributeVariable> getAttributeVariables() {
        return new HashSet<AttributeVariable>();
    }

    @Override
    public String toString() {
        return _t.getName()+"."+_att.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TupleConstantAttribute other = (TupleConstantAttribute) obj;
        if (this._t != other._t && (this._t == null || !this._t.equals(other._t))) {
            return false;
        }
        if (this._att != other._att && (this._att == null || !this._att.equals(other._att))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this._t != null ? this._t.hashCode() : 0);
        hash = 53 * hash + (this._att != null ? this._att.hashCode() : 0);
        return hash;
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.tupleConstantAttribute(_t, _att);
    }
}
