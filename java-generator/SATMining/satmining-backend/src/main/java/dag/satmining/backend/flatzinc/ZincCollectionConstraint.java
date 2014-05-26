/* ZincCollectionConstraint.java

   Copyright (C) 2014 Emmanuel Coquery.

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
exception statement from your version.

 */

/**
 * 
 */
package dag.satmining.backend.flatzinc;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;

import static dag.satmining.backend.flatzinc.ZincBackend.TRUE;

/**
 * @author ecoquery
 *
 */
public abstract class ZincCollectionConstraint implements ZincConstraint {

    protected final ZincLiteral[] _lits;
    protected final ZincLiteral _equivTo;

    public ZincCollectionConstraint(ZincLiteral equiv, ZincLiteral ...lits) {
        this._lits = Arrays.copyOf(lits, lits.length);
        this._equivTo = equiv;
    }
    
    public ZincCollectionConstraint(ZincLiteral equiv, Collection<ZincLiteral> lits) {
        this._lits = lits.toArray(new ZincLiteral[lits.size()]);
        this._equivTo = equiv;
    }
    
    protected void printLitsAsArray(PrintWriter out) {
        out.print("{");
        _lits[0].printB(out);
        for(int i = 1; i < _lits.length; ++i) {
            out.print(", ");
            _lits[i].printB(out);
        }
        out.print("}");
    }
    
    protected void printEQLit(PrintWriter out) {
        if (_equivTo == null) {
            out.print(TRUE);
        } else {
            _equivTo.printB(out);
        }
    }
    
    protected void printWithEq(PrintWriter out, String constraintName) {
        out.print(constraintName);
        out.print("(");
        printLitsAsArray(out);
        out.print(", ");
        printEQLit(out);
        out.println(");");
    }

    protected void printNoEq(PrintWriter out, String constraintName) {
        out.print(constraintName);
        out.print("(");
        printLitsAsArray(out);
        out.println(");");
    }

}
