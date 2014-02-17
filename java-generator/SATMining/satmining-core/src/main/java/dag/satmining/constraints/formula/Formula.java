/* ./satmining-core/src/main/java/dag/satmining/constraints/formula/Formula.java

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
