/* ZincLiteral.java

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

import dag.satmining.constraints.Literal;

/**
 * @author Emmanuel Coquery <emmanuel.coquery@liris.cnrs.fr> Implementation of
 *         the {@link Literal} interface for FlatZinc backend.
 */
public final class ZincLiteral implements Literal<ZincLiteral>, Comparable<ZincLiteral> {

    private final int _vid;
    private int _idx;
    private final boolean _pos;
    private ZincLiteral _opposite = null; 
    private boolean _strong = false; 
    static final String STRONG_POSITIVE = "sp";
    static final String STRONG_NEGATIVE = "sn";
    static final String INTRODUCED_POSITIVE = "ip";
    static final String INTRODUCED_NEGATIVE = "in";
    static final String INTRODUCED_INT_POS = "iip";
    static final String INTRODUCED_INT_NEG = "iin";
    
    
    public ZincLiteral(int vid, int idx, boolean pos, boolean isStrong) {
        this._vid = vid;
        this._idx = idx;
        this._pos = pos;
        this._strong = isStrong;
    }

    public int getIdx() {
        return _idx;
    }

    @Override
    public boolean isPositive() {
        return _pos;
    }

    @Override
    public ZincLiteral getOpposite() {
        if (_opposite == null) {
            _opposite = new ZincLiteral(_vid, _idx, !_pos, _strong);
            _opposite._opposite = this;
        }
        return _opposite;
    }

    @Override
    public int getVariableId() {
        return _vid;
    }

    @Override
    public int toDimacs() {
        return _vid * (_pos ? 1 : -1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_pos ? 1231 : 1237);
        result = prime * result + _vid;
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
        ZincLiteral other = (ZincLiteral) obj;
        if (_pos != other._pos)
            return false;
        if (_vid != other._vid)
            return false;
        return true;
    }

    @Override
    public int compareTo(ZincLiteral o) {
        return this.toDimacs() - o.toDimacs();
    }

    public void setIdx(int newIdx) {
        _idx = newIdx;
        if (_opposite != null) {
            _opposite._idx = newIdx;
        }
    }

    void printB(PrintWriter out) {
        if (_pos) {
            if (_strong) {
                out.print(STRONG_POSITIVE);
            } else {
                out.print(INTRODUCED_POSITIVE);
            }
        } else {
            if (_strong) {
                out.print(STRONG_NEGATIVE);
            } else {
                out.print(INTRODUCED_NEGATIVE);
            }
        }
        out.print("[");
        out.print(_idx);
        out.print("]");
    }
    
    void printI(PrintWriter out) {
        if (_pos) {
            out.print(INTRODUCED_INT_POS);
        } else {
            out.print(INTRODUCED_INT_NEG);
        }
        out.print("[");
        out.print(_vid);
        out.print("]");
    }

    public boolean isStrong(){
        return _strong;
    }
    
    public void setStrong(boolean strong) {
        this._strong = strong;
        if (_opposite != null) {
            _opposite._strong = strong;
        }
    }
    
    void printOppositeConstraintB(PrintWriter out) {
        out.print("bool_not(");
        printB(out);
        out.print(",");
        _opposite.printB(out);
        out.println(");");
    }
    
    void printIntBoolConstraint(PrintWriter out) {
        out.print("bool2int(");
        printB(out);
        out.print(",");
        printI(out);
        out.println(");");
    }    
}
