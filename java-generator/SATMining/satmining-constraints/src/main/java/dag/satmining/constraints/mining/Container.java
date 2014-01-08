package dag.satmining.constraints.mining;

import dag.satmining.constraints.Constraint;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

import java.util.ArrayList;
import java.util.Collection;

import dag.satmining.NoSolutionException;

/**
 * A constraint which is simply a conjunction of other constraints.
 * 
 * @author ecoquery
 * 
 */
public class Container<L extends Literal<L>> implements Constraint<L> {

	private Collection<Constraint<L>> _constraints;

	public Container() {
		_constraints = new ArrayList<Constraint<L>>();
	}

	public final void addConstraint(Constraint<L> constraint) {
		_constraints.add(constraint);
	}

	@Override
	public final void addClauses(PBBuilder<L> sat) throws NoSolutionException {
		for (Constraint<L> constraint : _constraints) {
			constraint.addClauses(sat);
		}
	}

}
