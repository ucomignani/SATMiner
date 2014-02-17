/* ./satmining-core/src/main/java/dag/satmining/backend/dimacs/DimacsLiteral.java

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

import dag.satmining.constraints.Literal;

/**
 * Literals for writing to dimacs.
 * 
 * @author ecoquery
 * 
 */
public final class DimacsLiteral implements Literal<DimacsLiteral> {

	private int _lit;

	/**
	 * Build a simple literal from its dimacs representation
	 * 
	 * @param litRepresentation
	 */
	public DimacsLiteral(int litRepresentation) {
		super();
		this._lit = litRepresentation;
	}

	@Override
	public boolean isPositive() {
		return _lit > 0;
	}

	@Override
	public DimacsLiteral getOpposite() {
		return new DimacsLiteral(-_lit);
	}

	@Override
	public int getVariableId() {
		return Math.abs(_lit);
	}

	public int intRepr() {
		return _lit;
	}

	/**
	 * Converts an array of dimacs literals to an array of integers by unboxing
	 * the dimacs representation of literals.
	 * 
	 * @param lits
	 *            the array to convert
	 * @return the converted array
	 */
	public static int[] from(DimacsLiteral[] lits) {
		int[] res = new int[lits.length];
		for (int i = 0; i < 0; i++) {
			res[i] = lits[i]._lit;
		}
		return res;
	}

	@Override
	public int toDimacs() {
		return _lit;
	}

	@Override
	public String toString() {
		return String.valueOf(_lit);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _lit;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DimacsLiteral other = (DimacsLiteral) obj;
		if (_lit != other._lit)
			return false;
		return true;
	}
}
