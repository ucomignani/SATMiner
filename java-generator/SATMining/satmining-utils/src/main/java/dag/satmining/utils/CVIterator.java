package dag.satmining.utils;

import java.util.Iterator;

public class CVIterator<E,F extends E> implements Iterator<E> {

	private Iterator<F> _delegate;
	
	public CVIterator(Iterator<F> delegate) {
		_delegate = delegate;
	}
	
	public boolean hasNext() {
		return _delegate.hasNext();
	}

	public E next() {
		return _delegate.next();
	}

	public void remove() {
		_delegate.remove();
	}

}
