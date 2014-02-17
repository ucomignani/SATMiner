/* ./satmining-seq/src/main/java/dag/satmining/problem/seq/SPClosed.java

   Copyright (C) 2013, 2014 Emmanuel Coquery.

This file is part of SATMiner

SATMiner is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

SATMiner is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with SATMiner; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

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
