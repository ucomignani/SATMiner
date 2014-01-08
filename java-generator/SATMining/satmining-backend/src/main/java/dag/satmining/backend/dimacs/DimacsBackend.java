package dag.satmining.backend.dimacs;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.constraints.impl.AbstractClauseBuilder;

/**
 * Class for dimacs based backends.
 * 
 * @author ecoquery
 * 
 */
public abstract class DimacsBackend extends
		AbstractClauseBuilder<DimacsLiteral> {

	private static final Logger LOG = LoggerFactory.getLogger(DimacsBackend.class);
	
	private int _nextVariable;
	protected Set<Integer> _strongBackdoorClause;

	protected DimacsBackend() {
		this._strongBackdoorClause = new TreeSet<Integer>();
		this._nextVariable = 1;
	}

	@Override
	public DimacsLiteral newLiteral() {
		return newLiteral(true, false);
	}

	@Override
	public DimacsLiteral newStrongLiteral() {
		return newLiteral(true, true);
	}

	@Override
	public DimacsLiteral newLiteral(boolean positive, boolean strong) {
		int variable = _nextVariable++;
		int literal = positive ? variable : -variable;
		LOG.debug("new literal(strong: {}): {}",strong,literal);
		if (strong) {
			_strongBackdoorClause.add(variable);
		}
		return new DimacsLiteral(literal);
	}

	@Override
	public void addToStrongBackdoor(DimacsLiteral l) {
		_strongBackdoorClause.add(l.getVariableId());
	}

	public int getNbVariables() {
		return _nextVariable - 1;
	}

	@Override
	public DimacsLiteral[] lArray(int size) {
		return new DimacsLiteral[size];
	}

	@Override
	public DimacsLiteral[][] lMatrix(int size, int size2) {
		return new DimacsLiteral[size][size2];
	}

	@Override
	protected DimacsLiteral[] toArray(Collection<DimacsLiteral> c) {
		return c.toArray(new DimacsLiteral[c.size()]);
	}

	@Override
	public DimacsLiteral fromDimacs(int dimacs) {
		return new DimacsLiteral(dimacs);
	}
}
