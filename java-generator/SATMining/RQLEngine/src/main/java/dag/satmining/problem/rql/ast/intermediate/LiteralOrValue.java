/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/intermediate/LiteralOrValue.java

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

package dag.satmining.problem.rql.ast.intermediate;

import dag.satmining.constraints.Literal;
import dag.satmining.constraints.MinimalClauseBuilder;

public final class LiteralOrValue {
	
	int _l;
	boolean _v;
	public static final LiteralOrValue TRUE = new LiteralOrValue(true);
	public static final LiteralOrValue FALSE = new LiteralOrValue(false);

	private LiteralOrValue(boolean b) {
		this._v = b;
		this._l = 0;
	}

	public LiteralOrValue(int l) {
		this._v = false;
		this._l = l;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _l;
		result = prime * result + (_v ? 1231 : 1237);
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
		LiteralOrValue other = (LiteralOrValue) obj;
		if (_l != other._l)
			return false;
		if (_v != other._v)
			return false;
		return true;
	}

	public LiteralOrValue neg() {
		if (_l == 0) {
			return (_v ? FALSE : TRUE);
		} else {
			return new LiteralOrValue(-_l);
		}
	}
	
	public <L extends Literal<L>> L getLiteral(MinimalClauseBuilder<L> b) {
		return b.fromDimacs(_l);
	}
	
	public static LiteralOrValue trueV() {
		return TRUE;
	}
	
	public static LiteralOrValue falseV() {
		return FALSE;
	}
}