package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class BNeg extends BFormula {

	private final BFormula _a;

	public BNeg(BFormula a) {
		this._a = a;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(PBBuilder<L> handler, BitSet data)
			throws NoSolutionException {
		LiteralOrValue lv =  _a.getRepresentation(handler, data);
		return lv.neg();
	}

	@Override
	BitSet getMask() {
		return _a.getMask();
	}

}
