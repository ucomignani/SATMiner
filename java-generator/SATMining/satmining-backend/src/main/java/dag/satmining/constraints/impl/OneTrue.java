package dag.satmining.constraints.impl;

import static dag.satmining.constraints.formula.Formula.*;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Ineq;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public class OneTrue<L extends Literal<L>> {

	private MinimalClauseBuilder<L> _builder;

	public OneTrue(MinimalClauseBuilder<L> builder) {
		super();
		this._builder = builder;
	}

	private L[] buildFound(L[] a) throws NoSolutionException {
		L[] found = _builder.lArray(a.length);
		found[0] = a[0];
		for (int i = 1; i < a.length; i++) {
			// found[i] <=> found[i-1] \/ a[i]
			found[i] = or(found[i - 1], a[i]).tseitinLit(_builder);
		}
		return found;
	}

	private L[] buildFound2(L[] a, L[] found) throws NoSolutionException {
		L[] found2 = _builder.lArray(a.length);
		found2[0] = _builder.newLiteral();
		_builder.addClause(found2[0].getOpposite());
		// found2[1] <=> a[0] /\ a[1]
		found2[1] = and(a[0], a[1]).tseitinLit(_builder);
		for (int i = 2; i < a.length; i++) {
			// found2[i] <=> found2[i-1] \/ (found[i-1] /\ a[i])
			found2[i] = or(found2[i - 1], and(found[i - 1], a[i])).tseitinLit(
					_builder);
		}
		return found2;
	}

	public void atMostOneTrue(L[] a) throws NoSolutionException {
		L[] found = buildFound(a);
		for (int i = 1; i < a.length; i++) {
			// ~(found[i-1] /\ a[i])
			_builder.addClause(found[i - 1].getOpposite(), a[i].getOpposite());
		}
	}

	public void atMostOneTrueR(L[] a, L eq) throws NoSolutionException {
		L[] found = buildFound(a);
		L[] found2 = buildFound2(a, found);
		// eq <=> ~found2[a.length-1]
		_builder.unify(eq, found2[a.length - 1].getOpposite());
	}

	public void atLeastOneTrue(L[] a) throws NoSolutionException {
		_builder.addClause(a);
	}

	public void atLeastOneTrueR(L[] a, L eq) throws NoSolutionException {
		equiv(eq, or(a)).direct(_builder);
	}

	public void exactlyOneTrue(L[] lits) throws NoSolutionException {
		_builder.addClause(lits);
		atMostOneTrue(lits);
	}

	public void exactlyOneTrueR(L[] a, L eq) throws NoSolutionException {
		L atMostOne = _builder.newLiteral();
		atMostOneTrueR(a, atMostOne);
		L atLeastOne = _builder.newLiteral();
		atLeastOneTrueR(a, atLeastOne);
		equiv(eq,and(atMostOne,atLeastOne)).direct(_builder);
	}

	public void addIneq(L[] a, Ineq ineq) throws NoSolutionException {
		switch (ineq) {
		case EQ:
			exactlyOneTrue(a);
			break;
		case LEQ:
			atMostOneTrue(a);
			break;
		case GEQ:
			atLeastOneTrue(a);
		}
	}
	
	public void addRIneq(L[]a, Ineq ineq, L eq) throws NoSolutionException {
		switch(ineq) {
		case EQ:
			exactlyOneTrueR(a, eq);
			break;
		case GEQ:
			atLeastOneTrueR(a, eq);
			break;
		case LEQ:
			atMostOneTrueR(a, eq);
		}
	}
}
