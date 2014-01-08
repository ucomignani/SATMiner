package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class AtomicHolder extends BFormula {

	private final int _dataOffset;

	public AtomicHolder(int offset) {
		this._dataOffset = offset;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(PBBuilder<L> handler, BitSet data) {
		return (LiteralOrValue) (data.get(_dataOffset) ? LiteralOrValue.trueV()
				: LiteralOrValue.falseV());
	}

	@Override
	BitSet getMask() {
		BitSet bs = new BitSet();
		bs.set(_dataOffset);
		return bs;
	}

}
