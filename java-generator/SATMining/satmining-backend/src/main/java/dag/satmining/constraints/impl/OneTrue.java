/* ./satmining-backend/src/main/java/dag/satmining/constraints/impl/OneTrue.java

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

package dag.satmining.constraints.impl;

import static dag.satmining.constraints.formula.Formula.*;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class OneTrue<L extends Literal<L>> {

	private MinimalClauseBuilder<L> _builder;

	public OneTrue(MinimalClauseBuilder<L> builder) {
		super();
		this._builder = builder;
	}

	private L[] buildFound(L[] a) throws NoSolutionException {
		L[] found = _builder.lArray(a.length);
		found[0] = a[0];
		for (int i = 1; i < a.length; i++) {
			// found[i] <=> found[i-1] \/ a[i]
			found[i] = or(found[i - 1], a[i]).tseitinLit(_builder);
		}
		return found;
	}

	private L[] buildFound2(L[] a, L[] found) throws NoSolutionException {
		L[] found2 = _builder.lArray(a.length);
		found2[0] = _builder.newLiteral();
		_builder.addClause(found2[0].getOpposite());
		// found2[1] <=> a[0] /\ a[1]
		found2[1] = and(a[0], a[1]).tseitinLit(_builder);
		for (int i = 2; i < a.length; i++) {
			// found2[i] <=> found2[i-1] \/ (found[i-1] /\ a[i])
			found2[i] = or(found2[i - 1], and(found[i - 1], a[i])).tseitinLit(
					_builder);
		}
		return found2;
	}

	public void atMostOneTrue(L[] a) throws NoSolutionException {
		L[] found = buildFound(a);
		for (int i = 1; i < a.length; i++) {
			// ~(found[i-1] /\ a[i])
			_builder.addClause(found[i - 1].getOpposite(), a[i].getOpposite());
		}
	}

	public void atMostOneTrueR(L[] a, L eq) throws NoSolutionException {
		L[] found = buildFound(a);
		L[] found2 = buildFound2(a, found);
		// eq <=> ~found2[a.length-1]
		_builder.unify(eq, found2[a.length - 1].getOpposite());
	}

	public void atLeastOneTrue(L[] a) throws NoSolutionException {
		_builder.addClause(a);
	}

	public void atLeastOneTrueR(L[] a, L eq) throws NoSolutionException {
		equiv(eq, or(a)).direct(_builder);
	}

	public void exactlyOneTrue(L[] lits) throws NoSolutionException {
		_builder.addClause(lits);
		atMostOneTrue(lits);
	}

	public void exactlyOneTrueR(L[] a, L eq) throws NoSolutionException {
		L atMostOne = _builder.newLiteral();
		atMostOneTrueR(a, atMostOne);
		L atLeastOne = _builder.newLiteral();
		atLeastOneTrueR(a, atLeastOne);
		equiv(eq,and(atMostOne,atLeastOne)).direct(_builder);
	}

	public void addIneq(L[] a, Ineq ineq) throws NoSolutionException {
		switch (ineq) {
		case EQ:
			exactlyOneTrue(a);
			break;
		case LEQ:
			atMostOneTrue(a);
			break;
		case GEQ:
			atLeastOneTrue(a);
		}
	}
	
	public void addRIneq(L[]a, Ineq ineq, L eq) throws NoSolutionException {
		switch(ineq) {
		case EQ:
			exactlyOneTrueR(a, eq);
			break;
		case GEQ:
			atLeastOneTrueR(a, eq);
			break;
		case LEQ:
			atMostOneTrueR(a, eq);
		}
	}
}
