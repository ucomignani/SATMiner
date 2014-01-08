package dag.satmining.utils;

import java.util.Iterator;

public class ArrayIterator<E> implements Iterator<E> {

	private E[] _array;
	private int _idx;
	
	public <F extends E>ArrayIterator(F[] array) {
		_array = array;
		_idx = 0;
	}
	
	public boolean hasNext() {
		return _idx < _array.length;
	}

	public E next() {
		return _array[_idx++];
	}

	public void remove() {
		throw new UnsupportedOperationException("remove");
	}

}
