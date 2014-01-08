/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.backend.sat4j;

import java.util.Iterator;

import org.sat4j.specs.IVecInt;

import dag.satmining.backend.dimacs.DimacsLiteral;

/**
 *
 * @author ecoquery
 */
public class IVecIntIterator implements Iterator<DimacsLiteral>{

    private IVecInt _lits;
    private int _next = 0;
    
    public IVecIntIterator(IVecInt lits) {
        this._lits = lits;
    }

    public boolean hasNext() {
        return _next < _lits.size();
    }

    public DimacsLiteral next() {
        return new DimacsLiteral(_lits.get(_next++));
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
