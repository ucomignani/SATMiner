package dag.satmining.constraints;

import java.util.Collection;

import dag.satmining.NoSolutionException;

/**
 * Extends ClauseBuilder with pseudo boolean constraints.
 * 
 * @author ecoquery
 * 
 * @param <L>
 *            the type of literals.
 */
public interface PBBuilder<L extends Literal<L>> extends ClauseBuilder<L> {
	/**
	 * Adds a pseudo boolean inequality.
	 * 
	 * @param lits
	 *            the literals to count.
	 * @param ineq
	 *            the type of inequality.
	 * @param value
	 *            the integer to compare to.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addPBInequality(L[] lits, Ineq ineq, int value)
			throws NoSolutionException;

	/**
	 * Adds a pseudo boolean inequality.
	 * 
	 * @param lits
	 *            the literals to count.
	 * @param ineq
	 *            the type of inequality.
	 * @param value
	 *            the integer to compare to.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addPBInequality(Collection<L> lits, Ineq ineq, int value)
			throws NoSolutionException;

	/**
	 * Adds a reified pseudo boolean inequality.
	 * 
	 * @param lits
	 *            the literals to count.
	 * @param ineq
	 *            the type of inequality.
	 * @param value
	 *            the integer to compare to.
	 * @param equivalentTo
	 *            the literal that will be equivalent to the inequality.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedPBInequality(L[] lits, Ineq ineq, int value, L equivalentTo)
			throws NoSolutionException;

	/**
	 * Adds a reified pseudo boolean inequality.
	 * 
	 * @param lits
	 *            the literals to count.
	 * @param ineq
	 *            the type of inequality.
	 * @param value
	 *            the integer to compare to.
	 * @param equivalentTo
	 *            the literal that will be equivalent to the inequality.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addReifiedPBInequality(Collection<L> lits, Ineq ineq, int value,
			L equivalentTo) throws NoSolutionException;

	/**
	 * Adds clauses specifying that exactly one literal should be true among
	 * lits.
	 * 
	 * @param lits
	 *            the literals.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addExactlyOneTrue(Collection<L> lits) throws NoSolutionException;

	/**
	 * Adds clauses specifying that exactly one literal should be true among
	 * lits.
	 * 
	 * @param lits
	 *            the literals.
	 * @throws NoSolutionException
	 *             if the backend determines that this clause will make the SAT
	 *             problem unsatisfiable.
	 */
	void addExactlyOneTrue(L[] lits) throws NoSolutionException;

}
