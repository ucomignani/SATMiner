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
