package dag.satmining.constraints.mining;

import dag.satmining.constraints.Constraint;
import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal frequency constraint for sequence patterns.
 * 
 * @author ecoquery
 * 
 */
public class MinFrequency<L extends Literal<L>> implements Constraint<L> {

	private static final Logger LOG = LoggerFactory
			.getLogger(MinFrequency.class);

	/**
	 * The support constraint.
	 */
	private AbstractSupport<L> _support;
	/**
	 * The length of the content.
	 */
	private int _contentLength;
	/**
	 * The minimal number of occurrences of the pattern.
	 */
	private int _minFreq;

	/**
	 * Creates a {@link MinFrequency} constraint.
	 * 
	 * @param support
	 *            the support constraint associated to the min frequency
	 *            constraint.
	 * @param minFrequency
	 *            the minimal number of occurrences.
	 */
	public MinFrequency(AbstractSupport<L> support, int minFrequency) {
		_support = support;
		_contentLength = support.size();
		_minFreq = minFrequency;
	}

	@Override
	public final void addClauses(PBBuilder<L> satHandler)
			throws NoSolutionException {
		L[] literals = satHandler.lArray(_contentLength);
		for (int i = 0; i < _contentLength; i++) {
			literals[i] = _support.getLiteral(i);
			if (literals[i] == null) {
				LOG.error("null literal in support");
			}
		}
		satHandler.addPBInequality(literals, Ineq.GEQ, _minFreq);
	}
}
