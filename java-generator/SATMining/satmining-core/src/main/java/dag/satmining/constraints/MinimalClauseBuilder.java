/* ./satmining-core/src/main/java/dag/satmining/constraints/MinimalClauseBuilder.java

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
import dag.satmining.output.SolutionWriter;

/**
 * Minimal interface to implement to be used as a model backend.
 * 
 * @author ecoquery
 * 
 * @param <L>
 *            the type of literals. May be specialized for better performance by
 *            avoiding dynamic casts.
 */
public interface MinimalClauseBuilder<L extends Literal<L>> {

	/**
	 * The representation of a dimacs literal.
	 * @param dimacs the dimacs representation of the literal
	 * @return the literal specialized representation
	 */
	L fromDimacs(int dimacs);
	
	/**
	 * Creates a positive literal from a new non strong variable.
	 * 
	 * @return the created literal.
	 */
	L newLiteral();

	/**
	 * Creates a new literal from a new strong variable.
	 * 
	 * @param positive
	 *            sign of the literal.
	 * @return the created literal.
	 */
	L newStrongLiteral();

	/**
	 * Creates a new literal from a new variable.
	 * 
	 * @param positive
	 *            sign of the literal.
	 * @param strong
	 *            true if the variable is in the strong backdoor.
	 * @return
	 */
	L newLiteral(boolean positive, boolean strong);

	/**
	 * Creates an L array of the given size.
	 * 
	 * @param size
	 * @return the created array.
	 */
	L[] lArray(int size);

	/**
	 * Creates an L array of the given size.
	 * 
	 * @param size
	 * @param filled
	 *            true if the array is to be initialized with (non strong) new
	 *            literals.
	 * @return the created array.
	 */
	L[] lArray(int size, boolean filled);

	/**
	 * Creates a L matrix of the given size.
	 * 
	 * @param size
	 * @param size2
	 * @return the created matrix.
	 */
	L[][] lMatrix(int size, int size2);

	/**
	 * Creates a L matrix of the given size.
	 * 
	 * @param size
	 * @param size2
	 * @param filled
	 *            true if the matrix is to be initialized with (non strong) new
	 *            literals.
	 * @return the created matrix.
	 */
	L[][] lMatrix(int size, int size2, boolean filled);

	/**
	 * Adds a clause to the problem.
	 * 
	 * @param lits
	 *            the literals to be included in the clause.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addClause(L[] lits) throws NoSolutionException;

	/**
	 * Adds a clause to the problem.
	 * 
	 * @param l
	 *            the literal to be included in the clause.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addClause(L l) throws NoSolutionException;

	/**
	 * Adds a clause to the problem.
	 * 
	 * @param l1
	 *            the 1st literal to be included in the clause.
	 * @param l2
	 *            the 2nd literal to be included in the clause. the literals to
	 *            be included in the clause.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addClause(L l1, L l2) throws NoSolutionException;

	/**
	 * Adds a clause to the problem.
	 * 
	 * @param l1
	 *            the 1st literal to be included in the clause.
	 * @param l2
	 *            the 2nd literal to be included in the clause.
	 * @param l3
	 *            the 3rd literal to be included in the clause.
	 * 
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addClause(L l1, L l2, L l3) throws NoSolutionException;

	/**
	 * Adds a clause to the problem.
	 * 
	 * @param lits
	 *            the literals to be included in the clause.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addClause(Collection<L> lits) throws NoSolutionException;

	/**
	 * Add a previously created variable to the strong backdoor.
	 * 
	 * @param l
	 */
	void addToStrongBackdoor(L l);

	/**
	 * Tells the builder that the problem is written. This can be used e.g. to
	 * effectively write clauses to disk after computing the number of
	 * variables, clauses, etc.
	 * 
	 * @throws NoSolutionException
	 */
	void endProblem() throws NoSolutionException;

	/**
	 * Make the provided literals equivalent.
	 * 
	 * @param lits
	 *            the literals to make equivalent.
	 * @throws NoSolutionException
	 */
	void unify(L[] lits) throws NoSolutionException;

	/**
	 * Make the provided literals equivalent.
	 * 
	 * @param l1
	 *            first literal
	 * @param l2
	 *            second literal
	 * @throws NoSolutionException
	 */
	void unify(L l1, L l2) throws NoSolutionException;

	/**
	 * A writer that outputs the generated formula.
	 * 
	 * @return a writer to write the generated formula instead of the computed
	 *         patterns.
	 */
	SolutionWriter getCNFWriter();

}
