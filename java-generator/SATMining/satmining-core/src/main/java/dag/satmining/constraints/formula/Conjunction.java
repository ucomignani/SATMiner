package dag.satmining.constraints.formula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class Conjunction extends NAryFormula {

	public Conjunction(Collection<Formula> args) {
		super(args);
	}
	
	public Conjunction(Formula... args){
		super(args);
	}

	@Override
	protected <L extends Literal<L>> List<List<L>> directGen(MinimalClauseBuilder<L> builder) {
		List<List<L>> res = new ArrayList<List<L>>();
		for (Formula f : _args) {
			try {
				for (List<L> c : ((SFormula) f).directGen(builder)) {
					res.add(c);
				}
			} catch (ClassCastException e) {
				throw new IllegalStateException(
						"direct transformation on unsanitized formula", e);
			}
		}
		return res;
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder) throws NoSolutionException {
		L eqF = builder.newLiteral();
		L[] cl = builder.lArray(_args.length+1);
		for(int i = 0; i < _args.length; i++) {
			L eqI = _args[i].tseitinLit(builder);
			cl[i] = eqI.getOpposite();
			builder.addClause(eqI,eqF.getOpposite());
		}
		cl[_args.length] = eqF;
		builder.addClause(cl);
		return eqF;
	}

	@Override
	protected SFormula sameFromArgs(List<Formula> args) {
		return new Conjunction(args);
	}

	@Override
	protected SFormula dualFromArgs(List<Formula> args) {
		return new Disjunction(args);
	}

}
