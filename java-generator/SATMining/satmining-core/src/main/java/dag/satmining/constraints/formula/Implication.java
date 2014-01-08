package dag.satmining.constraints.formula;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class Implication extends Formula {

	protected Formula _arg1;
	protected Formula _arg2;
	
	public Implication(Formula arg1, Formula arg2) {
		this._arg1 = arg1;
		this._arg2 = arg2;
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		return or(not(_arg1),_arg2).tseitinLit(builder);
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		if (pushNeg) {
			return and(_arg1.sanitize(false),_arg2.sanitize(true)); // A /\ ~B
		} else {
			return or(_arg1.sanitize(true),_arg2.sanitize(false)); // ~A \/ B
		}
	}
	
}
