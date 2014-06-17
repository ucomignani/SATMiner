/* ./satmining-backend/src/main/java/dag/satmining/backend/pb/gen/CardNetworksPBFactory.java

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

package dag.satmining.backend.pb.gen;

import static dag.satmining.constraints.formula.Formula.equiv;
import static dag.satmining.constraints.formula.Formula.and;

import java.util.Arrays;
import java.util.Collection;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.impl.OneTrue;

/**
 * Implements pseudo boolean inequalities using Cardinality Networks.
 * Implementation based on
 * 
 * Cardinality Networks: a theoretical and empirical study DOI
 * 10.1007/s10601-010-9105-0
 * 
 * Roberto Asín · Robert Nieuwenhuis · Albert Oliveras · Enric
 * Rodríguez-Carbonell
 * 
 * @author ecoquery
 * 
 * @param <L>
 */
// Remark on indices: in the paper sequences start at 1, so there is a shift on
// arrays indices
public class CardNetworksPBFactory<L extends Literal<L>> extends
		AbstractPBDelegateBuilder<L> {

	private OneTrue<L> _oneTrue;

	public CardNetworksPBFactory(ClauseBuilder<L> internalBuilder) {
		super(internalBuilder);
		_oneTrue = new OneTrue<L>(internalBuilder);
	}

	@Override
	public void addPBInequality(L[] lits, Ineq ineq, int value)
			throws NoSolutionException {
		addIneq(lits, ineq, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPBInequality(Collection<L> lits, Ineq ineq, int value)
			throws NoSolutionException {
		addPBInequality((L[]) lits.toArray(), ineq, value);
	}

	@Override
	public void addReifiedPBInequality(L[] lits, Ineq ineq, int value,
			L equivalentTo) throws NoSolutionException {
		addRIneq(lits, ineq, value, equivalentTo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addReifiedPBInequality(Collection<L> lits, Ineq ineq,
			int value, L equivalentTo) throws NoSolutionException {
		addReifiedPBInequality((L[]) lits.toArray(), ineq, value, equivalentTo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addExactlyOneTrue(Collection<L> lits)
			throws NoSolutionException {
		addExactlyOneTrue((L[]) lits.toArray());
	}

	@Override
	public void addExactlyOneTrue(L[] lits) throws NoSolutionException {
		_oneTrue.exactlyOneTrue(lits);
	}

	private void addLeqPropag(L a, L b, L c1, L c2) throws NoSolutionException {
		addClause(a.getOpposite(), b.getOpposite(), c2);
		addClause(a.getOpposite(), c1);
		addClause(b.getOpposite(), c1);
	}

	private void addGeqPropag(L a, L b, L c1, L c2) throws NoSolutionException {
		addClause(a, b, c1.getOpposite());
		addClause(a, c2.getOpposite());
		addClause(b, c2.getOpposite());
	}

	private void addPropag(L a, L b, L c1, L c2, boolean leq, boolean geq)
			throws NoSolutionException {
		if (leq) {
			addLeqPropag(a, b, c1, c2);
		}
		if (geq) {
			addGeqPropag(a, b, c1, c2);
		}
	}

	private L[] split(L[] a, boolean even) {
		L[] res = lArray(a.length / 2);
		for (int i = even ? 0 : 1; i < a.length; i = i + 2) {
			res[i / 2] = a[i];
		}
		return res;
	}

	private L[] hMerge(L[] a, L[] b, boolean leq, boolean geq)
			throws NoSolutionException {
		if (a.length == 1) {
			L[] c = lArray(2, true);
			addPropag(a[0], b[0], c[0], c[1], leq, geq);
			return c;
		} else {
			int n = a.length;
			L[] d = hMerge(split(a, true), split(b, true), leq, geq);
			L[] e = hMerge(split(a, false), split(b, false), leq, geq);
			L[] c = lArray(n * 2);
			for (int i = 1; i < n * 2 - 1; i++) {
				c[i] = newLiteral();
			}
			c[0] = d[0];
			c[2 * n - 1] = e[n - 1];
			for (int i = 1; i < n; i++) {
				addPropag(d[i], e[i - 1], c[2 * i - 1], c[2 * i], leq, geq);
			}
			return c;
		}
	}

	private L[] hSort(L[] a, boolean leq, boolean geq)
			throws NoSolutionException {
		if (a.length == 2) {
			L[] a1 = lArray(1);
			a1[0] = a[0];
			L[] a2 = lArray(1);
			a2[0] = a[1];
			return hMerge(a1, a2, leq, geq);
		} else {
			L[] d = hSort(Arrays.copyOfRange(a, 0, a.length / 2), leq, geq);
			L[] e = hSort(Arrays.copyOfRange(a, a.length / 2, a.length), leq,
					geq);
			return hMerge(d, e, leq, geq);
		}
	}

	private L[] sMerge(L[] a, L[] b, boolean leq, boolean geq)
			throws NoSolutionException {
		if (a.length == 1) {
			L[] c = lArray(2, true);
			addPropag(a[0], b[0], c[0], c[1], leq, geq);
			return c;
		} else {
			L[] d = sMerge(split(a, true), split(b, true), leq, geq);
			L[] e = sMerge(split(a, false), split(b, false), leq, geq);
			L[] c = lArray(a.length + 1);
			c[0] = d[0];
			for (int i = 1; i <= a.length / 2; i++) {
				c[2 * i] = newLiteral();
				c[2 * i - 1] = newLiteral();
				addPropag(d[i], e[i - 1], c[2 * i - 1], c[2 * i], leq, geq);
			}
			return c;
		}
	}

	private L[] card(L[] a, int k, boolean leq, boolean geq)
			throws NoSolutionException {
		if (a.length == k) {
			return hSort(a, leq, geq);
		} else {
			L[] d = card(Arrays.copyOfRange(a, 0, k), k, leq, geq);
			L[] e = card(Arrays.copyOfRange(a, k, a.length), k, leq, geq);
			L[] c = sMerge(d, e, leq, geq);
			return Arrays.copyOf(c, c.length - 1);
		}
	}

	private int findPower(int p) {
		int pw = (int) Math.ceil(Math.log(p) / Math.log(2));
		return 2 << (pw - 1);
	}

	private void addIneqSmall(L[] a, Ineq ineq, int p)
			throws NoSolutionException {
		if (p == 1) {
			_oneTrue.addIneq(a, ineq);
		} else {
			// assumes a.length >= p*2
			int k = findPower(p);
			L[] aCopy = Arrays.copyOf(a, a.length + (k - (a.length % k)));
			for (int i = a.length; i < aCopy.length; i++) {
				aCopy[i] = newLiteral();
				addClause(aCopy[i].getOpposite());
			}
			boolean leq = (ineq == Ineq.EQ || ineq == Ineq.LEQ);
			boolean geq = (ineq == Ineq.EQ || ineq == Ineq.GEQ);
			L[] c = card(aCopy, k, leq, geq);
			if (leq) {
				addClause(c[p].getOpposite());
			}
			if (geq) {
				addClause(c[p - 1]);
			}
		}
	}

	private void addIneq(L[] a, Ineq ineq, int p) throws NoSolutionException {
		if (p > a.length / 2) {
			L[] aOpp = lArray(a.length);
			for (int i = 0; i < aOpp.length; i++) {
				aOpp[i] = a[i].getOpposite();
			}
			addIneqSmall(aOpp, ineq.op(), a.length - p);
		} else {
			addIneqSmall(a, ineq, p);
		}
	}

	private void addRIneqSmall(L[] a, Ineq ineq, int p, L eq)
			throws NoSolutionException {
		if (p == 1) {
			_oneTrue.addRIneq(a, ineq, eq);
		} else {
			// assumes a.length >= p*2
			int k = findPower(p);
			L[] aCopy = Arrays.copyOf(a, a.length + (k - (a.length % k)));
			for (int i = a.length; i < aCopy.length; i++) {
				aCopy[i] = newLiteral();
				addClause(aCopy[i].getOpposite());
			}
			L[] c = card(aCopy, k, true, true); // we need all propagators for
												// the
												// reified version.
			switch (ineq) {
			case LEQ:
				unify(eq, c[p].getOpposite());
				break;
			case GEQ:
				unify(eq, c[p - 1]);
				break;
			case EQ:
				equiv(eq, and(c[p - 1], c[p].getOpposite())).direct(_builder);
				break;
			default:
				throw new Error("Bug in Ineq: unknown case");
			}
		}
	}

	private void addRIneq(L[] a, Ineq ineq, int p, L eq)
			throws NoSolutionException {
		if (p > a.length / 2) {
			L[] aOpp = lArray(a.length);
			for (int i = 0; i < aOpp.length; i++) {
				aOpp[i] = a[i].getOpposite();
			}
			addRIneqSmall(aOpp, ineq.op(), a.length - p, eq);
		} else {
			addRIneqSmall(a, ineq, p, eq);
		}
	}

	@Override
	public void addReifiedWPBInequality(L[] lits, int[] coefs, Ineq ineq,
			int value, L equivTo) throws NoSolutionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addWPBInequality(L[] lits, int[] coefs, Ineq ineq, int value)
			throws NoSolutionException {
		// TODO Auto-generated method stub
		
	}

}
