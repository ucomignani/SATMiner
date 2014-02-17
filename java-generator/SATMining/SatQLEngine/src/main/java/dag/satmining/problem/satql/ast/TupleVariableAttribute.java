/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/TupleVariableAttribute.java

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

import java.util.HashSet;
import java.util.Set;

import dag.satmining.problem.satql.ast.sql.SQLValue;
import dag.satmining.problem.satql.ast.sql.TupleAccess;

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
