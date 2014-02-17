/* ./satmining-backend/src/main/java/dag/satmining/constraints/impl/AbstractClauseBuilder.java

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

import java.util.Collection;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Literal;

public abstract class AbstractClauseBuilder<L extends Literal<L>> implements
		ClauseBuilder<L> {

	@Override
	public L[] lArray(int size, boolean filled) {
		L[] res = lArray(size);
		if (filled) {
			for (int i = 0; i < size; i++) {
				res[i] = newLiteral();
			}
		}
		return res;
	}

	@Override
	public L[][] lMatrix(int size, int size2, boolean filled) {
		L[][] m = lMatrix(size, size2);
		if (filled) {
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size2; j++) {
					m[i][j] = newLiteral();
				}
			}
		}
		return m;
	}

	@Override
	public void addClause(Collection<L> lits) throws NoSolutionException {
		addClause(toArray(lits));
	}

	@Override
	public void addClause(L l) throws NoSolutionException {
		addClause(toArray(l));
	}

	@Override
	public void addClause(L l1, L l2) throws NoSolutionException {
		addClause(toArray(l1, l2));
	}

	@Override
	public void addClause(L l1, L l2, L l3) throws NoSolutionException {
		addClause(toArray(l1, l2, l3));
	}

	@Override
	public void unify(L[] lits) throws NoSolutionException {
		for (int i = 1; i < lits.length; i++) {
			unify(lits[0], lits[i]);
		}
	}

	@Override
	public void unify(L l1, L l2) throws NoSolutionException {
		addClause(l1.getOpposite(), l2);
		addClause(l1, l2.getOpposite());
	}

	@Override
	public void addReifiedConjunction(L equivalentTo, L[] lits)
			throws NoSolutionException {
		L[] longClause = lArray(lits.length + 1);
		for (int i = 0; i < lits.length; i++) {
			longClause[i] = lits[i].getOpposite();
			addClause(toArray(lits[i], equivalentTo.getOpposite()));
		}
		longClause[lits.length] = equivalentTo;
		addClause(longClause);
	}

	@Override
	public void addReifiedConjunction(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		addReifiedConjunction(equivalentTo, toArray(lits));
	}

	@Override
	public void addReifiedConjunction(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		addReifiedConjunction(equivalentTo, toArray(l1, l2));
	}

	@Override
	public void addReifiedClause(L equivalentTo, L[] lits)
			throws NoSolutionException {
		addReifiedConjunction(equivalentTo.getOpposite(), opposite(lits));
	}

	private L[] opposite(L[] lits) {
		L[] res = lArray(lits.length);
		for (int i = 0; i < res.length; i++) {
			res[i] = lits[i].getOpposite();
		}
		return res;
	}

	@Override
	public void addReifiedClause(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		addReifiedClause(equivalentTo, toArray(lits));
	}

	@Override
	public void addReifiedClause(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		addReifiedClause(equivalentTo, toArray(l1, l2));
	}

	@Override
	public void addReifiedClause(L equivalentTo, L l1, L l2, L l3)
			throws NoSolutionException {
		addReifiedClause(equivalentTo, toArray(l1, l2, l3));
	}

	protected abstract L[] toArray(Collection<L> c);

	private L[] toArray(L l) {
		L[] lits = lArray(1);
		lits[0] = l;
		return lits;
	}

	private L[] toArray(L l, L l2) {
		L[] lits = lArray(2);
		lits[0] = l;
		lits[1] = l2;
		return lits;
	}

	private L[] toArray(L l, L l2, L l3) {
		L[] lits = lArray(3);
		lits[0] = l;
		lits[1] = l2;
		lits[2] = l3;
		return lits;
	}
}