/* ./satmining-constraints/src/main/java/dag/satmining/constraints/mining/AbstractSupport.java

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

package dag.satmining.constraints.mining;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;

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
	private void initLiterals(ReifiedWeightedPBBuilder<L> sat) {
		_positionLiterals = sat.lArray(_size);
		for (int pos = 0; pos < _positionLiterals.length; pos++) {
			_positionLiterals[pos] = sat.newLiteral();
		}
	}

	@Override
	public void addClauses(ReifiedWeightedPBBuilder<L> sat) throws NoSolutionException {
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
	protected abstract void addMatchAt(int patternPos, ReifiedWeightedPBBuilder<L> satHandler)
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
