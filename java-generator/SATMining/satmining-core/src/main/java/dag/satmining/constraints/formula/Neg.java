package dag.satmining.constraints.formula;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class Neg extends Formula {

	private Formula _arg;
	
	public Neg(Formula arg) {
		this._arg = arg;
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		return _arg.tseitinLit(builder).getOpposite();
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		return _arg.sanitize(!pushNeg);
	}

}
