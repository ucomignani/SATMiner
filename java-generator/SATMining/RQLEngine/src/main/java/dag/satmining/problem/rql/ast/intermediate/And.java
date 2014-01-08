package dag.satmining.problem.rql.ast.intermediate;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class And extends BFormula {
	
	private final BFormula[] _conj;
	private BitSet _mask = null;
	
	public And(BFormula... conj) {
		_conj = conj;
	}
	
	public And(Collection<BFormula> conj) {
		this(conj.toArray(new BFormula[conj.size()]));
	}

	@Override
	 <L extends Literal<L>> LiteralOrValue makeRepresentation(PBBuilder<L> handler, BitSet data)
			throws NoSolutionException {
		HashSet<L> lits = new HashSet<L>();
		for(BFormula f : _conj) {
			LiteralOrValue v = f.getRepresentation(handler, data);
			if (LiteralOrValue.TRUE.equals(v)) {
				// do nothing
			} else if (LiteralOrValue.FALSE.equals(v)) {
				return v;
			} else {
				lits.add(v.getLiteral(handler));
			}
		}
		if (lits.isEmpty()) {
			return LiteralOrValue.trueV();
		} if (lits.size() == 1) {
			return new LiteralOrValue(lits.iterator().next().toDimacs());
		} else {
			L eq = handler.newLiteral();
			handler.addReifiedConjunction(eq, lits);
			return new LiteralOrValue(eq.toDimacs());
		}
	}

	@Override
	BitSet getMask() {
		if (_mask == null) {
			_mask = new BitSet();
			for(BFormula f : _conj) {
				_mask.or(f.getMask());
			}
		}
		return _mask;
	}

	
	
}
