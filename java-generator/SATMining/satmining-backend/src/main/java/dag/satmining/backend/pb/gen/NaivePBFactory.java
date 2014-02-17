/* ./satmining-backend/src/main/java/dag/satmining/backend/pb/gen/NaivePBFactory.java

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

import static java.lang.Math.min;

import static dag.satmining.constraints.formula.Formula.and;
import static dag.satmining.constraints.formula.Formula.or;
import static dag.satmining.constraints.formula.Formula.not;

import java.util.Collection;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;

/**
 * Turns a ClauseBuilder into a PBBuilder. The naive encoding is based on a
 * matrix m of literals, where m[i][n] states whenever atLeast/atMost n literals
 * where set to true among the literals 0 .. i to count.
 * 
 * @author ecoquery
 * 
 * @param <L>
 */
public class NaivePBFactory<L extends Literal<L>> extends
		AbstractPBDelegateBuilder<L> {

	public NaivePBFactory(ClauseBuilder<L> internalBuilder) {
		super(internalBuilder);
	}

	@Override
	public void addPBInequality(L[] lits, Ineq ineq, int value)
			throws NoSolutionException {
		switch (ineq) {
		case LEQ:
			addClause(addAtMost(lits, value));
			break;
		case GEQ:
			addClause(addAtLeast(lits, value));
			break;
		case EQ:
			addPBInequality(lits, Ineq.GEQ, value);
			addPBInequality(lits, Ineq.LEQ, value);
		}
	}

	@Override
	public void addPBInequality(Collection<L> lits, Ineq ineq, int value)
			throws NoSolutionException {
		addPBInequality(toArray(lits), ineq, value);
	}

	@Override
	public void addReifiedPBInequality(L[] lits, Ineq ineq, int value,
			L equivalentTo) throws NoSolutionException {
		switch (ineq) {
		case EQ:
			unify(addAtMost(lits, value), addAtLeast(lits, value));
			unify(addAtMost(lits, value), equivalentTo);
			break;
		case LEQ:
			unify(addAtMost(lits, value), equivalentTo);
			break;
		case GEQ:
			unify(addAtLeast(lits, value), equivalentTo);
			break;
		}
	}

	@Override
	public void addReifiedPBInequality(Collection<L> lits, Ineq ineq,
			int value, L equivalentTo) throws NoSolutionException {
		addReifiedPBInequality(toArray(lits), ineq, value, equivalentTo);
	}

	@Override
	public void addExactlyOneTrue(Collection<L> lits)
			throws NoSolutionException {
		addExactlyOneTrue(toArray(lits));
	}

	@Override
	public void addExactlyOneTrue(L[] lits) throws NoSolutionException {
		L l = addAtMost(lits, 1);
		addReifiedClause(l, lits);
	}

	private L addAtMost(L[] lits, int value) throws NoSolutionException {

		/* Valid case */
		if (value >= lits.length) {
			L l = newLiteral();
			addClause(l);
			return l;
		}

		/*
		 * The literal matrix used for counting m[i][k] is true iff atMost k
		 * literals are true among lits[0..i]
		 * 
		 * m[i][i+1..value] is true
		 * 
		 * m[0][0] = ~lits[0]
		 * 
		 * m[i][0] <=> ~lits[i] /\ m[i-1][0]
		 * 
		 * m[i][k] <=> m[i-1][k-1] \/ (m[i-1][k] /\ ~lits[i])
		 */
		L[][] m = lMatrix(lits.length, value + 1);
		m[0][0] = lits[0].getOpposite();
		for (int i = 1; i < m.length; i++) {
			m[i][0] = newLiteral();
			addReifiedConjunction(m[i][0], lits[i].getOpposite(), m[i - 1][0]);
			for (int k = 1; k <= min(i - 1, value); k++) {
				m[i][k] = or(m[i - 1][k - 1],
						and(m[i - 1][k], not(lits[i])))
						.tseitinLit(_builder);
			}
			if (i <= value) {
				// k=i
				m[i][i] = or(m[i - 1][i - 1], not(lits[i])).tseitinLit(
						_builder);
			}
		}
		return m[lits.length - 1][value];
	}

	private L addAtLeast(L[] lits, int value) throws NoSolutionException {
		return addAtMost(opposite(lits), lits.length - value);
	}

	private L[] opposite(L[] lits) {
		L[] res = lArray(lits.length);
		for (int i = 0; i < res.length; i++) {
			res[i] = lits[i].getOpposite();
		}
		return res;
	}

	private L[] toArray(Collection<L> lits) {
		L[] litsT = _builder.lArray(lits.size());
		int i = 0;
		for (L l : lits) {
			litsT[i++] = l;
		}
		return litsT;
	}
}
