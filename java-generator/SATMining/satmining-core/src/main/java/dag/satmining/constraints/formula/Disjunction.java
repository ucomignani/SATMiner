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
