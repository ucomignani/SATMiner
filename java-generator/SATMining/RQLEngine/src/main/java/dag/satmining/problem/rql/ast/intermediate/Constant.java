package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public final class Constant extends BFormula {

	public static final Constant TRUE = new Constant(true);
	public static final Constant FALSE = new Constant(false);

	private final boolean _val;

	private Constant(boolean val) {
		this._val = val;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(
			PBBuilder<L> handler, BitSet data) throws NoSolutionException {
		return (_val ? LiteralOrValue.TRUE : LiteralOrValue.FALSE);
	}

	@Override
	BitSet getMask() {
		return new BitSet();
	}

}
