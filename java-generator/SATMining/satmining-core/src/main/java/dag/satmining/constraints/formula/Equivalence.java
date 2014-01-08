package dag.satmining.constraints.formula;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class Equivalence extends Formula {
	
	private Formula _arg1;
	private Formula _arg2;
	
	public Equivalence(Formula arg1, Formula arg2) {
		this._arg1 = arg1;
		this._arg2 = arg2;
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		return and(impl(_arg1,_arg2),impl(_arg2,_arg1)).tseitinLit(builder);
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		SFormula sarg1 = _arg1.sanitize(false);
		SFormula snarg1 = _arg1.sanitize(true);
		SFormula sarg2 = _arg2.sanitize(false);
		SFormula snarg2 = _arg2.sanitize(true);
		if (pushNeg) {
			return and(or(sarg1,sarg2),or(snarg1,snarg2));
		} else {
			return and(or(snarg1,sarg2),or(snarg2,sarg1));
		}
	}

}
