/* ./SATQLEngine/src/main/java/dag/satmining/problem/satql/ast/BoundVariables.java

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
	public Set<AttributeVariable> attCmp(AttributeEntity a,
			AttributeEntity b) {
		return new HashSet<AttributeVariable>();
	}

    @Override
    public Set<AttributeVariable> sqlAtom(SQLDelegateAtom s) {
        return new HashSet<AttributeVariable>();
    }
}
