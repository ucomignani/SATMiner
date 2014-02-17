/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/TupleConstantAttribute.java

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
