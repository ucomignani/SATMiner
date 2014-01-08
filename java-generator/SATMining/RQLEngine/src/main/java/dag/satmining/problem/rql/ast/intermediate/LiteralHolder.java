package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class LiteralHolder extends BFormula {

	private final int _lit;

	public LiteralHolder(int lit) {
		_lit = lit;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(
			PBBuilder<L> handler, BitSet data) {
		return new LiteralOrValue(_lit);
	}

	@Override
	BitSet getMask() {
		return new BitSet();
	}

}
