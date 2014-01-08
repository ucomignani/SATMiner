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
public class TupleVariableAttribute extends MiningValue {

    private final TupleVariable _tuple;
    private final AttributeVariable _attribute;

    public TupleVariableAttribute(TupleVariable t, AttributeVariable v) {
        this._tuple = t;
        this._attribute = v;
    }

    @Override
    public SQLValue generateSQLExpression(AttributeValuation attVals) {
        return new TupleAccess(_tuple.getName(), attVals.getAtt(_attribute).getName());
    }

    @Override
    public Set<AttributeVariable> getAttributeVariables() {
        HashSet<AttributeVariable> s = new HashSet<AttributeVariable>();
        s.add(_attribute);
        return s;
    }

    @Override
    public String toString() {
        return _tuple.getName()+"."+_attribute.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TupleVariableAttribute other = (TupleVariableAttribute) obj;
        if (this._tuple != other._tuple && (this._tuple == null || !this._tuple.equals(other._tuple))) {
            return false;
        }
        if (this._attribute != other._attribute && (this._attribute == null || !this._attribute.equals(other._attribute))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this._tuple != null ? this._tuple.hashCode() : 0);
        hash = 83 * hash + (this._attribute != null ? this._attribute.hashCode() : 0);
        return hash;
    }

    @Override
    public <E> E accept(Visitor<E> v) {
        return v.tupleVariableAttribute(_tuple, _attribute);
    }
}
