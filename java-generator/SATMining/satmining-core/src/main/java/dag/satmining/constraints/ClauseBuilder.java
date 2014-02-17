/* ./satmining-core/src/main/java/dag/satmining/constraints/ClauseBuilder.java

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

package dag.satmining.constraints;

import java.util.Collection;

import dag.satmining.NoSolutionException;

/**
 * Enables the use of reified clauses conjunctions and clauses.
 * 
 * @author ecoquery
 * 
 * @param <L>
 *            the type of literals (see {@link MinimalClauseBuilder}).
 */
public interface ClauseBuilder<L extends Literal<L>> extends
		MinimalClauseBuilder<L> {

	/**
	 * Adds the equivalent between the conjunction of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the conjunction
	 * @param lits
	 *            the literals appearing in the conjunction
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedConjunction(L equivalentTo, L [] lits) throws NoSolutionException;

	/**
	 * Adds the equivalent between the conjunction of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the conjunction
	 * @param l1
	 *            the 1st appearing in the conjunction
	 * @param l2
	 *            the 2nd appearing in the conjunction
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedConjunction(L equivalentTo, L l1, L l2) throws NoSolutionException;

	/**
	 * Adds the equivalent between the conjunction of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the conjunction
	 * @param lits
	 *            the literals appearing in the conjunction
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedConjunction(L equivalentTo, Collection<L> lits) throws NoSolutionException;

	/**
	 * Adds the equivalent between the clause of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the clause
	 * @param lits
	 *            the literals appearing in the clause
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedClause(L equivalentTo, L [] lits) throws NoSolutionException;

	/**
	 * Adds the equivalent between the clause of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the clause
	 * @param l1
	 *            the 1st literal appearing in the clause
	 * @param l2
	 *            the 2nd literal appearing in the clause
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedClause(L equivalentTo, L l1, L l2) throws NoSolutionException;

	/**
	 * Adds the equivalent between the clause of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the clause
	 * @param l1
	 *            the 1st literal appearing in the clause
	 * @param l2
	 *            the 2nd literal appearing in the clause
	 * @param l3
	 *            the 3rd literal appearing in the clause
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedClause(L equivalentTo, L l1, L l2, L l3) throws NoSolutionException;

	/**
	 * Adds the equivalent between the clause of lits and the literal
	 * equivalentTo
	 * 
	 * @param equivalentTo
	 *            the literal the will equivalent to the clause
	 * @param lits
	 *            the literals appearing in the clause
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedClause(L equivalentTo, Collection<L> lits) throws NoSolutionException;

}
