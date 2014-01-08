package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class Or extends BFormula {

	private final BFormula[] _disj;
	private BitSet _mask = null;

	public Or(BFormula... disj) {
		_disj = disj;
	}

	public Or(Collection<BFormula> conj) {
		this(conj.toArray(new BFormula[conj.size()]));
	}

	@Override
	BitSet getMask() {
		if (_mask == null) {
			_mask = new BitSet();
			for (BFormula f : _disj) {
				_mask.or(f.getMask());
			}
		}
		return _mask;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(
			PBBuilder<L> handler, BitSet data) throws NoSolutionException {
		HashSet<L> lits = new HashSet<L>();
		for (BFormula f : _disj) {
			LiteralOrValue v = f.getRepresentation(handler, data);
			if (LiteralOrValue.FALSE.equals(v)) {
				// do nothing
			} else if (LiteralOrValue.TRUE.equals(v)) {
				return LiteralOrValue.trueV();
			} else {
				lits.add(v.getLiteral(handler));
			}
		}
		if (lits.isEmpty()) {
			return LiteralOrValue.falseV();
		}
		if (lits.size() == 1) {
			return new LiteralOrValue(lits.iterator().next().toDimacs());
		} else {
			L eq = handler.newLiteral();
			handler.addReifiedClause(eq, lits);
			return new LiteralOrValue(eq.toDimacs());
		}
	}

}
