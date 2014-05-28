/* ./satmining-backend/src/main/java/dag/satmining/constraints/impl/PBReifier.java

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

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.WeightedPBBuilder;

/**
 * Allows to add reified pb constraints from non reified ones
 * 
 * @author ecoquery
 * 
 */
public class PBReifier<L extends Literal<L>> {

	private WeightedPBBuilder<L> _builder;

	public PBReifier(WeightedPBBuilder<L> builder) {
		this._builder = builder;
	}

	public void addReifiedIneqality(L[] lits, Ineq ineq, int value,
			L equivalentTo) throws NoSolutionException {
		switch (ineq) {
		case GEQ:
			addReifiedGeq(lits, value, equivalentTo);
			break;
		case EQ:
			addReifiedIneqality(lits, Ineq.LEQ, value, equivalentTo);
			addReifiedIneqality(lits, Ineq.GEQ, value, equivalentTo);
			break;
		case LEQ:
			addReifiedIneqality(lits, Ineq.GEQ, value + 1,
					equivalentTo.getOpposite());
			break;
		default:
			throw new Error("Bug in Ineq: unknown case");
		}
	}

	private void addReifiedGeq(L[] lits, int value, L eqLit)
			throws NoSolutionException {
		int n = lits.length;
		// eqLit <=> (lits[0] + ... + lits[n-1] >= v)
		//
		// Transformed into to weighted inequalities
		//
		// lits[0] + ... + lits[n-1] + v* ~eqLit >= v
		//
		// ~lits[0] + ... + ~lits[n-1] + (n-v)*eqLit >= n-v
		//
		L[] wLits1 = _builder.lArray(n + 1);
		int[] coefs1 = new int[n + 1];
		L[] wLits2 = _builder.lArray(n + 1);
		int[] coefs2 = new int[n + 1];
		for (int i = 0; i < n; i++) {
			coefs1[i] = coefs2[i] = 1;
			wLits1[i] = lits[i];
			wLits2[i] = lits[i].getOpposite();
		}
		coefs1[n] = value;
		wLits1[n] = eqLit.getOpposite();
		coefs2[n] = (n - value + 1);
		wLits2[n] = eqLit;
		_builder.addWPBInequality(wLits1, coefs1, Ineq.GEQ, value);
		_builder.addWPBInequality(wLits2, coefs2, Ineq.GEQ, n - value + 1);
	}

}
