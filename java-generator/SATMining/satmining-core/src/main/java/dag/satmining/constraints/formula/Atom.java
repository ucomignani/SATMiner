package dag.satmining.constraints.formula;

import java.util.ArrayList;
import java.util.List;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

/**
 * Atomic formula holding a literal.
 * 
 * @author ecoquery
 * 
 * @param <L>
 */
public class Atom extends SFormula {

	/**
	 * The literal used as an atom.
	 */
	private int _lit;

	/**
	 * Builds an atom from the given literal.
	 * 
	 * @param lit
	 *            the represented literal.
	 */
	public Atom(Literal<?> lit) {
		this._lit = lit.toDimacs();
	}
	
	private Atom(int dimacs) {
		this._lit = dimacs;
	}

	@Override
	public <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder) {
		return builder.fromDimacs(_lit);
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		if (pushNeg) {
			return new Atom(-_lit);
		} else {
			return this;
		}
	}

	@Override
	protected <L extends Literal<L>> List<List<L>> directGen(MinimalClauseBuilder<L> builder) {
		List<List<L>> res = new ArrayList<List<L>>();
		List<L> c = new ArrayList<L>();
		c.add(builder.fromDimacs(_lit));
		res.add(c);
		return res;
	}

}
