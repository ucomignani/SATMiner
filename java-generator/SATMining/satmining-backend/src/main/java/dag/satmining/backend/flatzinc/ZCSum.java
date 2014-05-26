/* WSum.java

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

import dag.satmining.constraints.Ineq;

/**
 * @author ecoquery
 * 
 */
public class ZCSum extends ZincCollectionConstraint {
    private final int[] _coeffs;
    private final int _sum;
    private final Ineq _cmp;

    public ZCSum(ZincLiteral[] lits, int[] coefs, Ineq ineq, int value,
            ZincLiteral equiv) {
        super(equiv, lits);
        _coeffs = Arrays.copyOf(coefs, coefs.length);
        _sum = value;
        _cmp = ineq;
    }

    public ZCSum(ZincLiteral[] lits, Ineq ineq, int value, ZincLiteral equiv) {
        super(equiv, lits);
        _coeffs = new int[lits.length];
        Arrays.fill(_coeffs, 1);
        _sum = value;
        _cmp = ineq;
    }

    public ZCSum(Collection<ZincLiteral> lits, Ineq ineq, int value,
            ZincLiteral equiv) {
        this(lits.toArray(new ZincLiteral[lits.size()]), ineq, value, equiv);
    }

    @Override
    public void print(PrintWriter out) {
        if (_cmp == Ineq.EQ) {
            out.print("int_lin_eq");
        } else {
            out.print("int_lin_le");
        }
        if (_equivTo != null) {
            out.print("_reif");
        }
        out.print("(");
        for (int i = 0; i < _lits.length; ++i) {
            out.print(i == 0 ? "{" : ",");
            out.print(_cmp == Ineq.GEQ ? -_coeffs[i] : _coeffs[i]);
        }
        for (int i = 0; i < _lits.length; ++i) {
            out.print(i == 0 ? "}, {" : ",");
            _lits[i].printI(out);
        }
        out.print("}, ");
        out.print(_cmp == Ineq.GEQ ? -_sum : _sum);
        if (_equivTo != null) {
            out.print(", ");
            _equivTo.printB(out);
        }
        out.println(");");
    }

}
