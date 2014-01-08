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
