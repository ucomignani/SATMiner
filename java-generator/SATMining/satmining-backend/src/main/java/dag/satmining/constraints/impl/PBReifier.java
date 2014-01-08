package dag.satmining.constraints.impl;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

/**
 * Allows to add reified pb constraints from non reified ones
 * 
 * @author ecoquery
 * 
 */
public class PBReifier<L extends Literal<L>> {

	public static interface WeightedPBBuilder<L extends Literal<L>> extends
			MinimalClauseBuilder<L> {
		void addWPBInequality(L[] lits, int[] coefs, Ineq ineq, int value)
				throws NoSolutionException;
	}

	private WeightedPBBuilder<L> _builder;

	public PBReifier(WeightedPBBuilder<L> builder) {
		this._builder = builder;
	}

	public void addReifiedIneqality(L[] lits, Ineq ineq, int value,
			L equivalentTo) throws NoSolutionException {
		switch (ineq) {
		case GEQ:
			addReifiedGeq(lits, value, equivalentTo);
			break;
		case EQ:
			addReifiedIneqality(lits, Ineq.LEQ, value, equivalentTo);
			addReifiedIneqality(lits, Ineq.GEQ, value, equivalentTo);
			break;
		case LEQ:
			addReifiedIneqality(lits, Ineq.GEQ, value + 1,
					equivalentTo.getOpposite());
			break;
		default:
			throw new Error("Bug in Ineq: unknown case");
		}
	}

	private void addReifiedGeq(L[] lits, int value, L eqLit)
			throws NoSolutionException {
		int n = lits.length;
		// eqLit <=> (lits[0] + ... + lits[n-1] >= v)
		//
		// Transformed into to weighted inequalities
		//
		// lits[0] + ... + lits[n-1] + v* ~eqLit >= v
		//
		// ~lits[0] + ... + ~lits[n-1] + (n-v)*eqLit >= n-v
		//
		L[] wLits1 = _builder.lArray(n + 1);
		int[] coefs1 = new int[n + 1];
		L[] wLits2 = _builder.lArray(n + 1);
		int[] coefs2 = new int[n + 1];
		for (int i = 0; i < n; i++) {
			coefs1[i] = coefs2[i] = 1;
			wLits1[i] = lits[i];
			wLits2[i] = lits[i].getOpposite();
		}
		coefs1[n] = value;
		wLits1[n] = eqLit.getOpposite();
		coefs2[n] = (n - value + 1);
		wLits2[n] = eqLit;
		_builder.addWPBInequality(wLits1, coefs1, Ineq.GEQ, value);
		_builder.addWPBInequality(wLits2, coefs2, Ineq.GEQ, n - value + 1);
	}

}
