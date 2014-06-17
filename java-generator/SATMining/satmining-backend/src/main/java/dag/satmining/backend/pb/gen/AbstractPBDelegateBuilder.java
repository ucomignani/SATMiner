/* ./satmining-backend/src/main/java/dag/satmining/backend/pb/gen/AbstractPBDelegateBuilder.java

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

package dag.satmining.backend.pb.gen;

import java.util.Collection;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.ClauseBuilder;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.ReifiedWeightedPBBuilder;
import dag.satmining.output.SolutionWriter;

public abstract class AbstractPBDelegateBuilder<L extends Literal<L>> implements ReifiedWeightedPBBuilder<L>{

	protected ClauseBuilder<L> _builder;
	
	public AbstractPBDelegateBuilder(ClauseBuilder<L> internalBuilder) {
		this._builder = internalBuilder;
	}

	public L newLiteral() {
		return _builder.newLiteral();
	}

	public L newStrongLiteral() {
		return _builder.newStrongLiteral();
	}

	public L newLiteral(boolean positive, boolean strong) {
		return _builder.newLiteral(positive, strong);
	}

	public void addReifiedConjunction(L equivalentTo, L[] lits)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, lits);
	}

	public void addReifiedConjunction(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, lits);
	}

	public L[] lArray(int size) {
		return _builder.lArray(size);
	}

	public L[] lArray(int size, boolean filled) {
		return _builder.lArray(size, filled);
	}

	public L[][] lMatrix(int size, int size2, boolean filled) {
		return _builder.lMatrix(size, size2, filled);
	}

	public L[][] lMatrix(int size, int size2) {
		return _builder.lMatrix(size, size2);
	}

	public void addClause(L[] l) throws NoSolutionException {
		_builder.addClause(l);
	}

	public void addReifiedClause(L equivalentTo, L[] lits)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, lits);
	}

	public void addClause(Collection<L> lits) throws NoSolutionException {
		_builder.addClause(lits);
	}

	public void addReifiedClause(L equivalentTo, Collection<L> lits)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, lits);
	}

	public void addToStrongBackdoor(L l) {
		_builder.addToStrongBackdoor(l);
	}

	public void endProblem() throws NoSolutionException {
		_builder.endProblem();
	}

	public void unify(L[] lits) throws NoSolutionException {
		_builder.unify(lits);
	}

	public L fromDimacs(int dimacs) {
		return _builder.fromDimacs(dimacs);
	}

	public void addReifiedConjunction(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		_builder.addReifiedConjunction(equivalentTo, l1, l2);
	}

	public void addClause(L l) throws NoSolutionException {
		_builder.addClause(l);
	}

	public void addReifiedClause(L equivalentTo, L l1, L l2)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, l1, l2);
	}

	public void addClause(L l1, L l2) throws NoSolutionException {
		_builder.addClause(l1, l2);
	}

	public void addReifiedClause(L equivalentTo, L l1, L l2, L l3)
			throws NoSolutionException {
		_builder.addReifiedClause(equivalentTo, l1, l2, l3);
	}

	public void addClause(L l1, L l2, L l3) throws NoSolutionException {
		_builder.addClause(l1, l2, l3);
	}

	public void unify(L l1, L l2) throws NoSolutionException {
		_builder.unify(l1, l2);
	}

	public SolutionWriter getCNFWriter() {
		return _builder.getCNFWriter();
	}
	
	
	
}
