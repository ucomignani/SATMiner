/* ./satmining-core/src/main/java/dag/satmining/constraints/formula/NAryFormula.java

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NAryFormula extends SFormula {

	protected Formula[] _args;

	public NAryFormula(Collection<Formula> args) {
		this._args = new Formula[args.size()];
		this._args = args.toArray(this._args);
	}
	
	public NAryFormula(Formula... args){
		this._args = args;
	}

	@Override
	protected SFormula sanitize(boolean pushNeg) {
		List<Formula> newArgs = new ArrayList<Formula>();
		boolean unchanged = true;
		for (Formula f : _args) {
			Formula f2 = f.sanitize(pushNeg);
			if (f != f2) {
				unchanged = false;
			}
			newArgs.add(f2);
		}
		if (unchanged) {
			return this;
		} else if (pushNeg) {
			return dualFromArgs(newArgs);
		} else {
			return sameFromArgs(newArgs);
		}
	}

	protected abstract SFormula sameFromArgs(List<Formula> args);

	protected abstract SFormula dualFromArgs(List<Formula> args);

}