package dag.satmining.constraints.formula;

import java.util.List;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

/**
 * Formulas that can generate directly CNFs, i.e. Atom, Conjunction, Disjunction.
 * 
 * @author ecoquery
 *
 * @param <L>
 */
public abstract class SFormula extends Formula {

	/**
	 * The CNF standing for this formula.
	 * 
	 * @return
	 */
	protected abstract <L extends Literal<L>> List<List<L>> directGen(MinimalClauseBuilder<L> builder);
	
}
