package dag.satmining.constraints.formula;

import java.util.Collection;
import java.util.List;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

/**
 * Root class for boolean formulas. Also serves as a builder.
 * 
 * @author ecoquery
 * 
 * @param <L>
 */
public abstract class Formula {

	public Formula() {
	}

	public <L extends Literal<L>> void tseitin(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		L eq = tseitinLit(builder);
		builder.addClause(eq);
	}

	public abstract <L extends Literal<L>> L tseitinLit(MinimalClauseBuilder<L> builder)
			throws NoSolutionException;

	public <L extends Literal<L>> void direct(MinimalClauseBuilder<L> builder)
			throws NoSolutionException {
		List<List<L>> toAdd = sanitize(false).directGen(builder);
		for (List<L> c : toAdd) {
			builder.addClause(c);
		}
	}

	protected abstract SFormula sanitize(boolean pushNeg);

	public static SFormula and(Formula... args) {
		return new Conjunction(args);
	}

	public static SFormula or(Formula... args) {
		return new Disjunction(args);
	}
	
	public static SFormula and(Literal<?>... args) {
		Formula[] atoms = new Formula[args.length];
		for(int i = 0; i < args.length; i++) {
			atoms[i] = new Atom(args[i]);
		}
		return and(atoms);
	}
	
	public static SFormula or(Literal<?>... args) {
		Formula[] atoms = new Formula[args.length];
		for(int i = 0; i < args.length; i++) {
			atoms[i] = new Atom(args[i]);
		}
		return or(atoms);
	}
	
	public static SFormula and(Literal<?> lit, Formula f) {
		return and(atom(lit),f);
	}
	
	public static SFormula and(Formula f, Literal<?> lit) {
		return and(f,atom(lit));
	}
	
	public static SFormula or(Literal<?> lit, Formula f) {
		return or(atom(lit),f);
	}
	
	public static SFormula or(Formula f, Literal<?> lit) {
		return or(f,atom(lit));
	}

	public static SFormula and(
			Collection<Formula> args) {
		return new Conjunction(args);
	}

	public static SFormula or(
			Collection<Formula> args) {
		return new Disjunction(args);
	}
	
	public static Formula atom(Literal<?> arg) {
		return new Atom(arg);
	}

	public static  Formula not(Formula arg) {
		return new Neg(arg);
	}

	public static Formula not(Literal<?> arg) {
		return new Neg(atom(arg));
	}

	public static Formula impl(Formula arg1, Formula arg2) {
		return new Implication(arg1, arg2);
	}

	public static Formula equiv(Formula arg1, Formula arg2) {
		return new Equivalence(arg1, arg2);
	}

	public static Formula equiv(Literal<?> arg1, Formula arg2) {
		return new Equivalence(atom(arg1), arg2);
	}

	public static Formula equiv(Formula arg1, Literal<?> arg2) {
		return new Equivalence(arg1, atom(arg2));
	}
}
