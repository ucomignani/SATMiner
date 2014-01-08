package dag.satmining.utils;

import java.util.Arrays;

public class ArrayContainer<E> {

	private E [] _array;
	
	public ArrayContainer(E[] array) {
		_array = array;
	}
	
	public String toString() {
		return Arrays.toString(_array);
	}
	
}
