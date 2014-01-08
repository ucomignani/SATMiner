package dag.satmining.utils;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BoundedIntPrefixTree<V> implements Map<int[], V> {

	private int _lb;
	private int _ub;
	private Node _root;
	private int _size = 0;

	public BoundedIntPrefixTree(int lowerBound, int upperBound) {
		_lb = lowerBound;
		_ub = upperBound;
		_root = new Node();
	}
	
	private final class Node implements Map.Entry<int[], V> {
		int[] _key;
		V _value;
		@SuppressWarnings("unchecked")
		Node[] _nxt = new BoundedIntPrefixTree.Node[_ub - _lb];

		public Node getNext(int val, boolean create) {
			int idx = val - _lb;
			if (_nxt[idx] == null && create) {
				_nxt[idx] = new Node();
			}
			return _nxt[idx];
		}

		@Override
		public int[] getKey() {
			return _key;
		}

		@Override
		public V getValue() {
			return _value;
		}

		@Override
		public V setValue(V value) {
			V old = _value;
			_value = value;
			return old;
		}
	}

	private Node lookup(int[] key, boolean create) {
		Node cur = _root;
		for (int i = 0; i < key.length && cur != null; i++) {
			cur = cur.getNext(key[i], create);
		}
		return cur;
	}

	@Override
	public void clear() {
		_root = new Node();
		_size = 0;
	}

	@Override
	public boolean containsKey(Object o) {
		Node n = lookup((int[]) o, false);
		return n != null && n._key != null;
	}

	@Override
	public boolean containsValue(Object o) {
		for (V v : values()) {
			if (v.equals(o)) {
				return true;
			}
		}
		return false;
	}

	private class NonEmptyNodeIterator implements Iterator<Node> {
		private Deque<Node> _stack;
		private Node _current = null;

		public NonEmptyNodeIterator() {
			_stack = new ArrayDeque<Node>();
			_stack.push(_root);
		}

		@Override
		public boolean hasNext() {
			if (_current == null) {
				findNextNonEmpty();
			}
			return _current != null;
		}

		private void findNextNonEmpty() {
			_current = null;
			while (!_stack.isEmpty() && _current == null) {
				_current = _stack.pop();
				for (int i = 0; i < _current._nxt.length; i++) {
					if (_current._nxt[i] != null) {
						_stack.push(_current._nxt[i]);
					}
				}
				if (_current._key == null) {
					_current = null;
				}
			}
		}

		@Override
		public Node next() {
			if (_current == null) {
				findNextNonEmpty();
			}
			Node n = _current;
			_current = null;
			return n;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public Set<Map.Entry<int[], V>> entrySet() {
		return new AbstractSet<Map.Entry<int[], V>>() {

			@Override
			public Iterator<java.util.Map.Entry<int[], V>> iterator() {
				return new CVIterator<Map.Entry<int[], V>, Node>(
						new NonEmptyNodeIterator());
			}

			@Override
			public int size() {
				return _size;
			}
		};
	}

	@Override
	public V get(Object key) {
		Node n = lookup((int[]) key, false);
		return n != null ? n._value : null;
	}

	@Override
	public boolean isEmpty() {
		return _size == 0;
	}

	@Override
	public Set<int[]> keySet() {
		return new AbstractSet<int[]>() {

			@Override
			public boolean contains(Object o) {
				return containsKey(o);
			}

			@Override
			public Iterator<int[]> iterator() {
				return new TransIterator<int[], Node>(
						new NonEmptyNodeIterator()) {

					@Override
					protected int[] transform(Node f) {
						return f._key;
					}
				};
			}

			@Override
			public int size() {
				return _size;
			}
		};
	}

	@Override
	public V put(int[] key, V value) {
		Node n = lookup(key, true);
		if (n._key == null) {
			n._key = Arrays.copyOf(key, key.length);
			_size++;
		}
		V old = n._value;
		n._value = value;
		if (value == null) {
			n._key = null;
			_size--;
		}
		return old;
	}

	@Override
	public void putAll(Map<? extends int[], ? extends V> map) {
		for (Map.Entry<? extends int[], ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		return put((int[]) key, null);
	}

	@Override
	public int size() {
		return _size;
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {

			@Override
			public Iterator<V> iterator() {
				return new TransIterator<V, Node>(new NonEmptyNodeIterator()) {

					@Override
					protected V transform(Node f) {
						return f._value;
					}
				};
			}

			@Override
			public int size() {
				return _size;
			}
		};
	}

}
