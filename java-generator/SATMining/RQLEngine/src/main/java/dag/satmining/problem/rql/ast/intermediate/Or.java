/* ./RQLEngine/src/main/java/dag/satmining/problem/rql/ast/intermediate/Or.java

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

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;

import dag.satmining.NoSolutionException;
import dag.satmining.constraints.Literal;
import dag.satmining.constraints.PBBuilder;

public class Or extends BFormula {

	private final BFormula[] _disj;
	private BitSet _mask = null;

	public Or(BFormula... disj) {
		_disj = disj;
	}

	public Or(Collection<BFormula> conj) {
		this(conj.toArray(new BFormula[conj.size()]));
	}

	@Override
	BitSet getMask() {
		if (_mask == null) {
			_mask = new BitSet();
			for (BFormula f : _disj) {
				_mask.or(f.getMask());
			}
		}
		return _mask;
	}

	@Override
	<L extends Literal<L>> LiteralOrValue makeRepresentation(
			PBBuilder<L> handler, BitSet data) throws NoSolutionException {
		HashSet<L> lits = new HashSet<L>();
		for (BFormula f : _disj) {
			LiteralOrValue v = f.getRepresentation(handler, data);
			if (LiteralOrValue.FALSE.equals(v)) {
				// do nothing
			} else if (LiteralOrValue.TRUE.equals(v)) {
				return LiteralOrValue.trueV();
			} else {
				lits.add(v.getLiteral(handler));
			}
		}
		if (lits.isEmpty()) {
			return LiteralOrValue.falseV();
		}
		if (lits.size() == 1) {
			return new LiteralOrValue(lits.iterator().next().toDimacs());
		} else {
			L eq = handler.newLiteral();
			handler.addReifiedClause(eq, lits);
			return new LiteralOrValue(eq.toDimacs());
		}
	}

}
