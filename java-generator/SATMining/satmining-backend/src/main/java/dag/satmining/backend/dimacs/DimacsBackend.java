/* ./satmining-backend/src/main/java/dag/satmining/backend/dimacs/DimacsBackend.java

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

package dag.satmining.backend.dimacs;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dag.satmining.constraints.impl.AbstractClauseBuilder;

/**
 * Class for dimacs based backends.
 * 
 * @author ecoquery
 * 
 */
public abstract class DimacsBackend extends
		AbstractClauseBuilder<DimacsLiteral> {

	private static final Logger LOG = LoggerFactory.getLogger(DimacsBackend.class);
	
	private int _nextVariable;
	protected Set<Integer> _strongBackdoorClause;

	protected DimacsBackend() {
		this._strongBackdoorClause = new TreeSet<Integer>();
		this._nextVariable = 1;
	}

	@Override
	public DimacsLiteral newLiteral() {
		return newLiteral(true, false);
	}

	@Override
	public DimacsLiteral newStrongLiteral() {
		return newLiteral(true, true);
	}

	@Override
	public DimacsLiteral newLiteral(boolean positive, boolean strong) {
		int variable = _nextVariable++;
		int literal = positive ? variable : -variable;
		LOG.debug("new literal(strong: {}): {}",strong,literal);
		if (strong) {
			_strongBackdoorClause.add(variable);
		}
		return new DimacsLiteral(literal);
	}

	@Override
	public void addToStrongBackdoor(DimacsLiteral l) {
		_strongBackdoorClause.add(l.getVariableId());
	}

	public int getNbVariables() {
		return _nextVariable - 1;
	}

	@Override
	public DimacsLiteral[] lArray(int size) {
		return new DimacsLiteral[size];
	}

	@Override
	public DimacsLiteral[][] lMatrix(int size, int size2) {
		return new DimacsLiteral[size][size2];
	}

	@Override
	protected DimacsLiteral[] toArray(Collection<DimacsLiteral> c) {
		return c.toArray(new DimacsLiteral[c.size()]);
	}

	@Override
	public DimacsLiteral fromDimacs(int dimacs) {
		return new DimacsLiteral(dimacs);
	}
}
