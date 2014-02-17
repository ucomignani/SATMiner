/* ./satmining-utils/src/main/java/dag/satmining/utils/MatrixCollection.java

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
