package dag.satmining.constraints;

import dag.satmining.NoSolutionException;

/**
 * Abstract class for representing constraints.
 * 
 * @author ecoquery
 */
public interface Constraint<L extends Literal<L>> {

	/**
	 * Adds the SAT clauses matching this high-level constraint to the sat
	 * handler.
	 * 
	 * @param satHandler
	 *            the object to which to add clauses.
	 */
	public abstract void addClauses(PBBuilder<L> satHandler)
			throws NoSolutionException;

}
