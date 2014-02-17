/* ./satmining-core/src/main/java/dag/satmining/constraints/formula/Disjunction.java

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

package dag.satmining.constraints.formula;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class Disjunction extends NAryFormula {

	public Disjunction(Collection<Formula> args) {
		super(args);
	}

	public Disjunction(Formula... args) {
		super(args);
	}

	@Override
	protected SFormula sameFromArgs(List<Formula> args) {
		return new Disjunction(args);
	}

	@Override
	protected <L extends Literal<L>> List<List<L>> directGen(MinimalClauseBuilder<L> builder) {
		try {
			List<List<L>> res = new ArrayList<List<L>>();
			List<List<List<L>>> argsCNFs = new ArrayList<List<List<L>>>();
			for (Formula f : _args) {
				argsCNFs.add(((SFormula) f).directGen(builder));
			}
			combine(res, argsCNFs, new ArrayDeque<List<L>>());
			return res;
		} catch (ClassCastException e) {
			throw new IllegalStateException(
					"direct transformation on unsanitized formula", e);
		}
	}

	private <L extends Literal<L>> void combine(List<List<L>> res, List<List<List<L>>> argsCNFs,
			Deque<List<L>> selected) {
		if (selected.size() == argsCNFs.size()) {
			List<L> c = new ArrayList<L>();
			for (List<L> c2 : selected) {
				c.addAll(c2);
			}
			res.add(c);
		} else {
			int idx = selected.size();
			for (List<L> clause : argsCNFs.get(idx)) {
				selected.push(clause);
				combine(res, argsCNFs, selected);
				selected.pop();
			}
		}
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		L[] cl = builder.lArray(_args.length + 1);
		L eqF = builder.newLiteral();
		for (int i = 0; i < _args.length; i++) {
			L eqA = _args[i].tseitinLit(builder);
			builder.addClause(eqF, eqA.getOpposite());
			cl[i] = eqA;
		}
		cl[_args.length] = eqF.getOpposite();
		builder.addClause(cl);
		return eqF;
	}

	@Override
	protected SFormula dualFromArgs(List<Formula> args) {
		return new Conjunction(args);
	}
}
