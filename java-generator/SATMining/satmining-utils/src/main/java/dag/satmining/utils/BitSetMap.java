/* ./satmining-utils/src/main/java/dag/satmining/utils/BitSetMap.java

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
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BitSetMap<E> implements Map<BitSet, E> {

	private Node<E> _root = new Node<E>();
	private int _size = 0;
	private int[] _map = null;
	private BitSet _mask = null;

	private int getBitSetIndex(int mapIndex) {
		return _map == null ? mapIndex : _map[mapIndex];
	}

	public static class Node<E> implements Map.Entry<BitSet, E> {
		Node<E> _tt;
		Node<E> _ff;
		E _element;
		BitSet _key;

		@Override
		public BitSet getKey() {
			return _key;
		}

		@Override
		public E getValue() {
			return _element;
		}

		@Override
		public E setValue(E value) {
			E old = _element;
			_element = value;
			return old;
		}
	}

	@Override
	public void clear() {
		_root = new Node<E>();
		_size = 0;
	}

	private Node<E> lookup(BitSet key, boolean build) {
		Node<E> cur = _root;
		int ubound = _map == null ? key.length() : _map.length;
		for (int i = 0; i < ubound; i++) {
			if (key.get(getBitSetIndex(i))) {
				if (cur._tt != null) {
					cur = cur._tt;
				} else if (build) {
					cur._tt = new Node<E>();
					cur = cur._tt;
				} else {
					return null;
				}
			} else {
				if (cur._ff != null) {
					cur = cur._ff;
				} else if (build) {
					cur._ff = new Node<E>();
					cur = cur._ff;
				} else {
					return null;
				}
			}
		}
		return cur;
	}

	@Override
	public boolean containsKey(Object key) {
		Node<E> node = lookup((BitSet) key, false);
		return node != null ? node._key != null : false;
	}

	private class NodeIterator implements Iterator<Node<E>> {

		private ArrayDeque<Node<E>> _stack;

		public NodeIterator() {
			_stack = new ArrayDeque<BitSetMap.Node<E>>();
			_stack.add(_root);
		}

		@Override
		public boolean hasNext() {
			return !_stack.isEmpty();
		}

		@Override
		public Node<E> next() {
			if (_stack.isEmpty()) {
				return null;
			} else {
				Node<E> res = _stack.pop();
				if (res._tt != null) {
					_stack.push(res._tt);
				}
				if (res._ff != null) {
					_stack.push(res._ff);
				}
				return res;
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

/*	private Iterable<Node<E>> getNodes() {
		return new Iterable<Node<E>>() {

			@Override
			public Iterator<Node<E>> iterator() {
				return new NodeIterator();
			}

		};
	}*/

	private class NonEmptyNodeIterator implements Iterator<Node<E>> {

		private Iterator<Node<E>> _internal;
		private Node<E> _current = null;

		public NonEmptyNodeIterator() {
			_internal = new NodeIterator();
		}

		private Node<E> findNextNonEmpty() {
			Node<E> res = null;
			while (_internal.hasNext() && res == null) {
				res = _internal.next();
				if (res._key == null) {
					res = null;
				}
			}
			return res;
		}

		@Override
		public boolean hasNext() {
			if (_current == null) {
				_current = findNextNonEmpty();
			}
			return _current != null;
		}

		@Override
		public Node<E> next() {
			if (_current != null) {
				Node<E> res = _current;
				_current = null;
				return res;
			} else {
				return findNextNonEmpty();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	private Iterable<Node<E>> getNonEmptyNodes() {
		return new Iterable<Node<E>>() {

			@Override
			public Iterator<Node<E>> iterator() {
				return new NonEmptyNodeIterator();
			}
		};
	}

	@Override
	public boolean containsValue(Object value) {
		for (Node<E> node : getNonEmptyNodes()) {
			if (node._element.equals(value)) {
				return true;
			}
		}
		return false;
	}

	private class EntrySet extends AbstractSet<Map.Entry<BitSet, E>> {

		@Override
		public Iterator<java.util.Map.Entry<BitSet, E>> iterator() {
			return new CVIterator<Map.Entry<BitSet, E>, Node<E>>(
					new NonEmptyNodeIterator());
		}

		@Override
		public int size() {
			return _size;
		}

	}

	@Override
	public Set<Map.Entry<BitSet, E>> entrySet() {
		return new EntrySet();
	}

	@Override
	public E get(Object key) {
		Node<E> node = lookup((BitSet) key, false);
		return node == null ? null : node._element;
	}

	@Override
	public boolean isEmpty() {
		return _size == 0;
	}

	private class KeyIterator extends TransIterator<BitSet, Node<E>> {

		public KeyIterator() {
			super(new NonEmptyNodeIterator());
		}

		@Override
		protected BitSet transform(Node<E> f) {
			return f._key;
		}

	}

	private class KeySet extends AbstractSet<BitSet> {

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public Iterator<BitSet> iterator() {
			return new KeyIterator();
		}

		@Override
		public int size() {
			return _size;
		}

	}

	@Override
	public Set<BitSet> keySet() {
		return new KeySet();
	}

	@Override
	public E put(BitSet key, E value) {
		Node<E> node = lookup(key, true);
		if (node._key == null) {
			node._key = ((BitSet)key.clone());
			node._key.and(_mask);
			_size++;
		}
		E old = node._element;
		node._element = value;
		if (value == null) {
			node._key = null;
			_size--;
		}
		return old;
	}

	@Override
	public void putAll(Map<? extends BitSet, ? extends E> m) {
		for (Map.Entry<? extends BitSet, ? extends E> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public E remove(Object key) {
		Node<E> node = lookup((BitSet) key, false);
		if (node != null) {
			E old = node._element;
			node._key = null;
			node._element = null;
			_size--;
			return old;
		} else {
			return null;
		}
	}

	@Override
	public int size() {
		return _size;
	}

	private class ValuesCollection extends AbstractCollection<E> {

		@Override
		public Iterator<E> iterator() {
			return new TransIterator<E, Node<E>>(new NonEmptyNodeIterator()) {

				@Override
				protected E transform(Node<E> f) {
					return f._element;
				}
			};
		}

		@Override
		public int size() {
			return _size;
		}

	}

	@Override
	public Collection<E> values() {
		return new ValuesCollection();
	}
	
	public void setMask(BitSet mask) {
		if (_size>0) {
			throw new IllegalStateException("Tried to set a mask on a non empty BitSetMap");
		}
		_mask = mask;
		List<Integer> map = new ArrayList<Integer>();
		if (mask.length() > 0) {
			int i = 0;
			while(i != -1) {
				i = mask.nextSetBit(i+1);
				if (i != -1) {
					map.add(i);
				}
			}
		}
		_map = new int[map.size()];
		for(int i = 0; i < _map.length; i++) {
			_map[i] = map.get(i);
		}
	}

}
