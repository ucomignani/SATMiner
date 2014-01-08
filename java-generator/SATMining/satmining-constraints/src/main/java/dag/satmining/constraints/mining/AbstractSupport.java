package dag.satmining.constraints.mining;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * Represents the notion of support in a mining problem.
 * 
 * @author ecoquery
 * 
 * @param <L>
 */
public abstract class AbstractSupport<L extends Literal<L>> implements
		Constraint<L> {
	
	private int _size;

	/**
	 * A literal for each candidate position of the pattern. We assume that the
	 * support is not strong as it should be instantiated when the pattern is
	 * known enough.
	 */
	private L[] _positionLiterals;

	/**
	 * Builds the support of the given number of transaction, positions, etc.
	 * 
	 * @param size
	 *            the number of transactions/positions.
	 */
	public AbstractSupport(int size) {
		this._size = size;
	}

	/**
	 * Initializes the variables for this constraint.
	 */
	private void initLiterals(PBBuilder<L> sat) {
		_positionLiterals = sat.lArray(_size);
		for (int pos = 0; pos < _positionLiterals.length; pos++) {
			_positionLiterals[pos] = sat.newLiteral();
		}
	}

	@Override
	public void addClauses(PBBuilder<L> sat) throws NoSolutionException {
		initLiterals(sat);
		for (int patternPos = 0; patternPos < _positionLiterals.length; patternPos++) {
			addMatchAt(patternPos, sat);
		}
	}

	/**
	 * Adds the clauses for matching at the given position.
	 * 
	 * @param patternPosition
	 *            the position of the pattern.
	 * @param satHandler
	 *            the handler for adding clauses.
	 */
	protected abstract void addMatchAt(int patternPos, PBBuilder<L> satHandler)
			throws NoSolutionException;

	/**
	 * The literal equivalent to pattern matching at the given position
	 * 
	 * @param position
	 * @return
	 */
	public L getLiteral(int position) {
		return _positionLiterals[position];
	}

	/**
	 * The size of the problem.
	 * 
	 * @return the size in terms of number of positions/transactions to match.
	 */
	public int size() {
		return _size;
	}
}
