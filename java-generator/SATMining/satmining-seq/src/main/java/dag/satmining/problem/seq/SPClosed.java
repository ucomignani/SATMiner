/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.problem.seq;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

/**
 * 
 * @author ecoquery
 */
public class SPClosed<L extends Literal<L>> implements Constraint<L> {

	private final static Logger LOG = LoggerFactory.getLogger(SPClosed.class);
	private SPDomain<L> _domain;
	private SPSupport<L> _support;

	public SPClosed(SPDomain<L> domain, SPSupport<L> support) {
		this._domain = domain;
		this._support = support;
	}

	@Override
	public void addClauses(PBBuilder<L> satHandler) throws NoSolutionException {
		// no replacing of chars by jokers
		for (int c = 0; c < _domain.getJoker(); ++c) { // for all letters except
														// the joker
			for (int i = 0; i < _domain.getPatternMaxSize(); ++i) { // for all
																	// positions
																	// in the
																	// pattern
				addLetterPositionClosed(c, i, satHandler);
			}
		}
		// no possible extension by shifting the pattern
		for (int c = 0; c < _domain.getJoker(); ++c) { // for all letters except
														// the joker
			for (int j = 1; j < _domain.getPatternMaxSize(); ++j) { // for all
																	// possible
																	// shift
																	// amount
				addLetterShiftMismatch(c, j, satHandler);
			}
			LOG.debug("Added shift on letter {}, joker is {}", c,
					_domain.getJoker());
		}
	}

	/**
	 * Formula expressing the closeness of the pattern at the given pattern
	 * position and for the given letter.
	 * 
	 * @param letter
	 *            the letter to consider
	 * @param i
	 *            the position in the pattern
	 * @param satHandler
	 *            the handler for adding clauses
	 */
	private void addLetterPositionClosed(int letter, int i,
			PBBuilder<L> satHandler) throws NoSolutionException {
		List<L> conj = new ArrayList<L>();
		for (int k = 0; k < _support.size(); ++k) {
			if (_support.size() <= i + k
					|| _support.getLetterAt(i + k) != letter) {
				conj.add(_support.getLiteral(k).getOpposite());
			}
		}
		L eqConj = satHandler.newStrongLiteral();
		satHandler.addReifiedConjunction(eqConj, conj);
		satHandler.addClause(eqConj.getOpposite(),
				_domain.getLiteral(i, letter, true));
	}

	/**
	 * Adds a constraint that if the pattern is small enough there should be at
	 * least one position of the pattern that make the pattern not extensible by
	 * shift.
	 * 
	 * @param letter
	 *            the candidate letter for shift expansion
	 * @param shift
	 *            the amount of shifting
	 * @param satHandler
	 *            the satHandler for generating and adding the clauses
	 * @throws NoSolutionException
	 */
	private void addLetterShiftMismatch(int letter, int shift,
			PBBuilder<L> satHandler) throws NoSolutionException {
		List<L> lits = new ArrayList<L>();
		for (int k = 0; k < _support.size(); ++k) {
			if (k - shift < 0 || _support.getLetterAt(k - shift) != letter) {
				lits.add(_support.getLiteral(k));
			}
		}
		L eqClause = satHandler.newStrongLiteral();
		satHandler.addReifiedClause(eqClause, lits);
		satHandler.addClause(_domain.trailLengthIsAtLeast(shift).getOpposite(),
				eqClause);
	}
}
