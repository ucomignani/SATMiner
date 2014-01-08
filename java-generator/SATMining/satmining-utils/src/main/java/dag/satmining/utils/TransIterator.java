package dag.satmining.utils;

import java.util.Iterator;

public abstract class TransIterator<E, F> implements Iterator<E> {

	protected Iterator<F> _internal;
	
	public TransIterator(Iterator<F> internal) {
		this._internal = internal;
	}

	protected abstract E transform(F f);

	@Override
	public boolean hasNext() {
		return _internal.hasNext();
	}

	@Override
	public E next() {
		F nxt = _internal.next();
		return nxt != null ? transform(nxt) : null;
	}

	@Override
	public void remove() {
		_internal.remove();
	}
	
}
