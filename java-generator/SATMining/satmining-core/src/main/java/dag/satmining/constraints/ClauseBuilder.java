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
