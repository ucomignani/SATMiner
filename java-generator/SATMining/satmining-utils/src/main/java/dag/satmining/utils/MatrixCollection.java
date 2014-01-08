package dag.satmining.utils;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MatrixCollection<E> extends AbstractCollection<E> {

	/**
	 * The underlying matrix.
	 */
	private E[][] _matrix;

	/**
	 * The size of the collection
	 */
	private int _size = -1;

	/**
	 * Constructs a collection wrapper around the given matrix.
	 * @param matrix the matrix to use.
	 */
	public MatrixCollection(E[][] matrix) {
		this._matrix = matrix;
	}
	
	@Override
	public final Iterator<E> iterator() {
		return new Iterator<E>() {

			private int _lineIdx = 0;
			private int _columnIdx = 0;

			public boolean hasNext() {
				return _lineIdx < _matrix.length
						&& _columnIdx < _matrix[_lineIdx].length;
			}

			public E next() {
				if (hasNext()) {
					E result = _matrix[_lineIdx][_columnIdx];
					_columnIdx ++;
					while (_lineIdx < _matrix.length && _columnIdx == _matrix[_lineIdx].length) {
						_lineIdx ++;
						_columnIdx = 0;
					}
					return result;
				} else {
					throw new NoSuchElementException();
				}
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public final int size() {
		if (_size == -1) {
			_size = 0;
			for (E[] line : _matrix) {
				_size += line.length;
			}
		}
		return _size;
	}

}
