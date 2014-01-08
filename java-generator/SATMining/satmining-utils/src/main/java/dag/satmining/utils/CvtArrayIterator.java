/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dag.satmining.utils;

import java.util.Iterator;

/**
 *
 * @author ecoquery
 */
public class CvtArrayIterator<E,F> implements Iterator<F> {

    private final E[] _array;
    private final Converter<E,F> _converter;
    private int _next = 0;
    
    public CvtArrayIterator(E[] data, Converter<E,F> cvt) {
        this._array = data;
        this._converter = cvt;
        this._next = 0;
    }
    
    public boolean hasNext() {
        return _next < _array.length;
    }

    public F next() {
        return _converter.convert(_array[_next++]);
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
