package dag.satmining.backend.boolvarpb;

import boolvar.model.Variable;
import boolvar.output.CNFformula;
import boolvar.output.Clause;
import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class BVPBWrapper<L extends Literal<L>> extends BVPBBuilder {

	private MinimalClauseBuilder<L> _backend;

	public BVPBWrapper(MinimalClauseBuilder<L> mcb) {
		_backend = mcb;
	}
	
	@Override
	public void endProblem() throws NoSolutionException {
		CNFformula cnf = getCNF();
		int lastVar = Variable.getNbUsed();
		for (int i = 1; i <= lastVar; i++) {
			if (isStrong(i)) {
				_backend.newStrongLiteral();
			} else {
				_backend.newLiteral();
			}
		}
		int nbClauses = cnf.size();
		for (int i = 0; i < nbClauses; i++) {
			Clause c = cnf.getClause(i);
			L[] c2 = _backend.lArray(c.size());
			for (int j = 0; j < c2.length; j++) {
				c2[j] = _backend
						.fromDimacs(BVLiteral.toDimacs(c.getLiteral(j)));
			}
			_backend.addClause(c2);
		}
		_backend.endProblem();
		freeInternal();
	}
}
