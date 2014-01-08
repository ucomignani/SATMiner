package dag.satmining.constraints.mining;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class False<L extends Literal<L>> implements Constraint<L> {

	@Override
	public final void addClauses(PBBuilder<L> sat) throws NoSolutionException {
		throw new NoSolutionException();
	}

}
