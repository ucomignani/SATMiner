/* ./satmining-backend/src/main/java/dag/satmining/backend/boolvarpb/BVPBOption.java

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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.boolvarpb;

import boolvar.model.constraints.PBconstraint;

/**
 *
 * @author ecoquery
 */
public enum BVPBOption {
    
    Linear, Direct, Bdd, Auto, PolyW, GlobC;
    
    public static BVPBOption fromString(String s) {
        if (s != null) {
            String lS = s.toLowerCase();
            for (BVPBOption o : BVPBOption.values()) {
                if (o.toString().toLowerCase().equals(lS)) {
                    return o;
                }
            }   
        }
        throw new IllegalArgumentException("unrecognized value: "+s);
    }

    public static BVPBOption fromBVPBCst(int cst) {
        switch (cst) {
            case PBconstraint.LINEAR:
                return Linear;
            case PBconstraint.DIRECT:
                return Direct;
            case PBconstraint.BDD:
                return Bdd;
            case PBconstraint.AUTO:
                return Auto;
            case PBconstraint.POLYW:
                return PolyW;
            case PBconstraint.GLOBC:
                return GlobC;
            default:
                throw new IllegalArgumentException("unrecognized value: "+cst);
        }
    }
    
    public int toBVPBCst() {
        switch (this) {
            case Auto:
                return PBconstraint.AUTO;
            case Bdd:
                return PBconstraint.BDD;
            case Direct:
                return PBconstraint.DIRECT;
            case GlobC:
                return PBconstraint.GLOBC;
            case Linear:
                return PBconstraint.LINEAR;
            case PolyW:
                return PBconstraint.POLYW;
        }
        throw new IllegalArgumentException("unrecognized value: "+this);
    }
    
    public static BVPBOption getVariant() {
        return fromBVPBCst(PBconstraint.getVariant());
    }
    
    public static void setVariant(BVPBOption o) {
        PBconstraint.setVariant(o.toBVPBCst());
    }
    
}
